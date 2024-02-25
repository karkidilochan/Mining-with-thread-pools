package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Register class represents a message for registering or deregistering a node.
 */
public class Register implements Event {
    private int type;

    private String ipAddress;
    private int port;

    /**
     * Constructs a Register object for registering or deregistering a node.
     * 
     * @param type      The type of registration.
     * @param ipAddress The IP address of the node.
     * @param port      The port number of the node.
     */
    public Register(int type, String ipAddress, int port) {
        this.type = type;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Constructs a Register object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public Register(byte[] marshalledData) throws IOException {
        // creating input stream to read byte data sent over network connection
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);

        // wrap internal bytes array with data input stream
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();

        int len = din.readInt();

        byte[] ipData = new byte[len];
        din.readFully(ipData, 0, len);

        this.ipAddress = new String(ipData);

        this.port = din.readInt();

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the Register object into a byte array.
     * 
     * @return The marshalled byte array.
     * @throws IOException If an I/O error occurs.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);

        byte[] ipData = ipAddress.getBytes();
        dout.writeInt(ipData.length);
        dout.write(ipData);

        dout.writeInt(port);

        dout.flush();
        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();
        return marshalledData;
    }

    /**
     * Returns a string representation of the Register object.
     * 
     * @return A string representing the type and connection details.
     */
    public String getRegisterReadable() {
        return Integer.toString(this.type) + " " + this.getConnectionReadable();
    }

    /**
     * Returns a readable representation of the connection details.
     * 
     * @return A string representing the IP address and port.
     */
    public String getConnectionReadable() {
        return this.ipAddress + ":" + Integer.toString(this.port);
    }

}