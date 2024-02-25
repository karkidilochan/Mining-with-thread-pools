package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message containing a list of messaging nodes.
 */
public class MessagingNodesList implements Event {

    private int type;
    private int numberOfPeers;
    private int threadPoolSize;

    private List<String> peers;

    /**
     * Constructs a MessagingNodesList object with the specified number of peers and
     * list of peers.
     * 
     * @param numberOfPeers The number of peers in the list.
     * @param peers         The list of peers.
     */
    public MessagingNodesList(int numberOfPeers, List<String> peers, int threadPoolSize) {
        this.type = Protocol.MESSAGING_NODES_LIST;
        this.numberOfPeers = numberOfPeers;
        this.peers = peers;
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Constructs a MessagingNodesList object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public MessagingNodesList(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputStream));

        this.type = din.readInt();
        this.threadPoolSize = din.readInt();
        this.numberOfPeers = din.readInt();

        this.peers = new ArrayList<String>(this.numberOfPeers);
        for (int i = 0; i < this.numberOfPeers; i++) {
            int len = din.readInt();
            byte[] bytes = new byte[len];
            din.readFully(bytes);
            this.peers.add(new String(bytes));
        }

        inputStream.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the MessagingNodesList object into a byte array.
     * 
     * @return The marshalled byte array.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.writeInt(threadPoolSize);
        dout.writeInt(numberOfPeers);

        for (String peer : peers) {
            byte[] bytes = peer.getBytes();
            dout.writeInt(bytes.length);
            dout.write(bytes);
        }

        // make sure buffer is flushed
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public int getNumberOfPeers() {
        return numberOfPeers;
    }

    public List<String> getPeersList() {
        return peers;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Returns a string representation of the MessagingNodesList object.
     */
    @Override
    public String toString() {
        return Integer.toString(this.type) + " "
                + Integer.toString(this.numberOfPeers) + " " + String.join(", ", this.peers);
    }

}
