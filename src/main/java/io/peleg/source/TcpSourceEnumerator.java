package io.peleg.source;

import lombok.AllArgsConstructor;
import org.apache.flink.api.connector.source.SourceSplit;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class TcpSourceEnumerator implements SplitEnumerator<TcpServerSplit, TcpSourceState> {
    private final SplitEnumeratorContext<TcpServerSplit> splitEnumeratorContext;

    @Override
    public void start() {

    }

    @Override
    public void handleSplitRequest(int i, @Nullable String s) {

    }

    @Override
    public void addSplitsBack(List<TcpServerSplit> list, int i) {

    }

    /**
     * Should assign given reader to pending splits using {@link SplitEnumeratorContext#assignSplit(SourceSplit, int)} or {@link SplitEnumeratorContext#assignSplits(SplitsAssignment)}.
     * @param i
     */
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
