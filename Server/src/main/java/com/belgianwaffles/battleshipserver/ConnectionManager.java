package com.belgianwaffles.battleshipserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ConnectionManager implements Runnable {

    // ----- Constants -----

    public static final int DEFAULT_PORT    = 27000;
    public static final int DEFAULT_TIMEOUT = 3000;



    // ----- Data -----

    private ServerSocket mServer;
    private Socket mClient1, mClient2;
    private boolean mRunningServer;



    // ----- Methods -----

    public ConnectionManager(int port) throws IOException {
        this.mServer = new ServerSocket(port);
        this.mClient1 = null;
        this.mClient2 = null;
        this.mRunningServer = true;
    }

    public void close() {
        // Closes server sockets and ends all games in progress
        this.mRunningServer = false;
        GameManager.endAllGames();
        try {
            this.mServer.close();
        } catch (IOException e) {
            System.err.println("Failed to close server");
        }
    }
    


    // ----- Threading -----
    
    @Override
    public void run() {
        System.out.println("Server is awaiting connections");
        while (this.mRunningServer) {
            Socket newClient;
            try {
                newClient = this.mServer.accept();
                newClient.setSoTimeout(DEFAULT_TIMEOUT);
                
                // Choose which client this is
                if (!this.checkConnection(this.mClient1)) {
                    System.out.println("Client 1 connected");
                    this.mClient1 = newClient;
                }
                else {
                    System.out.println("Client 2 connected");
                    this.mClient2 = newClient;
                }
            }
            catch (IOException e) {
                System.err.println("Something went wrong when connecting clients");
            }

            // Attempt to start game
            if (this.startGame(this.mClient1, this.mClient2)) {
                this.mClient1 = null;
                this.mClient2 = null;
            }
        }
    }

    // Ensures a client is still connected
    private boolean checkConnection(Socket client) {
        // If no client
        if (client == null) {
            return false;
        }
        
        return ping(client);
    }
    
    public static boolean ping(Socket client) {
        // Prepare ping packet
        DataPacket packet = new DataPacket();
        packet.serializeData();

        try {
            var output = new DataOutputStream(client.getOutputStream());
            output.write(packet.getBuffer());
            var input = new DataInputStream(client.getInputStream());
            byte[] received = new byte[DataPacket.Header.HEADER_SIZE];
            
            if (input.read(received, 0, DataPacket.Header.HEADER_SIZE) == -1) {
                System.out.println("Client was disconnected");
                return false;
            }
        }
        catch (IOException e) {
            System.err.println("Failed to ping client");
            return false;
        }
        return true;
    }

    private boolean startGame(Socket client1, Socket client2) {
        // Check state of client 1
        if (this.checkConnection(client1) && this.checkConnection(client2)) {
            // Create thread for game to run on
            GameManager gm = new GameManager(client1, client2);
            Thread gameThread = new Thread(gm);
            gameThread.start();
            return true;
        }
        return false;
    }
}
