package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.Iterator;

public class NonBlockingClient extends Client {

    public NonBlockingClient(String host, int port, int messageSize, int reportFrequency) {
        super(host, port, messageSize, reportFrequency);
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

            SocketChannel channel = (SocketChannel) key.channel();

            if (key.isConnectable()) {
                if (connector.makeAction(channel) != -1) {
                    channel.register(selector, SelectionKey.OP_WRITE);
                } else {
                    return false;
                }
            } else if (key.isWritable()) {
                int sentBytes = writer.makeAction(channel);
                if (sentBytes != -1) {
                    channel.register(selector, SelectionKey.OP_READ);
                    counter++;
                    totalSentMessages.incrementAndGet();
                    if (counter % reportFrequency == 0) {
                        System.out.println(MessageFormat.format("[thread {0}]: sent message #{1} ({2} bytes)",
                                Thread.currentThread().getId(), counter, sentBytes));
                    }
                } else {
                    return false;
                }
            } else if (key.isReadable()) {
                int readBytes = reader.makeAction(channel);
                if (readBytes != -1) {
                    channel.register(selector, SelectionKey.OP_WRITE);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}