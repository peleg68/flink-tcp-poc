package io.peleg.source;

import org.apache.flink.api.connector.source.SplitEnumerator;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class TcpSourceEnumerator implements SplitEnumerator<TcpServerSplit, TcpSourceState> {

    @Override
    public void start() {

    }

    @Override
    public void handleSplitRequest(int i, @Nullable String s) {

    }

    @Override
    public void addSplitsBack(List<TcpServerSplit> list, int i) {

    }

    @Override
    public void addReader(int i) {

    }

    @Override
    public TcpSourceState snapshotState(long l) throws Exception {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
