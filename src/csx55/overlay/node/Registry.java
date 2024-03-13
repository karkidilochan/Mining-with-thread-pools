package csx55.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.overlay.routing.TaskStatistics;
import csx55.overlay.tcp.TCPConnection;
import csx55.overlay.tcp.TCPServer;
import csx55.overlay.wireformats.Event;
// import csx55.overlay.wireformats.LinkWeights;
import csx55.overlay.wireformats.Protocol;
import csx55.overlay.wireformats.PullTrafficSummary;
import csx55.overlay.wireformats.Register;
import csx55.overlay.wireformats.RegisterResponse;
import csx55.overlay.wireformats.TaskInitiate;
import csx55.overlay.wireformats.TrafficSummary;

/**
 * The Registry class maintains information about messaging nodes and handles
 * various functionalities:
 * - Registering/Deregistering messaging nodes
 * - Constructing overlay by relaying routing messages between nodes
 * - Assigning weights to the links between nodes
 */
public class Registry implements Node {
    // Constants representing different commands
    private static final String LIST_MESSAGING_NODES = "list-messaging-nodes";
    private static final String SETUP_OVERLAY = "setup-overlay";
    private static final String START = "start";

    private int threadPoolSize;

    private Map<String, TCPConnection> connections = new HashMap<>();

    // private LinkWeights linkWeights = null;

    private AtomicInteger completedTasks = new AtomicInteger(0);

    private List<TrafficSummary> trafficSummary = new ArrayList<>();
    // private Map<String, TrafficSummary> trafficSummary = new
    // ConcurrentHashMap<>();

    /**
     * The main method of the Registry application.
     * It initializes the Registry, starts a TCP server, and provides user
     * interaction through commands.
     *
     * @param args Command-line arguments. Expects the port number as the first
     *             argument.
     */
    public static void main(String[] args) {
        // Check if the port number is provided as a command-line argument
        if (args.length < 1) {
            System.out.println("Error starting the Registry. Usage: java csx55.overlay.node.Registry portnum");
        }

        Registry registry = new Registry();

        /*
         * defining serverSocket in try-with-resources statement ensures
         * that the serverSocket is closed after the block ends
         */
        try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(args[0]))) {
            /*
             * start the server thread after initializing the server socket
             * invoke start function to start a new thread execution(invoking run() is not
             * the right way)
             */
            (new Thread(new TCPServer(registry, serverSocket))).start();

            // Take commands from console
            registry.takeCommands();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /* Takes user commands from console */
    private void takeCommands() {
        System.out.println("This is the Registry command console. Please enter a valid command to start the overlay.");

        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                String line = scan.nextLine().toLowerCase();
                String[] input = line.split("\\s+");
                switch (input[0]) {
                    case LIST_MESSAGING_NODES:
                        listMessagingNodes();
                        break;

                    case SETUP_OVERLAY:
                        setupOverlay(input);
                        break;

                    case START:
                        start(input[1]);
                        break;

                    default:
                        System.out.println("Please enter a valid command! Options are:\n" +
                                " - list-messaging-nodes\n" +
                                " - setup-overlay thread-pool-size\n" +
                                " - start number-of-rounds");
                        break;
                }
            }
        }
    }

    /**
     * Handles incoming events received from TCP connections.
     *
     * @param event      The event to handle.
     * @param connection The TCP connection associated with the event.
     */
    public void handleIncomingEvent(Event event, TCPConnection connection) {
        switch (event.getType()) {
            case Protocol.REGISTER_REQUEST:
                handleRegistrationEvent((Register) event, connection, true);
                break;

            case Protocol.DEREGISTER_REQUEST:
                handleRegistrationEvent((Register) event, connection, false);
                break;

            case Protocol.TRAFFIC_SUMMARY:
                handleTaskSummary((TrafficSummary) event);
                break;
        }
    }

    /**
     * Handles registration or deregistration events received from messaging nodes.
     * Synchronized to make sure this method runs in a single thread at a given
     * time.
     * 
     * @param registerEvent The registration event to handle.
     * @param connection    The TCP connection associated with the event.
     * @param register      A boolean indicating whether it's a registration or
     *                      deregistration request.
     */
    private synchronized void handleRegistrationEvent(Register registerEvent, TCPConnection connection,
            boolean register) {
        // typecast event object to Register
        String nodes = registerEvent.getConnectionReadable();
        String ipAddress = connection.getSocket().getInetAddress().getHostName().split("\\.")[0];

        String message = checkRegistrationStatus(nodes, ipAddress, register);
        byte status;
        if (message.length() == 0) {
            if (register) {
                connections.put(nodes, connection);
            } else {
                connections.remove(nodes);
                System.out.println("Deregistered " + nodes + ". There are now ("
                        + connections.size() + ") connections.\n");
            }
            message = "Registration request successful.  The number of messaging nodes currently "
                    + "constituting the overlay is (" + connections.size() + ").\n";
            status = Protocol.SUCCESS;
        } else {
            System.out.println("Unable to process request. Responding with a failure.");
            status = Protocol.FAILURE;
        }
        RegisterResponse response = new RegisterResponse(status, message);
        try {
            connection.getTCPSenderThread().sendData(response.getBytes());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            connections.remove(nodes);
            e.printStackTrace();
        }
    }

    /**
     * Generates a status message based on the registration details.
     *
     * @param nodeDetails  Details of the node (e.g., IP address and port).
     * @param connectionIP IP address extracted from the TCP connection.
     * @param register     A boolean indicating whether it's a registration or
     *                     deregistration request.
     */
    private String checkRegistrationStatus(String nodeDetails, String connectionIP,
            final boolean register) {

        String message = "";
        if (connections.containsKey(nodeDetails) && register) {
            message = "The node, " + nodeDetails + " had previously registered and has "
                    + "a valid entry in its registry. ";
        } else if (!connections.containsKey(nodeDetails) && !register) { // The case that the item is not in the
                                                                         // registry.
            message = "The node, " + nodeDetails + " had not previously been registered. ";
        }
        if (!nodeDetails.split(":")[0].equals(connectionIP)
                && !connectionIP.equals("localhost")) {
            message += "There is a mismatch in the address that is specified in request and "
                    + "the IP of the socket.";
        }

        System.out.println("Connected Node: " + nodeDetails);

        return message;
    }

    /**
     * Sets up the network overlay based on the provided input.
     *
     * @param edgesCount A string containing number of connecting edges for setting
     *                   up
     *                   the overlay.
     */
    private void setupOverlay(String[] input) {
        /* start thread pool of size given in input */
        try {
            this.threadPoolSize = Integer.parseInt(input[1]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid input: Enter valid integer for size of the thread pool.");
            e.printStackTrace();
        }
        /* TODO: need a check to see if overlay already exists */

        /*
         * create overlay with the existing connections and send the size of thread pool
         * to be created
         */
        try {
            (new Overlay()).setupOverlay(connections, threadPoolSize);
        } catch (Exception e) {
            // Handle exceptions during overlay setup
            System.out.println("Overlay setup failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        // Notify about successful overlay configuration
        System.out.println(
                "Network overlay successfully configured and sent to (" + connections.size()
                        + ") connections.");
    }

    /**
     * Initiates message sending among overlay nodes for the specified number of
     * rounds.
     *
     * @param maxRounds String containing the max number of rounds each node sends
     *                  payload messages.
     */
    private void start(String maxRounds) {

        // Check if the overlay has been configured and link weights have been sent
        if (threadPoolSize == 0) {
            System.out.println("Unable to start sending messages: Overlay configuration missing.");
            return;
        }

        // set default number of rounds
        int rounds;
        try {
            rounds = Integer.parseInt(maxRounds);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid input: Provide a valid integer as number of rounds.");
            e.printStackTrace();
            return;
        }

        // Create and send task initiation message to all connections
        TaskInitiate startTask = new TaskInitiate(rounds);
        connections.forEach((key, value) -> {
            try {
                value.getTCPSenderThread().sendData(startTask.getBytes());
            } catch (IOException | InterruptedException e) {
                System.out.println(
                        "Error occurred while sending task initiation message to connection: " + e.getMessage());
                e.printStackTrace();

            }
        });
        System.out.println("\n Tasks will begin soon...");
    }

    /**
     * Handles the completion of tasks by incrementing the completed task count and
     * initiating traffic summary retrieval
     * when all tasks have been completed.
     */
    private synchronized void handleTaskSummary(TrafficSummary summary) {
        System.out.println("Received task summary " + summary.toString());
        completedTasks.getAndIncrement();
        trafficSummary.add(summary);

        if (completedTasks.get() == connections.size()) {
            try {
                // Sleep for 15 seconds to allow all messages to be received.
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Thread sleep interrupted: " + e.getMessage());
                e.printStackTrace();
            }



            if (trafficSummary.size() == connections.size()) {
                display(trafficSummary);
//                trafficSummary.clear();
            }

            // Finally, reset the completed task count
//            completedTasks.set(0);
        }
    }

    /**
     * Lists the messaging nodes registered in the overlay.
     * Displays a message if no connections are present.
     */
    private void listMessagingNodes() {
        if (connections.size() == 0) {
            System.out.println(
                    "No connections in the registry.");
        } else {
            System.out.println("\nThere are " + connections.size() + " total links:\n");
            connections.forEach((key, value) -> System.out.println("\t" + key));
        }
    }

    public void display(List<TrafficSummary> statisticsSummary) {
        if (statisticsSummary.size() == 0) {
            System.out.println("No message statistics available.");
            return;
        }
        long totalGenerated = 0;
        long totalPulled = 0;
        long totalPushed = 0;
        long totalCompleted = 0;

        System.out.println(
                String.format( "\n%1$20s %2$12s %3$10s %4$15s %5$15s %6$10s",
                        "",
                        "Generated tasks",
                        "Pulled tasks",
                        "Pushed tasks",
                        "Completed tasks",
                        "% of tasks performed"));

        for (TrafficSummary summary : statisticsSummary) {
//            System.out.println(summary.toString());
            totalGenerated += summary.getGenerated();
            totalPulled += summary.getPulled();
            totalPushed += summary.getPushed();
            totalCompleted += summary.getCompleted();
        }

        for (TrafficSummary summary : statisticsSummary) {
            float percentCompleted = (summary.getCompleted() / totalCompleted) * 100;
            summary.percentCompleted = percentCompleted;
            String result = summary.toString() + percentCompleted;
            System.out.println(result);
        }

//        System.out.println(String.format("%1$20s %2$40s %3$20s %4$15s %5$15s\n",
//                "Sum:", Long.toString(totalGenerated),
//                Long.toString(totalPulled), Long.toString(totalPushed),
//                Long.toString(totalCompleted), (totalGenerated / totalPerformed) * 100));

        System.out.println(String.format("%1$20s %2$10s %3$10s %4$10s %5$10s %6$10.2f%%\n",
                "Sum:", Long.toString(totalGenerated),
                Long.toString(totalPulled), Long.toString(totalPushed),
                Long.toString(totalCompleted), ((double) totalCompleted / totalGenerated) * 100));
    }
}
