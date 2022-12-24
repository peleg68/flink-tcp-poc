package io.peleg.source;

import org.apache.flink.api.connector.source.SourceSplit;

public class TcpServerSplit implements SourceSplit {
    @Override
    public String splitId() {
        return null;
    }
}
