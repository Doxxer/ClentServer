package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteToServer extends ServerAction {
    private final MessageController messageController;

    public WriteToServer(String actionName, MessageController messageController) {
        super(actionName);
        this.messageController = messageController;
    }

    @Override
    protected void makeSocketAction(SocketChannel channel) throws IOException {
        this.messageLength = writeToChannel(channel, messageController.createRequest());
        this.timestamp = System.nanoTime();
    }

    private int writeToChannel(SocketChannel channel, byte[] message) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(message.length + 4);
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.position(0);

        int sentBytes = 0;
        while (buffer.hasRemaining()) {
            sentBytes += channel.write(buffer);
        }
        return sentBytes;
    }
}
