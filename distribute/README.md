Distribute
==========

Run your parser on multiple machines in parallel. Distribute provisions machines
on ec-2 or azure, then processes chunks of your data on each machine, and
finally terminates the machines.

Before you begin, run `./setup.sh` to make sure all dependencies are installed.

1.  Launch instances on ec-2 or azure.

    ```bash
    fab launch:cloud='ec-2',num=1
    ```
    This will launch 1 instance on ec-2. It will also put status information
    about the launched instance into `.state`.

    Note that using ec-2 and azure requires that the system has access to your credentials.
    For ec-2, make sure you have `.aws/credentials` set up or you can set the following environment variables.
    ```
    AWS_ACCESS_KEY_ID='...'
    AWS_SECRET_ACCESS_KEY='...'
    ```
    For azure, upload `ssh/mycert.cer` to the management portal via the "Upload" action of the "Settings" tab, and set the following environment variable.
    ```
    AZURE_SUBSCRIPTION_ID='...'
    ```

2.  Install dependencies on remote machines
    ```bash
    fab install
    ```

3.  Copy chunks to remote machines
    ```bash
    fab copy
    ```

4.  Run parser on remote machines
    ```bash
    fab parse
    ``` 

5.  Collect results
    ```bash
    fab collect
    ```

6.  Terminate remote machines
    ```bash
    fab terminate
    ```
    If termination is successful, the status information in `.state` will be deleted.
