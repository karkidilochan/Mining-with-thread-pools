package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents a message indicating the completion of a task.
 */
public class TaskComplete implements Event {

    private int type;
    private String host;
    private int port;

    /**
     * Constructs a TaskComplete object with the host and port information.
     * 
     * @param host The host address.
     * @param port The port number.
     */
    public TaskComplete(String host, int port) {
        this.type = Protocol.TASK_COMPLETE;
        this.host = host;
        this.port = port;
    }

    /**
     * Constructs a TaskComplete object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public TaskComplete(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        int len = din.readInt();
        byte[] bytes = new byte[len];
        din.readFully(bytes);
        this.host = new String(bytes);
        this.port = din.readInt();

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the TaskComplete object into a byte array.
     * 
     * @return The marshalled byte array.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        byte[] hostBytes = host.getBytes();
        dout.writeInt(hostBytes.length);
        dout.write(hostBytes);
        dout.writeInt(port);
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public String getConnection() {
        return this.host + ":" + Integer.toString(this.port);
    }

    /**
     * Returns a string representation of the TaskComplete object.
     * 
     * @return A string representing the task completion message.
     */
    @Override
    public String toString() {
        return Integer.toString(this.type) + " " + this.getConnection();
    }

}
