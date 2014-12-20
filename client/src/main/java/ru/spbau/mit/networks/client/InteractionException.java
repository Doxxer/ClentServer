package ru.spbau.mit.networks.client;

import java.io.IOException;

public class InteractionException extends IOException {
    public InteractionException(IOException e) {
        super(e);
    }
}
