package csx55.threads.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPSender implements Runnable {
    private LinkedBlockingQueue<byte[]> queue;

    protected DataOutputStream dout;

    /*
     * sender thread needs the output stream and a blocking queue for sending
     * messages
     */
    public TCPSender(Socket socket) throws IOException {
        /* setting a high value for queue size to store messages */
        final int queueSize = 1000;

        this.queue = new LinkedBlockingQueue<>(queueSize);
        this.dout = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        /* take data from the queue, and write it to the output stream */
        while (true) {
            try {
                byte[] data = queue.take();
                int len = data.length;

                dout.writeInt(len);
                dout.write(data);
                dout.flush();
            } catch (Exception e) {
                System.out.println("Error at TCPSender Thread:" + e.getMessage());
                e.printStackTrace();
            }

        }
    }

}