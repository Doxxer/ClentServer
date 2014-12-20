package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServerAction {
    private final String actionName;
    protected long timestamp;
    protected int messageLength;
    private boolean actionFailedBefore;

    public ServerAction(String actionName) {
        this.actionFailedBefore = false;
        this.actionName = actionName;
        this.timestamp = 0;
    }

    public void makeAction(SocketChannel channel) throws ConnectException, WrongResponseException, InteractionException {
        try {
            makeSocketAction(channel);
            if (actionFailedBefore) {
                actionFailedBefore = false;
                Logger.getGlobal().log(Level.INFO, MessageFormat.format("[thread {0}]: {1} OK", Thread.currentThread().getId(), actionName));
            }
        } catch (WrongResponseException | ConnectException e) {
            throw e;
        } catch (IOException e) {
            if (!actionFailedBefore) {
                actionFailedBefore = true;
                Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: {1} FAILED: {2}",
                        Thread.currentThread().getId(), actionName, e.toString()));
            }
            throw new InteractionException(e);
        }
    }

    protected abstract void makeSocketAction(SocketChannel channel) throws IOException;

    public long getTimestamp() {
        return timestamp;
    }

    public int getMessageLength() {
        return messageLength;
    }
}
