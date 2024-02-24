package csx55.threads.node;

import csx55.threads.tcp.TCPConnection;

import java.util.ArrayList;
import java.util.HashSet;

public class OverlayNode {
    /* create overlay topology with hosts and connections of Computation Nodes */

    public String hostString;
    protected TCPConnection connection;

    protected HashSet<String> neighbors;
    protected ArrayList<String> peers;

    public OverlayNode(String hostString, TCPConnection connection) {
        this.connection = connection;
        this.hostString = hostString;
        this.peers = new ArrayList<>();
        this.neighbors = new HashSet<>();
    }

    public void addLink(String link) {
        peers.add(link);
        updateNeighbor(link);
    }

    public void updateNeighbor(String link) {
        neighbors.add(link);
    }

}
