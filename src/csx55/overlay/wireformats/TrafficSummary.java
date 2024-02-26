package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import csx55.overlay.routing.TaskStatistics;

/**
 * Represents a message containing traffic statistics.
 */
public class TrafficSummary implements Event {

    private int type;
    private String ipAddress;
    private int portNumber;

    private int sentMessagesCount;
    private long sentMessagesSummation;
    private int receivedMessagesCount;
    private long receivedMessagesSummation;
    private int relayedMessagesCount;

    /**
     * Constructs a TrafficSummary object with the specified parameters.
     *
     * @param ipAddress          The IP address.
     * @param portNumber         The port number.
     * @param messagesStatistics The statistics of messages.
     */
    // public TrafficSummary(String ipAddress, int portNumber, TaskStatistics
    // messagesStats) {
    // this.type = Protocol.TRAFFIC_SUMMARY;
    // this.ipAddress = ipAddress;
    // this.portNumber = portNumber;
    // this.sentMessagesCount = messagesStats.getSentMessagesCount();
    // this.sentMessagesSummation = messagesStats.getSentMessagesSummation();
    // this.receivedMessagesCount = messagesStats.getReceivedMessagesCount();
    // this.receivedMessagesSummation = messagesStats.getReceivedSummationCount();
    // this.relayedMessagesCount = messagesStats.getRelayedMessagesCount();
    // }

    /**
     * Constructs a TrafficSummary object by unmarshalling the byte array.
     *
     * @param marshalledData The marshalled byte array containing the data.
     */
    public TrafficSummary(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        int len = din.readInt();
        byte[] bytes = new byte[len];
        din.readFully(bytes);

        this.ipAddress = new String(bytes);
        this.portNumber = din.readInt();
        this.sentMessagesCount = din.readInt();
        this.sentMessagesSummation = din.readLong();
        this.receivedMessagesCount = din.readInt();
        this.receivedMessagesSummation = din.readLong();
        this.relayedMessagesCount = din.readInt();

        inputData.close();
        din.close();
    }

    public int getSentMessagesCount() {
        return sentMessagesCount;
    }

    public int getReceivedMessagesCount() {
        return receivedMessagesCount;
    }

    public long getSentMessagesSummation() {
        return sentMessagesSummation;
    }

    public long getReceivedSummationCount() {
        return receivedMessagesSummation;
    }

    public int getRelayedMessagesCount() {
        return relayedMessagesCount;
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the TrafficSummary object into a byte array.
     *
     * @return The marshalled byte array.
     * @throws IOException If an I/O error occurs.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);

        byte[] ipBytes = ipAddress.getBytes();
        dout.writeInt(ipBytes.length);
        dout.write(ipBytes);

        dout.writeInt(portNumber);
        dout.writeInt(sentMessagesCount);
        dout.writeLong(sentMessagesSummation);
        dout.writeInt(receivedMessagesCount);
        dout.writeLong(receivedMessagesSummation);
        dout.writeInt(relayedMessagesCount);

        dout.flush();
        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    /**
     * Returns a string representation of the TrafficSummary object.
     *
     * @return A string representing the traffic summary.
     */
    public String toString() {
        return String.format("%1$20s %2$12s %3$10s %4$15s %5$15s %6$10s",
                ipAddress + ":" + Integer.toString(portNumber),
                Integer.toString(sentMessagesCount),
                Integer.toString(receivedMessagesCount),
                Long.toString(sentMessagesSummation),
                Long.toString(receivedMessagesSummation),
                Integer.toString(relayedMessagesCount));
    }

}
