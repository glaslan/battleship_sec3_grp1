package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;

public class GameManager implements Runnable {
    
    // ----- Constants -----
    
    private static int SLEEP_TIME = 10000;



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
            while (!sServerClosed) {
                try {
                    Thread.sleep(SLEEP_TIME);
                    GameManager.this.pingClients();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted");
                }
            }
        };
        new Thread(pingThread).start();

        // Main loop
        while (!this.mGameOver && !sServerClosed) {
        }
        
        // Actions for game end
        this.endGame();
    }
    
    
    
    // ----- Game ----- Methods -----
    
    // ----- Start ----- End -----
    
    private void startGame() {
        // Setup game states
    }
    
    private void endGame() {
        System.out.println("Ending game");
        // Close connections and end game
        try {
            this.mClient1.close();
            this.mClient2.close();
        } catch (IOException e) {
            System.err.println("Failed to close clients");
        }
    }

    public static void endAllGames() {
        sServerClosed = true;
    }



    // ----- Read -----

    private void pingClients() {
        System.out.println("Pinging");
        // If a client has disconnected, end game
        if (!ConnectionManager.ping(mClient1) || !ConnectionManager.ping(mClient2)) {
            System.err.println("Failed to ping a client");
            this.mGameOver = true;
        }
    }
    
    

    // ----- Update -----
}
