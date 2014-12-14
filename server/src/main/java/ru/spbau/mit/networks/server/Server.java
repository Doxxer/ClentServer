package ru.spbau.mit.networks.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Logger;


public class Server {
    public static final int BUFFER_CAPACITY = 4096;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final ServerSocketChannel acceptor = ServerSocketChannel.open();
    private final Selector selector = Selector.open();
    private Map<SocketChannel, ResizableByteBuffer> receivedData = new HashMap<>();

    public Server(int port) throws IOException {
        acceptor.socket().bind(new InetSocketAddress(port));
        acceptor.configureBlocking(false);
        acceptor.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        while (true) {
            final int selected;
            try {
                selected = selector.select();
            } catch (IOException e) {
                logger.warning("Critical error: couldn't perform selection");
                return;
            }
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
                    processWritable();
                }
                iterator.remove();
            }
        }

    }

    private void processWritable() {
        System.err.println("Writable");
    }

    private void processReadable(SelectionKey key) {
        final SocketChannel channel;
        channel = (SocketChannel) key.channel();
        final ResizableByteBuffer resizableBuffer;
        resizableBuffer = receivedData.get(channel);
        assert resizableBuffer != null;

        while (true) {
            final int byteCount;
            try {
                byteCount = channel.read(buffer);
            } catch (IOException e) {
                logger.warning("Reading error: " + e.getMessage());
                closeSocket(channel.socket());
                receivedData.remove(channel);
                break;
            }
            if (byteCount < 0) {
                closeSocket(channel.socket());
                byte[] bytes = resizableBuffer.getDataCopy();
                for (byte b: bytes) {
                    System.out.print((char) b);
                }
                System.out.println();
                receivedData.remove(channel);
                break;
            }
            if (byteCount == 0) {
                break;
            }

            buffer.flip();
            resizableBuffer.moveBytes(buffer);
            buffer.flip();
        }

        buffer.clear();
    }

    private void processConnectable() {
        System.err.println("Connectable");
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
            receivedData.put(channel, new ResizableByteBuffer());
        } catch (IOException e) {
            logger.warning("Channel error: " + e.getMessage());
            closeSocket(socket);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ce) {
            logger.warning("Socket closing error " + ce.getMessage());
        }
    }
}
