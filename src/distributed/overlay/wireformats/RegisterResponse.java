package distributed.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * RegisterResponse class represents a response message to a registration or
 * deregistration request.
 */
public class RegisterResponse implements Event {

    private int type;
    private byte status;
    private String info;

    /**
     * Constructs a RegisterResponse object for creating a response message.
     * 
     * @param status The status of the response.
     * @param info   Additional information about the response.
     */
    public RegisterResponse(byte status, String info) {
        this.type = Protocol.REGISTER_RESPONSE;
        this.status = status;
        this.info = info;
    }

    /**
     * Constructs a RegisterResponse object by unmarshalling the byte array.
     * 
     * @param marshalledData The marshalled byte array containing the data.
     */
    public RegisterResponse(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        this.status = din.readByte();
        int len = din.readInt();
        byte[] infoData = new byte[len];
        din.readFully(infoData, 0, len);
        this.info = new String(infoData);

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    /**
     * Marshals the RegisterResponse object into a byte array.
     * 
     * @return The marshalled byte array.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.writeByte(status);
        byte[] infoData = info.getBytes();
        dout.writeInt(infoData.length);
        dout.write(infoData);

        // making sure data from buffer is flushed
        dout.flush();
        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();
        return marshalledData;
    }

    public String toString() {
        return info;
    }

}