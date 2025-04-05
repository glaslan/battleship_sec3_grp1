package com.belgianwaffles.battleship;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GameWindowTest {

    // since this class manages the UI, most tests have been done manually

    @Test
    public void setTurnCorrectly() {
        GameWindow game = new GameWindow();

        // check if turn
        game.setTurn(true);
        assertTrue(game.isTurn());

        // check if not turn
        game.setTurn(false);
        assertTrue(!game.isTurn());
    }

    @Test 
    public void startGameSetsGameStarted() {
        GameWindow game = new GameWindow();

        // check if game has not been started
        assertTrue(!game.isGameStarted());

        // start game and check if game has been started
        game.startGame(new Grid());
        assertTrue(game.isGameStarted());

    }

}