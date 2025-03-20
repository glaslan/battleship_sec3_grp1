package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager implements Runnable {
    
    // ----- Constants -----
    
    private static final int SLEEP_TIME = 5;



    // ----- Data -----
    
    private final Socket mClient1, mClient2;
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
    @SuppressWarnings("ConvertToTryWithResources")
    public void run() {
        // Setup game state
        this.startGame();
        
        // Create thread for pinging clients
        Runnable pingThread = () -> {
            if (!this.mGameOver && !sServerClosed) {
                GameManager.this.pingClients();
            }
        };
        // Ping every SLEEP_TIME seconds
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(pingThread, SLEEP_TIME, SLEEP_TIME, TimeUnit.SECONDS);

        // Main loop
        while (this.play()) {}
        
        // Actions for game end
        executor.close();
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

        // Close client1
        try {
            this.mClient1.close();
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to close clients on thread id=" + Thread.currentThread().threadId());
        }
        // Close client 2
        try {
            this.mClient2.close();
        } catch (IOException | NullPointerException e) {
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
