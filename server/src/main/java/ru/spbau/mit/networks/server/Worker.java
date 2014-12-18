package ru.spbau.mit.networks.server;


public class Worker implements Runnable {
    private final byte[] data;
    private final ServerNotifier notifier;

    public Worker(byte[] data, ServerNotifier notifier) {
        this.data = data;
        this.notifier = notifier;
    }

    @Override
    public void run() {
        notifier.notifyServer(data);
    }
}
