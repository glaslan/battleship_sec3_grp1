package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;

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
                    System.err.println("Thread interrupted");
                }
            }
        };
        Thread t = new Thread(pingThread);
        t.setDaemon(true);
        t.start();

        // Main loop
        while (!this.mGameOver && !sServerClosed) {
            try {
                // This is a load bearring sleep statement
                // I do not know why, but removing this sleep causes the game to not end
                // For the love of all that is holy, DO NOT DELETE THIS SLEEP UNLESS YOU ARE PREPARED TO SUFFER
                Thread.sleep(0);
            } catch (InterruptedException e) {
            }
        }
        
        // Actions for game end
        this.endGame();
        System.out.println("Closed thread id=" + Thread.currentThread().threadId());
    }
    
    
    
    // ----- Game ----- Methods -----
    
    // ----- Start ----- End -----
    
    private void startGame() {
        System.out.println("Starting game on thread id=" + Thread.currentThread().threadId());
        // Setup game states
    }
    
    private void endGame() {
        System.out.println("Ending game on thread id=" + Thread.currentThread().threadId());

        // Close connections and end game
        try {
            this.mClient1.close();
            this.mClient2.close();
        } catch (IOException e) {
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
        catch (NullPointerException e) {
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
    }

    public static void endAllGames() {
        sServerClosed = true;
    }



    // ----- Read -----

    private void pingClients() {
        // If a client has disconnected, end game
        if (!ConnectionManager.ping(mClient1) || !ConnectionManager.ping(mClient2)) {
            System.err.println("Failed to ping a client on thread=" + Thread.currentThread().threadId());
            this.mGameOver = true;
        }
    }
    
    

    // ----- Update -----
}
