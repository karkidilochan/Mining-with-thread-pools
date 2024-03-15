package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import csx55.overlay.task.Task;

/**
 * Represents a message indicating the initiation of a task
 * with a specified number of rounds.
 */
public class MigrateTasks implements Event {

    int type;
    private List<Task> tasksList = new ArrayList<>();;

    public MigrateTasks(List<Task> tasksList) {
        this.type = Protocol.MIGRATE_TASKS;
        this.tasksList = tasksList;
    }

    public MigrateTasks(byte[] marshalledData) throws IOException {
        ByteArrayInputStream inputData = new ByteArrayInputStream(marshalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(inputData));

        this.type = din.readInt();
        int numberOfTasks = din.readInt();

        ObjectInputStream oiStream = new ObjectInputStream(din);
        for (int i = 0; i < numberOfTasks; i++) {
            try {
                Task task = (Task) oiStream.readObject();
                tasksList.add(task); // Add task to tasksList
            } catch (ClassNotFoundException e) {
                e.printStackTrace(); // Handle ClassNotFoundException appropriately
            }

        }
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
        dout.writeInt(tasksList.size());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(dout);
        for (Task task : tasksList) {
            objectOutputStream.writeObject(task);
            objectOutputStream.flush();
        }

        dout.flush();

        byte[] marshalledData = opStream.toByteArray();

        opStream.close();
        dout.close();

        return marshalledData;
    }

    public List<Task> getTasks() {
        return this.tasksList;
    }

    public int getTasksSize() {
        return this.tasksList.size();
    }

}
