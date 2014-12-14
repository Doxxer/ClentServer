package ru.spbau.mit.networks.client;

import java.util.ArrayList;

public class Main {
    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        // socat -v tcp-l:8888,fork exec:'/bin/cat'

        String host = args[0];
        int port = Integer.valueOf(args[1]);
        int threadsCount = Integer.valueOf(args[2]);

//        new Client(host, port).run();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (threads) {
                threads.forEach(Thread::interrupt);
            }
        }));
        for (int i = 0; i < threadsCount; i++) {
            Thread thread = new Thread(new Client("192.168.0.104", 8888));
            threads.add(thread);
            thread.start();
        }
    }
}