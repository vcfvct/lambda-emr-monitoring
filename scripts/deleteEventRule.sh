#!/usr/bin/env bash

ruleName=RC-hivecheck

## remove
aws events remove-targets --rule ${ruleName} --ids "1"
aws events delete-rule --name ${ruleName}