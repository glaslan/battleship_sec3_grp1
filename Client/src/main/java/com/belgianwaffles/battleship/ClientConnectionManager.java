package com.belgianwaffles.battleship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.belgianwaffles.battleship.Grid.GridCell;

public class ClientConnectionManager implements Runnable{

    public static final int DEFAULT_PORT    = 27000;
    public static final int DEFAULT_TIMEOUT = 3000;

    private GameWindow game;

    Socket connectionSocket;

    public ClientConnectionManager(GameWindow game) {
        this.game = game;

        // game.clearScreen();
    }

    @Override
    public void run() {
        
        try {
            connectionSocket = new Socket("localhost", DEFAULT_PORT);
            System.out.println("Connection Established");
        } catch (IOException e) {
            System.out.print("Switch to Bell");
            return;
        }

        while (true) {
            try {
                Packet packet = this.receivePacket();
                if (packet == null) {
                    continue;
                }

                // Determine what to do with the packet
                switch (packet.getType()) {
                    case Packet.PACKET_TYPE_PING -> this.pingServer();
                    case Packet.PACKET_TYPE_GRID -> this.getGridPacket(packet);
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private void getGridPacket(Packet packet) {

        Grid grid = packet.getGrid();
        boolean turn = packet.isTurn();

        System.out.println("Got the griddy");

        if (!game.isGameStarted()) {
            game.startGame();
        }
        game.setTurn(turn);
        GridCell[][] cells = grid.getCells();


        Grid newGrid = new Grid(cells);
        game.updatePlayerBoard(newGrid);
    }

    public void sendGridToServer(Grid grid) throws IOException {
        Packet packet = new Packet();
        packet.serialize(grid);

        var output = new DataOutputStream(connectionSocket.getOutputStream());
        output.write(packet.getBuffer());
    }
    
    /**
     * Awaits and receives a packet from the server
     * @return a serialized packet with data from the server
     */
    private Packet receivePacket() {
        try {
            // Get socket input
            InputStream input = new DataInputStream(connectionSocket.getInputStream());
    
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

            // Return the packet
            return packet;
        } catch (IOException e) {
            return null;
        }
    }

    public void pingServer() throws IOException {

        Packet packet = new Packet();
        packet.serialize();

        var output = new DataOutputStream(connectionSocket.getOutputStream());
        output.write(packet.getBuffer());
    }
}
