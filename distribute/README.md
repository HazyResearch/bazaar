Distribute
==========

Run your parser on multiple machines in parallel. Distribute provisions machines
on ec-2 or azure, then processes chunks of your data on each machine, and
finally terminates the machines.

1.  Check settings in `run.sh`, and run it

    ```
    ./run.sh
    ```
    Files `HOSTS` and `INSTANCE_IDS` will be created. 

2.  Install dependencies on remote machines
    ```
    fab -f fab.py install
    ```

3.  Split input and copy chunks to remote machines
    ```
    fab -f fab.py split 
    fab -f fab.py copy
    ```

4.  Run parser on remote machines
    ```
    fab -f fab.py parse
    ``` 

5.  Collect results
    ```
    fab -f fab.py collect
    ```

6.  Stop remote machines
    ```
    ./stop.sh
    ```
