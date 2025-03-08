package com.belgianwaffles.battleship;

import java.util.ArrayList;

public class Board {

    ArrayList<ArrayList<Tile>> tiles;

    public Board() {

        this.tiles = new ArrayList<>();

        // y first because first index determines row (height/y), second determines column (width/x)
        for (int y = 0; y<Constants.BOARD_DIMENSIONS;y++) {
            for (int x = 0; x<Constants.BOARD_DIMENSIONS;x++) {
                
                // instantiate the arraylists with tiles 
                this.tiles.get(y).set(x, new Tile());

            }
        }
    }

    public Tile getTileAtPosition(int x, int y) {

        // return null if out of bounds
        if (x<0 || x>=Constants.BOARD_DIMENSIONS || y<0 || y>=Constants.BOARD_DIMENSIONS) {
            return null;
        }

        return this.tiles.get(y).get(x);

    }

    public ArrayList<ArrayList<Tile>> getAllTiles() {
        return this.tiles;
    }
    
}
