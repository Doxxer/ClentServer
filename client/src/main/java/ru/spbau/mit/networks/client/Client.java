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
    private int counter;

    public Client(String host, int port) {
        this.hostname = host;
        this.port = port;
        this.counter = 0;
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

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        System.out.println(MessageFormat.format("{0}: connected to server", Thread.currentThread()));
                        try {
                            connect(key, selector);
                        } catch (IOException e) {
                            Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Error occurred while connecting to socket in {0}", Thread.currentThread()));
                            throw e;
                        }
                    }

                    if (key.isWritable()) {
                        try {
                            write(key);
                        } catch (IOException e) {
                            Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Error occurred while writing to socket in {0}", Thread.currentThread()));
                            throw e;
                        }
                    }
                }
            }
            System.out.println("Thread interrupted or nothing to select");
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = createMessage();
        int total = 0;
        while (buffer.hasRemaining()) {
            total += channel.write(buffer);
            System.out.println(MessageFormat.format("{0}: sent {1}/{2} bytes ahead ({3})",
                    Thread.currentThread(), total, buffer.capacity(), counter));
        }
        counter++;
        buffer.clear();
    }

    private ByteBuffer createMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("0");
        }
        return ByteBuffer.wrap(sb.toString().getBytes());
    }

    private void connect(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }
}