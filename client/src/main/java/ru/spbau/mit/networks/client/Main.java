package ru.spbau.mit.networks.client;

public class Main {
//    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.valueOf(args[1]);

//        new Client("192.168.0.105", port).run();

        new Client(host, port).run();

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            synchronized (threads) {
//                threads.forEach(Thread::interrupt);
//            }
//        }));

//        for (int i = 0; i < 2; i++) {
//            Thread thread = new Thread(new Client("192.168.0.104", 8888));
//            threads.add(thread);
//            thread.start();
    }
}
