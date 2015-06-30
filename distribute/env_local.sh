#!/usr/bin/env bash


# AZURE SETTINGS

# your azure subscription ID (look it up under 'Settings' in the management portal)
export AZURE_SUBSCRIPTION_ID='baf18420-bac2-4745-891d-16e249928ce8'

# name for service (must be unique among all azure users)
export AZURE_SERVICE_NAME='ddbazaa'

# eg. 'Standard_D2', or 'Standard_D14'
export AZURE_ROLE_SIZE='Standard_D2'


# EC2 SETTINGS

# For ec-2, we recommend that you keep your AWS_ACCESS_KEY_ID and your
# AWS_SECRET_ACCESS_KEY in ~/.aws/credentials.

# eg. 'm3.large'
export EC2_INSTANCE_TYPE='m3.large'

