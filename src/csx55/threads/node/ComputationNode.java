package csx55.threads.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.tcp.TCPServer;
import csx55.threads.wireformats.ComputationNodesList;
import csx55.threads.wireformats.Message;
import csx55.threads.wireformats.Protocol;
import csx55.threads.wireformats.Register;

public class ComputationNode implements Node {
    private final String host;
    private final int port;
    private TCPConnection registryConnection;

    /* data structure to store connections with other computation nodes */
    private Map<String, TCPConnection> connections = new ConcurrentHashMap<>();

    /*
     * constructor takes local host port and local port to initialize a computation
     * node
     */
    public ComputationNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /*
     * computation node on creation will start a TCPServer thread and register
     * itself with the registry
     * Then, stays live to take commands from user
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java csx55.threads.node.ComputationNode registry-host registry-portNum");
            System.exit(1);
        }
        /*
         * passing 0 to ServerSocket will return a free port
         * this server socket will accept incoming connections from other computation
         * nodes in the overlay
         */
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();

            ComputationNode computationNode = new ComputationNode(InetAddress.getLocalHost().getHostName(), port);

            /*
             * start a server thread for this computation node
             * this will create a TCP connection with sender and receiver threads
             */
            (new Thread(new TCPServer(computationNode, serverSocket))).start();

            // register the node
            computationNode.registerNode(args[0], Integer.valueOf(args[1]));

        } catch (IOException e) {
            System.out.println("An error occurred in the computation node: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void handleIncomingMessage(Message message, TCPConnection connection) {
        System.out.println("Received message: " + message.toString());

        switch (message.getType()) {
            case Protocol.REGISTER_REQUEST:
                /* store records of connections with other computation nodes */
                recordNewConnection((Register) message, connection);
                break;

            case Protocol.COMPUTATION_NODES_LIST:
                createOverlayConnections((ComputationNodesList) message);
                break;

        }
    }

    private void registerNode(String registryHost, int registryPort) {
        try {
            // create a client socket to registry
            Socket socketToRegistry = new Socket(registryHost, registryPort);

            /*
             * create a connection with this socket and print out the host and port of this
             * node
             */

            TCPConnection connection = new TCPConnection(this, socketToRegistry);
            System.out.println("A Computation Node is live at:" + this.host + ":" + this.port);

            /* send marshallized Register message to the registry */
            Register register = new Register(Protocol.REGISTER_REQUEST, this.host, this.port);
            connection.getSenderThread().sendData(register.getBytes());
            // TODO: check if this start is redundant, since this connection is supposed to
            // start at TCP server
            connection.start();

            this.registryConnection = connection;

        } catch (IOException | InterruptedException e) {
            System.out.println("Error registering node: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recordNewConnection(Register register, TCPConnection connection) {
        String nodeDetails = register.getNodeConnectionReadable();

        /* storing incoming connections */
        connections.put(nodeDetails, connection);
    }

    private void createOverlayConnections(ComputationNodesList message) {
        /*
         * create overlay connections through socket for each peer
         * then send a register message to these nodes
         * the receiving end will record the new connection in their connections
         */
        List<String> peers = message.getPeersList();

        for (String peer : peers) {
            String[] peerInfo = peer.split(":");
            try {
                /*
                 * for each peer, open a socket to it, and send it a Register message, this will
                 * update the connections in the receiving node
                 */
                int peerPort = Integer.parseInt(peerInfo[1]);
                Socket socketToNode = new Socket(peerInfo[0], peerPort);
                TCPConnection connection = new TCPConnection(this, socketToNode);
                Register register = new Register(Protocol.REGISTER_REQUEST, this.host, this.port);
                connection.getSenderThread().sendData(register.getBytes());
                connection.start();

                // also recording outgoing connections
                connections.put(peer, connection);
            } catch (NumberFormatException | IOException | InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        int numberOfPeers = message.getNumberOfPeers();
        System.out.println("Established connections with " + Integer.toString(numberOfPeers) + " peers.\n");

    }

}