package csx55.overlay.wireformats;

/**
 * Protocol interface defines the protocol constants used for communication
 * between nodes.
 */
public interface Protocol {

    final int REGISTER_REQUEST = 0;
    final int REGISTER_RESPONSE = 1;
    final int DEREGISTER_REQUEST = 2;
    final int MESSAGING_NODES_LIST = 3;
    final int MESSAGE = 4;
    final int LINK_WEIGHTS = 5;
    final int TASK_INITIATE = 6;
    final int TASK_COMPLETE = 7;
    final int PULL_TRAFFIC_SUMMARY = 8;
    final int TRAFFIC_SUMMARY = 9;
    final int TASKS_COUNT = 10;

    final byte SUCCESS = (byte) 200;
    final byte FAILURE = (byte) 500;
}
