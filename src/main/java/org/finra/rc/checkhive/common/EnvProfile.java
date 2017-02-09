package org.finra.rc.checkhive.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * User: Han Li Date: 1/26/17
 */
public enum EnvProfile
{
    local, dev, qa, prod;

    //try to find the SDLC from the current profiles.
    public static Optional<String> getSDLC(String[] profiles)
    {
        return Arrays.stream(profiles)
                     .filter(profile -> Arrays.stream(EnvProfile.values())
                                              .map(EnvProfile::name)
                                              .anyMatch(profile::equals))
                     .findFirst();
    }
}
