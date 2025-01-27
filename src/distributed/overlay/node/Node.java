package distributed.overlay.node;

import distributed.overlay.tcp.TCPConnection;
import distributed.overlay.wireformats.Event;

/**
 * Represents a node capable of handling incoming events from a TCP connection.
 */
public interface Node {
    /**
     * Handles incoming events from the TCP connection.
     *
     * @param event      The event to handle.
     * @param connection The TCP connection associated with the event.
     */
    // implicitly public
    void handleIncomingEvent(Event event, TCPConnection connection);
}
