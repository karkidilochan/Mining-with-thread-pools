package csx55.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import csx55.overlay.routing.TaskStatistics;
import csx55.overlay.task.Miner;
import csx55.overlay.task.Task;
import csx55.overlay.tcp.TCPConnection;
import csx55.overlay.tcp.TCPServer;
import csx55.overlay.threads.ThreadPool;
import csx55.overlay.wireformats.Event;
import csx55.overlay.wireformats.MessagingNodesList;
import csx55.overlay.wireformats.Protocol;
import csx55.overlay.wireformats.Register;
import csx55.overlay.wireformats.RegisterResponse;
import csx55.overlay.wireformats.TaskInitiate;
import csx55.overlay.wireformats.TrafficSummary;

/**
 * Implementation of the Node interface, represents a messaging node in the
 * network overlay system.
 * Messaging nodes facilitate communication between other nodes in the overlay.
 * This class handles registration with a registry, establishment of
 * connections,
 * message routing, and messageStatistics tracking.
 */
public class MessagingNode implements Node, Protocol {

    /*
     * port to listen for incoming connections, configured during messaging node
     * creation
     */
    private final Integer nodePort;
    private final String nodeHost;

    private ThreadPool threadPool;

    // Constants for command strings

    // create a TCP connection with the Registry
    private TCPConnection registryConnection;

    /*
     * data structure to store connections to other messaging nodes received from
     * Registry
     */
    private Map<String, TCPConnection> connections = new ConcurrentHashMap<>();

    private TaskStatistics messageStatistics = new TaskStatistics();

    /**
     * Constructs a new messaging node with the specified host and port.
     *
     * @param nodeHost The host name of the messaging node.
     * @param nodePort The port on which the messaging node listens for incoming
     *                 connections.
     */
    private MessagingNode(String nodeHost, int nodePort) {
        this.nodeHost = nodeHost;
        this.nodePort = nodePort;
    }

    /**
     * Main method to start the messaging node.
     * Reads registry host and port from command line arguments,
     * starts a TCP server thread, and registers the node with the registry.
     *
     * @param args Command line arguments - registry host and port.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsageAndExit();
        }
        System.out.println("Messaging node live at: " + new Date());
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            // assign a random available port
            int nodePort = serverSocket.getLocalPort();

            /*
             * get local host name and use assigned nodePort to initialize a messaging node
             */
            MessagingNode node = new MessagingNode(
                    InetAddress.getLocalHost().getHostName(), nodePort);

            /* start a new TCP server thread */
            (new Thread(new TCPServer(node, serverSocket))).start();

            // register this node with the registry
            node.registerNode(args[0], Integer.valueOf(args[1]));

            // facilitate user input in the console
            node.takeCommands();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Print the correct usage of the program and exits with a non-zero status
     * code.
     */
    private static void printUsageAndExit() {
        System.err.println("Usage: java csx55.overlay.node.MessagingNode registry-host registry-port");
        System.exit(1);
    }

    /**
     * Registers this node with the registry.
     *
     * @param registryHost The host name of the registry.
     * @param registryPort The port on which the registry listens for connections.
     */
    private void registerNode(String registryHost, Integer registryPort) {
        try {
            // create a socket to the Registry server
            Socket socketToRegistry = new Socket(registryHost, registryPort);
            TCPConnection connection = new TCPConnection(this, socketToRegistry);

            Register register = new Register(Protocol.REGISTER_REQUEST,
                    this.nodeHost, this.nodePort);

            System.out.println(
                    "Address of the Messaging Node: " + this.nodeHost + ":" + this.nodePort);

            // send "Register" message to the Registry
            connection.getTCPSenderThread().sendData(register.getBytes());
            connection.start();

            // Set the registry connection for this node
            this.registryConnection = connection;
        } catch (IOException | InterruptedException e) {
            System.out.println("Error registering node: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle user interaction with the messaging node.
     * Allows users to input commands for interacting with processes.
     * Commands include print-shortest-path and exit-overlay.
     */
    private void takeCommands() {
        System.out.println(
                "Enter a command to interact with the messaging node. Available commands: print-shortest-path, exit-overlay\n");
        boolean stop = false;
        try (Scanner scan = new Scanner(System.in)) {
            while (!stop) {
                String command = scan.nextLine().toLowerCase();
                switch (command) {

                    default:
                        System.out.println("Invalid Command. Available commands: print-shortest-path, exit-overlay\\n");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred during command processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Deregistering the messaging node and terminating: " + nodeHost + ":" + nodePort);
            // exitOverlay();
            System.exit(0);
        }
    }

    /**
     * Handles incoming messages from the TCP connection.
     *
     * @param event      The event to handle.
     * @param connection The TCP connection associated with the event.
     */
    public void handleIncomingEvent(Event event, TCPConnection connection) {
        System.out.println("Received event: " + event.toString());

        switch (event.getType()) {
            case Protocol.REGISTER_REQUEST:
                recordNewConnection((Register) event, connection);
                break;

            case Protocol.REGISTER_RESPONSE:
                handleRegisterResponse((RegisterResponse) event);
                break;

            case Protocol.MESSAGING_NODES_LIST:
                createOverlayConnections((MessagingNodesList) event);
                break;

            case Protocol.TASK_INITIATE:
                handleTaskInitiation((TaskInitiate) event);
                break;

        }
    }

    /**
     * Handles a REGISTER_RESPONSE event.
     *
     * @param response The REGISTER_RESPONSE event to handle.
     */
    private void handleRegisterResponse(RegisterResponse response) {
        System.out.println("Received registration response from the registry: " + response.toString());
    }

    /**
     * Creates overlay connections with the messaging nodes listed in the
     * provided MessagingNodeList.
     *
     * @param nodeList The MessagingNodesList containing information about the
     *                 peers.
     */
    private void createOverlayConnections(MessagingNodesList messagingNodeList) {
        List<String> peers = messagingNodeList.getPeersList();
        int threadPoolSize = messagingNodeList.getThreadPoolSize();

        for (String peer : peers) {
            String[] peerInfo = peer.split(":");
            try {
                int peerPort = Integer.parseInt(peerInfo[1]);
                Socket socketToMessagingNode = new Socket(peerInfo[0], peerPort);
                TCPConnection connection = new TCPConnection(this, socketToMessagingNode);
                Register register = new Register(Protocol.REGISTER_REQUEST, this.nodeHost, this.nodePort);
                connection.getTCPSenderThread().sendData(register.getBytes());
                connection.start();

                // also recording outgoing connections
                connections.put(peer, connection);

                /* then, create the thread pool of given size */
                this.threadPool = new ThreadPool(threadPoolSize, this);

            } catch (NumberFormatException | IOException | InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        int numberOfPeers = messagingNodeList.getNumberOfPeers();
        System.out.println("Established connections with " + Integer.toString(numberOfPeers) + " peers.\n");

    }

    /**
     * Records a new connection established with another node.
     *
     * @param register   The Register message containing information about the new
     *                   connection.
     * @param connection The TCP connection established with the new node.
     */
    private void recordNewConnection(Register register, TCPConnection connection) {
        String nodeDetails = register.getConnectionReadable();

        // Store the connection in the connections map
        connections.put(nodeDetails, connection);
    }

    private void handleTaskInitiation(TaskInitiate taskInitiate) {
        int rounds = taskInitiate.getNumberOfRounds();

        Random random = new Random();

        /*
         * at each round, node completes a random number of tasks between 1-1000
         */
        try {
            for (int round = 1; round < rounds + 1; round++) {
                // TODO:
                // int noOfTasks = random.nextInt(1000) + 1;
                int noOfTasks = 5;
                for (int i = 1; i < noOfTasks + 1; i++) {
                    Task task = new Task(InetAddress.getLocalHost().getHostAddress(), nodePort, i,
                            new Random().nextInt());
                    threadPool.addTask(task);

                    messageStatistics.addGenerated();
                }

            }
            this.threadPool.start();

            waitForTasksToComplete();

        } catch (IOException e) {
            System.out.println("Error occurred while adding tasks to queue: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void waitForTasksToComplete() {
        while (true) {
            long tasksTotal = messageStatistics.getGenerated() - messageStatistics.getPushed() + messageStatistics.getPulled();
            if (tasksTotal == messageStatistics.getCompleted()) {
                System.out.println("Completed count:" + messageStatistics.getCompleted());
                System.out.println("Generated count:" + messageStatistics.getGenerated());

                return;
            }
        }
    }

    /**
     * Sends a traffic summary including messaging messageStatistics in response to
     * a request from the registry. Also resets all associated counters.
     */

//     private void sendTrafficSummary() {
//     TrafficSummary trafficSummary = new TrafficSummary(nodeHost, nodePort,
//     messageStatistics);
//
//     try {
//     registryConnection.getTCPSenderThread().sendData(trafficSummary.getBytes());
//     } catch (IOException | InterruptedException e) {
//     System.out.println("Error occurred while sending traffic summary response: "
//     + e.getMessage());
//     e.printStackTrace();
//     }
//     // Reset all messaging messageStatistics counters
//     messageStatistics.reset();
//     }

    public TaskStatistics getNodeStatistics() {
        return messageStatistics;
    }

}
