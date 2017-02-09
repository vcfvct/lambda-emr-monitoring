package org.finra.rc.checkhive;

import org.junit.Test;

import org.finra.rc.checkhive.common.EnvProfile;

/**
 * User: Han Li Date: 1/24/17
 */
public class LambdaAppTest
{
    //@Ignore
    @Test
    public void testMain()
    {
        LambdaApp lambdaApp = new LambdaApp();
        lambdaApp.handleRequest(EnvProfile.local.name(), null);
    }
}
