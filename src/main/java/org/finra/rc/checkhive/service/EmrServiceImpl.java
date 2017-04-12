package org.finra.rc.checkhive.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.ClusterState;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.ListClustersRequest;
import com.amazonaws.services.elasticmapreduce.model.ListClustersResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.finra.rc.checkhive.common.EnvProfile;

/**
 * User: Han Li Date: 1/26/17
 */
@Service
public class EmrServiceImpl implements EmrService
{
//    private static final Logger LOGGER = LoggerFactory.getLogger("splunk.logger");
    private static final Logger LOGGER = LoggerFactory.getLogger(EmrServiceImpl.class);

    public static final String FAILED = "Step Failed";

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("hiveJdbcTemplate")
    private JdbcTemplate hiveJdbcTemplate;

    private int timeoutInSec = 10;

    @Override
    //for a random backoff between 2s and 5s milliseconds and up to 2 attempts
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay=2000, maxDelay = 5000))
    public void connectHive()
    {
        String query = "show databases";
        LOGGER.info("Ready to run query: " + query);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(() -> {
            hiveJdbcTemplate.query(query, rs -> {
                if (rs.next())
                {
                    LOGGER.info(rs.getString("database_name"));
                }
                return null;
            });
        });
        executor.shutdown();
        try {
            future.get(timeoutInSec, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException ex) {
            String msg = "Execution Failed: " + ex.getMessage();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        catch (TimeoutException ie) {
            LOGGER.error("Error Getting result in {} seconds.", timeoutInSec);
            throw new RuntimeException("Query Timed out.");
        }

        LOGGER.info("Query finished. Quit....");
    }

    @Override
    public void reloadHiveServer2()
    {
        String fileToRun = env.getProperty("rc.reloadScript");
        addShellStep(fileToRun, "Restart-hive-server from Lambda");
    }

    public void addShellStep(String fileToRun, String stepName)
    {
        LOGGER.info(String.format("ready to run file: %s AND stepname: %s", fileToRun, stepName));

        ClientConfiguration clientConf = new ClientConfiguration();
        //local only
        if (Arrays.stream(env.getActiveProfiles())
                  .anyMatch(EnvProfile.local.name()::equals))
        {
            clientConf.withProxyHost("proxye1.finra.org")
                      .withProxyPort(8080);

        }
        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                                                                        .withClientConfiguration(clientConf)
                                                                        .build();

        ListClustersRequest listActiveClustersRequest = new ListClustersRequest().withClusterStates(ClusterState.RUNNING, ClusterState.WAITING);
        Optional<ClusterSummary> maybeCluster = getClusterSummary(emr, listActiveClustersRequest);
        if (maybeCluster.isPresent())
        {
            String clusterId = maybeCluster.get()
                                           .getId();
            LOGGER.info("Found cluster with id: " + clusterId);
            HadoopJarStepConfig hadoopJarStepConfig = new HadoopJarStepConfig().withJar(env.getProperty("aws.scriptRunJar"))
                                                                               .withArgs(fileToRun);
            StepConfig stepConfig = new StepConfig().withHadoopJarStep(hadoopJarStepConfig)
                                                    .withName(stepName)
                                                    .withActionOnFailure(ActionOnFailure.CONTINUE);
            AddJobFlowStepsRequest addJobFlowStepsRequest = new AddJobFlowStepsRequest().withJobFlowId(clusterId)
                                                                                        .withSteps(stepConfig);
            AddJobFlowStepsResult addJobFlowStepsResult = emr.addJobFlowSteps(addJobFlowStepsRequest);
            String rs = addJobFlowStepsResult.getStepIds()
                                             .stream()
                                             .findFirst()
                                             .orElse(FAILED);
            LOGGER.info("submit step result: " + rs);
        }
        else
        {
            LOGGER.error("failed to locate the cluster");
        }
    }

    private Optional<ClusterSummary> getClusterSummary(AmazonElasticMapReduce emr, ListClustersRequest listActiveClustersRequest)
    {
        ListClustersResult listClustersResult = emr.listClusters(listActiveClustersRequest);
        Optional<ClusterSummary> maybeCluster = listClustersResult.getClusters()
                                                                  .stream()
                                                                  .filter(clusterSummary -> clusterSummary.getName()
                                                                                                          .equals(env.getProperty("rc.clusterName")))
                                                                  .findFirst();
        if (maybeCluster.isPresent())
        {
            return maybeCluster;
        }
        else
        {
            //if not found and still more pages
            String paginationMarker = listClustersResult.getMarker();
            if (!StringUtils.isEmpty(paginationMarker))
            {
                listActiveClustersRequest.setMarker(paginationMarker);
                return getClusterSummary(emr, listActiveClustersRequest);
            }
            else
            {
                return Optional.empty();
            }
        }
    }
}
