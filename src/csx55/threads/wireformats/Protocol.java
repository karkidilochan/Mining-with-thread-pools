package csx55.threads.wireformats;

public interface Protocol {
    final int REGISTER_REQUEST = 0;

    final int COMPUTATION_NODES_LIST = 1;

    final byte SUCCESS = (byte) 200;
    final byte FAILURE = (byte) 500;
}
