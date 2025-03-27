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



    // ----- Data -----
    
    private final Socket mClient1, mClient2;
    private final Grid mGrid;
    private ArrayList <Ship> p1Ships;
    private ArrayList <Ship> p2Ships;

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
        this.p1Ships = new ArrayList<>();
        this.p2Ships = new ArrayList<>();
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
        while (this.play()) {
            // Receive packet from player
            Packet received = this.findPacket(this.mCurrentSocket, Packet.PACKET_TYPE_GRID);
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
                this.generateSugarSharks();
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


    public static ArrayList<Ship> createAllP1Ships(Grid g) {
        ArrayList <Ship> shipList = new ArrayList<>();
        // no need to check first ship since the board should be empty
        shipList.add(new Ship(5));

        Ship temp = createValidShipP1(g, 4);
        // i dont trust java
        shipList.add(new Ship(temp));

        temp = createValidShipP1(g, 3);
        shipList.add(new Ship(temp));

        temp = createValidShipP1(g, 3);
        shipList.add(new Ship(temp));

        temp = createValidShipP1(g, 2);
        shipList.add(new Ship(temp));
        
        
        return shipList;
    }

    // im making two functions since this one is already huge
    public static boolean shipPlacementIsAvailableP1(Grid g, Ship s) {
        // endpoint check
        if(!(s.getStartX()-1 < 0)) {
            if(g.getCells()[s.getStartX()-1][s.getStartY()].hasShipP1()) {
                return false;
            }  
        }
        if(!(s.getEndX()+1 >= Grid.GRID_SIZE)) {
            if(g.getCells()[s.getEndX()+1][s.getStartY()].hasShipP1()) {
                return false;
            }
        }
        if(!(s.getEndY()+1 >= Grid.GRID_SIZE)) {
            if(g.getCells()[s.getStartX()][s.getEndY()+1].hasShipP1()) {
                return false;
            }
        }
        if(!(s.getStartY()-1 < 0)) {
            if(g.getCells()[s.getStartX()][s.getStartY()-1].hasShipP1()) {
                return false;
            }
        }
        // body checks

        // horizontal ship
        if(s.getIsHorizontal()) {
            
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(g.getCells()[i][s.getStartY()].hasShipP1()) {
                    return false;
                }
                // prevents out of bounds checks
                if(!(s.getStartY() == Grid.GRID_SIZE-1)) {
                    if(g.getCells()[i][s.getStartY()+1].hasShipP1()) {
                        return false;
                    }
                }  
                if(!(s.getStartY() == 0)) {
                    if(g.getCells()[i][s.getStartY()-1].hasShipP1()) {
                        return false;
                    }    
                }
            }
        }
        // vertical ship
        else {
            
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(g.getCells()[s.getStartX()][i].hasShipP1()) {
                    return false;
                }
                if(!(s.getStartX() == Grid.GRID_SIZE-1)) {
                    if(g.getCells()[s.getStartX()+1][i].hasShipP1()) {
                        return false;
                    }  
                }
                if(!(s.getStartX() == 0)) {
                    if(g.getCells()[s.getStartX()-1][i].hasShipP1()) {
                        return false;
                    }    
                }
            }
        }
        return true;
    }

    public static void placeShipP1(Grid g, Ship s) {
        if(s.getIsHorizontal()) {
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                g.getCells()[i][s.getStartY()].setShipP1(true);   
            }
        }
        else {
            for (int i = 0; i <= s.getEndY(); i++) {
                g.getCells()[s.getStartX()][i].setShipP1(true);   
            }
        }
    }

    public static void placeAllShipsP1(Grid g, ArrayList<Ship> s) {
        for (int i = 0; i < s.size(); i++) {
            placeShipP1(g, s.get(i));  
        }
    }

    public static Ship createValidShipP1(Grid g, int size) {
        boolean isValidPlacement;
        Ship s;
        do {
            s = new Ship(size);
            isValidPlacement = shipPlacementIsAvailableP1(g, s);
            System.out.println("ran: " + isValidPlacement);
            System.out.println("\nship:\n " + s);
            System.out.println("\nGrid:\n" + g + "\n");
        } while(!isValidPlacement);
        
        return s;
    }
    
    
    private static boolean shipPlacementIsAvailableP2(Grid g, Ship s) {
   
        // horizontal ship
        if(s.getIsHorizontal()) {
            if(!(s.getStartX()-1 < 0)) {
                if(g.getCells()[s.getStartX()-1][s.getStartY()].hasShipP2()) {
                    return false;
                }  
            }
            if(!(s.getEndX()+1 >= Grid.GRID_SIZE)) {
                if(g.getCells()[s.getStartX()+1][s.getStartY()].hasShipP2()) {
                    return false;
                }
            }
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(g.getCells()[i][s.getStartY()].hasShipP2()) {
                    return false;
                }
                // prevents out of bounds checks
                if(!(s.getStartY() == Grid.GRID_SIZE-1)) {
                    if(g.getCells()[i][s.getStartY()+1].hasShipP2()) {
                        return false;
                    }
                }  
                if(!(s.getStartY() == 0)) {
                    if(g.getCells()[i][s.getStartY()-1].hasShipP2()) {
                        return false;
                    }    
                }
            }
        }
        // vertical ship
        else {
            if(!(s.getEndY()+1 >= Grid.GRID_SIZE)) {
                if(g.getCells()[s.getStartX()][s.getEndY()+1].hasShipP2()) {
                    return false;
                }
            }
            if(!(s.getStartY()-1 < 0)) {
                if(g.getCells()[s.getStartX()][s.getStartY()-1].hasShipP2()) {
                    return false;
                }
            }
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(g.getCells()[s.getStartX()][i].hasShipP2()) {
                    return false;
                }
                if(!(s.getStartX() == Grid.GRID_SIZE-1)) {
                    if(g.getCells()[s.getStartX()+1][i].hasShipP2()) {
                        return false;
                    }  
                }
                if(!(s.getStartX() == 0)) {
                    if(g.getCells()[s.getStartX()-1][i].hasShipP2()) {
                        return false;
                    }    
                }
            }
        }
        return true;
    }

    public static ArrayList<Ship> createAllP2Ships(Grid g) {
        ArrayList <Ship> shipList = new ArrayList<>();
        // no need to check first ship since the board should be empty
        shipList.add(new Ship(5));

        Ship temp = createValidShipP2(g, 4);
        // i dont trust java
        shipList.add(new Ship(temp));

        temp = createValidShipP2(g, 3);
        shipList.add(new Ship(temp));

        temp = createValidShipP2(g, 3);
        shipList.add(new Ship(temp));

        temp = createValidShipP2(g, 2);
        shipList.add(new Ship(temp));
        
        
        return shipList;
    }  

    private static void placeShipP2(Grid g, Ship s) {
        if(s.getIsHorizontal()) {
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                g.getCells()[i][s.getStartY()].setShipP2(true);   
            }
        }
        else {
            for (int i = 0; i <= s.getEndY(); i++) {
                g.getCells()[s.getStartX()][i].setShipP2(true);   
            }
        }
    }

    public static void placeAllShipsP2(Grid g, ArrayList<Ship> s) {
        for (int i = 0; i < s.size(); i++) {
            placeShipP2(g, s.get(i));  
        }
    }

    private static Ship createValidShipP2(Grid g, int size) {
        boolean isValidPlacement;
        Ship s;
        do {
            s = new Ship(size);
            isValidPlacement = shipPlacementIsAvailableP2(g, s);
        } while(!isValidPlacement);
        
        return s;
    }
    
    
    
    // ----- Start ----- End -----
    
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
        Packet p1 = null, p2 = null;
        while (p1 == null && this.play()) { p1 = this.findPacket(this.mClient1, Packet.PACKET_TYPE_GRID); }
        while (p2 == null && this.play()) { p2 = this.findPacket(this.mClient2, Packet.PACKET_TYPE_GRID); }

        // Check game over
        if (!this.play()) {
            return;
        }

        try {
            // Combine grids into one
            Grid p1Grid = p1.getGrid();
            Grid p2Grid = p2.getGrid();
            this.mGrid.combine(p1Grid, p2Grid);
        } catch (IllegalStateException e) {
            FileLogger.logError(GameManager.class, "startGame()", "Failed to parse grid from client");
            System.err.println("Failed to parse grid from client");
            this.mGameOver = true;
        }
    }
    
    /**
     * After ending the game, closes all necessary data
     */
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
        // If a client has disconnected, end game
        if (!this.ping(this.mClient1) || !this.ping(this.mClient2)) {
            FileLogger.logError(GameManager.class, "pingClients()", 
            "Failed to ping a client on thread=" + Thread.currentThread().threadId());
            System.err.println("Failed to ping a client on thread=" + Thread.currentThread().threadId());
            this.mGameOver = true;
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
     * Adds a packet to be checked later
     * @param client the client that received the packet
     * @param packet the received packet
     */
    private synchronized void addPacket(Socket client, Packet packet) {
        this.mPackets.add(new PacketMap(client, packet));
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
