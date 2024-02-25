package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Represents payload to be sent between nodes in the overlay
 * network.
 */
public class Message implements Event {

    private int type;
    private int position;
    private int payload;
    private String[] route;

    /**
     * Constructs a Message object with the specified payload, position, and route.
     * 
     * @param payload  The payload of the message.
     * @param position The position of the message within the routing path.
     * @param route    The routing path of the message.
     */
    public Message(int payload, int position, String[] route) {
        this.type = Protocol.MESSAGE;
        this.payload = payload;
        this.position = position;
        this.route = route;
    }

    /**
     * Constructs a Message object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public Message(byte[] marshalledData) throws IOException {

        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        this.payload = din.readInt();
        this.position = din.readInt();

        int len = din.readInt();
        this.route = new String[len];

        for (int i = 0; i < len; i++) {
            int stringLength = din.readInt();
            byte[] bytes = new byte[stringLength];
            din.readFully(bytes, 0, stringLength);
            this.route[i] = new String(bytes);
        }

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the Message object into a byte array.
     * 
     * @return The marshalled byte array.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.writeInt(payload);
        dout.writeInt(position);
        dout.writeInt(route.length);

        // loop through collections of strings
        for (String path : route) {
            byte[] bytes = path.getBytes();
            dout.writeInt(bytes.length);
            dout.write(bytes);
        }

        // make sure to flush data from buffer
        dout.flush();

        // get a copy of bytes array from internal buffer
        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();
        return marshalledData;
    }

    public String toString() {
        return Integer.toString(this.type) + " "
                + Integer.toString(this.payload) + " "
                + Integer.toString(this.position) + " "
                + Arrays.toString(route);
    }

    public void incrementPosition() {
        position++;
    }

    public int getPayload() {
        return payload;
    }

    public int getPosition() {
        return position;
    }

    public String[] getRoutingPath() {
        return route;
    }
}
