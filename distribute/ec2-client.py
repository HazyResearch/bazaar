#!/usr/bin/env python

import botocore.session
import errno
import getopt
import os
import shutil
import sys
import time

EC2_INSTANCE_TYPE = os.environ.get('EC2_INSTANCE_TYPE')
if not EC2_INSTANCE_TYPE:
    print('EC2_INSTANCE_TYPE is not set.')
    exit(1)

AMI = 'ami-d05e75b8'
USERNAME = 'ubuntu'
PUBLIC_KEY = 'ssh/bazaar.key.pub'
REGION = 'us-east-1'

class EC2Client:

    def __init__(self):
        self.session = botocore.session.get_session()
        self.client = self.session.create_client('ec2', region_name=REGION)

    def import_key_pair(self):
        with open(PUBLIC_KEY, "rb") as pubKeyFile:
            f = pubKeyFile.read()
            bytes = bytearray(f)

        response = self.client.import_key_pair(
            KeyName='bazaar',
            PublicKeyMaterial=bytes
        )

    def delete_key_pair(self):
        response = self.client.delete_key_pair(
            KeyName='bazaar'
        )

    def create_security_group(self):
        # check if security group exists already
        try:
            response = self.client.describe_security_groups(
               GroupNames=['bazaar-group'],
            )
        except:
            #if not response['SecurityGroups']:
            print("Creating security group bazaar-group")
            response = self.client.create_security_group(
               GroupName='bazaar-group',
               Description='Security Group enabling SSH for DeepDive\'s Bazaar',
            )
            #response.authorize('tcp', 22, 22, '0.0.0.0/0')
            response = self.client.authorize_security_group_ingress(
                GroupName='bazaar-group',
                IpProtocol='tcp',
                FromPort=22,
                ToPort=22,
                CidrIp='0.0.0.0/0')
            print(response)

    def run_instances(self, num=1):
        response = self.client.run_instances(
            ImageId=AMI, #'ami-d05e75b8',
            MinCount=int(num),
            MaxCount=int(num),
            KeyName='bazaar',
            SecurityGroups=[ 'bazaar-group' ],
            InstanceType=EC2_INSTANCE_TYPE, #'m3.large',
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

    def read_instance_ids(self):
        ids = []
        with open('.state/INSTANCE_IDS', 'r') as f:
            for line in f:
                ids.append(line.rstrip())
        return ids

    def wait_for_public_dns(self):
        ids = self.read_instance_ids()

        response = None
        while True:
            response = self.client.describe_instances(
                InstanceIds=ids
            )
            num_pending = 0
            for inst in response['Reservations'][0]['Instances']:
                if inst['State']['Name'] == 'pending':
                    num_pending = num_pending + 1
            if num_pending == 0:
                break
            sys.stdout.write('.')
            sys.stdout.flush()
            time.sleep(2)

        print('')
        with open('.state/HOSTS', 'w') as f:
            for inst in response['Reservations'][0]['Instances']:
                f.write(USERNAME + '@' + inst['PublicDnsName'] + ':22\n')
        with open('.state/DIRS', 'w') as f:
            for inst in response['Reservations'][0]['Instances']:
                f.write('/home/' + USERNAME + '\n')
 
    def terminate_instances(self):
        ids = self.read_instance_ids()
        response = self.client.terminate_instances(
            InstanceIds=ids
        )
        shutil.rmtree(".state")

    def create_state_dir(self):
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
    print('Launching ' + str(num_instances) + ' instances on ec2')
    client = EC2Client()
    client.delete_key_pair()
    client.import_key_pair()
    client.create_state_dir()
    client.create_security_group()
    client.run_instances(num_instances)
    client.wait_for_public_dns()

def terminate():
    client = EC2Client()
    client.delete_key_pair()
    client.import_key_pair()
    client.terminate_instances()

def usage():
    print("Usage: ec2-client.py launch|terminate [OPTIONS]")
    exit(1)

def main(argv):
    if len(argv) < 1:
        usage()
    cmd = argv[0]
    if cmd == 'launch':
        launch(argv[1:])
    elif cmd == 'terminate':
        terminate()
    else:
        usage()

if __name__ == "__main__":
   main(sys.argv[1:])
