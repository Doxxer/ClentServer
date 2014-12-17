package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class WriteToServer extends ServerAction {
    private final MessageGenerator messageGenerator;

    public WriteToServer(String actionName, int nextSocketState, MessageGenerator messageGenerator) {
        super(actionName, nextSocketState);
        this.messageGenerator = messageGenerator;
    }

    @Override
    protected int makeSocketAction(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        int sentBytes = writeToChannel(channel, messageGenerator.createRequest());
        channel.register(selector, nextSocketState);

        return sentBytes;
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
