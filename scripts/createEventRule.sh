#!/usr/bin/env bash

ruleName=RC-hivecheck-test

## create
aws events put-rule --name ${ruleName} \
--schedule-expression 'rate(5 minutes)' \
--state ENABLED \
--description 'check hive connection test'

aws events put-targets --rule ${ruleName} \
--targets '[{"Id":"1", "Arn":"arn:aws:lambda:us-east-1:465257512377:function:RC-checkhive"}]'

## remove
aws events remove-targets --rule ${ruleName} --ids "1"
aws events delete-rule --name ${ruleName}