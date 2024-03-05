package csx55.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import csx55.overlay.routing.TaskStatistics;
import csx55.overlay.task.Miner;
import csx55.overlay.task.Task;
import csx55.overlay.tcp.TCPConnection;
import csx55.overlay.tcp.TCPServer;
import csx55.overlay.threads.ThreadPool;
import csx55.overlay.wireformats.Event;
import csx55.overlay.wireformats.MigrateResponse;
import csx55.overlay.wireformats.MigrateTasks;
import csx55.overlay.wireformats.CheckStatus;
import csx55.overlay.wireformats.ComputeNodesList;
import csx55.overlay.wireformats.Protocol;
import csx55.overlay.wireformats.PullRequest;
import csx55.overlay.wireformats.ReadyToExecute;
import csx55.overlay.wireformats.Register;
import csx55.overlay.wireformats.RegisterResponse;
import csx55.overlay.wireformats.TaskComplete;
import csx55.overlay.wireformats.TaskInitiate;
import csx55.overlay.wireformats.TasksCount;
import csx55.overlay.wireformats.TrafficSummary;

/**
 * Implementation of the Node interface, represents a messaging node in the
 * network overlay system.
 * Messaging nodes facilitate communication between other nodes in the overlay.
 * This class handles registration with a registry, establishment of
 * connections,
 * message routing, and messageStatistics tracking.
 */
public class ComputeNode implements Node, Protocol {

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

    private Map<String, Integer> overlayTasksCount = new ConcurrentHashMap<>();

    private int overlaySize;

    private List<Task> generatedTasks = new ArrayList<>();
    private List<Task> migratedTasks = new ArrayList<>();

    private int balancedCount;

    private final int BATCH_SIZE = 10;

    private AtomicBoolean READY_TO_EXECUTE = new AtomicBoolean(false);

    private final int RANDOM_MAX = 1000;

    private AtomicBoolean ROUND_COMPLETED = new AtomicBoolean(false);

    // private final CountDownLatch latch;

    /**
     * Constructs a new messaging node with the specified host and port.
     *
     * @param nodeHost The host name of the messaging node.
     * @param nodePort The port on which the messaging node listens for incoming
     *                 connections.
     */
    private ComputeNode(String nodeHost, int nodePort) {
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
            ComputeNode node = new ComputeNode(
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
                createOverlayConnections((ComputeNodesList) event);
                break;

            case Protocol.TASK_INITIATE:
                handleTaskInitiation((TaskInitiate) event);
                break;

            case Protocol.TASKS_COUNT:
                relayTasksCount((TasksCount) event);
                break;

            case Protocol.CHECK_STATUS:
                handleStatusCheck((CheckStatus) event, connection);
                break;

            case Protocol.PULL_REQUEST:
                handlePullRequest((PullRequest) event, connection);
                break;

            case Protocol.MIGRATE_TASKS:
                handleTasksMigration((MigrateTasks) event, connection);
                break;

            case Protocol.MIGRATE_RESPONSE:
                handleMigrateResponse();
                break;

            case Protocol.STATUS_RESPONSE:
                handleStatusResponse();
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
    private void createOverlayConnections(ComputeNodesList messagingNodeList) {
        List<String> peers = messagingNodeList.getPeersList();
        int threadPoolSize = messagingNodeList.getThreadPoolSize();
        overlaySize = messagingNodeList.getOverlaySize();

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
        System.out.println("Established connections with " + Integer.toString(numberOfPeers) + " peers.\n" + peers);

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
        // connections.put(nodeDetails, connection);
    }

    private void handleTaskInitiation(TaskInitiate taskInitiate) {
        int rounds = taskInitiate.getNumberOfRounds();

        Random random = new Random();

        /*
         * at each round, node completes a random number of tasks between 1-1000
         */
        try {
            for (int round = 1; round < rounds + 1; round++) {
                this.ROUND_COMPLETED.set(false);
                int noOfTasks = random.nextInt(RANDOM_MAX) + 1;
                for (int i = 1; i < noOfTasks + 1; i++) {
                    Task task = new Task(InetAddress.getLocalHost().getHostAddress(), nodePort, i,
                            new Random().nextInt());
                    // threadPool.addTask(task);
                    generatedTasks.add(task);

                }
                messageStatistics.addGenerated(noOfTasks);

                /* now, first send no of tasks generated around the ring */
                sendTasksCount(noOfTasks);
                System.out.println("Sent tasks count to peers");

                /* then, wait for all tasks count and perform pair wise load balancing */
                calculateMean();
                balanceLoad();

                /*
                 * finally, start thread pool and wait for taskS to complete
                 */
                // TODO: add a latch/semaphore to indicate if ready for execution
                waitForTasksToComplete();
            }

            /*
             * send traffic summary to registry after tasks after all rounds are completed
             */

        } catch (IOException e) {
            System.out.println("Error occurred while adding tasks to queue: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void calculateMean() {
        System.out.println("Waiting to receive count of tasks from all nodes...");

        while (true) {
            if (overlayTasksCount.size() == overlaySize - 1) {
                break;
            }
        }

        int totalTasks = 0;
        for (int count : overlayTasksCount.values()) {
            totalTasks += count;
        }

        /* estimate fair share of work */
        this.balancedCount = (int) Math.ceil(totalTasks / overlaySize);
    }

    private void balanceLoad() {

        int totalCount = generatedTasks.size() + migratedTasks.size();

        int balanceDifference = Math.abs(totalCount - this.balancedCount);
        System.out.println(overlayTasksCount);
        System.out.println("Gen count vs mean difference" + generatedTasks.size() + " " + balanceDifference);
        int tolerance = (int) 0.1 * this.balancedCount;
        int lowerBound = balancedCount - tolerance;
        int upperBound = balancedCount + tolerance;
        /* if difference is less than batch size no load migration needed */
        if (balanceDifference >= lowerBound && balanceDifference <= upperBound) {
            /* push tasks to thread queue and start */
            // TODO
            startThreadPool();
        } else {
            for (Map.Entry<String, TCPConnection> entry : connections.entrySet()) {
                TCPConnection neighborConnection = entry.getValue();
                /*
                 * send a request to migrate to peers
                 * check response to see their current total tasks count, and if they are ready
                 * to start
                 * if they are ready to start,
                 */
                try {
                    CheckStatus message = new CheckStatus(totalCount);
                    neighborConnection.getTCPSenderThread().sendData(message.getBytes());
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error occurred while sending status check: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        }

    }

    private void waitForTasksToComplete() {
        /* check semaphore that is adjusted by the threadpool */
        while (true) {
            if (this.ROUND_COMPLETED.get()) {
                break;
            }
        }
    }

    /**
     * Sends a traffic summary including messaging messageStatistics in response to
     * a request from the registry. Also resets all associated counters.
     */

    // private void sendTrafficSummary() {
    // TrafficSummary trafficSummary = new TrafficSummary(nodeHost, nodePort,
    // messageStatistics);
    //
    // try {
    // registryConnection.getTCPSenderThread().sendData(trafficSummary.getBytes());
    // } catch (IOException | InterruptedException e) {
    // System.out.println("Error occurred while sending traffic summary response: "
    // + e.getMessage());
    // e.printStackTrace();
    // }
    // // Reset all messaging messageStatistics counters
    // messageStatistics.reset();
    // }

    public void sendTasksCount(int numberOfTasks) {
        /* nodeDetail = ipAddress:port */
        for (Map.Entry<String, TCPConnection> entry : connections.entrySet()) {
            TCPConnection neighborConnection = entry.getValue();

            try {
                TasksCount countMessage = new TasksCount(getSelfReadable(), numberOfTasks);
                neighborConnection.getTCPSenderThread().sendData(countMessage.getBytes());
            } catch (IOException | InterruptedException e) {
                System.out.println("Error occurred while sending generated tasks count: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void relayTasksCount(TasksCount message) {
        String nodeDetails = message.getHost();
        /* if own message, stop */
        if (nodeDetails.equals(getSelfReadable())) {
            return;
        }

        /*
         * if other nodes message, store the count if not already in the tasks count
         * hashmap, then relay forward
         */

        if (!overlayTasksCount.containsKey(nodeDetails)) {
            overlayTasksCount.putIfAbsent(nodeDetails, message.getCount());
        }
        for (Map.Entry<String, TCPConnection> entry : connections.entrySet()) {
            TCPConnection neighborConnection = entry.getValue();

            try {
                neighborConnection.getTCPSenderThread().sendData(message.getBytes());
            } catch (IOException | InterruptedException e) {
                System.out.println("Error occurred while sending generated tasks count: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // private void balanceLoad() {

    /*
     * To balance load:
     * first find the mean, then the amount the node needs to reach the mean
     * then check if the neighbor has already reach mean(
     * send balance request to B, B sends sum of generated and migrated count back,
     * )
     * , if not, check if it has more or less than its count
     * use balance request and response
     * push or pull the difference in batches of 10 to reach the mean
     * send back to the node the difference
     * 
     * 
     */

    /*
     * Compare own workload with neighbors and perform load balancing if needed
     * for now, dont care about other node, for current node try to reach the mean
     * Implement logic for load balancing operations (e.g., pulling or pushing
     * tasks)
     * Load balancing should be performed in small batches
     * Prevent oscillatory behavior by avoiding further migration of tasks that have
     * already migrated
     */

    // }

    private synchronized void handleStatusCheck(CheckStatus message, TCPConnection connection) {
        int totalCount = generatedTasks.size() + migratedTasks.size();
        int neighborsCount = message.getCount();
        System.out.println("Self count vs Neighbors Count vs mean: " + totalCount + " " + neighborsCount + " "
                + balancedCount);

        boolean isBalanced = Math.abs(totalCount - neighborsCount) < this.BATCH_SIZE;
        boolean isReady = this.READY_TO_EXECUTE.get();
        if (isReady) {
            try {
                ReadyToExecute response = new ReadyToExecute();
                connection.getTCPSenderThread().sendData(response.getBytes());
            } catch (IOException | InterruptedException e) {
                System.out.println("Error occurred while sending status response: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (isBalanced) {
                this.READY_TO_EXECUTE.set(true);
                startThreadPool();
                try {
                    ReadyToExecute response = new ReadyToExecute();
                    connection.getTCPSenderThread().sendData(response.getBytes());
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error occurred while sending status response: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                int balanceDifference = Math.abs(totalCount - this.balancedCount);
                if (totalCount > neighborsCount) {
                    /* push load in batches equal to balance difference */
                    migrateTasks(balanceDifference, connection);
                } else if (totalCount < neighborsCount) {
                    /* send pull request for balance difference */
                    try {
                        PullRequest response = new PullRequest(balanceDifference);
                        connection.getTCPSenderThread().sendData(response.getBytes());
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Error occurred while sending pull request: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private synchronized void handlePullRequest(PullRequest message, TCPConnection connection) {
        int totalCount = message.getNumberOfTasks();
        migrateTasks(totalCount, connection);
    }

    private synchronized void handleTasksMigration(MigrateTasks event, TCPConnection connection) {
        /*
         * first add tasks to the migrated tasks array
         * send a migration response
         * and then start thread
         */

        try {
            migratedTasks.addAll(event.getTasks());
            MigrateResponse message = new MigrateResponse();
            connection.getTCPSenderThread().sendData(message.getBytes());
            this.messageStatistics.addPulled(event.getTasksSize());
            this.READY_TO_EXECUTE.set(true);
            startThreadPool();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending migration response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void handleStatusResponse() {
        boolean isReady = this.READY_TO_EXECUTE.get();
        if (!isReady) {
            this.READY_TO_EXECUTE.set(true);
            startThreadPool();
        }
    }

    private synchronized void handleMigrateResponse() {
        /* start thread pool in the node that sent the tasks */
        boolean isReady = this.READY_TO_EXECUTE.get();
        if (!isReady) {
            this.READY_TO_EXECUTE.set(true);
            startThreadPool();
        }

    }

    public TaskStatistics getNodeStatistics() {
        return messageStatistics;
    }

    private String getSelfReadable() {
        return this.nodeHost + ":" + this.nodePort;
    }

    private synchronized void migrateTasks(int targetCount, TCPConnection connection) {
        // TODO: include batch sizes
        List<Task> extractedTasks = new ArrayList<>(generatedTasks.subList(0, targetCount));
        try {
            MigrateTasks message = new MigrateTasks(extractedTasks);
            connection.getTCPSenderThread().sendData(message.getBytes());
            generatedTasks.subList(0, targetCount).clear();
            this.messageStatistics.addPushed(targetCount);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while migrating tasks: " + e.getMessage());
            e.printStackTrace();
            generatedTasks.addAll(0, extractedTasks);
        }
    }

    private void startThreadPool() {

        threadPool.addTasks(generatedTasks);
        threadPool.addTasks(migratedTasks);

        this.threadPool.start();

        // waitForTasksToComplete();
    }

    public void notifyRoundComplete() {
        this.ROUND_COMPLETED.set(true);
    }

    public boolean getIfRoundComplete() {
        return this.ROUND_COMPLETED.get();
    }

    public void sendTaskCompleteMessage() {
        TrafficSummary trafficSummary = new TrafficSummary(nodeHost, nodePort, messageStatistics);

        try {
            registryConnection.getTCPSenderThread().sendData(trafficSummary.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending task summary response: " + e.getMessage());
            e.printStackTrace();
        }
        // Reset all messaging messageStatistics counters
        messageStatistics.reset();
    }
}
