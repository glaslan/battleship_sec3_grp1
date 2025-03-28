package com.belgianwaffles.battleshipserver;

import java.util.Random;

public class Ship {

    // ----- Subclasses -----

    private record Coordinate(int x, int y) {}
    
    

    // ----- Data -----

    private static int sLength2 = 1;
    private static int sLength3 = 2;
    private static int sLength4 = 1;
    private static int sLength5 = 1;
    
    private final Coordinate mStart, mEnd;
    private int mLength;
    private final boolean mIsHorizontal;
    private final boolean[] mShotSpots;

    private int mShotCount;



    // ----- Methods -----

    // ----- Creation -----
    
    /**
     * Creates unsunk ship at given coordinates.
     * @param x1 start x coordinate
     * @param y1 start y coordinate
     * @param x2 end x coordinate
     * @param y2 end y coordinate
     * @throws IllegalArgumentException on detection of an invalid ship
     */
    public Ship(int x1, int y1, int x2, int y2) throws IllegalArgumentException {
        // Add coordinates
        this.mStart = new Coordinate(x1, y1);
        this.mEnd = new Coordinate(x2, y2);

        // Check if ship is horizontal
        if (y1 == y2) {
            this.mIsHorizontal = true;
            this.mLength = (x2 - x1) + 1;
        }
        // Check if ship is vertical
        else if (x1 == x2) {
            this.mIsHorizontal = false;
            this.mLength = (y2 - y1) + 1;
        }
        // Ship is invalid
        else {
            throw new IllegalArgumentException();
        }

        // Update ship counts based on length
        switch (this.mLength) {
        case 2:
            if (sLength2 == 0) {
                //throw new IllegalArgumentException();
            }
            sLength2--;
            break;
        case 3:
            if (sLength3 == 0) {
                //throw new IllegalArgumentException();
            }
            sLength3--;
            break;
        case 4:
            if (sLength4 == 0) {
                //throw new IllegalArgumentException();
            }
            sLength4--;
            break;
        case 5:
            if (sLength5 == 0) {
                //throw new IllegalArgumentException();
            }
            sLength5--;
            break;
        default:
            //throw new IllegalArgumentException();
        }

        // Other variables
        this.mShotCount = 0;
        this.mShotSpots = new boolean[this.mLength];
    }

    public Ship(int sizeOfShip) {
        Ship s = createRandomShip(sizeOfShip);
        this.mEnd = s.mEnd;
        this.mLength = s.mLength;
        this.mShotCount = s.mShotCount;
        this.mIsHorizontal = s.mIsHorizontal;
        this.mStart = s.mStart;
        this.mShotSpots = new boolean[this.mLength];
    }

    // this is needed because i dont trust java
    public Ship(Ship s) {
        this.mEnd = s.mEnd;
       
        this.mLength = s.mLength;
        this.mShotCount = s.mShotCount;
        this.mIsHorizontal = s.mIsHorizontal;
        this.mStart = s.mStart;
        this.mShotSpots = new boolean[this.mLength];
    }

    private Ship createRandomShip(int sizeOfShip) {
        final Random rng = new Random(System.currentTimeMillis());

        int index = rng.nextInt(0, Grid.GRID_SIZE * Grid.GRID_SIZE);
        int x = index % Grid.GRID_SIZE;
        int y = index / Grid.GRID_SIZE;
        int xEnd;
        int yEnd;
        boolean IsHorizontal = rng.nextBoolean();

        if (IsHorizontal) {
            yEnd = y;
            if(x+sizeOfShip-1 >= Grid.GRID_SIZE) {
                
                xEnd = x-(sizeOfShip-1);
                // reorients so the lower x value is always the start
                int temp = xEnd;
                xEnd = x;
                x = temp;
            }
            else {
                xEnd = x+(sizeOfShip-1);
            }
            
        }
        else {
            xEnd = x;
            if(y+sizeOfShip-1 >= Grid.GRID_SIZE) {
                yEnd = y-(sizeOfShip-1);
                // reorients so the lower y value is always the start
                int temp = yEnd;
                yEnd = y;
                y = temp;
            }
            else {
                yEnd = y+(sizeOfShip-1);
            }
        }
        Ship s = new Ship(x, y, xEnd, yEnd);
        s.mLength = sizeOfShip;
        return s;
    }



    // ----- Read -----

    /**
     * This returns if the ship still has valid squares
     * @return true if ship is sunk
     */
    public boolean isSunk() {
        return (this.mShotCount >= this.mLength);
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
        // Check y, must be in range
        if (y != this.mStart.y) {
            return;
        }

        // Check x, must be in range
        if (x <= this.mStart.x || this.mEnd.x < x) {
            return;
        }

        // Ship was shot, but ensure its not same spot
        int index = x - this.mStart.x;
        try {
            // Ship not shot yet
            if (!this.mShotSpots[index]) {
                this.mShotSpots[index] = true;
                this.mShotCount++;
            }
            // Already shot here
            else {
                return;
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("How dare");
        }
    }
    
    /**
     * Shoots vertical ship
     * @param x x coordinate of shot
     * @param y y coordinate of shot
     */
    private void shootVertical(int x, int y) {
        // Check x, must be in range
        if (x != this.mStart.x) {
            return;
        }
    
        // Check y, must be in range
        if (y <= this.mStart.y || this.mEnd.y < y) {
            return;
        }
    
        // Ship was shot, but ensure its not same spot
        int index = y - this.mStart.y;
        try {
            // Ship not shot yet
            if (!this.mShotSpots[index]) {
                this.mShotSpots[index] = true;
                this.mShotCount++;
            }
            // Already shot here
            else {
                return;
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("How dare");
        }
    }


    public boolean getIsHorizontal() {
        return mIsHorizontal;
    }

    public int getStartX() {
        return mStart.x;
    }

    public int getStartY() {
        return mStart.y;
    }

    public int getEndX() {
        return mEnd.x;
    }

    public int getEndY() {
        return mEnd.y;
    }

    @Override
    public String toString() {
        String s = "Start x: " + this.getStartX() + "\n";
        s += "Start y: " + this.getStartY() + "\n";
        s += "End x: " + this.getEndX() + "\n";
        s += "End y: " + this.getEndY() + "\n";
        s += "Length: " + this.mLength;

        return s;
    }
}

