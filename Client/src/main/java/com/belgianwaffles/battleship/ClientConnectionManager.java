package com.belgianwaffles.battleship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                var input = new DataInputStream(connectionSocket.getInputStream());
                byte[] received =  input.readAllBytes();

                DataPacket rec = new DataPacket(received);
                if (input.read(received, 0, DataPacket.Header.HEADER_SIZE) == -1) {
                    System.out.println("No connection");
                    return;
                }

                if (rec.getType() == DataPacket.PACKET_TYPE_PING) {
                    pingServer();
                }

            } catch (IOException e) {
                return;
            }
            
        }

        
            
            
    }

    public void pingServer() throws IOException {

        DataPacket packet = new DataPacket();
        packet.serializeData();

        var output = new DataOutputStream(connectionSocket.getOutputStream());
        output.write(packet.getBuffer());

    }

    
    
}
