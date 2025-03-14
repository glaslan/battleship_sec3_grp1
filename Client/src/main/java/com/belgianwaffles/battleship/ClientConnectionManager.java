package com.belgianwaffles.battleship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.belgianwaffles.battleship.DataPacket.Header;

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
                byte[] head = new byte[Header.HEADER_SIZE];
            
                // Read header
                if (input.read(head, 0, head.length) == -1) {
                    System.out.println("No connection");
                    break;
                }

                // Create packet and check for ping
                DataPacket rec = new DataPacket(head);
                if (rec.getType() == DataPacket.PACKET_TYPE_PING) {
                    pingServer();
                    byte[] clear = new byte[1000];
                    input.read(clear, 0, clear.length);
                    continue;
                }
                System.out.println("How");
                
                // Read body and tail
                byte[] body = new byte[rec.getLength()];
                if (input.read(head, 0, head.length) == -1) {
                    continue;
                }
            } catch (IOException e) {
                break;
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
