package csx55.threads.node;

import java.net.ServerSocket;
import java.util.Scanner;

import csx55.threads.tcp.TCPConnection;
import csx55.threads.tcp.TCPServer;
import csx55.threads.wireformats.Message;

public class Registry implements Node {

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

    public void handleIncomingMessage(Message message, TCPConnection connection) {

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
                    default:
                        System.out.println("Please enter a valid command! Options are:\n" +
                                " - setup-overlay thread-pool-size\n" +
                                " - start number-of-rounds");
                        break;
                }
            }
        }
    }
}