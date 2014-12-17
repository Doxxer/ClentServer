package ru.spbau.mit.networks.client;

public interface MessageGenerator {
    byte[] createRequest();

    void checkServerResponse(byte[] serverMessage);
}
