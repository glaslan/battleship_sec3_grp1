package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class PacketTest {
    /**
     * Creates a ping packet and tests that there is no data inside it.
     * Packet should have a size of 9 (Head = 7, Body = 1, Tail = 1), but no data in that byte.
     * SVR-PKT-001
     */
    @Test
    public void CreatePacketParamsNone() {
        // Arrange
        int expectedSize = 1;
        int expectedType = Packet.PACKET_TYPE_PING;
        int expectedData = 0;
        int expectedLength = Packet.HEADER_SIZE + 1 + Packet.PACKET_TAIL_SIZE;
        
        // Act
        Packet packet = new Packet();
        packet.serialize();
        
        // Gather
        byte[] bytes = packet.getBuffer();
        
        int actualSize = packet.getLength();
        int actualType = packet.getType();
        int actualData = bytes[Packet.HEADER_SIZE];
        int actualLength = bytes.length;
        
        // Assert
        assertEquals(expectedSize, actualSize);
        assertEquals(expectedType, actualType);
        assertEquals(expectedData, actualData);
        assertEquals(expectedLength, actualLength);
    }
    /**
     * Creates a ping packet, then check that another packet can deserialize the bytes
     * SVR-PKT-002
     */
    @Test
    public void PacketDeserialize() {
        // Arrange
        Packet ping = new Packet();
        ping.serialize();
        
        int expectedSize = 1;
        int expectedType = Packet.PACKET_TYPE_PING;
        int expectedData = 0;
        int expectedLength = Packet.HEADER_SIZE + 1 + Packet.PACKET_TAIL_SIZE;
        
        // Act
        Packet packet = new Packet();
        packet.deserialize(ping.getBuffer());
        
        // Gather
        byte[] bytes = packet.getBuffer();
        
        int actualSize = packet.getLength();
        int actualType = packet.getType();
        int actualData = bytes[Packet.HEADER_SIZE];
        int actualLength = bytes.length;
        
        // Assert
        assertEquals(expectedSize, actualSize);
        assertEquals(expectedType, actualType);
        assertEquals(expectedData, actualData);
        assertEquals(expectedLength, actualLength);
    }
    /**
     * Adds all flags to a packet and checks they are saved properly
     * SVR-PKT-003
     */
    @Test
    public void PacketAllFlags() {
        // Arrange
        Packet packet = new Packet();
        int expectedUser = 0b0000111010100100;
        
        // Act
        packet.addFlag(Packet.PACKET_FLAG_NONE);
        packet.addTurn(Packet.PACKET_TURN_PONE);
        packet.addUser((short)expectedUser);
        packet.serialize();
        
        // Assert
        assertTrue(packet.hasFlag(Packet.PACKET_FLAG_NONE));
        assertEquals(expectedUser, packet.getUser());
    }
    /**
     * Pack a grid into packet, then get grid back
     * SVR-PKT-004
     */
    @Test
    public void PacketGridInOut() {
        // Arrange
        Grid grid = new Grid();
        var cells = grid.getCells();
        for (int i = 0; i < Grid.GRID_SIZE * Grid.GRID_SIZE; i++) {
            cells[i / Grid.GRID_SIZE][i % Grid.GRID_SIZE] = grid.new GridCell((byte)i);
        }
        grid = new Grid(cells);
        
        // Act
        Packet packet = new Packet();
        packet.serialize(grid);
        Packet recv = new Packet();
        recv.deserialize(packet.getBuffer());
        var packetedGrid = recv.getGrid();
        
        // Assert
        var packetedCells = packetedGrid.getCells();
        for (int i = 0; i < Grid.GRID_SIZE * Grid.GRID_SIZE; i++) {
            assertEquals(i, packetedCells[i / Grid.GRID_SIZE][i % Grid.GRID_SIZE].getCell());
        }
    }
    /**
     * Test that a non grid packet gets thrown
     * SVR-PKT-005
     */
    @Test
    public void PacketGridThrowsIllegalState() {
        // Arrange
        // Expected is an IllegalStateException throw
        
        // Act
        Packet packet = new Packet();
        packet.serialize();
        
        // Assert
        try {
            packet.getGrid();
            fail("Test did not throw");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }
    /**
     * Pack an image into packet, then get image back
     * SVR-PKT-006
     */
    @Test
    public void PacketImageCreateReceive() {
        // Arrange
        // Length from using Windows properties function on image and finding size
        int expectedLength = 2669115;
        int expectedType = Packet.PACKET_TYPE_IMAGE;
        int expectedUser = 0;

        // Act
        Packet packet = new Packet();
        packet.serialize("test-item.jpeg");
        packet.addFlag(Packet.PACKET_FLAG_SHIP_OK);
        Packet recv = new Packet();
        recv.deserialize(packet.getBuffer());
        // Ensure nothrow
        recv.getImage();
        
        // Assert - since the class is not ours, best I can do is assert the image information in the packet
        assertEquals(expectedLength, recv.getLength());
        assertEquals(expectedType, recv.getType());
        assertEquals(expectedUser, recv.getUser());
        assertTrue(packet.hasFlag(Packet.PACKET_FLAG_SHIP_OK));
        assertFalse(packet.hasFlag(Packet.PACKET_FLAG_SHIP_BROKE));
        assertFalse(packet.hasFlag(Packet.PACKET_FLAG_WATER));
    }
    /**
     * Test that a non image packet gets thrown
     * SVR-PKT-007
     */
    @Test
    public void PacketImageThrowsIllegalState() {
        // Arrange
        // Expected is an IllegalStateException throw
        
        // Act
        Packet packet = new Packet();
        packet.serialize();
        
        // Assert
        try {
            packet.getImage();
            fail("Test did not throw");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }
}
