package ru.spbau.mit.networks.server;

import java.nio.channels.SocketChannel;


public class WorkerException extends Exception {
    private final SocketChannel channel;

    public WorkerException(SocketChannel channel, Exception exception) {
        super(exception);
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
