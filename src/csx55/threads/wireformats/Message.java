package csx55.threads.wireformats;

import java.io.IOException;

public interface Message {
    public int getType();

    public byte[] getBytes() throws IOException;
}
