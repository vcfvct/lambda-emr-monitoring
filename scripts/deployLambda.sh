#!/usr/bin/env bash
functionToRefresh=RC-checkhive

echo start to refresh lambda: ${functionToRefresh}
localFile=/Users/liha/GIT/rc-aws-hive/hive-connection-check/target/hive-connection-check-1.0-SNAPSHOT.jar

#aws lambda update-function-code --function-name ${functionToRefresh} --zip-file fileb://${localFile}


s3Bucket=4652-5751-2377-application-dev-staging
s3Key=RC/checkhive/main.jar
aws s3 cp ${localFile} s3://${s3Bucket}/${s3Key} --sse
aws lambda update-function-code --function-name ${functionToRefresh} --s3-bucket ${s3Bucket} --s3-key ${s3Key}

