An AWS Lambda function which runs a Spring boot application to monitor and self-recover Hive Server2.

Basic flow is
* try to connect to Hive via jdbc(thru ELB to EMR master) and run basic query like `show databases;`. 
* The query has a will quite after 10s then retry. Without this timeout, the whole lambda function will be timed out since hive query waits for more than 5 minutes. 
* when succeed, quite
* when failed, we will try to execute the `reload hive-server2` command on master node.
