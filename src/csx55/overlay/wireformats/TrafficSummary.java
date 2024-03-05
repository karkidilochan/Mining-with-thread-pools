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

    private long generatedCount;
    private long pulledCount;
    private long pushedCount;
    private long completedCount;

    /**
     * Constructs a TrafficSummary object with the specified parameters.
     *
     * @param ipAddress          The IP address.
     * @param portNumber         The port number.
     * @param messagesStatistics The statistics of messages.
     */
    public TrafficSummary(String ipAddress, int portNumber, TaskStatistics messagesStats) {
        this.type = Protocol.TRAFFIC_SUMMARY;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.generatedCount = messagesStats.getGenerated();
        this.pulledCount = messagesStats.getPulled();
        this.pushedCount = messagesStats.getPushed();
        this.completedCount = messagesStats.getCompleted();
    }

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
        this.generatedCount = din.readLong();
        this.pulledCount = din.readLong();
        this.pushedCount = din.readLong();
        this.completedCount = din.readLong();

        inputData.close();
        din.close();
    }

    public long getGenerated() {
        return generatedCount;
    }

    public long getPulled() {
        return pulledCount;
    }

    public long getPushed() {
        return pushedCount;
    }

    public long getCompleted() {
        return completedCount;
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
        dout.writeLong(generatedCount);
        dout.writeLong(pulledCount);
        dout.writeLong(pushedCount);
        dout.writeLong(completedCount);

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
                Long.toString(generatedCount),
                Long.toString(pulledCount),
                Long.toString(pushedCount),
                Long.toString(completedCount));
    }

}
