package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client implements Runnable {
    public static final AtomicInteger totalSentMessages = new AtomicInteger(0);
    public static final AtomicLong totalTime = new AtomicLong(0);
    protected final String hostname;
    protected final int port;
    protected final ServerAction connector;
    protected final ServerAction writer;
    protected final ServerAction reader;
    private final int reportFrequency;
    private int localCounter;

    public Client(String host, int port, int messageSize, int reportFrequency) {
        this.port = port;
        this.hostname = host;
        this.reportFrequency = reportFrequency;
        this.localCounter = 0;

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

    protected void updateStatistics() {
        localCounter++;
        totalSentMessages.incrementAndGet();
        totalTime.addAndGet(reader.timestamp - writer.timestamp);
        if (reportFrequency != 0 && localCounter % reportFrequency == 0) {
            System.out.println(MessageFormat.format("[thread {0}]: sent message #{1} ({2} bytes)",
                    Thread.currentThread().getId(), localCounter, writer.getMessageLength()));
        }
    }
}
