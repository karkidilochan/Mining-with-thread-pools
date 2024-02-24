package csx55.threads.node;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.wireformats.ComputationNodesList;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

public class Overlay {

    /* create overlay from connections stored in the Registry */
    public void setupOverlay(Map<String, TCPConnection> connections, int connectingEdges) throws Exception {
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
            ArrayList<String> peers = node.peers;
            int numberOfPeers = peers.size();

            ComputationNodesList message = new ComputationNodesList(numberOfPeers, peers);
            node.connection.getSenderThread().sendData(message.getBytes());
        }

    }

}
