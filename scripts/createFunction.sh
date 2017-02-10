#!/usr/bin/env bash

aws lambda create-function --function-name RC-checkhive-test \
--runtime java8 \
--role 'arn:aws:iam::465257512377:role/SVC_LAMBDA_RC_SR' \
--handler org.finra.rc.checkhive.LambdaApp \
--code '{"S3Bucket":"4652-5751-2377-application-dev-staging", "S3Key": "RC/checkhive/main.jar"}' \
--description 'check hive connection' \
--timeout 180 \
--memory-size 384 \
--publish \
--vpc-config '{"SubnetIds": ["subnet-1d2e3435", "subnet-0df4547a"], "SecurityGroupIds": ["sg-cb17b1ae", "sg-0e7ae277"]}' \
--environment Variables={springEnv=dev}

aws lambda delete-function --function-name RC-checkhive-test