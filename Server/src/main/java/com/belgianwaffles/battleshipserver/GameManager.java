package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager implements Runnable {
    
    // ----- Constants -----
    
    private static final int SLEEP_TIME = 5;



    // ----- Data -----
    
    private final Socket mClient1, mClient2;
    private final Grid mGrid;

    // For easy swapping
    private Socket mCurrentSocket;

    // Ending games
    private boolean mGameOver;
    
    
    
        private boolean mCurrentPlayerIsOne;
        private static boolean sServerClosed;
        static {
            sServerClosed = false;
        }
    
    
    
        // ----- Methods -----
        
        public GameManager(Socket s1, Socket s2) {
            this.mClient1 = s1;
            this.mClient2 = s2;
            this.mGameOver = false;
            this.mCurrentSocket = this.mClient1;
            this.mGrid = new Grid();
            this.mCurrentPlayerIsOne = true;
        }
        
        
        
        // ----- Threading -----
        
        @Override
        @SuppressWarnings("ConvertToTryWithResources")
        public void run() {
            // Setup game state
            this.startGame();
    
            // Check game isnt already over
            if (this.mGameOver) {
                this.endGame();
                System.out.println("Closed thread id=" + Thread.currentThread().threadId());
            }
    
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
            while (this.play()) {
                // Receive packet from player
                Packet received = ConnectionManager.receivePacket(this.mCurrentSocket);
                if (received == null) {
                    continue;
                }
    
                // Check packet data
                if (received.getType() == Packet.PACKET_TYPE_GRID) {
                    Grid grid = received.getGrid();
                    if (this.mGrid.checkDifferences(grid) != 1) {
                        System.err.println("Too many grid changes received");
                        continue;
                    }
    
                    // Update grid
                    if (!this.mCurrentPlayerIsOne) {
                        grid.translateP1toP2();
                    }
                    this.mGrid.combine(this.mGrid, grid);
                    this.generateSugarSharks(mGrid);
                }
    
                // Do some stuff with sendsing
                
                // Swap players
                this.swapPlayers();

                // Checks if there are ships remaining
                if (this.getShipsRemaining() <= 0) {
                    this.mGameOver = true;
                }
            }
            
            // Actions for game end
            executor.close();
            this.endGame();
            System.out.println("Closed thread id=" + Thread.currentThread().threadId());
        }
        
        
        
        // ----- Game ----- Methods -----
            
        private int getShipsRemaining() {
            Grid.GridCell[][] matrix = this.mGrid.getCells();
            int shipsLeft = 0;
            // Check player 1
            if (this.mCurrentPlayerIsOne){
                for (int i = 0; i < Grid.GRID_SIZE; i++) {
                    for (int j = 0; j < Grid.GRID_SIZE; j++) {
                        if (matrix[i][j].hasShipP1() && !(matrix[i][j].hasShotP2())) {
                            shipsLeft++;
                        }
                    }
                }
            }
            // Check player 2
            else {
                for (int i = 0; i < Grid.GRID_SIZE; i++) {
                    for (int j = 0; j < Grid.GRID_SIZE; j++) {
                        if (matrix[i][j].hasShipP2() && !(matrix[i][j].hasShotP1())) {
                            shipsLeft++;
                        }
                    }
                }
            } 
            return shipsLeft;
        }
    
        private Grid generateSugarSharks(Grid grid) {
            // odds of sugar shark = 1/chance
            final int amountOfSharks = 3, maxattepts = 50;
            int numberOfSharks = amountOfSharks;
            //int chance = 10;
            Random rng = new Random(System.currentTimeMillis());
    
            for (int i = 0; i < Grid.GRID_SIZE; i++) {
                for (int j = 0; j < Grid.GRID_SIZE; j++) {
                    // clear current sharks
                    grid.getCells()[i][j].setSharkP1(false);
                    grid.getCells()[i][j].setSharkP2(false);
                }
            }
            // max attempts to create 3 sharks
            int attempts = maxattepts;
            // P1 sharks
            while (numberOfSharks > 0 && attempts > 0) {
                if (!grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].hasShipP2() 
                && !grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].hasShotP1()) {
                    grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].setSharkP1(true);
                    numberOfSharks--;
                    attempts = maxattepts;
                }
                attempts--;
            }
    
            numberOfSharks = amountOfSharks;
            attempts = maxattepts;
            while (numberOfSharks > 0 && attempts > 0) {
                if (!grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].hasShipP1() 
                && !grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].hasShotP2()) {
                    grid.getCells()[rng.nextInt(Grid.GRID_SIZE)][rng.nextInt(Grid.GRID_SIZE)].setSharkP2(true);
                    numberOfSharks--;
                    attempts = maxattepts;
                }
                attempts--;
            }
            return grid;
        }
    
        // ----- Start ----- End -----
        
        private void startGame() {
            System.out.println("Starting game on thread id=" + Thread.currentThread().threadId());
    
            // Setup game states
            Packet packet = new Packet();
            packet.serialize(this.mGrid);
    
            // Send packets, end game if fail
            if (!ConnectionManager.sendPacket(this.mClient1, packet)) {
                this.mGameOver = true;
            }
    
            if (!ConnectionManager.sendPacket(this.mClient2, packet)) {
                this.mGameOver = true;
            }
    
            // Receive packets with grid data
            Packet p1 = ConnectionManager.receivePacket(this.mClient1);
            Packet p2 = ConnectionManager.receivePacket(this.mClient2);
            try {
                // Combine grids into one
                Grid p1Grid = p1.getGrid();
                Grid p2Grid = p2.getGrid();
                p2Grid.translateP1toP2();
                this.mGrid.combine(p1Grid, p2Grid);
            } catch (IllegalStateException e) {
                System.err.println("Could not parse grid from client");
                this.mGameOver = true;
            }
        }
        
        private void endGame() {
            // Send winning data
            

            // Close clients
            System.out.println("Ending game on thread id=" + Thread.currentThread().threadId());
    
            // Close client1
            try {
                this.mClient1.close();
            } catch (IOException | NullPointerException e) {
                FileLogger.logError(GameManager.class, "endGame()", 
                "Failed to close clients on thread id=" + Thread.currentThread().threadId());
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
    
        /**
         * Swaps which player is currently being checked for packet receiving
         */
        private void swapPlayers() {
            if (this.mCurrentPlayerIsOne) {
                this.mCurrentSocket = this.mClient2;
                this.mCurrentPlayerIsOne = false;
        }
        else {
            this.mCurrentSocket = this.mClient1;
            this.mCurrentPlayerIsOne = true;
        }
    }
}
