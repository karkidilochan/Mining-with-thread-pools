package csx55.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * WireFormatGenerator is a singleton class responsible for generating messages
 * of different types.
 */
public class WireFormatGenerator {
    private static final WireFormatGenerator messageGenerator = new WireFormatGenerator();

    // private constructor to prevent instantiation
    private WireFormatGenerator() {
    }

    /**
     * Returns the singleton instance of WireFormatGenerator.
     * 
     * @return The WireFormatGenerator instance.
     */
    public static WireFormatGenerator getInstance() {
        return messageGenerator;
    }

    /**
     * Creates a wireformat message from the received marshaled bytes.
     * 
     * @param marshalledData The marshaled bytes representing the event.
     * @return The Event object created from the marshaled bytes.
     */
    /* Create message wireformats from received marshalled bytes */
    public Event createMessage(byte[] marshalledData) throws IOException {
        int type = ByteBuffer.wrap(marshalledData).getInt();
        switch (type) {
            case Protocol.REGISTER_REQUEST:
                return new Register(marshalledData);

            case Protocol.REGISTER_RESPONSE:
                return new RegisterResponse(marshalledData);

            case Protocol.DEREGISTER_REQUEST:
                return new Register(marshalledData);

            case Protocol.MESSAGING_NODES_LIST:
                return new ComputeNodesList(marshalledData);

            // case Protocol.LINK_WEIGHTS:
            // return new LinkWeights(marshalledData);

            case Protocol.TASK_INITIATE:
                return new TaskInitiate(marshalledData);

            case Protocol.MESSAGE:
                return new Message(marshalledData);

            case Protocol.TASK_COMPLETE:
                return new TaskComplete(marshalledData);

            case Protocol.PULL_TRAFFIC_SUMMARY:
                return new PullTrafficSummary(marshalledData);

            case Protocol.TRAFFIC_SUMMARY:
                return new TrafficSummary(marshalledData);

            case Protocol.TASKS_COUNT:
                return new TasksCount(marshalledData);

            case Protocol.MIGRATE_TASKS:
                return new MigrateTasks(marshalledData);

            case Protocol.PULL_REQUEST:
                return new PullRequest(marshalledData);

            case Protocol.MIGRATE_RESPONSE:
                return new MigrateResponse(marshalledData);


            default:
                System.out.println("Error: WireFormat could not be generated. " + type);
                return null;
        }
    }
}
