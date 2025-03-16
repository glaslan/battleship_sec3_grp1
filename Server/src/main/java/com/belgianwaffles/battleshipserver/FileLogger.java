package com.belgianwaffles.battleshipserver;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLogger {

    private static final Logger logger = Logger.getLogger("FileLogger");
    private FileHandler handler = null;

    public FileLogger() {

        try {
            // set false to get rid of append
            this.handler = new FileHandler("messageLog.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            this.handler.setFormatter(formatter);
            // not sure if this is needed?
            // logger.setUseParentHandlers(false);

        } catch (IOException | SecurityException ex) {
            System.out.println("Error creating file logger.");
        }
        
    }

    // maybe a little redundant but unique log file names could be useful
    public FileLogger(String fileName) {

        try {
            // set false to get rid of append
            this.handler = new FileHandler(fileName, true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            this.handler.setFormatter(formatter);
            // not sure if this is needed?
            // logger.setUseParentHandlers(false);

        } catch (IOException | SecurityException ex) {
            System.out.println("Error creating file logger.");
        }
        
    }

    public void logMessage(String message) {
        logger.info(message);
    }
}
