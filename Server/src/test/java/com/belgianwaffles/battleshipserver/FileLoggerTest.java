package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FileLoggerTest {
    /**
     * tests initialization of the FileLogger
     */
    @Test
    public void loggerInitializationTest() {
        assertTrue(FileLogger.initLoggerTest());
    }
    /**
     * tests initialization of the FileLogger
     */
    @Test
    public void logMessageTest() {
        FileLogger.initLoggerTest();
        Packet p = new Packet();
        p.addFlag((byte)0b00110011);
        p.addTurn((byte) 3);
        p.addUser((short) 12);
        Grid g = new Grid();
        p.serialize(g);
        assertTrue(FileLogger.logMessage("Message: " + p.toString()));
    }
    /**
     * tests initialization of the FileLogger
     */
    @Test
    public void logErrorTest() {
        assertTrue(FileLogger.initLoggerTest());
        assertTrue(FileLogger.logError(FileLoggerTest.class, "test method", "Error Test passing"));
    }
    /**
     * tests initialization of the FileLogger
     */
    @Test
    public void logPingTest() {
        assertTrue(FileLogger.initLoggerTest());
        Packet p = new Packet();
        p.serialize();
        assertTrue(FileLogger.logPing("Ping: " + p.toString()));
    }
}
