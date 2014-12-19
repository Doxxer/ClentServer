package ru.spbau.mit.networks.client;

import java.text.MessageFormat;
import java.util.ArrayList;

public class Main {
    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        // socat -v tcp-l:8888,fork exec:'/bin/cat'

        String host = args[0];
        int port = Integer.valueOf(args[1]);
        int threadsCount = Integer.valueOf(args[2]);
        int matrixSize = Integer.valueOf(args[3]);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> threads.forEach(Thread::interrupt)));

        for (int i = 0; i < threadsCount; i++) {
            Thread thread = new Thread(new BlockingClient(host, port, matrixSize));
            threads.add(thread);
            thread.start();
        }

        Thread totalMessagesSent = new Thread(() -> {
            int oldSent = Client.totalSentMessages.get();
            while (!Thread.interrupted()) {
                int newSent = Client.totalSentMessages.get();
                System.out.println(MessageFormat.format("MPS: {0}", newSent - oldSent));
                oldSent = newSent;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        totalMessagesSent.setDaemon(true);
        threads.add(totalMessagesSent);
        totalMessagesSent.start();
    }
}