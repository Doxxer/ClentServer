package ru.spbau.mit.networks.client;

import java.io.IOException;

public class WrongResponseException extends IOException {
    public WrongResponseException() {
    }

    public WrongResponseException(String message) {
        super(message);
    }

    public WrongResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongResponseException(Throwable cause) {
        super(cause);
    }
}
