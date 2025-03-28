package com.belgianwaffles.battleship;

public final class Grid {

    // ----- Subclasses -----

    public final class GridCell {

        // ----- Constants -----

        private static final byte MASK_ALL      = (byte)0b11111111;
        private static final byte MASK_SHARK_1  = (byte)0b10000000;
        private static final byte MASK_SHARK_2  = (byte)0b01000000;
        private static final byte MASK_SHIP_1   = (byte)0b00100000;
        private static final byte MASK_SHOT_1   = (byte)0b00010000;
        private static final byte MASK_SHIP_2   = (byte)0b00001000;
        private static final byte MASK_SHOT_2   = (byte)0b00000100;



        // ----- Data -----

        // first position:      is on fire
        // second position:     has shark
        // third position:      has ship p1
        // fourth position      shot by p1
        // fifth position:      has ship p2
        // sixth position:      shot by p2
        // seventh position:    nothing
        // eighth position:     nothing
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

        public void setSharkP1(boolean hasShark) {
            this.bitManipulate(MASK_SHARK_1, hasShark);
        }
        
        public void setSharkP2(boolean hasShark) {
            this.bitManipulate(MASK_SHARK_2, hasShark);
        }
        
        public void setShipP1(boolean hasShip) {
            this.bitManipulate(MASK_SHIP_1, hasShip);
        }
        
        public void setShotP1(boolean hasShip) {
            this.bitManipulate(MASK_SHOT_1, hasShip);
        }
        
        public void setShipP2(boolean hasShot) {
            this.bitManipulate(MASK_SHIP_2, hasShot);
        }
        
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
            str += "00 ";

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



    // ----- Getters -----

    /**
     * Copies cell data so it can be changed outside of class without affecting actual grid
     */
    private GridCell[][] copyCells() {
        GridCell[][] copy = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(this.mCells[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    /**
     * Gets a copy of the cell contents of the grid.
     * Can create a new grid with the changed data.
     * @return 2D array with all gridcells
     */
    public GridCell[][] getCells() {
        return this.copyCells();
    }
    
    @Override
    public String toString() {
        String str = "";
        
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                str += this.mCells[x][y].toString();
            }
            str += '\n';
        }
    
        return str;
    };
}
