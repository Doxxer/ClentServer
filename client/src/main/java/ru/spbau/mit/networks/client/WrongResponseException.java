package ru.spbau.mit.networks.client;

import java.io.IOException;

public class WrongResponseException extends IOException {

    public WrongResponseException(String message) {
        super(message);
    }

}
