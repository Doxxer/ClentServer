package ru.spbau.mit.networks.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;


public class Server {
    public static final int MAX_WAITING_TIME = 100;
    public static final int BUFFER_CAPACITY = 4096;
    private static final Logger logger;

    static {
        logger = Logger.getLogger(Server.class.getName());
    }

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final ServerSocketChannel acceptor = ServerSocketChannel.open();
    private final Selector selector = Selector.open();
    private final DataHolder dataHolder = new DataHolder();
    private final ExecutorService executor;

    public Server(int port, ExecutorService executor)
            throws IOException {
        this.executor = executor;
        acceptor.socket().bind(new InetSocketAddress(port));
        acceptor.configureBlocking(false);
        acceptor.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void serve() throws IOException {
        while (true) {
            final int selected = selector.select(MAX_WAITING_TIME);

            processPerformedTasks();

            if (selected == 0) {
                continue;
            }

            final Set<SelectionKey> keys = selector.selectedKeys();
            final Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                final SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    processAcceptable();
                } else if (key.isConnectable()) {
                    processConnectable();
                } else if (key.isReadable()) {
                    processReadable(key);
                } else if (key.isWritable()) {
                    processWritable(key);
                }
                iterator.remove();
            }
        }

    }

    private void processWritable(SelectionKey key) {
        final SocketChannel channel;
        channel = (SocketChannel) key.channel();
        final ByteBuffer buffer = dataHolder.getWriterBuffer(channel);

        while (buffer.hasRemaining()) {
            final int byteCount;
            try {
                byteCount = channel.write(buffer);
            } catch (IOException e) {
                logger.warning("Writing error: " + e.getMessage());
                closeConnection(channel);
                return;
            }

            logger.fine("Sent " + byteCount + " bytes to " + channel);

            if (byteCount == 0) {
                break;
            }
        }

        if (!buffer.hasRemaining()) {
            logger.fine("Connection " + channel + " is closed");
            closeConnection(channel);
        }
    }

    private void processReadable(SelectionKey key) {
        final SocketChannel channel;
        channel = (SocketChannel) key.channel();

        while (true) {
            final int byteCount;
            try {
                byteCount = channel.read(buffer);
            } catch (IOException e) {
                logger.warning("Reading error: " + e.getMessage());
                closeConnection(channel);
                break;
            }
            if (byteCount < 0) {
                buffer.clear();
                closeConnection(channel);
                return;
            }
            if (byteCount == 0) {
                break;
            }

            buffer.flip();
            dataHolder.moveReceivedData(channel, buffer);
            buffer.flip();
        }
        buffer.clear();

        processReceivedData(channel);
    }

    private void processConnectable() {
        assert false;
    }

    private void processAcceptable() {
        final Socket socket;
        try {
            socket = acceptor.socket().accept();
        } catch (IOException e) {
            logger.warning("Couldn't accept connection: " + e.getMessage());
            return;
        }

        try {
            SocketChannel channel = socket.getChannel();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            dataHolder.registerReceiver(channel);
            logger.fine("Accepted " + channel);
        } catch (IOException e) {
            logger.warning("Channel error: " + e.getMessage());
            closeSocket(socket);
        }
    }

    private void processReceivedData(SocketChannel channel) {
        Integer required = dataHolder.getFirstReceivedInteger(channel);
        int received = dataHolder.getReceivedByteCount(channel);

        if (required == null || received < required) {
            return;
        }

        logger.fine(
                "Received " + received  + " bytes from " + channel
                + "; required " + required + " bytes");

        if (required + Integer.BYTES < received) {
            logger.warning("Received too much data from " + channel);
            closeConnection(channel);
            return;
        }

        try {
            channel.register(selector, 0);
        } catch (ClosedChannelException e) {
            logger.warning("Channel " + channel + " is unexpectedly closed");
            return;
        }

        executor.submit(new Worker(
                dataHolder.extractReceivedData(channel),
                dataHolder.createNotifier(channel, selector)));
    }

    private void processPerformedTasks() {
        while (true) {
            Pair<SocketChannel, byte[]> pair = dataHolder.getProcessedData();
            if (pair == null) {
                return;
            }
            final SocketChannel channel = pair.first;
            final byte[] data = pair.second;

            try {
                channel.register(selector, SelectionKey.OP_WRITE);
            } catch (ClosedChannelException e) {
                logger.warning(
                        "Channel " + channel + " is unexpectedly closed");
                continue;
            }

            logger.fine("Task for " + channel + "is performed");

            dataHolder.registerWriter(channel, data);
        }
    }

    private void closeConnection(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            logger.warning("Connection closing error " + e.getMessage());
        }
        dataHolder.unregisterReceiver(channel);
        dataHolder.unregisterWriter(channel);
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ce) {
            logger.warning("Socket closing error " + ce.getMessage());
        }
    }
}
