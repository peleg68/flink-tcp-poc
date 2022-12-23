# flink-tcp-poc

This is a POC of a Flink job that connects to an arbitrary list of TCP servers.
The job will print all numbers received from servers that can be divided by 6.

This job is built in a fault-tolerant way so that if for some reason a connection is closed it will recreate it using Flink's restart mechanism. The restart strategy used is exponential delay strategy.

The repository also contains Python code to set up demo TCP servers that send random numbers between 1 and 666. After a connection is closed by the client the server will start listening again at the same port.

## Running Yourself

You can try to test this POC yourself by following these steps:
1. Set up the TCP servers by running these commands in different terminals:
   ```bash
    python ./src/main/python/tcpserver.py 8001
    python ./src/main/python/tcpserver.py 8002
    python ./src/main/python/tcpserver.py 8003
    python ./src/main/python/tcpserver.py 8004
    ```
   In each terminal window you should see something like this:
   ```
   INFO:root:Opening server on host  and port 8001.
   INFO:root:Started listening on host  and port 8001.
   ```
2. Open the root folder in IntelliJ.
3. Run the main method in `DataStreamJob`.
   > The `pom.xml` is configured to automatically add provided dependencies when run inside IntelliJ. If you are running outside of IntelliJ you would need to manually add provided Flink dependencies to the classpath. 
   
   The Flink job will print these logs:
   ```
   15:27:57,357 INFO  io.peleg.TcpSource [] - Task index 1 out of 4 allocated servers 1 to 2
   15:27:57,357 INFO  io.peleg.TcpSource [] - Task index 3 out of 4 allocated servers 3 to 4
   15:27:57,357 INFO  io.peleg.TcpSource [] - Task index 2 out of 4 allocated servers 2 to 3
   15:27:57,357 INFO  io.peleg.TcpSource [] - Task index 0 out of 4 allocated servers 0 to 1
   15:27:57,362 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8004
   15:27:57,362 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8003
   15:27:57,362 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8002
   15:27:57,362 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8001
   15:27:57,370 INFO  io.peleg.TcpSource [] - Connected successfully to localhost:8003
   15:27:57,370 INFO  io.peleg.TcpSource [] - Connected successfully to localhost:8001
   15:27:57,370 INFO  io.peleg.TcpSource [] - Connected successfully to localhost:8004
   15:27:57,370 INFO  io.peleg.TcpSource [] - Connected successfully to localhost:8002
   15:27:57,371 INFO  io.peleg.TcpSource [] - Initialized sockets and readers for task index 2
   15:27:57,371 INFO  io.peleg.TcpSource [] - Initialized sockets and readers for task index 1
   15:27:57,371 INFO  io.peleg.TcpSource [] - Initialized sockets and readers for task index 0
   15:27:57,372 INFO  io.peleg.TcpSource [] - Initialized sockets and readers for task index 3
   ```
   And the TCP servers will print these logs:
   ```
   INFO:root:Accepted connection on address ('127.0.0.1', 62278).
   ```
   
## Parallelism and Data Partitioning
The `TcpSource` is written in a scalable manner such that the connections to the different servers will be split fairly between all of the Flink workers in the cluster.

We can see this working by playing with the parallelism parameter. Currently, the parallelism is set to the amount of servers (4):
```java
env.setParallelism(servers.length);
```
This is because at a larger parallelism Flink will set up too much instances of the source operator which will not be allocated to any server.

Because the parallelism is equal to the amount of servers, each instance will be allocated a single server:
```
16:55:01,991 INFO  io.peleg.TcpSource [] - Task index 3 out of 4 allocated servers 3 to 4
16:55:01,991 INFO  io.peleg.TcpSource [] - Task index 1 out of 4 allocated servers 1 to 2
16:55:01,991 INFO  io.peleg.TcpSource [] - Task index 0 out of 4 allocated servers 0 to 1
16:55:01,991 INFO  io.peleg.TcpSource [] - Task index 2 out of 4 allocated servers 2 to 3
```

Let's try to set the parallelism to a number lower than the amount of servers:
```java
env.setParallelism(2);
```
Now when we run it this is how the servers are allocated:
```
16:47:24,670 INFO  io.peleg.TcpSource [] - Task index 1 out of 2 allocated servers 2 to 4
16:47:24,670 INFO  io.peleg.TcpSource [] - Task index 0 out of 2 allocated servers 0 to 2
```
We can even try un-even allocation. Let's remove one of the servers to only connect to 3 servers:
```java
String[] servers = {
       "localhost",
       "localhost",
       "localhost"
};

int[] ports = {
       8001,
       8002,
       8003
};
```
Now when we run it this is how the servers are allocated:
```
16:52:09,857 INFO  io.peleg.TcpSource [] - Task index 0 out of 2 allocated servers 0 to 2
16:52:09,857 INFO  io.peleg.TcpSource [] - Task index 1 out of 2 allocated servers 2 to 3
```

To sum it up, as long as the parallelism is not higher than the amount of servers, your cluster resources will be used evenly.

## Fault-Tolerance
We can try to shut down the one of the TCP servers and see the fault tolerance mechanism at work.

Let's try to shut down the server at port 8002 by pressing Ctrl + C on its terminal window.
Flink will immediately kick the restart mechanism into action and we going to see these Logs:
```
15:55:11,622 ERROR io.peleg.TcpSource [] - Error, got null reading from reader 0 on task 1, Socket is localhost:8002
15:55:12,709 INFO  io.peleg.TcpSource [] - Task index 1 out of 4 allocated servers 1 to 2
15:55:12,711 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8002
15:55:12,714 ERROR io.peleg.TcpSource [] - Connection refused by localhost:8002
15:55:14,613 INFO  io.peleg.TcpSource [] - Task index 1 out of 4 allocated servers 1 to 2
15:55:14,615 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8002
15:55:14,615 ERROR io.peleg.TcpSource [] - Connection refused by localhost:8002
```
Flink will keep trying to reconnect at bigger intervals until the 3 minutes threshold is hit or the server comes back online.

You can see that while the connection with the server on port 8002 is failing, the other connections are still active and outputting results.

Let's try to bring the server back online:
```bash
python ./src/main/python/tcpserver.py 8002
```

And now Flink will be able to reconnect successfully:
```
15:59:10,512 INFO  io.peleg.TcpSource [] - Task index 1 out of 4 allocated servers 1 to 2
15:59:10,514 INFO  io.peleg.TcpSource [] - Trying to connect to localhost:8002
15:59:10,515 INFO  io.peleg.TcpSource [] - Connected successfully to localhost:8002
15:59:10,516 INFO  io.peleg.TcpSource [] - Initialized sockets and readers for task index 1
```