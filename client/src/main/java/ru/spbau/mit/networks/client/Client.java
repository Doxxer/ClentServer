package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private final String hostname;
    private final int port;
    private final MessageController messageGenerator;
    private int counter;

    public Client(String host, int port) {
        this.hostname = host;
        this.port = port;
        this.counter = 0;
        messageGenerator = new MessageController();
    }

    @Override
    public void run() {
        try (SocketChannel clientChannel = SocketChannel.open();
             Selector selector = Selector.open()) {
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(hostname, port));
            clientChannel.register(selector, SelectionKey.OP_CONNECT);

            while (!Thread.interrupted() && selector.select(1000) > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isConnectable()) {
                        System.out.println(MessageFormat.format("{0}: connected to server", Thread.currentThread().getId()));
                        try {
                            connect(key, selector);
                        } catch (IOException e) {
                            Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Error occurred while connecting to socket in {0}", Thread.currentThread()));
                            throw e;
                        }
                    } else if (key.isWritable()) {
                        try {
                            write(key, selector);
                        } catch (IOException | InterruptedException e) {
                            Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Error occurred while writing to socket in {0}", Thread.currentThread()));
                            throw e;
                        }
                    } else if (key.isReadable()) {
                        try {
                            read(key, selector);
                        } catch (IOException e) {
                            Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Error occurred while reading from socket in {0}", Thread.currentThread()));
                            throw e;
                        }
                    }
                }
            }
            System.out.println("Thread interrupted or nothing to select");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
        }
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

        byte[] message = messageGenerator.createMessage();

        int sentBytes = writeToChannel(channel, message);
        counter++;
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println(MessageFormat.format("[thread {0}]: sent {1} bytes (message #{2})", Thread.currentThread().getId(), sentBytes, counter));
//        Thread.sleep(Thread.currentThread().getId() * 20);
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