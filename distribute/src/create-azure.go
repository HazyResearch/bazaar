package main

import (
    "encoding/base64"
    "fmt"

    "github.com/Azure/azure-sdk-for-go/management"
    "github.com/Azure/azure-sdk-for-go/management/hostedservice"
    "github.com/Azure/azure-sdk-for-go/management/storageservice"
    "github.com/Azure/azure-sdk-for-go/management/virtualmachine"
    "github.com/Azure/azure-sdk-for-go/management/vmutils"
)

func main() {
    dnsName := "ddyyy4"
    storageAccount := "ddyyy4mystorageaccount"
    location := "West US"
    vmSize := "Small"
    vmImage := "b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-14_04-LTS-amd64-server-20140724-en-us-30GB"
    userName := "testuser"
    userPassword := "Test123"

    client, err := management.ClientFromPublishSettingsFile("credentials.publishsettings", "")
    if err != nil {
        panic(err)
    }

    ss, err := storageservice.NewClient(client).CreateStorageService(storageservice.StorageAccountCreateParameters{
       ServiceName: storageAccount,
       Label: base64.StdEncoding.EncodeToString([]byte(storageAccount)),
       Location: location,
       AccountType: storageservice.AccountTypeStandardLRS,
    })
    if err != nil {
       panic(err)
    }

    println(ss)

    // create hosted service
    if err := hostedservice.NewClient(client).CreateHostedService(hostedservice.CreateHostedServiceParameters{
        ServiceName: dnsName,
        Location:    location,
        Label:       base64.StdEncoding.EncodeToString([]byte(dnsName))}); err != nil {
        panic(err)
    }

    // create virtual machine
    role := vmutils.NewVMConfiguration(dnsName, vmSize)
    vmutils.ConfigureDeploymentFromPlatformImage(
        &role,
        vmImage,
        fmt.Sprintf("http://%s.blob.core.windows.net/sdktest/%s.vhd", storageAccount, dnsName),
        "")
    vmutils.ConfigureForLinux(&role, dnsName, userName, userPassword)
    vmutils.ConfigureWithPublicSSH(&role)

    operationID, err := virtualmachine.NewClient(client).
        CreateDeployment(role, dnsName, virtualmachine.CreateDeploymentOptions{})
    if err != nil {
        panic(err)
    }
    if err := client.WaitForOperation(operationID, nil); err != nil {
        panic(err)
    }
}
