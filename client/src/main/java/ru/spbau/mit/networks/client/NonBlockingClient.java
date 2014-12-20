package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
                try {
                    if (selector.select(500) >= 0) {
                        interactWithServer(selector);
                    } else {
                        break;
                    }
                } catch (InteractionException ignored) {
                    // if Interaction exception occurred - just break and retry again
                    break;
                } // else: go out and close thread
            }
        }
    }

    private void interactWithServer(Selector selector) throws WrongResponseException, ConnectException, ClosedChannelException, InteractionException {
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (!key.isValid()) {
                continue;
            }

            SocketChannel channel = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                connector.makeAction(channel);
                channel.register(selector, SelectionKey.OP_WRITE);
            } else if (key.isWritable()) {
                writer.makeAction(channel);
                channel.register(selector, SelectionKey.OP_READ);
            } else if (key.isReadable()) {
                reader.makeAction(channel);
                channel.register(selector, SelectionKey.OP_WRITE);
                updateStatistics();
            }
        }
    }
}