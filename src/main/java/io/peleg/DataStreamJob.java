package io.peleg;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String[] servers = {
            "localhost",
            "localhost",
            "localhost",
            "localhost",
        };

        int[] ports = {
            8001,
            8002,
            8003,
            8004,
        };

        env.addSource(new TcpSource(servers, ports))
                        .print("print-sink");

        env.execute("tcp-poc");
    }
}
