package org.finra.rc.checkhive;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import org.junit.Ignore;
import org.junit.Test;

import org.finra.pet.JCredStashFX;
import org.finra.rc.checkhive.common.Const;

/**
 * User: Han Li
 * Date: 11/29/16
 */
public class CredstashTest
{
    //run this if you want to fetch values from credstash locally.
    @Ignore
    @Test
    public void testGetCred() throws Exception
    {
        ClientConfiguration clientConf = new ClientConfiguration()
            .withProxyHost("proxye1.finra.org")
            .withProxyPort(8080);
        AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
        String region = Regions.US_EAST_1.getName();

        JCredStashFX jCredStashFX = new JCredStashFX(clientConf, provider, region);
        String cred3 = jCredStashFX.getCredential(Const.HIVE_SERVER_CREDSTASH_KEY, "RC", "dev", "adds", "credential-store");
        System.out.println(cred3);
    }
}
