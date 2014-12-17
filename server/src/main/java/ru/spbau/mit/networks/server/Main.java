package ru.spbau.mit.networks.server;

import java.io.IOException;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                printMessageAndExit(
                        "Required arguments: port, number of threads");
            }
            final int port = Integer.parseInt(args[0]);
            final int nThread = Integer.parseInt(args[1]);
            new Server(port, Executors.newFixedThreadPool(nThread)).serve();
        } catch (NumberFormatException e) {
            printMessageAndExit("Couldn't parse arguments");
        } catch (IOException e) {
            printMessageAndExit("Error: " + e.getMessage());
        }
    }

    private static void printMessageAndExit(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
