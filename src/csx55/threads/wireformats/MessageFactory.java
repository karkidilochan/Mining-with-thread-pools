package csx55.threads.wireformats;

import java.nio.ByteBuffer;

public class MessageFactory {
    private static final MessageFactory messageFactory = new MessageFactory();

    /* private constructor to prevent re-instantiation */
    private MessageFactory() {

    }

    /*
     * a public static function that will return the created instance of this
     * singleton class
     */
    public static MessageFactory getInstance() {
        return messageFactory;
    }

    public Message createMessage(byte[] marshalledData) {
        int type = ByteBuffer.wrap(marshalledData).getInt();

        switch (type) {
            default:
                System.out.println("Error generating message: " + type);
                return null;
        }
    }
}
