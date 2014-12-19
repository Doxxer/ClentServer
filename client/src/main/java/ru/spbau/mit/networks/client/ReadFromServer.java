package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ReadFromServer extends ServerAction {
    private final MessageController messageController;

    public ReadFromServer(String actionName, MessageController messageController) {
        super(actionName);
        this.messageController = messageController;
    }

    @Override
    protected int makeSocketAction(SocketChannel channel) throws IOException {
        int messageLength = readFromChannel(channel, 4).getInt();
        ByteBuffer message = readFromChannel(channel, messageLength);
        messageController.validateServerResponse(message.array());
        return messageLength;
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
