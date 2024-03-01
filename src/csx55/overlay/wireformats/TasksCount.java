package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TasksCount implements Event {
    int type;

    String nodeDetails;

    int numberOfTasks;

    public TasksCount(String nodeDetails, int numberOfTasks) {
        this.type = Protocol.TASKS_COUNT;
        this.nodeDetails = nodeDetails;
        this.numberOfTasks = numberOfTasks;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(opStream));

        dout.writeInt(type);
        dout.writeInt(numberOfTasks);

        byte[] hostBytes = nodeDetails.getBytes();
        dout.writeInt(hostBytes.length);
        dout.write(hostBytes);
        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public TasksCount(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        this.numberOfTasks = din.readInt();
        int len = din.readInt();
        byte[] bytes = new byte[len];
        din.readFully(bytes);
        this.nodeDetails = new String(bytes);

        inputData.close();
        din.close();
    }

    public int getType() {
        return type;
    }

    public String getHost() {
        return nodeDetails;
    }

    public int getCount() {
        return numberOfTasks;
    }
}
