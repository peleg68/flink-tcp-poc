package io.peleg.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.flink.api.connector.source.SourceSplit;

@Data
@AllArgsConstructor
public class TcpServerSplit implements SourceSplit {
    private final String host;
    private final int port;

    @Override
    public String splitId() {
        return host + ":" + port;
    }
}
