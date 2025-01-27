package distributed.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * PullTrafficSummary class represents request for traffic summary information.
 */
public class PullTrafficSummary implements Event {
    int type;

    /**
     * Construct a PullTrafficSummary object.
     */
    public PullTrafficSummary() {
        this.type = Protocol.PULL_TRAFFIC_SUMMARY;
    }

    /**
     * Constructs a PullTrafficSummary object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     * @throws IOException If an I/O error occurs.
     */
    public PullTrafficSummary(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshalls the PullTrafficSummary object into a byte array.
     * 
     * @return The marshalled byte array.
     * @throws IOException If an I/O error occurs.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    /**
     * Returns a string representation of the PullTrafficSummary object.
     */
    @Override
    public String toString() {
        return "PullTrafficSummary";
    }
}
