package csx55.threads.tcp;

import java.net.ServerSocket;
import java.net.Socket;

import csx55.threads.node.Node;

public class TCPServer implements Runnable {

    private ServerSocket serverSocket;

    private Node node;
    /* default constructor will take the current node and server socket */

    public TCPServer(Node node, ServerSocket serverSocket) {
        this.node = node;
        this.serverSocket = serverSocket;
    }

    public void run() {
        /*
         * main function runs the server socket until it's closed
         * then, server socket accepts and creates incoming connections
         */
        while (serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();

                /* create a TCP connection with the client socket and start */
                TCPConnection connection = new TCPConnection(node, clientSocket);
                connection.start();
            } catch (Exception e) {
                System.out.println("Error accepting incoming connection: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

    }

}