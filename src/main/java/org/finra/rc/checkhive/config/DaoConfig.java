package org.finra.rc.checkhive.config;

import java.text.MessageFormat;

import javax.sql.DataSource;

import com.amazon.hive.jdbc3.HS2Driver;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.StringUtils;

import org.finra.pet.JCredStashFX;
import org.finra.rc.checkhive.common.Const;
import org.finra.rc.checkhive.common.EnvProfile;

/**
 * User: Han Li Date: 1/24/17
 */
@Configuration
public class DaoConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DaoConfig.class);

    @Autowired
    private Environment env;

    @Bean(name = "hiveDataSource")
    public DataSource hiveDataSource()
    {
        String server = env.getProperty("hive.url");
        String user = env.getProperty("hive.username");

        String pw = getHivePw();
        String connectionUrl = MessageFormat.format(
            "jdbc:hive2://{0}:{1}/{2};SSL=1;AuthMech=4;CAIssuedCertNamesMismatch=1;UID={3};PWD={4}", server, 443, "erd", user, pw);
        //NOTE here, we have to use the jdbc3 version of Hive Server 2 driver to bypass the SSL in ELB.
        return new SimpleDriverDataSource(new HS2Driver(), connectionUrl);
    }

    private String getHivePw()
    {
        //if password is in config file, then we do not go to dynamo.
        if (!StringUtils.isEmpty(env.getProperty("hive.password")))
        {
            return env.getProperty("hive.password");
        }

        JCredStashFX jCredStashFx = new JCredStashFX(new ClientConfiguration(), new DefaultAWSCredentialsProviderChain(), Regions.US_EAST_1.getName());
        try
        {
            return jCredStashFx.getCredential(Const.HIVE_SERVER_CREDSTASH_KEY, "RC", EnvProfile.getSDLC(env.getActiveProfiles()).get(), null, "credential-store");
        }
        catch (Exception e)
        {
            String errorMsg = "error Getting pw from credstash ";
            LOGGER.error(errorMsg,e);
            throw new IllegalStateException(errorMsg);
        }
    }

    @Bean(name = "hiveJdbcTemplate")
    public JdbcTemplate hiveJdbcTemplate()
    {
        return new JdbcTemplate(hiveDataSource());
    }
}
