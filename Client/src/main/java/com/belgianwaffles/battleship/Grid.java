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

    private final class Coordinate {

        // ----- Constants -----
        
        // ----- Data -----

        int mX, mY;
        
        public Coordinate(int x, int y) {
            this.mX = x;
            this.mY = y;
        }

        public int x() {
            return this.mX;
        }

        public int y() {
            return this.mY;
        }
    }
    
    

    // ----- Constants -----

    public static final int GRID_SIZE  = 10;
    


    // ----- Data -----

    private GridCell[][] mCells;
    private Coordinate mCoord1, mCoord2;



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



    // ----- Setters -----

    /**
     * Ensures that a coordinate is inbounds in the grid
     * @param x x coordinate of point
     * @param y y coordinate of point
     * @return true is inbounds, false if out of bounds
     */
    private boolean inBounds(int x, int y) {
        // Check x out of bounds
        if (x < 0 || x > GRID_SIZE - 1) {
            return false;
        }
        // Check y out of bounds
        if (y < 0 || y > GRID_SIZE - 1) {
            return false;
        }
        return true;
    }

    /**
     * Checks if any ships are too close to the selected area
     * @param isHorizontal if the ship being placed is horizontal
     * @return true if there is a ship too close, false if no ships
     */
    private boolean checkForShips(boolean isHorizontal) {
        if (isHorizontal) {
            // Loop checks
            for (int y = -1; y <= 1; y++) {
                if (!this.inBounds(this.mCoord1.x(), this.mCoord1.y() + y)) {
                    continue;
                }

                // Loop through x values
                for (int x = this.mCoord1.x(); x < this.mCoord2.x(); x++) {
                    // Check out of bounds
                    if (!this.inBounds(x, y)) {
                        continue;
                    }

                    // Check for ship
                    if (this.mCells[x][y].hasSharkP1()) {
                        return true;
                    }
                }
            }
        }
        else {
            // Loop checks
            for (int x = -1; x <= 1; x++) {
                if (!this.inBounds(this.mCoord1.x() + x, this.mCoord1.y())) {
                    continue;
                }
    
                // Loop through y values
                for (int y = this.mCoord1.y(); y < this.mCoord2.y(); y++) {
                    // Check out of bounds
                    if (!this.inBounds(x, y)) {
                        continue;
                    }
    
                    // Check for ship
                    if (this.mCells[x][y].hasSharkP1()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Places horizontal ships, sets up coordinates, and returns length
     * @param x1 x coordinate of first position
     * @param y1 y coordinate of first position
     * @param x2 x coordinate of second position
     * @param y2 y coordinate of second position
     * @return the length of the ship
     */
    private int placeShipsHorizontal(int x1, int y1, int x2, int y2) {
        // Check which is more left
        if (x1 < x2) {
            this.mCoord1 = new Coordinate(x1, y1);
            this.mCoord2 = new Coordinate(x2, y2);
        }
        else {
            this.mCoord1 = new Coordinate(x2, y2);
            this.mCoord2 = new Coordinate(x1, y1);
        }

        // Place ships
        for (int i = this.mCoord1.x(); i <= this.mCoord2.x(); i++) {
            this.mCells[i][this.mCoord1.y()].setShipP1(true);
        }
        return ((this.mCoord2.x() - this.mCoord1.x()) + 1);
    }

    /**
     * Places vertical ships, sets up coordinates, and returns length
     * @param x1 x coordinate of first position
     * @param y1 y coordinate of first position
     * @param x2 x coordinate of second position
     * @param y2 y coordinate of second position
     * @return the length of the ship
     */
    private int placeShipsVertical(int x1, int y1, int x2, int y2) {
        // Check which is more left
        if (y1 < y2) {
            this.mCoord1 = new Coordinate(x1, y1);
            this.mCoord2 = new Coordinate(x2, y2);
        }
        else {
            this.mCoord1 = new Coordinate(x2, y2);
            this.mCoord2 = new Coordinate(x1, y1);
        }

        // Place ships
        for (int i = this.mCoord1.y(); i <= this.mCoord2.y(); i++) {
            this.mCells[this.mCoord1.x()][i].setShipP1(true);
        }
        return ((this.mCoord2.y() - this.mCoord1.y()) + 1);
    }

    /**
     * Allows for the placing of ships. This function knows the rules of the game. Its very big brain (allegedly)
     * @param x1 x coordinate of first position
     * @param y1 y coordinate of first position
     * @param x2 x coordinate of second position
     * @param y2 y coordinate of second position
     * @return the length of the placed ship. Returns -1 on any failure
     */
    public int placeShip(int x1, int y1, int x2, int y2) {
        // Check inbounds
        if (!this.inBounds(x1, y1) || !this.inBounds(x2, y2)) {
            return -1;
        }
        
        // Holds if ship is horizontal or vertical
        boolean horizontal = false, vertical = false;

        // Checks for lines
        if (x1 == x2) { vertical = true; }
        if (y1 == y2) {horizontal = true;}
        
        // Ship is not in same column or row
        if (!horizontal && !vertical) {
            return -1;
        }
        
        // Determine if any ships are too close
        if (this.checkForShips(horizontal)) {
            return -1;
        }
        
        // Ships can be placed
        // For horizontal ships
        if (horizontal) {
            return this.placeShipsHorizontal(x1, y1, x2, y2);
        }
        // Checks verticallity
        else {
            return this.placeShipsVertical(x1, y1, x2, y2);
        }
    }

    /**
     * Undo's the most recent ship placement
     */
    public void undoShip() {
        // Check coordinates are real
        if (this.mCoord1 == null || this.mCoord2 == null) {
            return;
        }
        
        // Removes the ships
        // Checks for lines
        boolean horizontal = false, vertical = false;
        if (this.mCoord1.x() == this.mCoord2.x()) { vertical = true; }
        if (this.mCoord1.y() == this.mCoord2.y()) {horizontal = true;}

        // Horizontal ship replacements
        if (horizontal) {
            for (int i = this.mCoord1.x(); i < this.mCoord2.x(); i++) {
                this.mCells[i][this.mCoord1.y()].setShipP1(false);
            }
        }
        // Vertical ship replacements
        else if (vertical) {
            for (int i = this.mCoord1.y(); i < this.mCoord2.y(); i++) {
                this.mCells[this.mCoord1.x()][i].setShipP1(false);
            }
        }
        else {
            System.err.println("Ruh roh raggy");
        }
        // Coords set to null since ships are removed
        this.mCoord1 = null;
        this.mCoord2 = null;
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
        
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                str += this.mCells[i][j].toString();
            }
            str += '\n';
        }
    
        return str;
    };
}
