package org.finra.rc.checkhive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import org.finra.rc.checkhive.service.EmailService;
import org.finra.rc.checkhive.service.EmrService;

/**
 * User: Han Li Date: 1/24/17
 */
@SpringBootApplication
@EnableRetry
public class SpringApp implements CommandLineRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaApp.class);

    @Autowired
    private EmrService emrService;
    @Autowired
    private EmailService emailService;

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.to}")
    private String emailTo;

    public static void main(String[] args)
    {
        //we init the spring context in Lambda, this is just for testing/local
        SpringApplication.run(SpringApp.class);
    }

    @Override
    public void run(String... args)
    {
        try
        {
            emrService.connectHive();
        }
        catch (Exception e)
        {
            String err = e.getMessage();
            LOGGER.error("Error running query: " + err);
            emrService.reloadHiveServer2();

            String subject = "Reload Hive Server2 submitted";
            String emailBody = "Something wrong with cluster and the hive server2 is just reloaded with error: " + err;
            emailService.sendEmail(emailFrom, emailTo, subject, emailBody);
        }
    }

}
