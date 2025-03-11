package com.belgianwaffles.battleshipserver;

public final class Grid {

    // ----- Subclasses -----

    public final class GridCell {

        // ----- Constants -----

        private static final byte MASK_ALL      = (byte)0b11111111;
        private static final byte MASK_FIRE     = (byte)0b10000000;
        private static final byte MASK_SHARK    = (byte)0b01000000;
        private static final byte MASK_SHIP_1   = (byte)0b00000010;
        private static final byte MASK_SHOT_1   = (byte)0b10000001;
        private static final byte MASK_SHIP_2   = (byte)0b00001000;
        private static final byte MASK_SHOT_2   = (byte)0b10000100;

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

        public GridCell() {
            this.mCell = 0;
        }

        public GridCell(byte val) {
            this.mCell = val;
        }

        private void bitManipulate(byte mask, boolean turnOn) {
            if (turnOn) {
                this.mCell |= mask;
            }
            else {
                this.mCell &= mask ^ MASK_ALL;
            }
        }

        public void setOnFire(boolean isOnFire) {
            this.bitManipulate(MASK_FIRE, isOnFire);
        }
        
        public void setShark(boolean hasShark) {
            this.bitManipulate(MASK_SHARK, hasShark);
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

        public byte getCell() {
            return this.mCell;
        }
    }

    // ----- Constants -----

    public static final int GRID_SIZE  = 10;
    
    // ----- Data -----

    private GridCell[][] mCells;

    // ----- Methods -----
    
    public Grid() {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
    }
    
    public Grid(byte[] data) {
        this.mCells = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.mCells[i][j] = new GridCell(data[i * GRID_SIZE + j]);
            }
        }
    }

    // Copies cell data so it can be changed outside of class without affecting actual grid
    private GridCell[][] copyCells() {
        GridCell[][] copy = new GridCell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                copy[i][j] = this.mCells[i][j];
            }
        }
        return copy;
    }

    public GridCell[][] getCells() {
        return this.copyCells();
    }
}
