package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.Test;

public class ConnectionTest {

    /**
     * Tests the creation and closing of the sockets for the server
     */
    @Test
    public void ServerCreationAndDestruction() {
        ConnectionManager cm = null;
        try {
            cm = new ConnectionManager(ConnectionManager.DEFAULT_PORT);
        } catch (IOException e) {
            fail("Connection manager constructor threw exception");
        }
        assertTrue(cm.close());
    }
    /**
     * Allows for 2 clients to connect to the server
     */
    @Test
    public void ConnectTwoClients() {
        ConnectionManager cm = null;
        try {
            // Server creation
            cm = new ConnectionManager(ConnectionManager.DEFAULT_PORT);

            // Add clients
            Socket s1 = new Socket("localhost", ConnectionManager.DEFAULT_PORT);
            Socket s2 = new Socket("localhost", ConnectionManager.DEFAULT_PORT);
            
            // Close clients
            s1.close();
            s2.close();
        } catch (IOException e) {
            if (cm != null) {
                cm.close();
            }
            fail("Connection manager constructor threw exception");
        }
        assertTrue(cm.close());
    }
}
