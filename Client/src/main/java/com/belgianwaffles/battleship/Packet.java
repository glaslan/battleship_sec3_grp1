package com.belgianwaffles.battleship;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.ImageIcon;

public final class Packet {

    // ----- Header -----

    public static final int HEADER_SIZE        = 7;

    private final class Header {
        
        // ----- Constants -----

        private final byte HEAD_MASK_FLAGS  = (byte)0b11110000;
        private final byte HEAD_MASK_TYPE   = (byte)0b00001110;
        private final byte HEAD_MASK_TURN   = (byte)0b00000001;
        private final byte HEAD_MASK_USER   = (byte)0b11111111;
        private final byte HEAD_MASK_LENGTH = (byte)0b11111111;
        
        private final int HEAD_INDEX_FLAGS  = 0;
        private final int HEAD_INDEX_TYPE   = 0;
        private final int HEAD_INDEX_TURN   = 0;
        private final int HEAD_INDEX_USER   = 1;
        private final int HEAD_INDEX_LENGTH = 3;
        
        
        
        // ----- Data -----
        
        // Contains all header data after packing
        private final byte[] mData;
        
        
        
        // ----- Methods -----
        
        /**
         * Creates a blank header
         */
        public Header() {
            this.mData = new byte[HEADER_SIZE];
            this.addType(PACKET_TYPE_NONE);
        }

        /**
         * Copies data into header
         * @param bytes the bytes to put into the packet header
         */
        public void copy(byte[] bytes) {
            try {
                // Copy data into header
                System.arraycopy(bytes, 0, this.mData, 0, HEADER_SIZE);
            }
            // No out of bounds crashes today
            catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Out of bounds index passed to header");
            }
        }
        
        
        
        // ----- Bit ----- Manipulators -----
        
        /**
         * Manipulates specified bits in header
         * @param byteNum the index in the header, use defined values
         * @param mask the mask for the header item, use defined values
         * @param newData the new data to put into the byte
         */
        private void bitManipulate(int byteNum, byte mask, byte newData) {
            this.mData[byteNum] |= mask & newData;
        }
        
        /**
         * Adds specified flag to packet header
         * @param flag the flag to add
         */
        public void addFlag(byte flag) {
            this.bitManipulate(HEAD_INDEX_FLAGS, HEAD_MASK_FLAGS, flag);
        }
        
        /**
         * Type of packet being created
         * @param type use PACKET_TYPE_####
         */
        public void addType(byte type) {
            this.bitManipulate(HEAD_INDEX_TYPE, HEAD_MASK_TYPE, (byte)(type << 1));
        }
        
        /**
         * Sets the current players turn
         * @param turn use PACKET_TURN_P(ONE/TWO)
         */
        public void addTurn(byte turn) {
            this.bitManipulate(HEAD_INDEX_TURN, HEAD_MASK_TURN, turn);
        }
        
        /**
         * Adds a users id to the packet
         * @param user userId
         */
        public void addUser(short user) {
            this.bitManipulate(HEAD_INDEX_USER, HEAD_MASK_USER, (byte)((user & 0xff00) >> 8));
            this.bitManipulate(HEAD_INDEX_USER + 1, HEAD_MASK_USER, (byte)(user & 0x00ff));
        }
        
        /**
         * Adds length to the packet
         * @param length length of the packet body
         */
        public void addLength(int length) {
            this.bitManipulate(HEAD_INDEX_LENGTH,     HEAD_MASK_LENGTH, (byte)((length & 0xff000000) >> 24));
            this.bitManipulate(HEAD_INDEX_LENGTH + 1, HEAD_MASK_LENGTH, (byte)((length & 0x00ff0000) >> 16));
            this.bitManipulate(HEAD_INDEX_LENGTH + 2, HEAD_MASK_LENGTH, (byte)((length & 0x0000ff00) >> 8));
            this.bitManipulate(HEAD_INDEX_LENGTH + 3, HEAD_MASK_LENGTH, (byte)((length & 0x000000ff)));
        }



        // ----- Getters -----

        /**
         * Checks if packet has a specified flag
         * @param flag
         * @return true if packet has flag
         */
        public boolean hasFlag(byte flag) {
            return (this.mData[HEAD_INDEX_FLAGS] & flag) == flag;
        }
        
        /**
         * Gets the packet type
         * @return the type of packet. Check with PACKET_TYPE_####
         */
        public int getType() {
            return ((this.mData[HEAD_INDEX_TYPE] & HEAD_MASK_TYPE) >> 1);
        }
        
        /**
         * Gets the current players turn type
         * @return which players turn it is. Check with PACKET_TURN_P(ONE/TWO)
         */
        public boolean isTurn() {
            return (this.mData[HEAD_INDEX_TURN] & HEAD_MASK_TURN) == HEAD_MASK_TURN;
        }
        
        /**
         * Gets the userId from the packet
         * @return user specific id
         */
        public int getUser() {
            // Get bits to prevent negative shenanigans
            int bit1 = this.mData[HEAD_INDEX_USER] & 0b10000000;
            int bit2 = this.mData[HEAD_INDEX_USER + 1] & 0b10000000;

            // Get rest of byte
            int byte1 = ((int)(this.mData[HEAD_INDEX_USER] & 0b01111111) << 8) | (bit1 << 8);
            int byte2 = ((int)(this.mData[HEAD_INDEX_USER + 1] & 0b01111111)) | bit2;
            
            return byte1 | byte2;
        }

        /**
         * Gets the length of the packet body
         * @return <code>int</code> length of packet body
         */
        public int getLength() {
            // Get bits to prevent negative shenanigans
            int bit1 = this.mData[HEAD_INDEX_LENGTH]     & 0b10000000;
            int bit2 = this.mData[HEAD_INDEX_LENGTH + 1] & 0b10000000;
            int bit3 = this.mData[HEAD_INDEX_LENGTH + 2] & 0b10000000;
            int bit4 = this.mData[HEAD_INDEX_LENGTH + 3] & 0b10000000;

            // Get rest of byte
            int byte1 = ((int)(this.mData[HEAD_INDEX_LENGTH]     & 0b01111111) << 24) | (bit1 << 24);
            int byte2 = ((int)(this.mData[HEAD_INDEX_LENGTH + 1] & 0b01111111) << 16) | (bit2 << 16);
            int byte3 = ((int)(this.mData[HEAD_INDEX_LENGTH + 2] & 0b01111111) << 8)  | (bit3 << 8);
            int byte4 = ((int)(this.mData[HEAD_INDEX_LENGTH + 3] & 0b01111111))       | (bit4);

            return (byte1 | byte2 | byte3 | byte4);
        }
        
        /**
         * Gets the header data
         * @return <code>byte[]</code> with header data
         */
        public byte[] getData() {
            return this.mData;
        }

        /**
         * Packages the packet into a nicely formatted string
         * @return String of printableness
         */
        @Override
        public String toString() {
            // Packet type
            String str = "Packet type: ";
            switch (this.getType()) {
                case PACKET_TYPE_NONE -> str += "None";
                case PACKET_TYPE_PING -> str += "Ping";
                case PACKET_TYPE_GRID -> str += "Grid";
                case PACKET_TYPE_IMAGE-> str += "Image";
            }

            // Length
            str += ", length: " + this.getLength();

            // Flags
            str += ", flags: " + (int)(this.mData[HEAD_INDEX_FLAGS] & HEAD_MASK_FLAGS);

            // User id
            str += ", userId: " + this.getUser();

            // Newline before return
            str += "\n";
            return str;
        }
    }

    // ----- Constants -----

    // ----- Image ----- Flags -----
    
    public static final byte PACKET_FLAG_NONE       = (byte)0b00000000;
    public static final byte PACKET_FLAG_SHIP_OK    = (byte)0b10000000;
    public static final byte PACKET_FLAG_SHIP_BROKE = (byte)0b01000000;
    public static final byte PACKET_FLAG_WATER      = (byte)0b00100000;

    // ----- Grid ----- Flags -----
    
    public static final byte PACKET_TURN_TRUE       = (byte)0b00000001;
    public static final byte PACKET_FLAG_REFRESH    = (byte)0b10000000;
    public static final byte PACKET_FLAG_CONFIRM    = (byte)0b01000000;
    public static final byte PACKET_FLAG_SHIP_SUNK  = (byte)0b00100000;

    // ----- Flags ----- Flags -----

    public static final byte PACKET_FLAG_WINNER     = (byte)0b10000000;

    // ----- Other ----- Flags -----
    
    public static final byte PACKET_TYPE_NONE       = 0;
    public static final byte PACKET_TYPE_PING       = 1;
    public static final byte PACKET_TYPE_GRID       = 2;
    public static final byte PACKET_TYPE_IMAGE      = 3;
    public static final byte PACKET_TYPE_FLAGS      = 4;
    public static final int  PACKET_TAIL_SIZE       = 1;

    private static final String PACKET_IMAGE_PATH   = "../Assets/";
    


    // ----- Data -----
    
    // Data packet header
    private final Header mHeader;
    
    // Body of packet
    private byte[] mBody;

    // Tail (newline)
    private final byte[] mTail;

    // Packet whole
    private byte[] mData;



    // ----- Methods -----

    // ----- Constructors -----

    /**
     * Creates an empty data packet
     */
    public Packet() {
        this.mHeader = new Header();
        this.mBody = new byte[1];
        this.mTail = new byte[]{'\n'};
    }



    // ----- Packing -----

    /**
     * Requires body to be initialized to 
     */
    private void setHeader() {
        this.mHeader.addLength(this.mBody.length);
        this.mData = new byte[HEADER_SIZE + this.mHeader.getLength() + PACKET_TAIL_SIZE];
        System.arraycopy(this.mHeader.getData(), 0, this.mData, 0, HEADER_SIZE);
    }
    
    /**
     * Sets the specified index of the packet body to the given byte
     * @param index the index of the body to set byte in
     * @param data byte information for packet
     */
    private void setByte(int index, byte data) {
        try {
            this.mBody[index] = data;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Body out of bounds");
        }
    }

    /**
     * Adds body into the packet
     */
    private void setBody() {
        System.arraycopy(this.mBody, 0, this.mData, HEADER_SIZE, this.getLength());
    }

    /**
     * Adds the tail onto the end of the packet
     */
    private void setTail() {
        System.arraycopy(this.mTail, 0, this.mData, this.mHeader.getLength() + HEADER_SIZE, this.mTail.length);
    }

    /**
     * Packs all of the data into the packet after preparation
     */
    private void pack() {
        this.setHeader();
        this.setBody();
        this.setTail();
    }



    // ----- Serialization -----

    /**
     * Serializes a ping packet
     */
    public void serialize() {
        // Setup header
        this.mHeader.addType(PACKET_TYPE_PING);
        
        // Setup body, empty but not null
        this.mBody = new byte[1];
        
        // Pack data to packet
        this.pack();
    }

    /**
     * Serialized a game over packet
     * @param isWinner true if this player is the winner, false if they are not
     */
    public void serialize(boolean isWinner) {
        // Setup header
        this.mHeader.addType(PACKET_TYPE_FLAGS);
        if (isWinner) { this.addFlag(PACKET_FLAG_WINNER); }
        
        // Setup body, empty but not null
        this.mBody = new byte[1];
        this.setByte(0, (byte)0);
        
        // Pack data to packet
        this.pack();
    }

    /**
     * Serializes a grid packet for game information
     * @param grid the grid to serialize into the packet
     */
    public void serialize(Grid grid) {
        // Setup header
        this.mHeader.addType(PACKET_TYPE_GRID);
        
        // Setup body with gridcell information
        this.mBody = new byte[Grid.GRID_SIZE * Grid.GRID_SIZE];
        var cells = grid.getCells();
        for (int i = 0; i < this.mBody.length; i++) {
            this.setByte(i, cells[i / Grid.GRID_SIZE][i % Grid.GRID_SIZE].getCell());
        }
        
        // Pack data to packet
        this.pack();
    }

    /**
     * Serialized an image packet to send assets to the client
     * Recommended to set the flag for type after serializing
     * @param filename the name of the image file that will be sent to the client
     */
    public void serialize(String filename) {
        this.mHeader.addType(PACKET_TYPE_IMAGE);

        // Prepare
        try {
            File file = new File(PACKET_IMAGE_PATH + filename);
            this.mBody = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return;
        }

        // Pack data to packet
        this.pack();
    }

    /**
     * Takes an array of bytes from socket
     * @param <code>byte[]</code> array of bytes from socket
     */
    public void deserialize(byte[] bytes) {
        // Copy header info
        this.mHeader.copy(bytes);

        // Protect against out of index
        if (bytes.length == HEADER_SIZE) {
            return;
        }

        // Get body setup
        this.mBody = new byte[this.getLength()];
        System.arraycopy(bytes, HEADER_SIZE, this.mBody, 0, this.mBody.length);

        // Pack back into data
        this.pack();
    }



    // ----- Setters -----

    /**
     * Adds specified flag to packet header
     * @param flag the flag to add
     */
    public void addFlag(byte flag) {
        this.mHeader.addFlag(flag);
    }
    
    /**
     * Sets the current players turn
     * @param turn use PACKET_TURN_P(ONE/TWO)
     */
    public void addTurn(byte turn) {
        this.mHeader.addTurn(turn);
    }
    
    /**
     * Adds a users id to the packet
     * @param user userId
     */
    public void addUser(short user) {
        this.mHeader.addUser(user);
    }



    // ----- Getters -----

    /**
     * Checks if packet has a specified flag
     * @param flag
     * @return true if packet has flag
     */
    public boolean hasFlag(byte flag) {
        return this.mHeader.hasFlag(flag);
    }
    
    /**
     * Gets the packet type
     * @return the type of packet. Check with PACKET_TYPE_####
     */
    public int getType() {
        return this.mHeader.getType();
    }
    
    /**
     * Gets the current players turn type
     * @return if it is your turn to play
     */
    public boolean isTurn() {
        return this.mHeader.isTurn();
    }
    
    /**
     * Gets the userId from the packet
     * @return user specific id
     */
    public int getUser() {
        return this.mHeader.getUser();
    }

    /**
     * Gets the length of the packet body
     * @return <code>int</code> length of packet body
     */
    public int getLength() {
        return this.mHeader.getLength();
    }
    
    /**
     * Gets the databuffer, should make a call to serialize before to pack specific data
     * @return serialized data buffer, null if never serialized
     */
    public byte[] getBuffer() {
        return this.mData;
    }

    /**
     * Gets a grid object from a packet
     * @return Grid from packet body
     * @throws IllegalStateException if not of type PACKET_TYPE_GRID
     */
    public Grid getGrid() throws IllegalStateException {
        if ((byte)this.getType() != PACKET_TYPE_GRID) {
            throw new IllegalStateException();
        }
        return new Grid(this.mBody);
    }

    /**
     * Gets an image object from a packet
     * @return Image from packet body
     * @throws IllegalStateException if not of type PACKET_TYPE_IMAGE
     */
    public ImageIcon getImage() throws IllegalStateException {
        if ((byte)this.getType() != PACKET_TYPE_IMAGE) {
            throw new IllegalStateException();
        }
        return new ImageIcon(this.mBody);
    }

    /**
     * Allows for the packet to easily be printed to a file
     * @return a formatted string for a file
     */
    @Override
    public String toString() {
        String str = this.mHeader.toString();
        switch (this.getType()) {
            case PACKET_TYPE_NONE -> str += this.noneString();
            case PACKET_TYPE_PING -> str += this.pingString();
            case PACKET_TYPE_GRID -> str += this.gridString();
            case PACKET_TYPE_IMAGE-> str += this.assetString();
        }
        return str;
    }

    /**
     * Formatted string for packet of type none
     * @return string for none
     */
    private String noneString() {
        // Nothing todo
        return "";
    }

    /**
     * Formatted string for packet of type ping
     * @return string for ping
     */
    private String pingString() {
        // Nothing todo
        return "";
    }
    
    /**
     * Formatted string for packet of type grid
     * @return string for grid
     */
    private String gridString() {
        return this.getGrid().toString();
    }
    
    /**
     * Formatted string for packet of type image
     * @return string for image
     */
    private String assetString() {
        String str = "Sent icon of type: ";
        if (this.hasFlag(PACKET_FLAG_WATER)) {
            str += "Water";
        }
        else if (this.hasFlag(PACKET_FLAG_SHIP_OK)) {
            str += "Ship-Ok";
        }
        else if (this.hasFlag(PACKET_FLAG_SHIP_BROKE)) {
            str += "Ship-Broken";
        }
        else {
            str += "Unknown";
        }
        return str;
    }
}
