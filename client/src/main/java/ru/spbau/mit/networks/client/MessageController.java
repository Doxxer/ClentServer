package ru.spbau.mit.networks.client;

public interface MessageController {
    byte[] createRequest();

    void checkServerResponse(byte[] serverMessage);
}
