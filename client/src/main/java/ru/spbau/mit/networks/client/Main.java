package ru.spbau.mit.networks.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        // socat tcp-l:8888,fork exec:'/bin/cat'

        String host = args[0];
        int port = Integer.valueOf(args[1]);
        int threadsCount = Integer.valueOf(args[2]);
        int messageSize = Integer.valueOf(args[3]);
        int reportFrequency = Integer.valueOf(args[4]);
        String clientType = args[5];

        Runtime.getRuntime().addShutdownHook(new Thread(() -> threads.forEach(Thread::interrupt)));

        for (int i = 0; i < threadsCount; i++) {
            Client client = Objects.equals(clientType, "b") ?
                    new BlockingClient(host, port, messageSize, reportFrequency) :
                    new NonBlockingClient(host, port, messageSize, reportFrequency);
            Thread thread = new Thread(client);
            threads.add(thread);
            thread.start();
        }

        Thread threadManager = new Thread(() -> {
            int oldSent = Client.totalSentMessages.get();
            int tick = 0;
            while (!Thread.interrupted()) {
                int totalMessages = Client.totalSentMessages.get();
                int mps = tick == 0 ? 0 : totalMessages / tick;
                long totalTime = Client.totalTime.get();
                double awt = totalMessages == 0 ? 0 : ((totalTime / 1000) / 1000.0) / totalMessages;

                System.out.print(MessageFormat.format("Total messages = {0} | Total time wasted = {1} ns | ", totalMessages, totalTime));
                System.out.print(MessageFormat.format("MPS: {0} (last second = {1}) | ", mps, totalMessages - oldSent));
                System.out.print(MessageFormat.format("AWT: {0} ms\n", awt));

                tick += 1;
                oldSent = totalMessages;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        threadManager.setDaemon(true);
        threads.add(threadManager);
        threadManager.start();
    }
}