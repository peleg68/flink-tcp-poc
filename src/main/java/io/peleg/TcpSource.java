package io.peleg;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TcpSource extends RichParallelSourceFunction<String> {

    private volatile boolean running = true;
    private String[] servers;
    private int[] ports;

    public TcpSource(String[] servers, int[] ports) {
        this.servers = servers;
        this.ports = ports;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        int numTasks = getRuntimeContext().getNumberOfParallelSubtasks();
        int taskIndex = getRuntimeContext().getIndexOfThisSubtask();

        // Calculate which servers this Task Slot should connect to
        int numServersPerTask = (int)Math.ceil((double)servers.length / numTasks);
        int startIndex = taskIndex * numServersPerTask;
        int endIndex = Math.min((taskIndex + 1) * numServersPerTask, servers.length);

        // Connect to the servers and start reading data
        Socket[] sockets = new Socket[endIndex - startIndex];
        BufferedReader[] readers = new BufferedReader[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++) {
            sockets[i - startIndex] = new Socket(servers[i], ports[i]);
            readers[i - startIndex] = new BufferedReader(new InputStreamReader(sockets[i - startIndex].getInputStream()));
        }

        while (running) {
            readData(readers, ctx);
        }

        // Close the connections to the servers
        for (int i = 0; i < endIndex - startIndex; i++) {
            readers[i].close();
            sockets[i].close();
        }
    }

    private void readData(BufferedReader[] readers, SourceContext<String> ctx) throws Exception {
        // Read a line of data from each server
        for (int i = 0; i < readers.length; i++) {
            try {
                String line = readers[i].readLine();
                if (line == null) {
                    // Server connection was closed, try to reconnect
                    Socket socket = new Socket(servers[i], ports[i]);
                    readers[i] = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } else {
                    // Emit the data as a stream
                    ctx.collect(line);
                }
            } catch (Exception e) {
                // An error occurred, try to reconnect
                Socket socket = new Socket(servers[i], ports[i]);
                readers[i] = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        }
    }

    @Override
    public void cancel() {
        // Clean up resources when the Flink job is cancelled
        running = false;
    }
}

