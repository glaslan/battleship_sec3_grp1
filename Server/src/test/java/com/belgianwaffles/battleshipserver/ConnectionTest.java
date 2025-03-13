package com.belgianwaffles.battleshipserver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class ConnectionTest {

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

    @Test
    public void SocketTest() {
        DataPacket packet = new DataPacket();
        packet.serialize();
    }
}
