#!/usr/bin/env python3

from azure import *
from azure.servicemanagement import *
import errno
import getopt
import os
import shutil
import sys
import time

##############################################################
# THIS SECTION NEEDS TO BE CHANGED BY USER

# azure account settings
AZURE_SUBSCRIPTION_ID = 'baf18420-bac2-4745-891d-16e249928ce8'

# unique name for service (must be unique among all azure users)
service_name = 'ddbazaar'
##############################################################


# management certificate
AZURE_MGMT_CERT = 'ssh/mycert.pem'
# service certificate
cert_path='ssh/bazaar.pem'

# vm settings
linux_image_name = 'b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-14_04_2_LTS-amd64-server-20150309-en-us-30GB'
container_name = 'bazaarctr'
location = 'West US'

class AzureClient:

    def __init__(self):
        self.sms = ServiceManagementService(AZURE_SUBSCRIPTION_ID, AZURE_MGMT_CERT)

    def service_exists(self):
        try:
            props = self.sms.get_hosted_service_properties(service_name)
            return props is not None
        except:
            return False

    def create_hosted_service(self):
        if not self.service_exists():
            print('creating service with ' + service_name) 
            result = self.sms.create_hosted_service(
                service_name,
                service_name + 'label',
                service_name + 'description',
                location)
            self._wait_for_async(result.request_id)
            self.create_service_certificate()

    def list_services(self):
        result = self.sms.list_hosted_services()
        for hosted_service in result:
            print('- Service name: ' + hosted_service.service_name)
            print('  Management URL: ' + hosted_service.url)
            print('  Location: ' + hosted_service.hosted_service_properties.location)

    def delete_service():
        self.sms.delete_hosted_service(service_name)

    def delete_deployment():
        self.sms.delete_deployment('myhostedservice', 'v1')

    def _linux_role(self, role_name, subnet_name=None, port='22'):
        container_name = 'bazaarctr' + role_name
        host_name = 'hn' + role_name
        system = self._linux_config(host_name)
        os_hd = self._os_hd(linux_image_name,
			container_name,
			role_name + '.vhd')
        network = self._network_config(subnet_name, port)
        return (system, os_hd, network)

    def get_fingerprint(self):
        import hashlib
        with open (cert_path, "r") as myfile:
           data = myfile.readlines()
        lines = data[1:-1]
        all = ''.join([x.rstrip() for x in lines])
        key = base64.b64decode(all.encode('ascii'))
        fp = hashlib.sha1(key).hexdigest()
        return fp.upper()

    def _linux_config(self, hostname):
        SERVICE_CERT_THUMBPRINT = self.get_fingerprint() 
        pk = PublicKey(SERVICE_CERT_THUMBPRINT, u'/home/bazaar/.ssh/authorized_keys')
        pair = KeyPair(SERVICE_CERT_THUMBPRINT, u'/home/bazaar/.ssh/id_rsa')
        system = LinuxConfigurationSet(hostname, 'bazaar', 'u7;9jbp!', True)
        system.ssh.public_keys.public_keys.append(pk)
        system.ssh.key_pairs.key_pairs.append(pair)
        system.disable_ssh_password_authentication = True 
        return system

    def _network_config(self, subnet_name=None, port='22'):
        network = ConfigurationSet()
        network.configuration_set_type = 'NetworkConfiguration'
        network.input_endpoints.input_endpoints.append(
            ConfigurationSetInputEndpoint('SSH', 'tcp', port, '22'))
        if subnet_name:
            network.subnet_names.append(subnet_name)
        return network

    def _os_hd(self, image_name, target_container_name, target_blob_name):
        media_link = self._make_blob_url(
            #//credentials.getStorageServicesName(),
            'ddxstorage',
            target_container_name, target_blob_name)
        os_hd = OSVirtualHardDisk(image_name, media_link,
            disk_label=target_blob_name)
        return os_hd

    def _make_blob_url(self, storage_account_name, container_name, blob_name):
        return 'http://{0}.blob.core.windows.net/{1}/{2}'.format(
            storage_account_name, container_name, blob_name)

    def create_storage(self):
        name = 'ddxstorage'
        label = 'mystorageaccount'
        location = 'West US'
        desc = 'My storage account description.'

        result = self.sms.create_storage_account(name, desc, label, location=location)

        operation_result = self.sms.get_operation_status(result.request_id)
        print('Operation status: ' + operation_result.status)

    def list_storage(self):
        result = self.sms.list_storage_accounts()
        for account in result:
            print('Service name: ' + account.service_name)
            print('Location: ' + account.storage_service_properties.location)
            print('')

    def delete_storage(self):
        self.sms.delete_storage_account('mystorageaccount')

    def _wait_for_async(self, request_id):
        self.sms.wait_for_operation_status(request_id, timeout=600)

    def _wait_for_deployment(self, service_name, deployment_name,
    			  status='Running'):
       count = 0
       props = self.sms.get_deployment_by_name(service_name, deployment_name)
       while props.status != status:
          count = count + 1
          if count > 120:
             self.assertTrue(
                False, 'Timed out waiting for deployment status.')
          time.sleep(5)
          props = self.sms.get_deployment_by_name(
             service_name, deployment_name)

    def _wait_for_role(self, service_name, deployment_name, role_instance_name,
                      status='ReadyRole'):
        count = 0
        props = self.sms.get_deployment_by_name(service_name, deployment_name)
        while self._get_role_instance_status(props, role_instance_name) != status:
            count = count + 1
            if count > 120:
                self.assertTrue(
                    False, 'Timed out waiting for role instance status.')
            time.sleep(5)
            props = self.sms.get_deployment_by_name(
                service_name, deployment_name)

    def _get_role_instance_status(self, deployment, role_instance_name):
        for role_instance in deployment.role_instance_list:
            if role_instance.instance_name == role_instance_name:
                return role_instance.instance_status
        return None


    def delete_hosted_service(self):
        print('deleting hosted service')
        try:
            self.sms.delete_hosted_service(service_name, complete=True)
        except:
            pass
        if os.path.exists('.state'):
            shutil.rmtree('.state')

    def create_state_dir(self):
        try:
            os.makedirs('.state')
        except OSError as exc:
            if exc.errno == errno.EEXIST and os.path.isdir('.state'):
                print("Found existing .state dir. Please terminate instances first.")
                exit(1)
            else: raise

    def list_os_images_public(self):
        result = self.sms.list_os_images()
        for img in result:
            print(img.name)

    def create_service_certificate(self):
        with open(cert_path, "rb") as bfile:
            cert_data = base64.b64encode(bfile.read()).decode() 
            cert_format = 'pfx'
            cert_password = ''
            cert_res = self.sms.add_service_certificate(service_name=service_name,
                data=cert_data,
                certificate_format=cert_format,
                password=cert_password)
        self._wait_for_async(cert_res.request_id)

    def create_deployment_and_roles(self, num_machines = 1):
        deployment_name = service_name

        # one role for each machine
        roles = []
        for i in range(0, num_machines):
            roles.append(service_name + str(i))

        system, os_hd, network = self._linux_role(roles[0], port='2000')

        result = self.sms.create_virtual_machine_deployment(
            service_name, deployment_name, 'production',
            deployment_name + 'label', roles[0], system, os_hd,
            network, role_size='Small')

        self._wait_for_async(result.request_id)
        self._wait_for_deployment(service_name, deployment_name)
        self._wait_for_role(service_name, deployment_name, roles[0])

        for i in range(1, len(roles)):
            system, os_hd, network = self._linux_role(roles[i], port='59914')
            subnet_name = None
            port = 2000 + i
            network = self._network_config(subnet_name, port)

            result = self.sms.add_role(service_name, deployment_name, roles[i],
                system, os_hd, network)
            self._wait_for_async(result.request_id)
            self._wait_for_role(service_name, deployment_name, roles[i])
         
        # write to .state
        with open('.state/HOSTS', 'w') as f:
            for i in range(0, len(roles)):
                f.write('bazaar@' + service_name + '.cloudapp.net:' + str(2000+i) + '\n')

def launch(argv):
    num_instances = 1
    try:
        opts, args = getopt.getopt(argv,"n:",[])
    except getopt.GetoptError:
        #print " -n <numinstances>"
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-n':
            num_instances = int(arg)
    print('launching ' + str(num_instances) + ' instances')

    client = AzureClient()
    client.create_state_dir()
    client.create_hosted_service()
    client.create_deployment_and_roles(num_instances)

def terminate():
    client = AzureClient()
    client.delete_hosted_service()

def usage():
    print("Usage: azure-client.py launch|terminate [OPTIONS]")
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
