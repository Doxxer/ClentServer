package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.Iterator;

public class BlockingClient extends Client {

    public BlockingClient(String host, int port, int messageSize) {
        super(port, host, messageSize);
    }

    @Override
    protected void runClient() throws IOException {
        try (SocketChannel clientChannel = SocketChannel.open(); Selector selector = Selector.open()) {
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(hostname, port));
            clientChannel.register(selector, SelectionKey.OP_CONNECT);

            while (true) {
                if (!interactWithServer(selector)) {
                    break;
                }
            }
        }
    }

    private boolean interactWithServer(Selector selector) throws IOException {
        int selectResult = selector.select(500);
        if (selectResult <= 0) {
            return selectResult == 0;
        }

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (!key.isValid()) {
                continue;
            }

            SocketChannel socketChannel = (SocketChannel) key.channel();

            if (key.isConnectable()) {
                connector.makeAction(socketChannel);
                socketChannel.register(selector, SelectionKey.OP_WRITE);
            } else if (key.isWritable()) {
                int sentBytes = writer.makeAction(socketChannel);
                if (sentBytes != -1) {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    counter++;
                    totalSentMessages.incrementAndGet();
                    if (counter % 10 == 0) {
                        System.out.println(MessageFormat.format("[thread {0}]: sent message #{1} ({2} bytes)",
                                Thread.currentThread().getId(), counter, sentBytes));
                    }
                } else {
                    return false;
                }
            } else if (key.isReadable()) {
                int readBytes = reader.makeAction(socketChannel);
                if (readBytes != -1) {
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    //    private boolean blockingInteractingWithServer(SocketChannel channel) throws IOException {
//        writeToChannel(channel, messageController.createRequest());
//        totalSentMessages.incrementAndGet();
//
//        int messageLength = readFromChannel(channel, 4).getInt();
//        ByteBuffer message = readFromChannel(channel, messageLength);
//        messageController.validateServerResponse(message.array());
//        return true;
//    }


}