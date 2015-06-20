Distribute
==========

Run your parser on multiple machines in parallel. Distribute provisions machines
on ec-2 or azure, then processes chunks of your data on each machine, and
finally terminates the machines.

Before you start, run `./setup.sh` to make sure all dependencies are installed.

1.  Launch instances on ec-2 or azure.

    ```bash
    fab launch:cloud='ec-2',num=1
    ```
    This will launch 1 instance on ec-2.

    Files `HOSTS` and `INSTANCE_IDS` will be created. `HOSTS` contains the dns names of the launched instances and is needed for processing. `INSTANCE_IDS` contains cloud-specific instance identifiers that are needed for shutting down instances.
   

2.  Install dependencies on remote machines
    ```bash
    fab install
    ```

3.  Split input and copy chunks to remote machines
    ```bash
    fab split 
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
    If termination is successful, files `HOSTS` and `INSTANCE_IDS` will be deleted.
