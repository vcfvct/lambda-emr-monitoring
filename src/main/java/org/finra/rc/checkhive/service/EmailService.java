package org.finra.rc.checkhive.service;

/**
 * User: Han Li Date: 1/30/17
 */
public interface EmailService
{
    void sendEmail(String to, String from, String subject, String body);
}
