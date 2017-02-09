package org.finra.rc.checkhive.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * User: Han Li Date: 2/9/17
 */
public class EnvProfileTest
{
    @Test
    public void testGetSDLC()
    {
        String targetProfile = "prod";
        String[] profiles = {"world", targetProfile, "hello"};
        assertThat("should have profile", EnvProfile.getSDLC(profiles).isPresent(), is(true));
        assertThat("should match profile", EnvProfile.getSDLC(profiles).get(), is(targetProfile));
    }
}
