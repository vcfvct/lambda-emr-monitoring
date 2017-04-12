#!/bin/bash
echo ******************* kill process listening to port 10000
sudo fuser -k 10000/tcp

echo ******************* Ready to restart hive server2...
sudo start hive-server2
sudo reload hive-server2

exitCode=$?
if [ ${exitCode} -ne 0 ]
then
   echo Job failed with non 0 status
   exit ${exitCode}
fi
echo ******************* finished hive server2 restart.