package csx55.threads.node;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.wireformats.Message;

public interface Node {
    void handleIncomingMessage(Message message, TCPConnection connection);
}
