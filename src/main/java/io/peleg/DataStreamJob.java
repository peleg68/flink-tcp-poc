package io.peleg;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;

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
                "localhost",
                "localhost",
                "localhost",
                "localhost"
        };

        int[] ports = {
                8001,
                8002,
                8003,
                8004
        };

        env.setParallelism(servers.length);

        env.addSource(new TcpSource(servers, ports))
                .filter(s -> Integer.parseInt(s) % 50 == 0)
                .print();

        env.execute("tcp-poc");
    }
}
