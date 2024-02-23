package csx55.threads.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import csx55.threads.node.Node;
import csx55.threads.wireformats.Message;
import csx55.threads.wireformats.MessageFactory;

public class TCPReceiver implements Runnable {

    private Socket socket;

    private Node node;

    private TCPConnection connection;

    protected DataInputStream din;

    /*
     * Default constructor needs the client socket, node that is listening in the
     * client socket and the connection the node is present in
     */
    public TCPReceiver(Node node, Socket socket, TCPConnection connection) throws IOException {
        this.node = node;
        this.din = new DataInputStream(socket.getInputStream());
        this.connection = connection;
    }

    public void run() {
        /* as long as the socket exists, keep reading data from the client socket */
        while (socket != null) {
            try {
                int len = din.readInt();
                byte[] data = new byte[len];

                din.readFully(data);

                /*
                 * de-serialize the read data into a message type and call the appropriate
                 * handler
                 */
                Message message = (MessageFactory.getInstance()).createMessage(data);
                node.handleIncomingMessage(message, connection);
            } catch (Exception e) {
                System.out.println("Error at Receiver Thread: Connection Closed.");
                e.printStackTrace();
                break;
            }

        }
    }
}