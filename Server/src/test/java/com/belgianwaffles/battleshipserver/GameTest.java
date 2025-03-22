package com.belgianwaffles.battleshipserver;

import java.net.Socket;

import org.junit.jupiter.api.Test;

public class GameTest {
    /**
     * This will test that the game can be created, however it
     * will close after pinging due to no socket connections
     * SVR-GAME-001
     */
    @Test
    public void CreateGame() {
        Socket s1 = new Socket();
        Socket s2 = new Socket();
        GameManager gm = new GameManager(s1, s2);
        Thread gameThread = new Thread(gm);
        gameThread.start();
    }
    /**
     * Tests that all games can be ended by calling end all games method.
     * Will print that several errors occured, however this is due to null clients
     * SVR-GAME-002
     */
    @Test
    public void EndAllGames() {
        // Create 3 games worth of sockets
        Socket s11 = new Socket();
        Socket s12 = new Socket();
        Socket s21 = new Socket();
        Socket s22 = new Socket();
        Socket s31 = new Socket();
        Socket s32 = new Socket();

        // Create the 3 games
        GameManager gm1 = new GameManager(s11, s12);
        Thread gameThread1 = new Thread(gm1);
        gameThread1.start();
        GameManager gm2 = new GameManager(s21, s22);
        Thread gameThread2 = new Thread(gm2);
        gameThread2.start();
        GameManager gm3 = new GameManager(s31, s32);
        Thread gameThread3 = new Thread(gm3);
        gameThread3.start();

        // End all games before pings are sent so clients
        GameManager.endAllGames();
    }
}
