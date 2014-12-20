package ru.spbau.mit.networks.client;

import java.io.IOException;

public interface MessageController {
    byte[] createRequest();

    void validateServerResponse(byte[] serverMessage) throws IOException;
}
