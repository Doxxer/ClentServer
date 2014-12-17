package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ReadFromServer extends ServerAction {
    private final MessageGenerator messageGenerator;

    public ReadFromServer(String actionName, int nextSocketState, MessageGenerator messageGenerator) {
        super(actionName, nextSocketState);
        this.messageGenerator = messageGenerator;
    }

    @Override
    protected int makeSocketAction(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        int messageLength = readFromChannel(channel, 4).getInt();
        ByteBuffer message = readFromChannel(channel, messageLength);

        messageGenerator.checkServerResponse(message.array());

        channel.register(selector, nextSocketState);

        return messageLength + 4;
    }

    private ByteBuffer readFromChannel(SocketChannel channel, int size) throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(size);
        int readBytes = 0;
        do {
            int length = channel.read(sizeBuffer);
            if (length == -1) {
                throw new IOException("server unreachable");
            }
            readBytes += length;
        } while (readBytes < size);
        sizeBuffer.flip();
        return sizeBuffer;
    }
}
