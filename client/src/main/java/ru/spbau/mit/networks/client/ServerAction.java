package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServerAction {
    protected final int nextSocketState;
    protected final int failingSocketState;
    private final String actionName;
    private boolean actionFailed;

    public ServerAction(String actionName, int nextSocketState, int failingSocketState) {
        this.actionFailed = false;
        this.actionName = actionName;
        this.nextSocketState = nextSocketState;
        this.failingSocketState = 0;
    }

    public int makeAction(SelectionKey key, Selector selector) {
        try {
            int result = makeSocketAction(key, selector);
            if (actionFailed) {
                actionFailed = false;
                Logger.getGlobal().log(Level.INFO, MessageFormat.format("[thread {0}]: {1} OK", Thread.currentThread().getId(), actionName));
            }
            return result;
        } catch (IOException e) {
            if (!actionFailed) {
                actionFailed = true;
                Logger.getGlobal().log(Level.WARNING, MessageFormat.format("[thread {0}]: {1} FAILED: {2}",
                        Thread.currentThread().getId(), actionName, e.toString()));
            }
        }
        return -1;
    }

    protected abstract int makeSocketAction(SelectionKey key, Selector selector) throws IOException;
}
