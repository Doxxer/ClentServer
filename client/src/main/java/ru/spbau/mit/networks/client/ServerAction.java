package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServerAction {
    private final String actionName;
    private boolean actionFailed;

    public ServerAction(String actionName) {
        this.actionFailed = false;
        this.actionName = actionName;
    }

    public int makeAction(SocketChannel socketChannel) throws ConnectException, WrongResponseException {
        try {
            int result = makeSocketAction(socketChannel);
            if (actionFailed) {
                actionFailed = false;
                Logger.getGlobal().log(Level.INFO, MessageFormat.format("[thread {0}]: {1} OK", Thread.currentThread().getId(), actionName));
            }
            return result;
        } catch (WrongResponseException | ConnectException e) {
            throw e;
        } catch (IOException e) {
            if (!actionFailed) {
                actionFailed = true;
                Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: {1} FAILED: {2}",
                        Thread.currentThread().getId(), actionName, e.toString()));
            }
        }
        return -1;
    }

    protected abstract int makeSocketAction(SocketChannel channel) throws IOException;
}
