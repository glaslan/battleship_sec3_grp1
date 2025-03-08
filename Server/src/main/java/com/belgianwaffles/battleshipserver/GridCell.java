package com.belgianwaffles.battleshipserver;

public class GridCell {
    // first position: is on fire
    // second position: has shark
    // third/fourth position: ??
    // fifth position: has ship p2
    // sixth position: has ship p1
    // seventh position: shot by p2
    // eighth position: shot by p1
    char cell;
    public void setIsOnFire(boolean isOnFire) {
        if (isOnFire) {
            this.cell = (char) (cell | 0b10000000);
        } 
        else {
            this.cell = (char) (cell & 0b01111111);
        }
    }
    public void setHasShark(boolean hasShark) {
        if (hasShark) {
            this.cell = (char) (this.cell | 0b01000000);
        } 
        else {
            this.cell = (char) (this.cell & 0b10111111);
        }
    }
    public void setHasShipP2(boolean hasShip) {
        if (hasShip) {
            this.cell = (char) (this.cell | 0b00001000);
        } 
        else {
            this.cell = (char) (this.cell & 0b11110111);
        }
    }
    public void setHasShipP1(boolean hasShip) {
        if (hasShip) {
            this.cell = (char) (this.cell | 0b00000100);
        } 
        else {
            this.cell = (char) (this.cell & 0b11111011);
        }
    }
    public void setIsShotP2(boolean hasShot) {
        if (hasShot) {
            this.cell = (char) (this.cell | 0b00000010);
        } 
        else {
            this.cell = (char) (this.cell & 0b11111101);
        }
    }
    public void setIsShotP1(boolean hasShot) {
        if (hasShot) {
            this.cell = (char) (this.cell | 0b00000001);
        } 
        else {
            this.cell = (char) (this.cell & 0b11111110);
        }
    }
    public char getCell() {
        return this.cell;
    }
}
