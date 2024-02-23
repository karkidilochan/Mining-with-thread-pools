package csx55.threads.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Register implements Message {

    private int type;

    private String host;

    private int port;

    public Register(int type, String host, int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }

    /* constructor for un-marshalling data and intializing class fields */
    public Register(byte[] marshalledData) throws IOException {
        // creating input stream to read byte data sent over network connection
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);

        // wrap internal bytes array with data input stream
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();

        int len = din.readInt();

        byte[] ipData = new byte[len];
        din.readFully(ipData, 0, len);

        this.host = new String(ipData);

        this.port = din.readInt();

        inputData.close();
        din.close();
    }

    /* marshalling class fields to bytes array */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);

        byte[] ipData = host.getBytes();
        dout.writeInt(ipData.length);
        dout.write(ipData);

        dout.writeInt(port);

        dout.flush();
        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();
        return marshalledData;
    }

    public String getNodeConnectionReadable() {
        return this.host + ":" + Integer.toString(this.port);
    }

    public int getType() {
        return this.type;
    }
}
