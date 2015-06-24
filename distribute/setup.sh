#!/usr/bin/env bash

virtualenv env
env/bin/activate

pip3 install azure
pip3 install botocore

PLATFORM=$(uname)

# check dependencies

# install fabric 
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

# install SSH keys
if [ ! -f "./ssh/bazaar.key" ]; then
  echo "Creating SSH keys"
  mkdir ./ssh
  cd ./ssh

  # generate private/public key pair
  ssh-keygen -t rsa -b 2048 -f bazaar.key -N '' -C bazaar

  # generate azure pem file from openssh private key
  openssl req \
    -x509 \
    -days 365 \
    -nodes \
    -key bazaar.key \
    -out bazaar.pem \
    -newkey rsa:2048 \
    -subj "/"
  cd ..
fi

# install (separate) management certificates for azure
cd ssh
`openssl req -x509 -nodes -days 365 -newkey rsa:1024 -keyout mycert.pem -out mycert.pem`
`openssl x509 -inform pem -in mycert.pem -outform der -out mycert.cer`

echo 'NOTE: If you would like to use Azure, you must upload ssh/mycert.cer via the "Upload" action of the "Settings" tab of the management portal.'

