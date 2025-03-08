package com.belgianwaffles.battleshipserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    // main simply does not understand what im doing
    // suppressing its warnings
    @SuppressWarnings({ "unused", "null" })
    public static void main(String[] args) {
        final int gridLength = 10;
        System.out.println("Hello World!");
        GridCell [][] boardMatrix = new GridCell[gridLength][gridLength];
        DataPacket recievingP1Packet = null;
        DataPacket recievingP2Packet = null;
        DataPacket sendingPacketP1 = null;
        DataPacket sendingPacketP2 = null;
        ServerSocket serverP1Socket;
        ServerSocket serverP2Socket;
        Socket clientP1Socket;
        Socket clientP2Socket;

        while(true) {
            try {
                serverP1Socket = new ServerSocket(27000);
                System.out.println("Server socket P1 created");
                serverP2Socket = new ServerSocket(27001);
                System.out.println("Server socket P2 created");

                // accept client socket connection
                clientP1Socket = serverP1Socket.accept();
                System.out.println("Client P1 connected");

                clientP2Socket = serverP2Socket.accept();
                System.out.println("Client P2 connected");

                // setup input and output streams
                BufferedReader inP1 = new BufferedReader(new InputStreamReader(clientP1Socket.getInputStream()));
                PrintWriter outP1 = new PrintWriter(clientP1Socket.getOutputStream(), true);

                BufferedReader inP2 = new BufferedReader(new InputStreamReader(clientP2Socket.getInputStream()));
                PrintWriter outP2 = new PrintWriter(clientP2Socket.getOutputStream(), true);

                // Use this as an initial setup phase to figure out p1/p2 turn order and ship setup?


                // get message from P1 client
                String p1Message = inP1.readLine();
                System.out.println("Client says: " + p1Message);
                recievingP1Packet = new DataPacket(p1Message);

                // get message from P2 client
                String p2Message = inP2.readLine();
                System.out.println("Client says: " + p2Message);
                recievingP2Packet = new DataPacket(p2Message);

                // do some stuff with sending packets to prep it for sending a response
                // not sure if we've decided on any specific values for flags yet so
                // im hesitant to do this myself
                // ex:
                // sendingPacketP1.head.setItemSent(someVal);
                // sendingPacketP2.head.setItemSent(someVal);
                // sendingPacketP1.head.setTurn(someVal);
                // sendingPacketP1.setBoardMatrix(boardMatrix);
                // etc


                // send packet to client
                outP1.println(sendingPacketP1.getBuffer());
                outP2.println(sendingPacketP2.getBuffer());

                // main gameplay loop(?)
                boolean exitCondition = true;
                while (exitCondition) {
                    // play game

                }

                clientP1Socket.close();
                serverP1Socket.close();
                clientP2Socket.close();
                serverP2Socket.close();
            } catch (IOException ex) {
                System.out.println("An error occured while communicating with client");
            }
        }
    }
}
