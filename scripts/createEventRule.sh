#!/usr/bin/env bash

ruleName=RC-hivecheck
functionName=RC-checkhive

## create
aws events put-rule --name ${ruleName} \
--schedule-expression 'rate(5 minutes)' \
--state ENABLED \
--description 'check hive connection test'

# grant permission to the Rule to allow it to trigger the function
aws lambda add-permission --function-name ${functionName} \
--statement-id 123 \
--action 'lambda:InvokeFunction' \
--principal events.amazonaws.com \
--source-arn arn:aws:events:us-east-1:465257512377:rule/RC-hivecheck

# link rule and function
aws events put-targets --rule ${ruleName} \
--targets '[{"Id":"1", "Arn":"arn:aws:lambda:us-east-1:465257512377:function:RC-checkhive"}]'
