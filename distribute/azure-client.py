#!/usr/bin/env python3

from azure import *
from azure.servicemanagement import *

subscription_id = 'baf18420-bac2-4745-891d-16e249928ce8'
certificate_path = 'ssh/mycert.pem'

sms = ServiceManagementService(subscription_id, certificate_path)

def list_locations():
    result = sms.list_locations()
        for location in result:
            print(location.name)

def create_service():
    name = 'myhostedservice'
    label = 'myhostedservice'
    desc = 'my hosted service'
    location = 'West US'
    sms.create_hosted_service(name, label, desc, location)

def list_services():
    result = sms.list_hosted_services()

    for hosted_service in result:
        print('Service name: ' + hosted_service.service_name)
        print('Management URL: ' + hosted_service.url)
        print('Location: ' + hosted_service.hosted_service_properties.location)
        print('')

def delete_service():
    sms.delete_hosted_service('myhostedservice')

def delete_deployment():
    sms.delete_deployment('myhostedservice', 'v1')

def create_machine():
    name = 'myvm'
    location = 'West US'

    #Set the location
    sms.create_hosted_service(service_name=name,
	label=name,
	location=location)

    # Name of an os image as returned by list_os_images
    image_name = 'OpenLogic__OpenLogic-CentOS-62-20120531-en-us-30GB.vhd'

    # Destination storage account container/blob where the VM disk
    # will be created
    media_link = 'url_to_target_storage_blob_for_vm_hd'

    # Linux VM configuration, you can use WindowsConfigurationSet
    # for a Windows VM instead
    linux_config = LinuxConfigurationSet('myhostname', 'myuser', 'mypassword', True)

    os_hd = OSVirtualHardDisk(image_name, media_link)

    sms.create_virtual_machine_deployment(service_name=name,
	deployment_name=name,
	deployment_slot='production',
	label=name,
	role_name=name,
	system_config=linux_config,
	os_virtual_hard_disk=os_hd,
	role_size='Small')

def delete_machine():
    sms.delete_deployment(service_name='myvm',
        deployment_name='myvm')

    sms.delete_hosted_service(service_name='myvm')

def create_storage():
    name = 'mystorageaccount'
    label = 'mystorageaccount'
    location = 'West US'
    desc = 'My storage account description.'

    result = sms.create_storage_account(name, desc, label, location=location)

    operation_result = sms.get_operation_status(result.request_id)
    print('Operation status: ' + operation_result.status)

def list_storage():
    result = sms.list_storage_accounts()
    for account in result:
	print('Service name: ' + account.service_name)
	print('Location: ' + account.storage_service_properties.location)
	print('')

def delete_storage():
    sms.delete_storage_account('mystorageaccount')



