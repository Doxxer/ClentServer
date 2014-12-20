package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client implements Runnable {
    public static final AtomicInteger totalSentMessages = new AtomicInteger(0);
    protected final int reportFrequency;
    protected final String hostname;
    protected final int port;
    protected final ServerAction connector;
    protected final ServerAction writer;
    protected final ServerAction reader;
    protected int counter;

    public Client(String host, int port, int messageSize, int reportFrequency) {
        this.port = port;
        this.hostname = host;
        this.reportFrequency = reportFrequency;
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
                runClient();
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage());
        }
    }

    protected abstract void runClient() throws IOException;
}
