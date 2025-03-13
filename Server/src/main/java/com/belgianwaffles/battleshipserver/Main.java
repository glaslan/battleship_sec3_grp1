package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Critical line of code
        // Holds the whole program together
        System.out.println("Heyo!");
        
        // Create server and check connections on separate thread
        ConnectionManager connection;
        try {
            connection = new ConnectionManager(ConnectionManager.DEFAULT_PORT);
            Thread connectThread = new Thread(connection);
            connectThread.setDaemon(true);
            connectThread.start();
        } catch (IOException e) {
            System.err.println("Could not create server socket");
            return;
        }

        // Allows server to properly close with input
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("\nType anything to end server");
            input.nextLine();
            
            System.out.println("Exiting");
        }

        // Close connection socket
        if (connection.close()) {
            System.out.println("Successfully closed the server!");
        }
    }
}
