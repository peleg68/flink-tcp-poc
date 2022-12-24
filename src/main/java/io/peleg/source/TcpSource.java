package io.peleg.source;

import org.apache.flink.api.connector.source.*;
import org.apache.flink.core.io.SimpleVersionedSerializer;


public class TcpSource implements Source<String, TcpServerSplit, TcpSourceState> {
    @Override
    public Boundedness getBoundedness() {
        return null;
    }

    @Override
    public SourceReader<String, TcpServerSplit> createReader(SourceReaderContext sourceReaderContext) throws Exception {
        return null;
    }

    @Override
    public SplitEnumerator<TcpServerSplit, TcpSourceState> createEnumerator(SplitEnumeratorContext<TcpServerSplit> splitEnumeratorContext) throws Exception {
        return null;
    }

    @Override
    public SplitEnumerator<TcpServerSplit, TcpSourceState> restoreEnumerator(SplitEnumeratorContext<TcpServerSplit> splitEnumeratorContext, TcpSourceState tcpSourceState) throws Exception {
        return null;
    }

    @Override
    public SimpleVersionedSerializer<TcpServerSplit> getSplitSerializer() {
        return null;
    }

    @Override
    public SimpleVersionedSerializer<TcpSourceState> getEnumeratorCheckpointSerializer() {
        return null;
    }
}
