package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
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
        packet.serializeData();
    }
}
