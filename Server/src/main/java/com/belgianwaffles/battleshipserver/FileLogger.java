package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class FileLogger {

    

    // Constants

    private static final String LOG_PING  = "ping.log";
    private static final String LOG_GAME  = "game.log";
    private static final String LOG_ERROR = "error.log";
    private static final String LOG_PATH  = "Logs/";

    private static final Logger logger = Logger.getLogger("FileLogger");

    // Data

    private static FileHandler pingLog = null;
    private static FileHandler gameLog = null;
    private static FileHandler errorLog = null;

    /**
     * Initializes the logger with all needed paths, settings and formatting
     */
    public static void initLogger() {

        try {
            // set false to get rid of append
            pingLog = new FileHandler(LOG_PATH+LOG_PING, true);
            gameLog = new FileHandler(LOG_PATH+LOG_GAME, true);
            errorLog = new FileHandler(LOG_PATH+LOG_ERROR, true);
            
            SimpleFormatter formatter = new SimpleFormatter();
            gameLog.setFormatter(formatter);
            pingLog.setFormatter(formatter);
            errorLog.setFormatter(formatter);
            // not sure if this is needed?
            logger.setUseParentHandlers(false);

        } catch (IOException | SecurityException ex) {
            System.out.println("Error creating file logger.");
        }
        
    }

    /**
     * Logs a message being transmitted or received to the game log
     * @param message <code>String</code> Message to be written to the log
     */
    public static void logMessage(String message) {
        // logger is pain and this is how you have to manage multiple files
        logger.addHandler(gameLog);
        logger.log(Level.INFO, "{0}\n", message);
        logger.removeHandler(gameLog);
    }

    /**
     * Logs an error to the error log
     * @param <T>
     * @param sourceClass <code>Class</code> Class the error came from
     * @param sourceMethod <code>String</code> Class the error came from
     * @param message <code>String</code> Message to be written to the log
     */
    public static synchronized <T> void logError(Class <T> sourceClass, String sourceMethod, String message) {
        // this is why logger got deprecated
        logger.addHandler(errorLog);
        logger.logp(Level.WARNING, sourceClass.getName(), sourceMethod, 
        "Error in class " + sourceClass.getName() + " in method " + sourceMethod + 
        "\nmessage: " + message + "\n");
        logger.removeHandler(errorLog);
    }

    /**
     * Logs a ping to the ping log
     * @param message <code>String</code> Ping to be written to the log
     */
    public static void logPing(String message) {
        logger.addHandler(pingLog);
        logger.log(Level.INFO, "{0}\n", message);
        logger.removeHandler(pingLog);
    }

}
