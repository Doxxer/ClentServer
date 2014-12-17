package ru.spbau.mit.networks.server;

import java.util.concurrent.Callable;


public class Worker implements Callable<byte[]> {
    private final byte[] data;
    private final ServerNotifier notifier;

    public Worker(byte[] data, ServerNotifier notifier) {
        this.data = data;
        this.notifier = notifier;
    }

    @Override
    public byte[] call() throws Exception {
        notifier.notifyServer();
        return data;
    }
}
