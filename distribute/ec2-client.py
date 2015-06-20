#!/usr/bin/env python3

import botocore.session
import errno
import getopt
import os
import shutil
import sys
import time

session = botocore.session.get_session()
client = session.create_client('ec2', region_name='us-east-1')

# Expects the following environment variables
#    AWS_ACCESS_KEY_ID='...'
#    AWS_SECRET_ACCESS_KEY='...'

def import_key_pair():
    with open("ssh/bazaar.key.pub", "rb") as pubKeyFile:
        f = pubKeyFile.read()
        bytes = bytearray(f)

    response = client.import_key_pair(
        KeyName='bazaar',
        PublicKeyMaterial=bytes
    )

def delete_key_pair():
    response = client.delete_key_pair(
        KeyName='bazaar'
    )

def create_security_group():
    # check if security group exists already
    response = client.describe_security_groups(
        GroupNames=['bazaar-group'],
    )
    if not response['SecurityGroups']:
        print("Creating security group bazaar-group")
        response = client.create_security_group(
            GroupName='bazaar-group',
            Description='Security Group enabling SSH for DeepDive\'s Bazaar',
        )

def run_instances(num=1):
    response = client.run_instances(
        ImageId='ami-d05e75b8',
        MinCount=int(num),
        MaxCount=int(num),
        KeyName='bazaar',
        SecurityGroups=[ 'bazaar-group' ],
        InstanceType='m3.large',
        BlockDeviceMappings=[ 
            {
                'VirtualName': 'ephemeral0',
                'DeviceName': '/dev/xvdh',
            },
        ],
        Monitoring={
            'Enabled': False
        },
    )
    with open('.state/INSTANCE_IDS', 'w') as f:
        for inst in response['Instances']:
            f.write(inst['InstanceId'] + '\n')
    with open('.state/CLOUD', 'w') as f:
        f.write('ec-2')

def read_instance_ids():
    ids = []
    with open('.state/INSTANCE_IDS', 'r') as f:
        for line in f:
            ids.append(line.rstrip())
    return ids

def wait_for_public_dns():
    ids = read_instance_ids()

    response = None
    while True:
        response = client.describe_instances(
            InstanceIds=ids
        )
        num_pending = 0
        for inst in response['Reservations'][0]['Instances']:
            if inst['State']['Name'] == 'pending':
                num_pending = num_pending + 1
        if num_pending == 0:
            break
        print("Pending: %d" % num_pending)
        time.sleep(1)

    with open('.state/HOSTS', 'w') as f:
        for inst in response['Reservations'][0]['Instances']:
            f.write(inst['PublicDnsName'] + '\n')

def terminate_instances():
    ids = read_instance_ids()
    response = client.terminate_instances(
        InstanceIds=ids
    )
    shutil.rmtree(".state")

def create_state_dir():
    try:
        os.makedirs('.state')
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir('.state'):
            print("Found existing .state dir. Please terminate instances first.")
            exit(1)
        else: raise

def launch(argv):
    num_instances = 1
    try:
        opts, args = getopt.getopt(argv,"n:",[])
    except getopt.GetoptError:
        #print " -n <numinstances>"
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-n':
            num_instances = arg
    delete_key_pair()
    import_key_pair()
    create_state_dir()
    create_security_group()
    run_instances(num_instances)
    wait_for_public_dns()

def terminate():
    delete_key_pair()
    import_key_pair()
    terminate_instances()

def main(argv):
    cmd = argv[0]
    if cmd == 'launch':
        launch(argv[1:])
    elif cmd == 'terminate':
        terminate()
    else:
        print("Usage: ec2-client.py launch|terminate [OPTIONS]")

if __name__ == "__main__":
   main(sys.argv[1:])
