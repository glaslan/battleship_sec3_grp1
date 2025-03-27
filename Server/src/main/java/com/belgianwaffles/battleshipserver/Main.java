package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Critical line of code
        // Holds the whole program together
        System.out.println("Heyo!");

        Grid g = new Grid();
        ArrayList <Ship> shiplistp1;
        //ArrayList <Ship> shiplistp2;

        // CURRENT TESTING CODE
        // ik ik theyre all static this is just for testing
        shiplistp1 = GameManager.createAllP1Ships(g);

        System.out.println(g);

        System.out.println("Ships:\n\n");
        System.out.println("P1:");
        for (int i = 0; i < shiplistp1.size(); i++) {
            System.out.println(shiplistp1.get(i) + "\n");
            
        }
      

        // initialize logger
        FileLogger.initLogger();

        // Create server and check connections on separate thread
        ConnectionManager connection;
        try {
            connection = new ConnectionManager(ConnectionManager.DEFAULT_PORT);
            Thread connectThread = new Thread(connection);
            connectThread.setDaemon(true);
            connectThread.start();
        } catch (IOException e) {
            FileLogger.logError(Main.class, "main(String[])", "Could not create server socket");
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