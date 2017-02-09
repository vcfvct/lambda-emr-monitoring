package org.finra.rc.checkhive.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * User: Han Li Date: 1/30/17
 */
@Service
public class EmailServiceImpl implements EmailService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${email.host}")
    private String emailhost;

    @Override
    public void sendEmail(String from, String to,String subject, String body)
    {
        LOGGER.info(String.format("Trying to send email from: %s, to: %s, subject: %s, body: %s ", from, to, subject, body));
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", emailhost);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        try
        {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            LOGGER.info("Sent email successfully to " + to);
        }  catch (MessagingException mex) {
            LOGGER.error(mex.getMessage());
        }
    }
}
