package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents a message indicating the initiation of a task
 * with a specified number of rounds.
 */
public class TaskInitiate implements Event {

    int type;
    int numberOfRounds;

    /**
     * Constructs a TaskInitiate object with the specified number of rounds.
     * 
     * @param numberOfRounds The number of rounds for the task.
     */
    public TaskInitiate(int numberOfRounds) {
        this.type = Protocol.TASK_INITIATE;
        this.numberOfRounds = numberOfRounds;
    }

    /**
     * Constructs a TaskInitiate object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public TaskInitiate(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        this.numberOfRounds = din.readInt();

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the TaskInitiate object into a byte array.
     * 
     * @return The marshalled byte array.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.writeInt(numberOfRounds);
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public int getNumberOfRounds() {
        return this.numberOfRounds;
    }

    /**
     * Returns a string representation of the TaskInitiate object.
     * 
     * @return A string representing the task initiation message.
     */
    @Override
    public String toString() {
        return Integer.toString(type) + " " + Integer.toString(numberOfRounds);
    }
}
