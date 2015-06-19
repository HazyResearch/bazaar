#!/usr/bin/env bash

########################################################
CLOUD=ec2      # azure or ec2
NUM_MACHINES=1   

########################################################

PLATFORM=$(uname)

# check dependencies

# make sure that go is installed
command -v go >/dev/null 2>&1 || {
  echo >&2 "Go required but not installed. Aborting."; 
  if [[ "$PLATFORM" == "Darwin" ]]; then
    echo >&2 "You can install go with brew:"
    echo >&2 "    sudo brew install go"
  elif [[ "$PLATFORM" == "Linux" ]]; then
    echo >&2 "You can install go with:"
    echo >&2 "    sudo apt-get install go"
  fi
  exit 1; };

# make sure fabric is installed
command -v fab >/dev/null 2>&1 || {
  echo >&2 "fabric required but not installed. Aborting.";
  if [[ "$PLATFORM" == "Darwin" ]]; then
    echo >&2 "You can install fabric with brew:"
    echo >&2 "     sudo brew install fabric"
  elif [[ "$PLATFORM" == "Linux" ]]; then
    echo >&2 "You can install fabric with:"
    echo >&2 "     sudo apt-get install fabric"
  fi
  exit; };

# check if we have SSH keys
if [ ! -f "./ssh/bazaar.key" ]; then
  echo "Creating SSH keys"
  mkdir ./ssh
  cd ./ssh
  ssh-keygen -t rsa -b 2048 -f bazaar.key -N '' -C bazaar
  openssl req \
    -x509 \
    -days 365 \
    -new \
    -key bazaar.key \
    -out bazaar.pem \
    -subj "/C=US/ST=Denial/L=Springfield/O=Dis/CN=www.example.com"
  cd ..
fi

if [[ "$CLOUD" == "azure" ]]; then

  # check if credentials exist
  CRED_FILE="~/conf/credentials.publishsettings"
  if [ ! -f $CRED_FILE ]; then
    echo "Cannot find azure credentials at $CRED_FILE. Aborting."
    echo "You can download that file at "
    echo "         https://manage.windowsazure.com/publishsettings"
    exit 1
  fi

  echo "LAUNCHING $NUM_MACHINES MACHINES ON AZURE"

  go run src/create-azure.go 

elif [[ "$CLOUD" == "ec2" ]]; then
  CRED_FILE="$HOME/.aws/credentials"
  if [ ! -f $CRED_FILE ]; then
    echo "Cannot find ec2 credentials at $CRED_FILE. Aborting."
    echo "For more info, see "
    echo "       http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html"
    exit 1
  fi
  CONF_FILE="$HOME/.aws/config"
  if [ ! -f $CONF_FILE ]; then
    echo "Cannot find ec2 config file $CONF_FILE. Aborting."
    echo "Make sure that the file contains the 'region' parameter. For more info, see "
    echo "       http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html"
    exit 1
  fi
  echo "LAUNCHING $NUM_MACHINES MACHINES ON EC-2"

  go run src/create-ec2.go -n $NUM_MACHINES

fi


