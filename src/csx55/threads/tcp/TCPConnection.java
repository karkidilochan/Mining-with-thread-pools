package csx55.threads.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import csx55.threads.node.Node;

public class TCPConnection {
    /* A TCP connection contains a socket and sender and receiver threads */

    private Socket socket;
    private TCPSender sender;
    private TCPReceiver receiver;

    public TCPConnection(Node node, Socket socket) throws IOException {
        this.socket = socket;
        this.sender = new TCPSender(this.socket);
        this.receiver = new TCPReceiver(node, socket, this);
    }

    /* start the connection */
    public void start() {
        (new Thread(this.sender)).start();
        (new Thread(this.receiver)).start();
    }

    /*
     * close the connection by stopping the sender and receiver threads, and the
     * client socket
     */
    public void closeConnection() throws IOException, InterruptedException {
        /*
         * set a timer to ensure all message handlers are completed before shutting the
         * connection
         */
        TimeUnit.SECONDS.sleep(5);
        this.socket.close();
        this.sender.dout.close();
        this.receiver.din.close();
    }

    public TCPSender getSenderThread() {
        return this.sender;
    }

    public Socket getSocket() {
        return this.socket;
    }

}