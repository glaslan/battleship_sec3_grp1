package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class GameManager implements Runnable {
    
    // ----- Constants -----
    
    private static int SLEEP_TIME = 5000;



    // ----- Data -----
    
    private Socket mClient1, mClient2;
    private boolean mGameOver;

    private static boolean sServerClosed;
    static {
        sServerClosed = false;
    }



    // ----- Methods -----
    
    public GameManager(Socket s1, Socket s2) {
        this.mClient1 = s1;
        this.mClient2 = s2;
        this.mGameOver = false;
    }
    
    
    
    // ----- Threading -----
    
    @Override
    public void run() {
        System.out.println("Starting new game");
        // Setup game state
        this.startGame();
        
        // Create thread for pinging clients
        Runnable pingThread = () -> {
            while (!this.mGameOver && !sServerClosed) {
                try {
                    Thread.sleep(SLEEP_TIME);
                    GameManager.this.pingClients();
                } catch (InterruptedException e) {
                    FileLogger.logError(GameManager.class, "run()", 
            "Thread interrupted");
                    System.err.println("Thread interrupted");
                }
            }
        };
        Thread t = new Thread(pingThread);
        t.setDaemon(true);
        t.start();

        // Main loop
        while (this.play()) {}
        
        // Actions for game end
        this.endGame();
        System.out.println("Closed thread id=" + Thread.currentThread().threadId());
    }
    
    
    
    // ----- Game ----- Methods -----
    
    private int getShipsRemaining(Grid grid, int player) {
        Grid.GridCell[][] matrix = grid.getCells();
        int shipsLeft = 0;
        if (player == 1){
            for (int i = 0; i < Grid.GRID_SIZE; i++) {
                for (int j = 0; j < Grid.GRID_SIZE; j++) {
                    if (matrix[i][j].hasShipP1() && !(matrix[i][j].hasShotP2())) {
                        shipsLeft++;
                    }
                }
            }
        }
        else if (player == 2){
            for (int i = 0; i < Grid.GRID_SIZE; i++) {
                for (int j = 0; j < Grid.GRID_SIZE; j++) {
                    if (matrix[i][j].hasShipP2() && !(matrix[i][j].hasShotP1())) {
                        shipsLeft++;
                    }
                }
            }
        } 
        else {
            System.out.println("Invalid usage of this function");
            return -1;
        }
        return shipsLeft;
    }

    private Grid generateSugarSharks(Grid grid) {
        // odds of sugar shark = 1/chance
        int chance = 10;
        Random rng = new Random(System.currentTimeMillis());
        for (int i = 0; i < Grid.GRID_SIZE; i++) {
            for (int j = 0; j < Grid.GRID_SIZE; j++) {
                // clear current sharks
                grid.getCells()[i][j].setSharkP1(false);
                grid.getCells()[i][j].setSharkP2(false);
                // P1 sharks
                // currently my logic is P1 shark means its a shark P1 can see
                if (!grid.getCells()[i][j].hasShipP2() && !grid.getCells()[i][j].hasShotP1()) {
                    grid.getCells()[i][j].setSharkP1((rng.nextInt(chance) == chance-1));
                }
                // P2 sharks
                if (!grid.getCells()[i][j].hasShipP1() && !grid.getCells()[i][j].hasShotP2()) {
                    grid.getCells()[i][j].setSharkP2((rng.nextInt(chance) == chance-1));
                }
            }
        }
        return grid;
    }


    private void sendPacket(Socket client, Packet packet) {
        ConnectionManager.sendPacket(client, packet);
    }

    private Packet receivePacket(Socket client) {
        return ConnectionManager.receivePacket(client);
    }


    // ----- Start ----- End -----
    
    private void startGame() {
        System.out.println("Starting game on thread id=" + Thread.currentThread().threadId());
        // Setup game states
        Packet packet = new Packet();
        Grid grid = new Grid();
        packet.serialize(grid);
        // add/replace with flag: setupShips???
        packet.addTurn(Packet.PACKET_TURN_PONE);
        // temp fix to flag which user is which
        packet.addUser((short) 1);
        sendPacket(mClient1, packet);
        // response from client
        // does server need to maintain a copy of boards?
        // also i think the clients need to know which is p1 and which is p2
        packet = receivePacket(mClient1);
        packet.addUser((short) 2);
        sendPacket(mClient2, packet);
        packet = receivePacket(mClient2);


        /* 
        // enter main game loop?
        // my thoughts for the main loop functionality

        // this should not pass by reference because of how get grid returns? java isnt a real language
        packet.serialize(generateSugarSharks(packet.getGrid()));
        // p1 turn
        packet.addTurn(Packet.PACKET_TURN_PONE);
        packet.addUser((short) 1);
        sendPacket(mClient1, packet);
        packet = receivePacket(mClient1);
        if (getShipsRemaining(packet.getGrid(), 2) == 0) {
            //p1 win
        }
        //p2 turn
        packet.addTurn(Packet.PACKET_TURN_PTWO);
        packet.addUser((short) 2);
        sendPacket(mClient2, packet);
        packet = receivePacket(mClient2);
        if (getShipsRemaining(packet.getGrid(), 1) == 0) {
            //p2 win
        }

        */
        
    }
    
    private void endGame() {
        System.out.println("Ending game on thread id=" + Thread.currentThread().threadId());

        // Close client1
        try {
            this.mClient1.close();
        } catch (IOException e) {
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
        catch (NullPointerException e) {
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
        // Close client 2
        try {
            this.mClient2.close();
        } catch (IOException e) {
            FileLogger.logError(GameManager.class, "endGame()", 
            "Failed to close clients on thread id=" + Thread.currentThread().threadId());
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
        catch (NullPointerException e) {
            FileLogger.logError(GameManager.class, "endGame()", 
            "Failed to close clients on thread id=" + Thread.currentThread().threadId());
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
    }

    public static synchronized void endAllGames() {
        sServerClosed = true;
    }



    // ----- Read -----

    private synchronized boolean play() {
        return (!this.mGameOver && !sServerClosed);
    }

    private synchronized void pingClients() {
        // If a client has disconnected, end game
        if (!ConnectionManager.ping(mClient1) || !ConnectionManager.ping(mClient2)) {
            FileLogger.logError(GameManager.class, "pingClients()", 
            "Failed to ping a client on thread=" + Thread.currentThread().threadId());
            System.err.println("Failed to ping a client on thread=" + Thread.currentThread().threadId());
            this.mGameOver = true;
        }
    }
    
    

    // ----- Update -----
}
