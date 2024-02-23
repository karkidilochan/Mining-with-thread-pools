package csx55.threads.node;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.tcp.TCPServer;

public class ComputationNode implements Node {
    private final String host;
    private final Integer port;

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

        } catch (Exception e) {
            System.out.println("An error occurred in the computation node: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void registerNode(String registryHost, Integer registryPort) {
        try {
            // create a client socket to registry
            Socket socketToRegistry = new Socket(registryHost, registryPort);

            /*
             * create a connection with this socket and print out the host and port of this
             * node
             */

            TCPConnection connectionToRegistry = new TCPConnection(this, socketToRegistry);
            System.out.println("A Computation Node is live at:" + this.host + ":" + this.port);

            /* send Register message to the registry */

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}