package io.peleg;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
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

        log.info("Task index {} out of {} allocated servers {} to {}", taskIndex, numTasks, startIndex, endIndex);

        // Connect to the servers and start reading data
        Socket[] sockets = new Socket[endIndex - startIndex];
        BufferedReader[] readers = new BufferedReader[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++) {
            sockets[i - startIndex] = new Socket(servers[i], ports[i]);
            readers[i - startIndex] = new BufferedReader(new InputStreamReader(sockets[i - startIndex].getInputStream()));
        }

        log.info("Initialized sockets and readers for task index {}", taskIndex);

        while (running) {
            readData(readers, sockets, ctx, taskIndex);
        }

        // Close the connections to the servers
        for (int i = 0; i < endIndex - startIndex; i++) {
            readers[i].close();
            sockets[i].close();
        }
    }

    private void readData(BufferedReader[] readers, Socket[] sockets, SourceContext<String> ctx, int taskIndex) throws Exception {
        // Read a line of data from each server
        for (int i = 0; i < readers.length; i++) {
            log.debug("Trying to read from reader {} on task {}", i, taskIndex);

            String line;

            try {
                line = readers[i].readLine();
            } catch (IOException e) {
                log.error("Error reading from reader {} on task {}", i, taskIndex, e);
                line = null;
            }

            if (line == null) {
                log.debug("Closing socket for broken connection with server {} on task {}", i, taskIndex);

                readers[i].close();
                sockets[i].close();

                log.debug("Closed socket successfully for broken connection with server {} on task {}", i, taskIndex);

                log.info("Trying to reconnect to server {} on task {}", i, taskIndex);

                sockets[i] = new Socket(servers[i], ports[i]);
                readers[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));

                log.info("Initialized socket and reader for server {} on task {}", i, taskIndex);
            } else {
                // Emit the data as a stream
                ctx.collect(line);
            }
        }
    }

    @Override
    public void cancel() {
        // Clean up resources when the Flink job is cancelled
        running = false;
    }
}

