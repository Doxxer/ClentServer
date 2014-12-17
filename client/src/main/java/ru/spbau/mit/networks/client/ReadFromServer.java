package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ReadFromServer extends ServerAction {
    private final MessageController messageController;

    public ReadFromServer(String actionName, int nextSocketState, int failingSocketState, MessageController messageController) {
        super(actionName, nextSocketState, failingSocketState);
        this.messageController = messageController;
    }

    @Override
    protected int makeSocketAction(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int messageLength = readFromChannel(channel, 4).getInt();
            ByteBuffer message = readFromChannel(channel, messageLength);
            messageController.checkServerResponse(message.array());
            channel.register(selector, nextSocketState);
            return -1;
        } catch (IOException e) {
            channel.register(selector, failingSocketState);
            throw e;
        }
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
