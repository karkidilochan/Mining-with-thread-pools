package distributed.overlay.wireformats;

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
public class CheckStatus implements Event {

    private int type;
    private int tasksCount;

    /**
     * Constructs a TaskInitiate object with the specified number of rounds.
     * 
     * @param numberOfRounds The number of rounds for the task.
     */
    public CheckStatus(int tasksCount) {
        this.type = Protocol.CHECK_STATUS;
        this.tasksCount = tasksCount;
    }

    /**
     * Constructs a TaskInitiate object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public CheckStatus(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        this.tasksCount = din.readInt();

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
        dout.writeInt(tasksCount);
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public int getCount() {
        return tasksCount;
    }

}
