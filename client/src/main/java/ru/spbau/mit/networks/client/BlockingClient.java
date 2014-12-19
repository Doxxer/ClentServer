package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;

public class BlockingClient extends Client {

    public BlockingClient(String host, int port, int messageSize, int reportFrequency) {
        super(host, port, messageSize, reportFrequency);
    }

    @Override
    protected void runClient() throws IOException {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(hostname, port));
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            while (true) {
                if (!interactWithServer(channel)) {
                    break;
                }
            }
        }
    }

    private boolean interactWithServer(SocketChannel channel) throws WrongResponseException, ConnectException {
        int sentBytes = writer.makeAction(channel);
        if (sentBytes != -1) {
            counter++;
            totalSentMessages.incrementAndGet();
            if (counter % reportFrequency == 0) {
                System.out.println(MessageFormat.format("[thread {0}]: sent message #{1} ({2} bytes)", Thread.currentThread().getId(), counter, sentBytes));
            }
        } else {
            return false;
        }
        return reader.makeAction(channel) != -1;
    }
}
