package com.nsu.burym;

import com.nsu.burym.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
            server.closeConnection();
        } catch (IOException e) {
            System.out.println("Can't create server socket: " + e.getMessage());
        }
    }
}
