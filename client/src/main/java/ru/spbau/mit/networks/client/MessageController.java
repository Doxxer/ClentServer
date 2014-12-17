package ru.spbau.mit.networks.client;

import java.io.IOException;

public interface MessageController {
    byte[] createRequest();

    void checkServerResponse(byte[] serverMessage) throws IOException;
}
