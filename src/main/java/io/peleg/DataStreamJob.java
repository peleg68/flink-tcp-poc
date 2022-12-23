package io.peleg;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setRestartStrategy(RestartStrategies.exponentialDelayRestart(
                Time.seconds(1L),
                Time.seconds(60L),
                2.0,
                Time.minutes(3L),
                0.1
        ));

        String[] servers = {
                "localhost"
        };

        int[] ports = {
                8001
        };

        env.setParallelism(servers.length);

        env.addSource(new TcpSource(servers, ports))
                .filter(s -> Integer.parseInt(s) % 50 == 0)
                .print();

        env.execute("tcp-poc");
    }
}
