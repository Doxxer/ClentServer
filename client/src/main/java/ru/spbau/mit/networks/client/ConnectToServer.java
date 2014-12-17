package ru.spbau.mit.networks.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ConnectToServer extends ServerAction {

    public ConnectToServer(String actionName, int nextSocketState) {
        super(actionName, nextSocketState);
    }

    @Override
    protected int makeSocketAction(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.register(selector, nextSocketState);
        return 0;
    }
}
