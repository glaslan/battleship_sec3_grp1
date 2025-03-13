package com.belgianwaffles.battleshipserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

    public boolean close() {
        // Closes server sockets and ends all games in progress
        this.mRunningServer = false;
        GameManager.endAllGames();
        try {
            this.mServer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to close server");
            return false;
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
                // Failed to start game

                // Check if client 2 disconnected
                if (!this.checkConnection(this.mClient2)) {
                    this.mClient2 = null;
                }
                // Check if client 1 disconnected
                // Can move client 2 in always since both will be null anyway
                if (!this.checkConnection(this.mClient1)) {
                    this.mClient1 = this.mClient2;
                }
            }
        }
    }

    /**
     * Ensures a client is still connected to the server
     * @param client <code>Socket</code> to send ping to
     * @return <code>boolean</code> <code>true</code> if connection is still live
     */
    private boolean checkConnection(Socket client) {
        // If no client
        if (client == null) {
            return false;
        }
        
        return ping(client);
    }
    
    /**
     * Creates a ping packet and sends it to the clients.
     * Waits for a ping packet to be sent back within DEFAULT_TIMEOUT
     * @param client <code>Socket</code> to send ping to
     */
    public static boolean ping(Socket client) {
        // Prepare ping packet
        DataPacket packet = new DataPacket();
        packet.serialize();

        // Send ping
        try {
            var output = new DataOutputStream(client.getOutputStream());
            output.write(packet.getBuffer());
            var input = new DataInputStream(client.getInputStream());

            // create log of sent message
            Log.log(Log.LOG_PING, packet.toString());
            
            // Read ping from client
            byte[] received = new byte[DataPacket.HEADER_SIZE];
            if (input.read(received, 0, received.length) == -1) {
                System.out.println("Client was disconnected");
                return false;
            }

            // Deserialize the packet
            packet.deserialize(received);

            // Create log of received message
            Log.log(Log.LOG_PING, packet.toString());
        }
        catch (SocketTimeoutException e) {
            System.err.println("Failed to ping socket in time");
            return false;
        }
        catch (IOException e) {
            System.err.println("Failed to ping client");
            return false;
        }
        catch (NullPointerException e) {
            System.err.println("No client");
            return false;
        }
        return true;
    }

    /**
     * Attempts to start a game with the 2 given client <code>Socket</code> classes
     * @param client1 <code>Socket</code> of the first client
     * @param client2 <code>Socket</code> of the second client
     * @return <code>boolean</code> returns <code>true</code> when a thread for a new game has been created. 
     * Returns false if either of the clients have disconnected from the server
     */
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
