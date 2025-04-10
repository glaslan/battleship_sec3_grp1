package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager implements Runnable {

    // ----- Subclasses -----

    private record PacketMap(Socket client, Packet packet) {}
    
    // ----- Constants -----
    
    private static final int SLEEP_TIME = ConnectionManager.DEFAULT_TIMEOUT / 2;
    private static final int DEFAULT_ID = -1;



    // ----- Data -----
    
    private final Socket mClient1, mClient2;
    private final Grid mGrid;
    private int mUserId1, mUserId2;

    // For easy swapping
    private Socket mCurrentSocket;

    // Packet thread
    private List<PacketMap> mPackets;

    // Ending games
    private boolean mGameOver;
    private boolean mCurrentPlayerIsOne;



    private static boolean sServerClosed;
    static {
        sServerClosed = false;
    }



    // ----- Methods -----
    
    /**
     * Creates a new game with the passed clients
     * @param s1 first client
     * @param s2 second client
     */
    public GameManager(Socket s1, Socket s2) {
        this.mClient1 = s1;
        this.mClient2 = s2;
        this.mCurrentSocket = this.mClient1;

        this.mGameOver = false;
        this.mCurrentPlayerIsOne = true;

        this.mGrid = new Grid();
        this.mPackets = new ArrayList<>();
    }
    
    
    
    // ----- Threading -----
    
    /**
     * Main thread for game to run on
     */
    @Override
    @SuppressWarnings("ConvertToTryWithResources")
    public void run() {
        // Create thread for pinging clients
        Runnable pingThread = () -> {
            if (!this.mGameOver && !sServerClosed) {
                GameManager.this.pingClients();
            }
        };
        // Ping every SLEEP_TIME seconds
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(pingThread, 0, SLEEP_TIME, TimeUnit.MILLISECONDS);

        // Setup game state
        this.startGame();

        // Main loop
        boolean gridSent = false;
        while (this.play()) {
            // Send grid on first run
            if (!gridSent) {
                this.sendGridsToPlayers();
                gridSent = true;
            }
            
            // Receive packet from player
            Packet received = this.findPacket(this.mCurrentSocket, Packet.PACKET_TYPE_GRID);
            if (received == null) {
                continue;
            }

            // Check packet data
            Grid grid = received.getGrid();
            if (this.mGrid.checkDifferences(grid) != 1) {
                System.err.println("Too many grid changes received");
                this.sendGridsToPlayers();
                continue;
            }

            // Update grid
            boolean hitShip = false;
            if (this.mCurrentPlayerIsOne) {
                int prevHits = this.mGrid.hitCountP1();
                this.mGrid.combine(grid, this.mGrid);
                if (prevHits < this.mGrid.hitCountP1()) {
                    hitShip = true;
                }
            }
            else {
                grid.translateP1toP2();
                int prevHits = this.mGrid.hitCountP2();
                this.mGrid.combine(this.mGrid, grid);
                if (prevHits < this.mGrid.hitCountP2()) {
                    hitShip = true;
                }
                this.generateSugarSharks();
            }

            // Swap players
            this.swapPlayers();
            gridSent = false;

            // Checks if there are ships remaining
            if (this.getShipsRemaining() <= 0) {
                this.sendGridsToPlayers();
                this.mGameOver = true;
            }

            // Swap back if ship was hit
            if (hitShip) {
                this.swapPlayers();
            }
        }
        
        // Actions for game end
        executor.close();
        this.endGame();
        System.out.println("Closed thread id=" + Thread.currentThread().threadId());
    }
    
    
    
    // ----- Game ----- Methods -----
    
    // now obsolete from Grid.checkShipsCount()?
    /**
     * Checks for how many ships are left in the grid
     * @return number of ships remaining
     */
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

    /**
     * Generates sugar sharks for held grid
     */
    private void generateSugarSharks() {
        // ----- Method ----- Constants -----

        final int amountOfSharks = 3, maxattepts = 50;
        
        // ----- Data ----- Prep -----

        this.mGrid.removeSharks();
        
        // ----- Main ----- Logic -----
        
        // Create sharks for player 1
        for (int i = 0; i < amountOfSharks; i++) {
            this.generateSingleSugarShark(1, maxattepts);
        }

        // Create sharks for player 2
        for (int i = 0; i < amountOfSharks; i++) {
            this.generateSingleSugarShark(2, maxattepts);
        }
    }

    /**
     * Generates a single sugar shark for a given grid
     * @param player the player to add a shark to
     * @param maxAttempts the amount of cells to try randomly before brute forcing
     */
    private void generateSingleSugarShark(int player, int maxAttempts) {
        final Random rng = new Random(System.currentTimeMillis());

        // Gets cells to update
        var cells = this.mGrid.getCells();

        // First, attempt to randomly place shark
        for (int i = 0; i < maxAttempts; i++) {
            // Setup indexing
            int index = rng.nextInt(0, Grid.GRID_SIZE * Grid.GRID_SIZE);
            int x = index % Grid.GRID_SIZE, y = index / Grid.GRID_SIZE;
            
            // Check if cell has ship or has been shot
            if (this.checkCell(player, cells[x][y])) {
                // Determine which player to set
                if (player == 1) {
                    cells[x][y].setSharkP1(true);
                }
                else {
                    cells[x][y].setSharkP2(true);
                }

                // Update cells
                this.mGrid.setCells(cells);
                return;
            }
        }

        // First attempt failed, brute force from top left to bottom right
        for (int i = 0; i < Grid.GRID_SIZE * Grid.GRID_SIZE; i++) {
            // Setup indexing
            int index = i;
            int x = index % Grid.GRID_SIZE, y = index / Grid.GRID_SIZE;
            
            // Check if cell has ship or has been shot
            if (this.checkCell(player, cells[x][y])) {
                // Determine which player to set
                if (player == 1) {
                    cells[x][y].setSharkP1(true);
                }
                else {
                    cells[x][y].setSharkP2(true);
                }

                // Update cells
                this.mGrid.setCells(cells);
                return;
            }
        }
    }
    
    /**
     * Checks the flags in cell for if a shark can be placed
     * @param player player 1 or player 2
     * @param cell the cell to check
     * @return true if cell does not have invalid flags in positions
     */
    private boolean checkCell(int player, Grid.GridCell cell) {
        if (player == 1) {
            // Cannot have shark
            if (cell.hasSharkP1()) {
                return false;
            }
        
            // Cannot be shot
            if (cell.hasShotP1()) {
                return false;
            }
        
            // Cannot have ship
            if (cell.hasShipP2()) {
                return false;
            }
            return true;
        }
        if (player == 2) {
            // Cannot have shark
            if (cell.hasSharkP2()) {
                return false;
            }

            // Cannot be shot
            if (cell.hasShotP2()) {
                return false;
            }

            // Cannot have ship
            if (cell.hasShipP1()) {
                return false;
            }
            return true;
        }

        // Something went wrong
        return false;
    }

    /**
     * Sends the grids to each of the clients with required flags and data
     */
    private void sendGridsToPlayers() {
        // Get client grids
        Grid g1 = new Grid(this.mGrid.getCells());
        Grid g2 = new Grid(this.mGrid.getCells());

        // Setup packets
        g1.getGridP1();
        g2.getGridP2();
        
        // Setup packets
        Packet p1 = new Packet();
        Packet p2 = new Packet();

        // Base flags off of current player
        if (this.mCurrentPlayerIsOne) {
            p1.addTurn(Packet.PACKET_TURN_TRUE);
        }
        else {
            p2.addTurn(Packet.PACKET_TURN_TRUE);
        }

        // Serialize grids
        p1.serialize(g1);
        p2.serialize(g2);

        // Send grids
        ConnectionManager.sendPacket(this.mClient1, p1);
        ConnectionManager.sendPacket(this.mClient2, p2);
    }

    
    
    // ----- Start ----- End -----

    /**
     * Allows client to get a grid to begin the game
     * @param client the client socket to check
     * @param player the player number, either 1 or 2
     */
    private void generatePlayerGrid(Socket client, int player) {
        Packet packet = null;
        while (this.play() && packet == null) {
            // Find the grid packet from client
            packet = this.findPacket(client, Packet.PACKET_TYPE_GRID);
            if (packet == null) {
                continue;
            }

            // Check for refresh
            if (packet.hasFlag(Packet.PACKET_FLAG_REFRESH)) {
                // Send another board
                Grid g = new Grid();
                g.generateShipsPlayer1();
                packet.serialize(g);
                ConnectionManager.sendPacket(client, packet);
                packet = null;
                continue;
            }
            
            // Grid confirmed
            if (packet.hasFlag(Packet.PACKET_FLAG_CONFIRM)) {
                Grid g = packet.getGrid();
                // Change grid based on player number
                if (player == 1) {
                    this.mGrid.combine(g, this.mGrid);
                }
                else {
                    g.translateP1toP2();
                    this.mGrid.combine(this.mGrid, g);
                }
            }
        }
    }
    
    /**
     * Sets up game and sends some packets to clients with board information
     */
    private void startGame() {
        System.out.println("Starting game on thread id=" + Thread.currentThread().threadId());

        // Create threads for receiving from each client
        Runnable receiveP1 = () -> {
            while (this.play()) {
                this.receivePacket(this.mClient1);
            }
        };
        Thread receiveThreadP1 = new Thread(receiveP1);
        receiveThreadP1.setDaemon(true);
        receiveThreadP1.start();
        Runnable receiveP2 = () -> {
            while (this.play()) {
                this.receivePacket(this.mClient2);
            }
        };
        Thread receiveThreadP2 = new Thread(receiveP2);
        receiveThreadP2.setDaemon(true);
        receiveThreadP2.start();
        
        // Send image packets
        Packet packetBackgroundP1 = new Packet();
        Packet packetBackgroundP2 = new Packet();
        packetBackgroundP1.serialize("p1Background.png");
        packetBackgroundP2.serialize("p2Background.png");

        // Send packets, end game if fail
        ConnectionManager.sendPacket(this.mClient1, packetBackgroundP1);
        ConnectionManager.sendPacket(this.mClient2, packetBackgroundP2);

        // Setup game states
        Packet packetGrid1 = new Packet();
        Packet packetGrid2 = new Packet();
        Grid gridGenerated1 = new Grid();
        Grid gridGenerated2 = new Grid();
        gridGenerated1.generateShipsPlayer1();
        gridGenerated2.generateShipsPlayer1();
        packetGrid1.serialize(gridGenerated1);
        packetGrid2.serialize(gridGenerated2);

        // Send packets, end game if fail
        if (!ConnectionManager.sendPacket(this.mClient1, packetGrid1)) {
            this.mGameOver = true;
        }

        if (!ConnectionManager.sendPacket(this.mClient2, packetGrid2)) {
            this.mGameOver = true;
        }

        // Receive packets with grid data

        // Create runnables
        Runnable getGridP1 = () -> {
            this.generatePlayerGrid(this.mClient1, 1);
        };
        Runnable getGridP2 = () -> {
            this.generatePlayerGrid(this.mClient2, 2);
        };

        // Setup threads
        Thread checkGridP1 = new Thread(getGridP1);
        checkGridP1.start();
        Thread checkGridP2 = new Thread(getGridP2);
        checkGridP2.start();

        // Join the threads to ensure completion
        try {
            checkGridP1.join();
            checkGridP2.join();
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted");
        }
    }
    
    /**
     * After ending the game, closes all necessary data
     */
    private void endGame() {
        // Send winning data
        Packet winPacket = new Packet();
        Packet lossPacket = new Packet();

        winPacket.serialize(true);
        lossPacket.serialize(false);

        // Determine winner from current player
        if (this.mCurrentPlayerIsOne) {
            // P1 win, P2 loss
            ConnectionManager.sendPacket(this.mClient1, winPacket);
            ConnectionManager.sendPacket(this.mClient2, lossPacket);
        }
        else {
            // P1 loss, P2 win
            ConnectionManager.sendPacket(this.mClient1, lossPacket);
            ConnectionManager.sendPacket(this.mClient2, winPacket);
        }
        
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

    /**
     * Allows for ending all games simultaneously on all threads
     */
    public static synchronized void endAllGames() {
        sServerClosed = true;
    }



    // ----- Read -----

    /**
     * Checks if game is still able to be playing
     * @return true if game is still valid
     */
    private synchronized boolean play() {
        return (!this.mGameOver && !sServerClosed);
    }

    /**
     * Pings clients to ensure connections have not been severed
     */
    private void pingClients() {
        // Check client 1 hasn't disconnected
        if (!this.ping(this.mClient1)) {
            FileLogger.logError(GameManager.class, "pingClients()", 
            "Failed to ping client 1 on thread=" + Thread.currentThread().threadId());
            System.err.println("Failed to ping client 2 on thread=" + Thread.currentThread().threadId());
            this.mGameOver = true;
            
            // Send packet to other player, stating their win
            Packet packet = new Packet();
            packet.serialize(true);
            ConnectionManager.sendPacket(this.mClient2, packet);
        }
        
        // Check client 1 hasn't disconnected
        if (!this.ping(this.mClient2)) {
            FileLogger.logError(GameManager.class, "pingClients()", 
            "Failed to ping client 2 on thread=" + Thread.currentThread().threadId());
            System.err.println("Failed to ping client 2 on thread=" + Thread.currentThread().threadId());
            this.mGameOver = true;

            // Send packet to other player, stating their win
            Packet packet = new Packet();
            packet.serialize(true);
            ConnectionManager.sendPacket(this.mClient1, packet);
        }
    }

    /**
     * Pings clients and checks packet type. If not a ping, adds to packet list
     * @param client the client to ping
     */
    private boolean ping(Socket client) {
        
        // ----- Send ----- Ping -----
        
        // Prepare ping packet
        Packet packet = new Packet();
        packet.serialize();
        
        // Send ping
        if (!ConnectionManager.sendPacket(client, packet)) {
            return false;
        }
        
        // Create log of sent ping
        FileLogger.logPing(packet.toString());
        
        
        
        // ----- Receive ----- Ping -----

        do {
            // Try to find a ping packet
            packet = this.findPacket(client, Packet.PACKET_TYPE_PING);
            
            // Check if packet is received
            if (packet != null) {
                break;
            }

            // Try to find a null packet
            packet = this.findPacket(client, Packet.PACKET_TYPE_NONE);
            if (packet != null) {
                System.err.println("Client disconnected");
                FileLogger.logMessage("Client disconnected");
                return false;
            }
        } while (packet == null);
        
        // Create log of received ping
        FileLogger.logPing(packet.toString());
        
        return true;
    }
    
    /**
     * Receive packets from given client
     * @param client the client to receive packets from
     */
    private void receivePacket(Socket client) {
        // Receive and add packet to list
        Packet packet = ConnectionManager.receivePacket(client);
        this.addPacket(client, packet);
    }
    
    /**
     * Checks the userId from received packet
     * @param client client that sent the packet
     * @param packet the packet
     * @return
     */
    private boolean verifyUserId(Socket client, Packet packet) {
        // Null packets are allowed
        if (packet == null) {
            return true;
        }

        // Get the ID out of the packet
        int id = packet.getUser();

        // Client 1 check
        if (client == this.mClient1) {
            // Set userId if not set
            if (this.mUserId1 == DEFAULT_ID) {
                this.mUserId1 = id;
                return true;
            }
            
            // Check if set
            if (this.mUserId1 == id) {
                return true;
            }
        }
        // Client 2 check
        else if (client == this.mClient2) {
            // Set userId if not set
            if (this.mUserId2 == DEFAULT_ID) {
                this.mUserId2 = id;
                return true;
            }
            
            // Check if set
            if (this.mUserId2 == id) {
                return true;
            }
        }
        // Not verified
        else {
            this.mGameOver = true;
        }
        return false;
    }
    
    /**
     * Adds a packet to be checked later
     * @param client the client that received the packet
     * @param packet the received packet
     */
    private synchronized void addPacket(Socket client, Packet packet) {
        // Verify the packet before adding
        if (this.verifyUserId(client, packet)) {
            this.mPackets.add(new PacketMap(client, packet));
        }
    }

    /**
     * Allows for reading and removal of items from list. This is a non-blocking method and will return immediately if no packet is found.
     * @param client the client to check packets for
     * @param type the desired type of packet. PACKET_TYPE_NONE to find a null packet for termination
     * @return the desired packet. Null otherwise
     */
    private synchronized Packet findPacket(Socket client, int type) {
        // Check packet list
        for (int i = 0; i < this.mPackets.size(); i++) {
            PacketMap pm = this.mPackets.get(i);

            // Check for nulls if wanted
            if (pm.packet == null && type == Packet.PACKET_TYPE_NONE) {
                this.removePacket(i);
                return new Packet();
            }

            // Ensure no null before accessing data
            if (pm.packet == null) {
                continue;
            }
            
            // Checks for specified params
            if (pm.client == client && pm.packet.getType() == type) {
                Packet packet = this.mPackets.get(i).packet;
                this.removePacket(i);
                return packet;
            }
        }
        
        // No packet found for that client with set params
        return null;
    }
    
    /**
     * Removes packet from list
     * @param index the index of the item to remove
     */
    private synchronized void removePacket(int index) {
        this.mPackets.remove(index);
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
