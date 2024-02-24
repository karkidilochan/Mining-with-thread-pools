package csx55.threads.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.tcp.TCPServer;
import csx55.threads.wireformats.Message;
import csx55.threads.wireformats.Protocol;
import csx55.threads.wireformats.Register;

public class Registry implements Node {

    private Map<String, TCPConnection> registryConnections = new HashMap<>();

    private int threadPoolSize;

    private final String SETUP_OVERLAY = "setup-overlay";

    public static void main(String[] args) {
        /* take port for registry from command line and start a server socket */

        if (args.length < 1) {
            System.out.println("Error starting the Registry. Usage: java csx55.threads.node.Registry portnum");
        }

        Registry registry = new Registry();

        /* use the server socket to start a TCPServer thread */
        try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(args[0]));) {
            (new Thread(new TCPServer(registry, serverSocket))).start();

            /* keep the command line live to take user commands */
            registry.takeCommands();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public void takeCommands() {
        System.out.println("This is the Registry command console. Please enter a valid command to start the overlay.");

        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                String line = scan.nextLine().toLowerCase();
                // find whitespaces and convert input string into array of substrings
                String[] input = line.split("\\s+");
                String command = input[0];
                switch (command) {
                    case (SETUP_OVERLAY):
                        setupOverlay(input);
                        break;
                    default:
                        System.out.println("Please enter a valid command! Options are:\n" +
                                " - setup-overlay thread-pool-size\n" +
                                " - start number-of-rounds");
                        break;
                }
            }
        }
    }

    public void handleIncomingMessage(Message message, TCPConnection connection) {
        switch (message.getType()) {
            case Protocol.REGISTER_REQUEST:
                handleRegisterRequest((Register) message, connection, true);
                break;

            default:
                break;
        }

    }

    /*
     * registration should be synchronized since only one registration request
     * should be handled at one time i.e. one thread at a time
     */
    public synchronized void handleRegisterRequest(Register registerMessage, TCPConnection connection,
            boolean register) {
        String nodes = registerMessage.getNodeConnectionReadable();
        String ipAddress = connection.getSocket().getInetAddress().getHostName().split("\\.")[0];

        String message = checkRegistrationStatus(nodes, ipAddress, register);
        byte status;
        if (message.length() == 0) {
            if (register) {
                registryConnections.put(nodes, connection);
            } else {
                registryConnections.remove(nodes);
                System.out.println("Deregistered " + nodes + ". There are now ("
                        + registryConnections.size() + ") connections.\n");
            }
            message = "Registration request successful.  The number of messaging nodes currently "
                    + "constituting the overlay is (" + registryConnections.size() + ").\n";
            status = Protocol.SUCCESS;
        } else {
            System.out.println("Unable to process request. Responding with a failure.");
            status = Protocol.FAILURE;
        }
        /*
         * RegisterResponse response = new RegisterResponse(status, message);
         * try {
         * connection.getTCPSenderThread().sendData(response.getBytes());
         * } catch (IOException | InterruptedException e) {
         * System.out.println(e.getMessage());
         * connections.remove(nodes);
         * e.printStackTrace();
         * }
         */
    }

    private String checkRegistrationStatus(String nodeDetails, String connectionIP,
            final boolean register) {

        String message = "";
        if (registryConnections.containsKey(nodeDetails) && register) {
            message = "The node, " + nodeDetails + " had previously registered and has "
                    + "a valid entry in its registry. ";
        } else if (!registryConnections.containsKey(nodeDetails) && !register) {
            /*
             * The case that the item is not in the registry.
             */
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

    private void setupOverlay(String[] input) {
        /* start thread pool of size given in input */
        try {
            this.threadPoolSize = Integer.parseInt(input[1]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid input: Enter valid integer for size of the thread pool.");
            e.printStackTrace();
        }
        /* TODO: need a check to see if overlay already exists */

        /* create overlay with the edges count and the existing connections */
        int connectingEdges = 4;
        try {
            (new Overlay()).setupOverlay(registryConnections, connectingEdges);
        } catch (Exception e) {
            // Handle exceptions during overlay setup
            System.out.println("Overlay setup failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        // Notify about successful overlay configuration
        System.out.println(
                "Network overlay successfully configured and sent to (" + registryConnections.size()
                        + ") connections.");
    }
}