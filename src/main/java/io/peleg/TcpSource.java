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
            // Read a line of data from each server
            for (int i = 0; i < endIndex - startIndex; i++) {
                String line = readers[i].readLine();
                ctx.collect(line);
            }
        }

        // Close the connections to the servers
        for (int i = 0; i < endIndex - startIndex; i++) {
            readers[i].close();
            sockets[i].close();
        }
    }

    @Override
    public void cancel() {
        // Clean up resources when the Flink job is cancelled
        running = false;
    }
}

