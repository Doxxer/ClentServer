package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    public static AtomicInteger totalSentMessages = new AtomicInteger(0);

    private final String hostname;
    private final int port;
    private final MessageController messageGenerator;
    private int counter;
    private boolean connectionFailed = false;
    private boolean writingFailed = false;

    public Client(String host, int port) {
        this.hostname = host;
        this.port = port;
        this.counter = 0;
        messageGenerator = new MessageController();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                try (SocketChannel clientChannel = SocketChannel.open();
                     Selector selector = Selector.open()) {
                    clientChannel.configureBlocking(false);
                    clientChannel.connect(new InetSocketAddress(hostname, port));
                    clientChannel.register(selector, SelectionKey.OP_CONNECT);

                    while (readWriteFromSocket(selector)) {

                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
        }
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
//
//                    readWriteFromSocket(selector);
//                    readWriteFromSocket(selector);
//                    readWriteFromSocket(selector);
//                }
//            }
//        } catch (Exception e) {
//            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
//        }
//    }
//
    private boolean readWriteFromSocket(Selector selector) throws IOException, InterruptedException {
        if (selector.select(500) <= 0) {
            return false;
        }

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (!key.isValid()) {
                continue;
            }

            if (key.isConnectable()) {
                try {
                    connect(key, selector);
                    if (connectionFailed) {
                        connectionFailed = false;
                        Logger.getGlobal().log(Level.INFO, MessageFormat.format("[thread {0}]: connection OK", Thread.currentThread().getId()));
                    }
                } catch (IOException e) {
                    if (!connectionFailed) {
                        connectionFailed = true;
                        Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: connection FAILED", Thread.currentThread().getId()));
                    }
                }
            } else if (key.isWritable()) {
                try {
                    write(key, selector);
                    if (writingFailed) {
                        writingFailed = false;
                        Logger.getGlobal().log(Level.INFO, MessageFormat.format("[thread {0}]: writing OK", Thread.currentThread().getId()));
                    }
                } catch (IOException | InterruptedException e) {
                    if (!writingFailed) {
                        writingFailed = true;
                        Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: writing FAILED", Thread.currentThread().getId()));
                    }
                }
            } else if (key.isReadable()) {
                try {
                    read(key, selector);
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: reading FAILED", Thread.currentThread().getId()));
                    throw e;
                }
            }
        }
        return true;
    }

    private void connect(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.register(selector, SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        int messageLength = readFromChannel(channel, 4).getInt();
        ByteBuffer message = readFromChannel(channel, messageLength);

        messageGenerator.decode(message.array());

        channel.register(selector, SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key, Selector selector) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel) key.channel();

        byte[] message = messageGenerator.getMessage();

        int sentBytes = writeToChannel(channel, message);
        counter++;
        totalSentMessages.incrementAndGet();
        channel.register(selector, SelectionKey.OP_READ);
//        if (counter % 100 == 0) {
//            System.out.println(MessageFormat.format("[thread {0}]: sent {1} bytes (message #{2})", Thread.currentThread().getId(), sentBytes, counter));
//        }
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