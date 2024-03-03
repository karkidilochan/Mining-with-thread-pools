package csx55.overlay.node;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import csx55.overlay.tcp.TCPConnection;
// import csx55.overlay.wireformats.LinkWeights;
import csx55.overlay.wireformats.ComputeNodesList;

/**
 * Represents the overlay network setup and management functionality.
 * Create overlay topology and connections using OverlayNode and LinkWeights
 */
public class Overlay {

    /* create overlay from connections stored in the Registry */
    public void setupOverlay(Map<String, TCPConnection> connections, int threadPoolSize)
            throws Exception {
        /* get total existing connections and ensure there are existing connections */
        int totalConnections = connections.size();
        /*
         * take ipaddresses of all connections and create node instances of overlay
         * and form a ring connection between these instances
         */
        String[] ipAddresses = new String[totalConnections];
        OverlayNode[] topology = new OverlayNode[totalConnections];
        int index = 0;
        for (Entry<String, TCPConnection> entry : connections.entrySet()) {
            String hostAddress = entry.getKey();
            ipAddresses[index] = hostAddress;
            topology[index++] = new OverlayNode(hostAddress, entry.getValue());
        }
        /*
         * form a ring topology from the obtained list of ipaddresses and overlay nodes
         */
        for (int node = 0; node < totalConnections; node++) {
            topology[node].addLink(ipAddresses[(node + 1) % totalConnections]);
        }

        /* then disperse this connection info to each node */
        for (OverlayNode node : topology) {
            List<String> peers = node.peers;
            int numberOfPeers = peers.size();

            ComputeNodesList message = new ComputeNodesList(numberOfPeers, peers, threadPoolSize, totalConnections);
            node.connection.getTCPSenderThread().sendData(message.getBytes());
        }

    }

}
