package com.belgianwaffles.battleshipserver;

import java.util.ArrayList;

public final class Grid {

    // ----- Subclasses -----

    public final class GridCell {

        // ----- Constants -----

        private static final byte MASK_ALL      = (byte)0b11111111;
        private static final byte MASK_SHARK_1  = (byte)0b10000000;
        private static final byte MASK_SHIP_1   = (byte)0b01000000;
        private static final byte MASK_SHOT_1   = (byte)0b00100000;
        private static final byte MASK_SUNK_1   = (byte)0b00010000;
        private static final byte MASK_SHARK_2  = (byte)0b00001000;
        private static final byte MASK_SHIP_2   = (byte)0b00000100;
        private static final byte MASK_SHOT_2   = (byte)0b00000010;
        private static final byte MASK_SUNK_2   = (byte)0b00000001;



        // ----- Data -----

        // If you want to know what the stuff means, refer to masks
        byte mCell;



        // ----- Methods -----

        /**
         * Creates an empty gridcell
         */
        public GridCell() {
            this.mCell = 0;
        }

        /**
         * Creates a gridcell with received value
         * @param val the value for the cell
         */
        public GridCell(byte val) {
            this.mCell = val;
        }

        /**
         * Creates a gridcell from another given gridcell
         * @param gc the gridcell to copy
         */
        public GridCell(GridCell gc) {
            this.mCell = gc.mCell;
        }

        /**
         * Manipulates the bits within the cell to turn on or off the specified bit
         * @param mask bitmask for wanted bit
         * @param turnOn if the bit should be turned on or off
         */
        private void bitManipulate(byte mask, boolean turnOn) {
            if (turnOn) {
                this.mCell |= mask;
            }
            else {
                this.mCell &= mask ^ MASK_ALL;
            }
        }


        
        // ----- Setters -----

        /**
         * Sets the shark bit for p1
         * @param hasShark true if shark should be set
         */
        public void setSharkP1(boolean hasShark) {
            this.bitManipulate(MASK_SHARK_1, hasShark);
        }
        
        /**
         * Sets the shark bit for p2
         * @param hasShark true if shark should be set
         */
        public void setSharkP2(boolean hasShark) {
            this.bitManipulate(MASK_SHARK_2, hasShark);
        }
        
        /**
         * Sets the ship bit for p1
         * @param hasShip true if ship should be placed
         */
        public void setShipP1(boolean hasShip) {
            this.bitManipulate(MASK_SHIP_1, hasShip);
        }
        
        /**
         * Sets the ship bit for p2
         * @param hasShip true if ship should be placed
         */
        public void setShipP2(boolean hasShot) {
            this.bitManipulate(MASK_SHIP_2, hasShot);
        }
        
        /**
         * Sets the shot bit for p1
         * @param hasShot true if shot should be placed
         */
        public void setShotP1(boolean hasShip) {
            this.bitManipulate(MASK_SHOT_1, hasShip);
        }
        
        /**
         * Sets the shot bit for p2
         * @param hasShot true if shot should be placed
         */
        public void setShotP2(boolean hasShot) {
            this.bitManipulate(MASK_SHOT_2, hasShot);
        }
        
        /**
         * Sets the sunk bit for p1
         * @param hasShot true if shot should be placed
         */
        public void setSunkP1(boolean hasShip) {
            if (this.hasShipP1()) {
                this.bitManipulate(MASK_SHOT_1, hasShip);
            }
        }
        
        /**
         * Sets the sunk bit for p2
         * @param hasShot true if shot should be placed
         */
        public void setSunkP2(boolean hasShot) {
            if (this.hasShipP2()) {
                this.bitManipulate(MASK_SHOT_2, hasShot);
            }
        }



        // ----- Getters -----

        /**
         * Retrieves the bitdata from a specified bit based on the mask
         * @return true if bit is set, false if not
         */
        private boolean getBit(byte mask) {
            return (this.mCell & mask) == mask;
        }

        /**
         * Gets the full cell data
         * @return the held cell data
         */
        public byte getCell() {
            return this.mCell;
        }

        /**
         * Checks if the tile has a shark for p1
         * @return true if there is a shark
         */
        public boolean hasSharkP1() {
            return this.getBit(MASK_SHARK_1);
        }
        
        /**
         * Checks if the tile has a shark for p2
         * @return true if there is a shark
         */
        public boolean hasSharkP2() {
            return this.getBit(MASK_SHARK_2);
        }
        
        /**
         * Checks if the tile has a ship for p1
         * @return true if there is a ship
         */
        public boolean hasShipP1() {
            return this.getBit(MASK_SHIP_1);
        }
        
        /**
         * Checks if the tile has a ship for p2
         * @return true if tile has a ship
         */
        public boolean hasShipP2() {
            return this.getBit(MASK_SHIP_2);
        }
        
        /**
         * Checks if the tile has been shot by p1
         * @return true if tile has been shot
         */
        public boolean hasShotP1() {
            return this.getBit(MASK_SHOT_1);
        }
        
        /**
         * Checks if the tile has been shot by p2
         * @return true if tile has been shot
         */
        public boolean hasShotP2() {
            return this.getBit(MASK_SHOT_2);
        }

        /**
         * Checks if the ship has been sunk by p2
         * @return true if tile has been shot
         */
        public boolean hasSunkP1() {
            return this.getBit(MASK_SUNK_1);
        }

        /**
         * Checks if the ship has been sunk by p1
         * @return true if tile has been shot
         */
        public boolean hasSunkP2() {
            return this.getBit(MASK_SUNK_2);
        }

        /**
         * Converts the cells data from p1 to p2
         */
        public void translateP1toP2() {
            // Swap flags
            this.mCell = (byte)((this.mCell >> 4));
            
            // Only have player 1 data, alt+f4 player 2 data
            this.mCell = (byte)(this.mCell & GRID_CELL_P2);
        }
        
        /**
         * Converts the cell data from p2 to p1
         */
        public void translateP2toP1() {
            // Swap flags
            this.mCell = (byte)((this.mCell << 4));
            
            // Only have player 2 data, alt+f4 player 1 data
            this.mCell = (byte)(this.mCell & GRID_CELL_P1);
        }

        /**
         * Removes unnecessary data from grid, use before sending
         */
        public void getPlayer1() {
            // Save important bits
            byte bits = (byte)(this.hasShotP2() ? MASK_SHOT_2 : 0);
            if (this.hasShotP1() && this.hasShipP2()) {
                bits |= (byte)(MASK_SHIP_2);
            }

            // Remove bits
            this.mCell &= GRID_CELL_P1;
            
            // Save this cells data and important p2 data
            this.mCell = (byte)((this.mCell & GRID_CELL_P1) | bits);
        }
        
        /**
         * Removes unnecessary data from grid for player 2, use before sending
         */
        public void getPlayer2() {
            // Save important bits
            byte bits = (byte)(this.hasShotP1() ? MASK_SHOT_1 : 0);
            if (this.hasShotP2() && this.hasShipP1()) {
                bits |= (byte)(MASK_SHIP_1);
            }

            // Remove bits
            this.mCell &= GRID_CELL_P2;
            
            // Save this cells data and important p2 data
            this.mCell = (byte)(((this.mCell & GRID_CELL_P2) << 4) | (bits >> 4));
        }
        
        /**
         * Gives a binary representation of the cell byte data
         * @return a formatted, printable cell
         */
        @Override
        public String toString() {
            String str = "";

            // Add bits
            str += (this.hasSharkP1() ? "1" : "0");
            str += (this.hasShipP1() ? "1" : "0");
            str += (this.hasShotP1() ? "1" : "0");
            str += (this.hasSunkP1() ? "1" : "0");
            str += (this.hasSharkP2() ? "1" : "0");
            str += (this.hasShipP2() ? "1" : "0");
            str += (this.hasShotP2() ? "1" : "0");
            str += (this.hasSunkP2() ? "1" : "0");

            return str;
        }
    }



    // ----- Constants -----

    public static final int GRID_SIZE       = 10;
    private static final byte GRID_CELL_P1  = (byte)0b11110000;
    private static final byte GRID_CELL_P2  = (byte)0b00001111;
    


    // ----- Data -----

    private GridCell[][] mCells;
    private ArrayList <Ship> p1Ships;
    private ArrayList <Ship> p2Ships;



    // ----- Methods -----
    
    /**
     * Creates a blank grid
     */
    public Grid() {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell();
            }
        }
        this.p1Ships = new ArrayList<>();
        this.p2Ships = new ArrayList<>();
    }
    
    /**
     * Creates a grid with the given array of bytes
     * @param data <code>byte[]</code> that has the grid information loaded
     */
    public Grid(byte[] data) {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell(data[i * GRID_SIZE + j]);
            }
        }
    }
    
    /**
     * Creates a grid from another grids cell arrays
     * @param cells the cells from another grid
     */
    public Grid(GridCell[][] cells) {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell(cells[i][j].getCell());
            }
        }
    }
    
    /**
     * Allows for the combination of 2 grids into 1.
     * @param g1 player 1's grid
     * @param g2 player 2's grid
     */
    public synchronized void combine(Grid g1, Grid g2) {
        // Combine cells
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell((byte)((g1.mCells[i][j].getCell() & GRID_CELL_P1) | (g2.mCells[i][j].getCell() & GRID_CELL_P2)));
            }
        }
    }

    /**
     * Gets the cell contents of the grid.
     * Can create a new grid with the changed data.
     * @return 2D array with all gridcells
     */
    public GridCell[][] getCells() {
        return this.mCells;
    }

    /**
     * Allows for cells to be passed to grid
     * @param cells the new cells to put into grid
     */
    public void setCells(GridCell[][] cells) {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell(cells[i][j].getCell());
            }
        }
    }

    /**
     * Changes the grid data from being in player 1 positions to player 2 positions
     */
    public void translateP1toP2() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j].translateP1toP2();
            }
        }
    }
    
    /**
     * Changes the grid data from being in player 1 positions to player 2 positions
     */
    public void translateP2toP1() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j].translateP2toP1();
            }
        }
    }
    
    /**
     * Changes the grid data from being in player 1 positions to player 2 positions
     */
    public void getGridP1() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j].getPlayer1();
            }
        }
    }
    
    /**
     * Changes the grid data from being in player 1 positions to player 2 positions
     */
    public void getGridP2() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j].getPlayer2();
            }
        }
    }
    
    /**
     * Allows for comparing of 2 grids to see how many differences there are
     * @return the number of differences in the grid
     */
    public int checkDifferences(Grid other) {
        return 1;
        // int diff = 0;
        // for (int i = 0; i < GRID_SIZE; i++) {
        //     for (int j = 0; j < GRID_SIZE; j++) {
        //         if (this.mCells[i][j] != other.mCells[i][j]) {
        //             diff++;
        //         }
        //     }
        // }
        // return diff;
    }

    public int checkShipCount(int player) {
        int shipsLeft = 0;
        if (player == 1) {
            for (int i = 0; i < p1Ships.size(); i++) {
                if(!p1Ships.get(i).isSunk()) {
                    shipsLeft++;
                }
            }
            
        }
        else {
            for (int i = 0; i < p2Ships.size(); i++) {
                if(!p2Ships.get(i).isSunk()) {
                    shipsLeft++;
                }
            }
        }
        return shipsLeft;
    }
    
    /**
     * Removes all sharks from the grid
     */
    public void removeSharks() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j].setSharkP1(false);
                this.mCells[i][j].setSharkP2(false);
            }
        }
    }



    // ----- Ship ----- Creation -----

    /**
     * Randomly generates ships in the grid for player 1
     */
    public void generateShipsPlayer1() {
        // Clear old ships
        this.clearShipsPlayer1();

        // Generate new ones
        this.createAllP1Ships();
    }

    /**
     * Randomly generates ships in the grid for player 2
     */
    public void generateShipsPlayer2() {
        // Clear old ships
        this.clearShipsPlayer2();

        // Generate new ones
        this.createAllP2Ships();
    }

    /**
     * Removes old ships from grid so new ones can be added
     */
    private void clearShipsPlayer1() {
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            this.mCells[i % GRID_SIZE][i / GRID_SIZE].setShipP1(false);
        }
        this.p1Ships.clear();
    }

    /**
     * Removes old ships from grid so new ones can be added
     */
    private void clearShipsPlayer2() {
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            this.mCells[i % GRID_SIZE][i / GRID_SIZE].setShipP2(false);
        }
        this.p2Ships.clear();
    }
    
    
    // Ship Generation Methods

    // creates and places all p1 ships
    private void createAllP1Ships() {
        
        // no need to check first ship since the board should be empty
        Ship temp = new Ship(5);
        this.p1Ships.add(temp);
        this.placeShipP1(temp);

        temp = createValidShipP1(4);
        // i dont trust java
        this.p1Ships.add(new Ship(temp));
        this.placeShipP1(temp);

        temp = createValidShipP1(3);
        this.p1Ships.add(new Ship(temp));
        this.placeShipP1(temp);

        temp = createValidShipP1(3);
        this.p1Ships.add(new Ship(temp));
        this.placeShipP1(temp);

        temp = createValidShipP1(2);
        this.p1Ships.add(new Ship(temp));
        this.placeShipP1(temp);
    }

    // im making two functions since this one is already huge
    // checks for a valid placement for a p1 ship
    private boolean shipPlacementIsAvailableP1(Ship s) {
        // endpoint check
        if(!(s.getStartX()-1 < 0)) {
            if(this.mCells[s.getStartX()-1][s.getStartY()].hasShipP1()) {
                return false;
            }  
        }
        if(!(s.getEndX()+1 >= Grid.GRID_SIZE)) {
            if(this.mCells[s.getEndX()+1][s.getStartY()].hasShipP1()) {
                return false;
            }
        }
        if(!(s.getEndY()+1 >= Grid.GRID_SIZE)) {
            if(this.mCells[s.getStartX()][s.getEndY()+1].hasShipP1()) {
                return false;
            }
        }
        if(!(s.getStartY()-1 < 0)) {
            if(this.mCells[s.getStartX()][s.getStartY()-1].hasShipP1()) {
                return false;
            }
        }
        // body checks

        // horizontal ship
        if(s.getIsHorizontal()) {
            
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                if(this.mCells[i][s.getStartY()].hasShipP1()) {
                    return false;
                }
                // prevents out of bounds checks
                if(!(s.getStartY() == Grid.GRID_SIZE-1)) {
                    if(this.mCells[i][s.getStartY()+1].hasShipP1()) {
                        return false;
                    }
                }  
                if(!(s.getStartY() == 0)) {
                    if(this.mCells[i][s.getStartY()-1].hasShipP1()) {
                        return false;
                    }    
                }
            }
        }
        // vertical ship
        else {
            
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(this.mCells[s.getStartX()][i].hasShipP1()) {
                    return false;
                }
                if(!(s.getStartX() == Grid.GRID_SIZE-1)) {
                    if(this.mCells[s.getStartX()+1][i].hasShipP1()) {
                        return false;
                    }  
                }
                if(!(s.getStartX() == 0)) {
                    if(this.mCells[s.getStartX()-1][i].hasShipP1()) {
                        return false;
                    }    
                }
            }
        }
        return true;
    }

    private void placeShipP1(Ship s) {
        if(s.getIsHorizontal()) {
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                this.mCells[i][s.getStartY()].setShipP1(true);   
            }
        }
        else {
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                this.mCells[s.getStartX()][i].setShipP1(true);   
            }
        }
    }

    // creates a valid p1 ship
    private Ship createValidShipP1(int size) {
        boolean isValidPlacement;
        Ship s;
        do {
            s = new Ship(size);
            isValidPlacement = this.shipPlacementIsAvailableP1(s);
        } while(!isValidPlacement);
        
        return s;
    }
    
    // checks for a valid placement for a p2 ship
    private boolean shipPlacementIsAvailableP2(Ship s) {
        // endpoint check
        if(!(s.getStartX()-1 < 0)) {
            if(this.mCells[s.getStartX()-1][s.getStartY()].hasShipP2()) {
                return false;
            }  
        }
        if(!(s.getEndX()+1 >= Grid.GRID_SIZE)) {
            if(this.mCells[s.getEndX()+1][s.getStartY()].hasShipP2()) {
                return false;
            }
        }
        if(!(s.getEndY()+1 >= Grid.GRID_SIZE)) {
            if(this.mCells[s.getStartX()][s.getEndY()+1].hasShipP2()) {
                return false;
            }
        }
        if(!(s.getStartY()-1 < 0)) {
            if(this.mCells[s.getStartX()][s.getStartY()-1].hasShipP2()) {
                return false;
            }
        }
        // body checks

        // horizontal ship
        if(s.getIsHorizontal()) {
            
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                if(this.mCells[i][s.getStartY()].hasShipP2()) {
                    return false;
                }
                // prevents out of bounds checks
                if(!(s.getStartY() == Grid.GRID_SIZE-1)) {
                    if(this.mCells[i][s.getStartY()+1].hasShipP2()) {
                        return false;
                    }
                }  
                if(!(s.getStartY() == 0)) {
                    if(this.mCells[i][s.getStartY()-1].hasShipP2()) {
                        return false;
                    }    
                }
            }
        }
        // vertical ship
        else {
            
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                if(this.mCells[s.getStartX()][i].hasShipP2()) {
                    return false;
                }
                if(!(s.getStartX() == Grid.GRID_SIZE-1)) {
                    if(this.mCells[s.getStartX()+1][i].hasShipP2()) {
                        return false;
                    }  
                }
                if(!(s.getStartX() == 0)) {
                    if(this.mCells[s.getStartX()-1][i].hasShipP2()) {
                        return false;
                    }    
                }
            }
        }

        return true;
    }

    // creates and places all p2 ships
    private void createAllP2Ships() {  

        // no need to check first ship since the board should be empty
        Ship temp = new Ship(5);
        this.p2Ships.add(temp);
        this.placeShipP2(temp);

        temp = createValidShipP2(4);
        // i dont trust java
        this.p2Ships.add(new Ship(temp));
        this.placeShipP2(temp);

        temp = createValidShipP2(3);
        this.p2Ships.add(new Ship(temp));
        this.placeShipP2(temp);

        temp = createValidShipP2(3);
        this.p2Ships.add(new Ship(temp));
        this.placeShipP2(temp);

        temp = createValidShipP2(2);
        this.p2Ships.add(new Ship(temp));
        this.placeShipP2(temp);

    }  

    private void placeShipP2(Ship s) {
        if(s.getIsHorizontal()) {
            for (int i = s.getStartX(); i <= s.getEndX(); i++) {
                this.mCells[i][s.getStartY()].setShipP2(true);   
            }
        }
        else {
            for (int i = s.getStartY(); i <= s.getEndY(); i++) {
                this.mCells[s.getStartX()][i].setShipP2(true);   
            }
        }
    }

    // creates a valid p2 ship
    private Ship createValidShipP2(int size) {
        boolean isValidPlacement;
        Ship s;
        do {
            s = new Ship(size);
            isValidPlacement = this.shipPlacementIsAvailableP2(s);
        } while(!isValidPlacement);
        
        return s;
    }

    
    // ----- Extras -----

    /**
     * Gives a formatted grid string
     * @return A formatted, printable string
     */
    @Override
    public String toString() {
        String str = "";
        
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                str += this.mCells[x][y].toString() + " ";
            }
            str += '\n';
        }
    
        return str;
    };
}
