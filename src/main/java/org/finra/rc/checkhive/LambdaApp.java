package org.finra.rc.checkhive;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import org.finra.rc.checkhive.common.Const;

/**
 * User: Han Li Date: 1/24/17
 */
public class LambdaApp implements RequestHandler<Object, String>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaApp.class);

    @Override
    public String handleRequest(Object options, Context context)
    {
        LOGGER.info("incoming object: " + options);
        String toReturn = "Cluster Jdbc healthy!";
        try
        {
            //try to find Const.ENV_KEY from incoming args and environment variable;
            Optional<String> maybeEnv = Stream.of(System.getenv(Const.ENV_KEY), options instanceof String ? (String) options : null)
                                              .filter(Objects::nonNull)
                                              .findFirst();
            if (maybeEnv.isPresent())
            {
                SpringApplication app = new SpringApplication(SpringApp.class);
                app.setAdditionalProfiles(maybeEnv.get());
                app.run();
            }
            else
            {
                throw new RuntimeException("No '" + Const.ENV_KEY + "' found from args or environment variable.");
            }
        }
        catch (Exception e)
        {
            LOGGER.error("ERROR", e);
            toReturn = "run query failed";
        }
        return toReturn;
    }
}
