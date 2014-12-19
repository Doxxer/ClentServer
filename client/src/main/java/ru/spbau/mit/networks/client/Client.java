package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    public static final AtomicInteger totalSentMessages = new AtomicInteger(0);

    private final String hostname;
    private final int port;
    private final ServerAction connector, writer, reader;
    private int counter;

    public Client(String host, int port, int messageSize) {
        this.hostname = host;
        this.port = port;
        this.counter = 0;

        MessageController messageController = new MatrixMessageController(messageSize);
        connector = new ConnectToServer("connecting");
        writer = new WriteToServer("writing", messageController);
        reader = new ReadFromServer("reading", messageController);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                try (SocketChannel clientChannel = SocketChannel.open(); Selector selector = Selector.open()) {
                    clientChannel.configureBlocking(false);
                    clientChannel.connect(new InetSocketAddress(hostname, port));
                    clientChannel.register(selector, SelectionKey.OP_CONNECT);

                    while (interactWithServer(selector)) {
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
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