package org.finra.rc.checkhive.service;

/**
 * User: Han Li Date: 1/26/17
 */
public interface EmrService
{
    void connectHive();

    void reloadHiveServer2();
}
