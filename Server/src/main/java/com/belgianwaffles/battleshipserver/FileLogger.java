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
    private static final String TEST_PATH = LOG_PATH + "Tests/";

    private static final Logger logger = Logger.getLogger("FileLogger");

    // Data

    private static FileHandler pingLog = null;
    private static FileHandler gameLog = null;
    private static FileHandler errorLog = null;


    // Methods

    /**
     * Initializes the logger with all needed file paths, settings and formatting
     */
    public static boolean initLogger() {
        // prevents double initialization
        if (pingLog != null && errorLog != null && gameLog != null) {
            return true;
        }
        try {
            // set false to get rid of append
            // setting pings to false since theres so many of them
            pingLog = new FileHandler(LOG_PATH+LOG_PING, false);
            gameLog = new FileHandler(LOG_PATH+LOG_GAME, true);
            errorLog = new FileHandler(LOG_PATH+LOG_ERROR, true);
        } 
        catch (IOException | SecurityException ex) {
                
            try {
                System.err.println("Could not create file logger in standard log path, retrying.");
                // attempt to cd into server to create logs
                pingLog = new FileHandler("Server/"+LOG_PATH+LOG_PING, false);
                gameLog = new FileHandler("Server/"+LOG_PATH+LOG_GAME, true);
                errorLog = new FileHandler("Server/"+LOG_PATH+LOG_ERROR, true);
            } 
            catch (IOException | SecurityException ex1) {
                System.err.println("Could not create file loggers");
                return false;
            }
            
        }

        SimpleFormatter formatter = new SimpleFormatter();
        gameLog.setFormatter(formatter);
        pingLog.setFormatter(formatter);
        errorLog.setFormatter(formatter);
        // this prevents logger from printing to stdout
        // seeing every ping in stdout would get quite annoying
        logger.setUseParentHandlers(false);

    
        return true;
    }

    // Logging methods

    /**
     * Logs a message being transmitted or received to the game log
     * @param message <code>String</code> Message to be written to the log
     */
    public static boolean logMessage(String message) {
        // logger is pain and this is how you have to manage multiple files
        logger.addHandler(gameLog);
        logger.log(Level.INFO, "{0}\n", message);
        logger.removeHandler(gameLog);
        return true;
    }

    /**
     * Logs an error to the error log
     * @param <T>
     * @param sourceClass <code>Class</code> Class the error came from
     * @param sourceMethod <code>String</code> Class the error came from
     * @param message <code>String</code> Message to be written to the log
     */
    public static synchronized <T> boolean logError(Class <T> sourceClass, String sourceMethod, String message) {
        // this is why logger got deprecated
        logger.addHandler(errorLog);
        logger.logp(Level.WARNING, sourceClass.getName(), sourceMethod, 
        "Error in class " + sourceClass.getName() + " in method " + sourceMethod + 
        "\nmessage: " + message + "\n");
        logger.removeHandler(errorLog);
        return true;
    }

    /**
     * Logs a ping to the ping log
     * @param message <code>String</code> Ping to be written to the log
     */
    public static boolean logPing(String message) {
        logger.addHandler(pingLog);
        logger.log(Level.INFO, "{0}\n", message);
        logger.removeHandler(pingLog);
        return true;
    }


    // TESTING VERSION
    // just so our main log folder doesnt get flooded
    public static boolean initLoggerTest() {
        // prevents double initialization
        if (pingLog != null && errorLog != null && gameLog != null) {
            return true;
        }
        try {
            // set false to get rid of append
            // setting pings to false since theres so many of them
            pingLog = new FileHandler(TEST_PATH+LOG_PING, false);
            gameLog = new FileHandler(TEST_PATH+LOG_GAME, false);
            errorLog = new FileHandler(TEST_PATH+LOG_ERROR, false);
        } 
        catch (IOException | SecurityException ex) {
                
            try {
                System.err.println("Could not create file logger in standard log path, retrying.");
                // attempt to cd into server to create logs
                pingLog = new FileHandler("Server/"+TEST_PATH+LOG_PING, false);
                gameLog = new FileHandler("Server/"+TEST_PATH+LOG_GAME, false);
                errorLog = new FileHandler("Server/"+TEST_PATH+LOG_ERROR, false);
            } 
            catch (IOException | SecurityException ex1) {
                System.err.println("Could not create file loggers");
                return false;
            }
            
        }

        SimpleFormatter formatter = new SimpleFormatter();
        gameLog.setFormatter(formatter);
        pingLog.setFormatter(formatter);
        errorLog.setFormatter(formatter);
        // this prevents logger from printing to stdout
        // seeing every ping in stdout would get quite annoying
        logger.setUseParentHandlers(false);

    
        return true;
    }
}
