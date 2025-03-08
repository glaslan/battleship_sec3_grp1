package com.belgianwaffles.battleship;

public class Tile {


    public boolean P1_Segment;
    public boolean P2_Segment;
    public boolean P1_Shot;
    public boolean P2_Shot;
    public boolean P1_SugarShark;
    public boolean P2_SugarShark;

    // Will be used for the client when setting up ships on their end, they will send their board to the server
    // which will then distinguish between P1 and P2 segments
    public boolean Segment;

    public Tile() {
        this.P1_Segment = false;
        this.P2_Segment = false;
        this.P1_Shot = false;
        this.P2_Shot = false;
        this.P1_SugarShark = false;
        this.P2_SugarShark = false;
        this.Segment = false;
    }

    // setters/set functions
    public void P1Shoot() {
        this.P1_Shot = true;
    }

    public void P2Shoot() {
        this.P2_Shot = true;
    }

    public void SetP1SugarShark(boolean hasSugarShark) {
        this.P1_SugarShark = hasSugarShark;
    }

    public void SetP2SugarShark(boolean hasSugarShark) {
        this.P2_SugarShark = hasSugarShark;
    }

    public void P1SetSegment(boolean hasShipSegment) {
        this.P1_Segment = hasShipSegment;
    }

    public void P2SetSegment(boolean hasShipSegment) {
        this.P2_Segment = hasShipSegment;
    }
    
    public void SetSegment(boolean hasShipSegment) {
        this.Segment = hasShipSegment;
    }


    // getters 
    public boolean hasBeenShotByP1() {
        return this.P1_Shot;
    }

    public boolean hasBeenShotByP2() {
        return this.P2_Shot;
    }

    public boolean hasP1Segment() {
        return this.P1_Segment;
    }

    public boolean hasP2Segment() {
        return this.P2_Segment;
    }

    public boolean hasSegment() {
        return this.Segment;
    }

    public boolean hasSugarSharkP1() {
        return this.P1_SugarShark;
    }

    public boolean hasSugarSharkP2() {
        return this.P2_SugarShark;
    }
}
