package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

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
                try {
                    interactWithServer(channel);
                } catch (InteractionException ignored) {
                    break;
                }
            }
        }
    }

    private void interactWithServer(SocketChannel channel) throws WrongResponseException, ConnectException, InteractionException {
        writer.makeAction(channel);
        reader.makeAction(channel);
        totalSentMessages.incrementAndGet();
        totalTime.addAndGet(reader.timestamp - writer.timestamp);
        updateStatistics();
    }
}
