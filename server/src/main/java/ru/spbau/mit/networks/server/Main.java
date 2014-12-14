package ru.spbau.mit.networks.server;

import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                printMessageAndExit("Required argument: port");
            }
            final int port = Integer.parseInt(args[0]);
            new Server(port).run();
        } catch (NumberFormatException e) {
            printMessageAndExit("Port must be an integer");
        } catch (IOException e) {
            printMessageAndExit("Error: " + e.getMessage());
        }
    }

    private static void printMessageAndExit(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
