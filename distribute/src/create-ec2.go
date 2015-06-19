package main

import (
    "bufio"
    "flag"
    "fmt"
    "io/ioutil"
    "log"
    "os"
    "path/filepath"
    "strings"
    "time"
    "github.com/aws/aws-sdk-go/aws"
    //"github.com/aws/aws-sdk-go/aws/awsutil"
    "github.com/aws/aws-sdk-go/service/ec2"
)

func Dir() (string) {
    dir, err := filepath.Abs(filepath.Dir(os.Args[0]))
    if err != nil {
            log.Fatal(err)
    }
    return dir
}

func ImportKeyPair(svc *ec2.EC2) {
    // read public key
    bytes, err := ioutil.ReadFile("ssh/bazaar.key.pub")
    if err != nil {
        fmt.Print("Cannot read key file: %s", err)
        return
    }

    params := &ec2.ImportKeyPairInput{
        KeyName:           aws.String("bazaar"),
        PublicKeyMaterial: bytes,
    }

    _, err = svc.ImportKeyPair(params)
    if err != nil {
        fmt.Printf("Error import KeyPair: %s", err)
        return
    }
}

func DeleteKeyPair(svc *ec2.EC2) {
    params := &ec2.DeleteKeyPairInput{
        KeyName: aws.String("bazaar"),
    }
    _, err := svc.DeleteKeyPair(params)
    if err != nil {
        fmt.Printf("Error import KeyPair: %s", err)
        return
    }
}

func CreateSecurityGroup(svc *ec2.EC2) {
    params_cr := &ec2.CreateSecurityGroupInput{
        Description: aws.String("enable SSH for DeepDive's Bazaar"),
        GroupName:   aws.String("bazaar_group"),
    }
    _, err := svc.CreateSecurityGroup(params_cr)
    if err != nil {
        fmt.Printf("Error creating SecurityGroup: %s", err)
        return
    }

    // authorize SSH ingress
    params_au := &ec2.AuthorizeSecurityGroupIngressInput{
        CIDRIP:    aws.String("0.0.0.0/0"),
        FromPort:  aws.Long(22),
        GroupName: aws.String("bazaar_group"),
        IPProtocol: aws.String("tcp"),
	ToPort: aws.Long(22),
    }
    _, err = svc.AuthorizeSecurityGroupIngress(params_au)

    if err != nil {
        fmt.Printf("Error creating authorizing security group ingress: %s", err)
        return
    }
}

func DeleteSecurityGroup(svc *ec2.EC2) {
    params := &ec2.DeleteSecurityGroupInput{
        GroupName: aws.String("bazaar_group"),
    }
    _, err := svc.DeleteSecurityGroup(params)

    if err != nil {
        fmt.Printf("Error deleting security group: %s", err)
        return
    }
}

func RunInstances(svc *ec2.EC2) ([]*string) { 
    params := &ec2.RunInstancesInput{
        ImageID:      aws.String("ami-d05e75b8"),
        InstanceType: aws.String("m3.large"),
        MinCount:     aws.Long(1),
        MaxCount:     aws.Long(1),
        KeyName:      aws.String("bazaar"),
    }

    runResult, err := svc.RunInstances(params)
    if err != nil {
        log.Println("Could not create instance", err)
        panic(err)
    }

    instanceID := *runResult.Instances[0].InstanceID
    log.Println("Created instance", *runResult.Instances[0].InstanceID)

    // Add tags to the instance
    _, err = svc.CreateTags(&ec2.CreateTagsInput{
        Resources: []*string{runResult.Instances[0].InstanceID},
        Tags:      []*ec2.Tag{
            &ec2.Tag{
                Key:   aws.String("Name"),
                Value: aws.String("instanceName"),
            },
        },
    })
    if err != nil {
        log.Println("Could not create tags for instance", instanceID, err)
        panic(err)
    }

    instanceIDs := make([]*string, len(runResult.Instances))
    for i, k := range runResult.Instances {
       instanceIDs[i] = k.InstanceID
    }

    return instanceIDs
}

func TerminateInstances(svc *ec2.EC2) {
    bytes, err := ioutil.ReadFile("INSTANCE_IDS")

    if err != nil {
        log.Println("Could not find instance ids: ", err)
        panic(err)
    }
    lines := strings.Split(strings.TrimSpace(string(bytes)), "\n")
    instanceIDs := make([]*string, len(lines))
    for i, _ := range lines {
        fmt.Println(lines[i])
        instanceIDs[i] = aws.String(lines[i])
    }

    params := &ec2.TerminateInstancesInput{
        InstanceIDs: instanceIDs,
        //InstanceIDs: []*string{
        //     aws.String("i-7359b5a0"),
        //},
    }
    _, err = svc.TerminateInstances(params)

    if err != nil {
        log.Println("Could not terminate instances: ", err)
        return
    }
}

func GetStates(svc *ec2.EC2, instanceIDs []*string) ([]*string) {

    params := &ec2.DescribeInstanceStatusInput{
        IncludeAllInstances: aws.Boolean(true),
        InstanceIDs:instanceIDs,
    }
    res, err := svc.DescribeInstanceStatus(params)

    if err != nil {
        log.Println("Could not terminate instances: ", err)
        panic(err)
    }

    states := make([]*string, len(instanceIDs))
    
    for i, stat := range res.InstanceStatuses {
        states[i] = stat.InstanceState.Name
    }
    return states
}

func GetDNSs(svc *ec2.EC2, instanceIDs [] *string) ([]*string) {
    
    params := &ec2.DescribeInstancesInput{
        InstanceIDs: instanceIDs,
    } 
    resp, err := svc.DescribeInstances(params)

    if err != nil {
        fmt.Printf("Error: %s", err)
        panic(err)
    }

    //fmt.Println(awsutil.StringValue(resp))

    dns := make([]*string, len(instanceIDs))
    for i, k := range resp.Reservations[0].Instances {
       dns[i] = k.PublicDNSName
    }
    return dns
}


func WaitForInstances(svc *ec2.EC2, instanceIDs []*string) {
    numPending := 0
    for { 
        numPending = 0
        states := GetStates(svc, instanceIDs)
        for _, state := range states {
            //if state == "shutting-down" {
                
            //}
            fmt.Println(*state)
            if *state == "pending" {
                numPending = numPending + 1
            }
        }

        if numPending > 0 {
            fmt.Println("Waiting for instances: ", numPending)
            time.Sleep(1 * time.Second)
        } else {
            break
        }
    }
    dnss := GetDNSs(svc, instanceIDs)

    // write dns names to file
    WriteLinesToFile("HOSTS", dnss)
    WriteLinesToFile("INSTANCE_IDS", instanceIDs)
}

func WriteLinesToFile(filename string, lines []*string) {
    // write dns names to file
    f, err := os.Create(filename)
    if err != nil {
        panic(err)
    }
    // close fo on exit and check for its returned error
    defer func() {
        if err := f.Close(); err != nil {
            panic(err)
        }
    }()
    w := bufio.NewWriter(f)
    for _, line := range lines {
        w.WriteString(*line + "\n")
    }
    w.Flush()
}


var numMachines int
var cmd string

func Init() {
    flag.IntVar(&numMachines, "n", 1, "number of machines to launch")
    flag.StringVar(&cmd, "c", "create", "create|destroy")
    flag.Parse()
}

func main() {    
    Init()

    svc := ec2.New(&aws.Config{Region: "us-east-1"})

    if cmd == "create" {
        DeleteKeyPair(svc)
        ImportKeyPair(svc)

        DeleteSecurityGroup(svc)
        CreateSecurityGroup(svc) 

        instanceIDs := RunInstances(svc)
        WaitForInstances(svc, instanceIDs)

        fmt.Println(`DONE. Your instances are live. 
           Note that it might still take a few minutes until you can log in.`)

    } else if cmd == "destroy" {
        TerminateInstances(svc)
    }
}
