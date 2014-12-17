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
        connector = new ConnectToServer("connecting", SelectionKey.OP_WRITE, SelectionKey.OP_CONNECT);
        writer = new WriteToServer("writing", SelectionKey.OP_READ, SelectionKey.OP_CONNECT, messageController);
        reader = new ReadFromServer("reading", SelectionKey.OP_WRITE, SelectionKey.OP_CONNECT, messageController);
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
//                        Thread.sleep(200);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
        }
    }

    private boolean interactWithServer(Selector selector) throws IOException {
        int selectResult = selector.selectNow();
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
            if (key.isConnectable()) {
                connector.makeAction(key, selector);
            } else if (key.isWritable()) {
                int sentBytes = writer.makeAction(key, selector);
                if (sentBytes != -1) {
                    counter++;
                    totalSentMessages.incrementAndGet();
                    if (counter % 1 == 0) {
                        System.out.println(MessageFormat.format("[thread {0}]: sent message #{1} ({2} bytes)",
                                Thread.currentThread().getId(), counter, sentBytes));
                    }
                }
            } else if (key.isReadable()) {
                reader.makeAction(key, selector);
            }
        }
        return true;
    }

//    @Override
//    public void run() {
//        try {
//            while (!Thread.interrupted()) {
//                try (SocketChannel clientChannel = SocketChannel.open();
//                     Selector selector = Selector.open()) {
//                    clientChannel.configureBlocking(false);
//                    clientChannel.connect(new InetSocketAddress(hostname, port));
//                    clientChannel.register(selector, SelectionKey.OP_CONNECT);
//                    interactWithServer(selector);
//                    interactWithServer(selector);
//                    interactWithServer(selector);
//                }
//            }
//        } catch (Exception e) {
//            Logger.getGlobal().log(Level.SEVERE, e.createRequest());
//        }
//    }
//

}