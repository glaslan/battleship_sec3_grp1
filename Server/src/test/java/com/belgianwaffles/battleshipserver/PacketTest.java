package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PacketTest {
    /**
     * Creates a ping packet and tests that there is no data inside it.
     * Packet should have a size of 7 (Head = 5, Body = 1, Tail = 1), but no data in that byte.
     */
    @Test
    public void CreatePacketParamsNone() {
        // Arrange
        int expectedSize = 1;
        int expectedType = DataPacket.PACKET_TYPE_PING;
        int expectedData = 0;
        int expectedLength = DataPacket.HEADER_SIZE + 1 + DataPacket.PACKET_TAIL_SIZE;
        
        // Act
        DataPacket packet = new DataPacket();
        packet.serialize();

        // Gather
        byte[] bytes = packet.getBuffer();

        int actualSize = packet.getLength();
        int actualType = packet.getType();
        int actualData = bytes[DataPacket.HEADER_SIZE];
        int actualLength = bytes.length;

        // Assert
        assertEquals(expectedSize, actualSize);
        assertEquals(expectedType, actualType);
        assertEquals(expectedData, actualData);
        assertEquals(expectedLength, actualLength);
    }
    /**
     * Creates a ping packet, then check that another packet can deserialize the bytes
     */
    @Test
    public void PacketDeserialize() {
        // Arrange
        DataPacket ping = new DataPacket();
        ping.serialize();
        
        int expectedSize = 1;
        int expectedType = DataPacket.PACKET_TYPE_PING;
        int expectedData = 0;
        int expectedLength = DataPacket.HEADER_SIZE + 1 + DataPacket.PACKET_TAIL_SIZE;
        
        // Act
        DataPacket packet = new DataPacket();
        packet.deserialize(ping.getBuffer());
    
        // Gather
        byte[] bytes = packet.getBuffer();
    
        int actualSize = packet.getLength();
        int actualType = packet.getType();
        int actualData = bytes[DataPacket.HEADER_SIZE];
        int actualLength = bytes.length;
    
        // Assert
        assertEquals(expectedSize, actualSize);
        assertEquals(expectedType, actualType);
        assertEquals(expectedData, actualData);
        assertEquals(expectedLength, actualLength);
    }
}
