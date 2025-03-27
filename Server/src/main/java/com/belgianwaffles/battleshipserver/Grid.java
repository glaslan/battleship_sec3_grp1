package com.belgianwaffles.battleshipserver;

public final class Grid {

    // ----- Subclasses -----

    public final class GridCell {

        // ----- Constants -----

        private static final byte MASK_ALL      = (byte)0b11111111;
        private static final byte MASK_SHARK_1  = (byte)0b10000000;
        private static final byte MASK_SHIP_1   = (byte)0b01000000;
        private static final byte MASK_SHOT_1   = (byte)0b00100000;
        private static final byte MASK_SHARK_2  = (byte)0b00010000;
        private static final byte MASK_SHIP_2   = (byte)0b00001000;
        private static final byte MASK_SHOT_2   = (byte)0b00000100;



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
         * Sets the shot bit for p1
         * @param hasShot true if shot should be placed
         */
        public void setShotP1(boolean hasShip) {
            this.bitManipulate(MASK_SHOT_1, hasShip);
        }
        
        /**
         * Sets the ship bit for p2
         * @param hasShip true if ship should be placed
         */
        public void setShipP2(boolean hasShot) {
            this.bitManipulate(MASK_SHIP_2, hasShot);
        }
        
        /**
         * Sets the shot bit for p2
         * @param hasShot true if shot should be placed
         */
        public void setShotP2(boolean hasShot) {
            this.bitManipulate(MASK_SHOT_2, hasShot);
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
         * Checks if the tile has been shot by p1
         * @return true if tile has been shot
         */
        public boolean hasShotP1() {
            return this.getBit(MASK_SHOT_1);
        }
        
        /**
         * Checks if the tile has a ship for p2
         * @return true if tile has a ship
         */
        public boolean hasShipP2() {
            return this.getBit(MASK_SHIP_2);
        }
        
        /**
         * Checks if the tile has been shot by p2
         * @return true if tile has been shot
         */
        public boolean hasShotP2() {
            return this.getBit(MASK_SHOT_2);
        }

        /**
         * Converts the cells data from p1 to p2
         */
        public void translateP1toP2() {
            if (this.hasSharkP1()) {
                this.setSharkP1(false);
                this.setSharkP2(true);
            }
            if (this.hasShipP1()) {
                this.setShipP1(false);
                this.setShipP2(true);
            }
            if (this.hasShotP1()) {
                this.setShotP1(false);
                this.setShotP2(true);
            }
        }

        /**
         * Converts the cell data from p2 to p1
         */
        public void translateP2toP1() {
            if (this.hasSharkP2()) {
                this.setSharkP2(false);
                this.setSharkP1(true);
            }
            if (this.hasShipP2()) {
                this.setShipP2(false);
                this.setShipP1(true);
            }
            if (this.hasShotP2()) {
                this.setShotP2(false);
                this.setShotP1(true);
            }
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
            str += (this.hasSharkP2() ? "1" : "0");
            str += (this.hasShipP1() ? "1" : "0");
            str += (this.hasShotP1() ? "1" : "0");
            str += (this.hasShipP2() ? "1" : "0");
            str += (this.hasShotP2() ? "1" : "0");

            // Empty bits, can be added later
            str += "00";

            return str;
        }
        
    }

    // ----- Constants -----

    public static final int GRID_SIZE  = 10;
    


    // ----- Data -----

    private GridCell[][] mCells;



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
     * Converts g2 into second player data in grid.
     * @param g1 player 1's grid
     * @param g2 player 2's grid
     */
    public void combine(Grid g1, Grid g2) {
        // Translate grid
        g2.translateP1toP2();
        
        // Combine cells
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell((byte)(g1.mCells[i][j].getCell() | g2.mCells[i][j].getCell()));
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
     * Allows for comparing of 2 grids to see how many differences there are
     * @return the number of differences in the grid
     */
    public int checkDifferences(Grid other) {
        int diff = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (this.mCells[i][j] != other.mCells[i][j]) {
                    diff++;
                }
            }
        }
        return diff;
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

    /**
     * Gives a formatted grid string
     * @return A formatted, printable string
     */
    @Override
    public String toString() {
        String str = "";
        
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                str += this.mCells[j][i].toString() + " ";
            }
            str += '\n';
        }
    
        return str;
    };
}
