package com.belgianwaffles.battleshipserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public final class ConnectionManager implements Runnable {

    // ----- Constants -----

    public static final int DEFAULT_PORT    = 27000;
    public static final int DEFAULT_TIMEOUT = 20000;



    // ----- Data -----

    private final ServerSocket mServer;
    private Socket mClient1, mClient2;
    private boolean mRunningServer;


    // ----- Methods -----

    /**
     * Sets up the server
     * @param port the port for the server to listen on
     * @throws IOException from any issues when creating server
     */
    public ConnectionManager(int port) throws IOException {
        this.mServer = new ServerSocket(port);
        this.mClient1 = null;
        this.mClient2 = null;
        this.mRunningServer = true;
    }

    /**
     * Closes the server and ends all currently running games
     * @return true if successful
     */
    public boolean close() {
        // Closes server sockets and ends all games in progress
        this.mRunningServer = false;
        GameManager.endAllGames();
        try {
            this.mServer.close();
            return true;
        } catch (IOException e) {
            FileLogger.logError(ConnectionManager.class, "close()", "Failed to close server");
            System.err.println("Failed to close server");
            return false;
        }
    }
    


    // ----- Threading -----
    
    /**
     * Main listening thread for the server
     */
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
                // Prevent error messages on server close
                if (!this.mRunningServer) {
                    break;
                }

                // Server not closed, something went wrong
                FileLogger.logError(ConnectionManager.class, "run()", 
                "Something went wrong when connecting clients");
                System.err.println("Something went wrong when connecting clients");
            }

            // Attempt to start game
            if (this.startGame(this.mClient1, this.mClient2)) {
                // Successfully started game
                this.mClient1 = null;
                this.mClient2 = null;
            }
            else {
                // Failed to start game
                System.out.println("Could not start game");

                // Check if client 2 disconnected
                if (!this.checkConnection(this.mClient2)) {
                    System.out.println("Disconnecting client 2");
                    this.mClient2 = null;
                }
                // Check if client 1 disconnected
                // Can move client 2 in always since both will be null anyway
                if (!this.checkConnection(this.mClient1)) {
                    System.out.println("Disconnecting client 1");
                    this.mClient1 = this.mClient2;
                }
            }
        }
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
        Packet packet = new Packet();
        packet.serialize();
        
        // Send ping
        if (!ConnectionManager.sendPacket(client, packet)) {
            return false;
        }
        // Create log of sent ping
        FileLogger.logPing(packet.toString());
        
        // Receive ping
        packet = ConnectionManager.receivePacket(client);
        if (packet == null) {
            return false;
        }
        
        // Check packet is ping
        if (packet.getType() != Packet.PACKET_TYPE_PING) {
            FileLogger.logError(ConnectionManager.class, "ping(Socket)", "Received invalid packet type");
            return false;
        }

        // Create log of received ping
        FileLogger.logPing(packet.toString());

        return true;
    }

    /**
     * Sends a packet to the specified client
     * @param client the client to send the packet to
     * @param packet the packet data to send
     */
    public static boolean sendPacket(Socket client, Packet packet) {
        try {
            var output = new DataOutputStream(client.getOutputStream());
            output.write(packet.getBuffer());
            return true;
        } catch (IOException | NullPointerException ex) {
            FileLogger.logError(ConnectionManager.class, "sendPacket(Packet, Socket)", "Failed to send packet");
            System.err.println("Failed to send packet");
            return false;
        }
    }

    /**
     * Awaits and receives a packet from the server
     * @return a serialized packet with data from the server
     */
    public static Packet receivePacket(Socket client) {
        try {
            // Get socket input
            InputStream input = new DataInputStream(client.getInputStream());
    
            // Read the head from the server
            Packet packet = new Packet();
            byte[] head = input.readNBytes(Packet.HEADER_SIZE);
            packet.deserialize(head);
            
            // Get the packet body
            byte[] body = input.readNBytes(packet.getLength() + Packet.PACKET_TAIL_SIZE);
            
            // Add all items to packet
            byte[] bytes = new byte[head.length + body.length];
            System.arraycopy(head, 0, bytes, 0, head.length);
            System.arraycopy(body, 0, bytes, head.length, body.length);

            // Pack into packet
            packet.deserialize(bytes);
            FileLogger.logMessage(packet.toString());

            // Return the packet
            return packet;
        } catch (IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }
}