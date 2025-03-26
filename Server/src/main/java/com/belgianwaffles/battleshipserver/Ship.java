package com.belgianwaffles.battleshipserver;

public class Ship {

    // ----- Subclasses -----

    private record Coordinate(int x, int y) {}
    
    

    // ----- Data -----
    
    private final Coordinate mStart, mEnd;
    private final int mLength;
    private final boolean mIsHorizontal, mIsValid;

    private boolean mIsSunk;
    private int mShotCount;



    // ----- Methods -----

    // ----- Creation -----
    
    /**
     * Creates unsunk ship at given coordinates. Call isValid() to ensure that ship is valid
     * @param x1 start x coordinate
     * @param y1 start y coordinate
     * @param x2 end x coordinate
     * @param y2 end y coordinate
     */
    public Ship(int x1, int y1, int x2, int y2) {
        // Add coordinates
        this.mStart = new Coordinate(x1, y1);
        this.mEnd = new Coordinate(x2, y2);

        // Check if ship is horizontal
        if (y1 == y2) {
            this.mIsHorizontal = true;
            this.mLength = x2 - x1;
        }
        // Check if ship is vertical
        else if (x1 == x2) {
            this.mIsHorizontal = false;
            this.mLength = y2 - y1;
        }
        // Ship is invalid
        else {
            this.mIsHorizontal = false;
            this.mLength = -1;
            this.mIsValid = false;
            return;
        }

        // Other variables
        this.mIsSunk = false;
        this.mIsValid = true;
        this.mShotCount = 0;
    }

    /**
     * This allows you to check that the created ship was valid
     * @return true if valid
     */
    public boolean isValid() {
        return this.mIsValid;
    }



    // ----- Read -----

    /**
     * This returns if the ship still has valid squares
     * @return true if ship is sunk
     */
    public boolean isSunk() {
        return this.mIsSunk;
    }



    // ----- Update -----

    /**
     * Attempts to shoot ship with given coordinates
     * @param x x coordinate of shot
     * @param y y coordinate of shot
     */
    public void shoot(int x, int y) {
        if (this.mIsHorizontal) {
            this.shootHorizontal(x, y);
        }
        else {
            this.shootVertical(x, y);
        }
    }
    
    /**
     * Shoots horizontal ship
     * @param x x coordinate of shot
     * @param y y coordinate of shot
     */
    private void shootHorizontal(int x, int y) {
        
    }

    /**
     * Shoots horizontal ship
     * @param x x coordinate of shot
     * @param y y coordinate of shot
     */
    private void shootVertical(int x, int y) {

    }
}
