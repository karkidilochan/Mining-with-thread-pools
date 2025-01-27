package distributed.overlay.node;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

import distributed.overlay.routing.TaskStatistics;
import distributed.overlay.task.Task;
import distributed.overlay.tcp.TCPConnection;
import distributed.overlay.tcp.TCPServer;
import distributed.overlay.threads.ThreadPool;
import distributed.overlay.wireformats.Event;
import distributed.overlay.wireformats.MigrateResponse;
import distributed.overlay.wireformats.MigrateTasks;
import distributed.overlay.wireformats.CheckStatus;
import distributed.overlay.wireformats.ComputeNodesList;
import distributed.overlay.wireformats.Protocol;
import distributed.overlay.wireformats.PushRequest;
import distributed.overlay.wireformats.Register;
import distributed.overlay.wireformats.RegisterResponse;
import distributed.overlay.wireformats.TaskComplete;
import distributed.overlay.wireformats.TaskInitiate;
import distributed.overlay.wireformats.TasksCount;
import distributed.overlay.wireformats.TrafficSummary;

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
    // private Map<String, TCPConnection> outgoingConnection = new
    // ConcurrentHashMap<>();

    private TCPConnection outgoingConnection;
    private String outgoingConnectionHost;

    private TCPConnection incomingConnection;
    private String incomingConnectionHost;

    // private Map<String, TCPConnection> incomingConnection = new
    // ConcurrentHashMap<>();

    private TaskStatistics messageStatistics = new TaskStatistics();

    private Map<String, Integer> overlayTasksCount = new ConcurrentHashMap<>();

    private int overlaySize;

    private List<Task> generatedTasks;
    private List<Task> migratedTasks;

    private int balancedCount;

    private AtomicBoolean READY_TO_EXECUTE = new AtomicBoolean(false);

    private final int RANDOM_MAX = 1000;

    private CountDownLatch roundCompleteLatch;

    private volatile boolean isMigratingTasks = false;

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
        System.err.println("Usage: java distributed.overlay.node.MessagingNode registry-host registry-port");
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

            case Protocol.PUSH_REQUEST:
                handlePushRequest((PushRequest) event, connection);
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

            case Protocol.PULL_TRAFFIC_SUMMARY:
                sendTrafficSummary();
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
                this.outgoingConnection = connection;
                this.outgoingConnectionHost = peer;

                /* then, create the thread pool of given size */
                this.threadPool = new ThreadPool(threadPoolSize, this);
                this.threadPool.start();

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
        this.incomingConnection = connection;
        this.incomingConnectionHost = nodeDetails;
    }

    private void handleTaskInitiation(TaskInitiate taskInitiate) {
        int round = taskInitiate.getNumberOfRounds();

        Random random = new Random();

        /*
         * at each round, node completes a random number of tasks between 1-1000
         */
        try {
            this.roundCompleteLatch = new CountDownLatch(1);

            this.generatedTasks = new ArrayList<>();
            this.migratedTasks = new ArrayList<>();

            int noOfTasks = random.nextInt(RANDOM_MAX) + 1;
            for (int i = 1; i < noOfTasks + 1; i++) {
                Task task = new Task(InetAddress.getLocalHost().getHostAddress(), nodePort, round,
                        new Random().nextInt());
                generatedTasks.add(task);

            }
            messageStatistics.addGenerated(noOfTasks);

            /* now, first send no of tasks generated around the ring */
            sendTasksCount(noOfTasks);

            /* then, wait for all tasks count and perform pair wise load balancing */
            calculateMean();
            balanceLoad();

            /*
             * finally, start thread pool and wait for taskS to complete
             */
            startThreadPool();

            waitForTasksToComplete();
            /*
             * send traffic summary to registry after tasks after all rounds are completed
             */
            sendTaskCompleteMessage();

        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while adding tasks to queue: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void calculateMean() {

        while (true) {
            if (overlayTasksCount.size() == overlaySize - 1) {
                break;
            }
        }
        int totalTasks = generatedTasks.size();
        for (int count : overlayTasksCount.values()) {
            totalTasks += count;
        }

        /* estimate fair share of work */
        this.balancedCount = (int) Math.ceil(totalTasks / overlaySize);
    }

    private boolean checkIfBalanced() {
        // int totalCount = generatedTasks.size() + migratedTasks.size();

        // int balanceDifference = Math.abs(totalCount - this.balancedCount);
        int tolerance = (int) Math.ceil(0.1 * this.balancedCount);
        int countAboveMean = 0;

        for (int value : overlayTasksCount.values()) {
            if (Math.abs(value - this.balancedCount) <= tolerance) {
                countAboveMean++;
            }
        }
        double percentAboveMean = (double) countAboveMean / overlayTasksCount.size() * 100;
        return percentAboveMean >= 70;
    }

    private void balanceLoad() {

        /*
         * first check your load, and compare with mean, if you are less than mean, wait
         * for a few while and check again
         * if you are more than mean, offload multiples of 10 to node with less nodes
         * than you
         * 
         */
        while (!checkIfBalanced()) {

            int totalCount = generatedTasks.size() + migratedTasks.size();

            /*
             * if heavily loaded send to your neighbor
             * else send a pull request
             * migration happens in batches of 10
             */
            int outgoingHostCount = overlayTasksCount.get(outgoingConnectionHost);
            int incomingHostsCount = overlayTasksCount.get(incomingConnectionHost);

            try {
                if (totalCount > this.balancedCount) {
                    /* means node is high loaded */
                    PushRequest message = new PushRequest(totalCount);

                    if (outgoingHostCount <= this.balancedCount) {
                        /* send a push request */
                        outgoingConnection.getTCPSenderThread().sendData(message.getBytes());
                    }

                    if (incomingHostsCount <= this.balancedCount) {
                        /* send a push request */
                        incomingConnection.getTCPSenderThread().sendData(message.getBytes());
                    }
                } else {
                    CheckStatus message = new CheckStatus(Math.abs(generatedTasks.size() - this.balancedCount));

                    /* means node is less loaded */
                    if (outgoingHostCount >= this.balancedCount) {
                        /* send a migration request */
                        outgoingConnection.getTCPSenderThread().sendData(message.getBytes());
                    }

                    if (incomingHostsCount >= this.balancedCount) {
                        /* send a migration request */
                        incomingConnection.getTCPSenderThread().sendData(message.getBytes());
                    }

                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Error occurred while sending status check: " +
                        e.getMessage());
                e.printStackTrace();
            }

            // Wait for a while before checking again
            try {
                TimeUnit.MILLISECONDS.sleep(10); // Adjust the sleep time as needed (e.g., 1000 milliseconds = 1 second)
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting: " + e.getMessage());
            }

        }
    }

    private void waitForTasksToComplete() throws InterruptedException {
        /* check semaphore that is adjusted by the threadpool */

        roundCompleteLatch.await();

        messageStatistics.addCompleted(generatedTasks.size() + migratedTasks.size());
    }

    public void sendTasksCount(int numberOfTasks) {
        /* nodeDetail = ipAddress:port */

        try {
            TasksCount countMessage = new TasksCount(getSelfReadable(), numberOfTasks);
            outgoingConnection.getTCPSenderThread().sendData(countMessage.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending generated tasks count: " + e.getMessage());
            e.printStackTrace();
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

        overlayTasksCount.put(nodeDetails, message.getCount());

        try {
            outgoingConnection.getTCPSenderThread().sendData(message.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending generated tasks count: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void handleStatusCheck(CheckStatus message, TCPConnection connection) {
        if (isMigratingTasks) {
            return;
        }

        this.isMigratingTasks = true;
        migrateTasks(10, connection);

    }

    private synchronized void handlePushRequest(PushRequest message, TCPConnection connection) {
        /* send back status message */

        CheckStatus statusMessage = new CheckStatus(Math.abs(generatedTasks.size() - this.balancedCount));
        try {
            connection.getTCPSenderThread().sendData(statusMessage.getBytes());

        } catch (Exception e) {
            System.out.println("Error occured while handling push request:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void handleTasksMigration(MigrateTasks event, TCPConnection connection) {
        /*
         * first add tasks to the migrated tasks array
         * send a migration response
         * and then start thread
         */

        try {
            // migratedTasks.addAll(event.getTasks());
            generatedTasks.addAll(event.getTasks());
            MigrateResponse message = new MigrateResponse();
            connection.getTCPSenderThread().sendData(message.getBytes());
            this.messageStatistics.addPulled(event.getTasksSize());

            this.isMigratingTasks = false;

            // System.out.println("Received tasks from " + connection.getSocket().toString()
            // + " " + event.getTasksSize());

            /* now update your peers about your new count */
            sendTasksCount(generatedTasks.size() + migratedTasks.size());

        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending migration response: " +
                    e.getMessage());
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
        this.isMigratingTasks = false;
    }

    public TaskStatistics getNodeStatistics() {
        return messageStatistics;
    }

    private String getSelfReadable() {
        return this.nodeHost + ":" + this.nodePort;
    }

    private synchronized void migrateTasks(int targetCount, TCPConnection connection) {
        List<Task> extractedTasks = new ArrayList<>(generatedTasks.subList(0, targetCount));
        try {
            MigrateTasks message = new MigrateTasks(extractedTasks);
            connection.getTCPSenderThread().sendData(message.getBytes());
            generatedTasks.subList(0, targetCount).clear();
            this.messageStatistics.addPushed(targetCount);
            // System.out.println("Pushed tasks" + connection.getSocket().toString() + " " +
            // targetCount);
            sendTasksCount(generatedTasks.size() + migratedTasks.size());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while migrating tasks: " + e.getMessage());
            e.printStackTrace();
            generatedTasks.addAll(0, extractedTasks);
        }
    }

    private synchronized void startThreadPool() {

        threadPool.addTasks(generatedTasks);
        // threadPool.addTasks(migratedTasks);
        // System.out.println(messageStatistics.getGenerated());

    }

    public CountDownLatch getRoundsLatch() {
        return this.roundCompleteLatch;
    }

    public void sendTaskCompleteMessage() {

        TaskComplete taskCompleteMessage = new TaskComplete(nodeHost, nodePort);
        try {
            registryConnection.getTCPSenderThread().sendData(taskCompleteMessage.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println(
                    "Couldn't inform registry of task completion" + e.getMessage());
            e.printStackTrace();
        }

        this.overlayTasksCount = new ConcurrentHashMap<>();
    }

    private void sendTrafficSummary() {
        TrafficSummary trafficSummary = new TrafficSummary(nodeHost, nodePort, messageStatistics);

        try {
            registryConnection.getTCPSenderThread().sendData(trafficSummary.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while sending traffic summary response: " + e.getMessage());
            e.printStackTrace();
        }
        // Reset all messaging messageStatistics counters
        messageStatistics.reset();
    }

}
