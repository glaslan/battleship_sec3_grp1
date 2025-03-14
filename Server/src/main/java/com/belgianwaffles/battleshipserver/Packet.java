package com.belgianwaffles.battleshipserver;

public final class Packet {

    // ----- Header -----

    public static final int HEADER_SIZE        = 5;

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
        }

        public void copy(byte[] bytes) {
            System.arraycopy(bytes, 0, this.mData, 0, HEADER_SIZE);
        }
        
        
        
        // ----- Bit ----- Manipulators -----
        
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
        public void addLength(short length) {
            this.bitManipulate(HEAD_INDEX_LENGTH, HEAD_MASK_LENGTH, (byte)((length & 0xff00) >> 8));
            this.bitManipulate(HEAD_INDEX_LENGTH + 1, HEAD_MASK_LENGTH, (byte)(length & 0x00ff));
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
        public int getTurn() {
            return (this.mData[HEAD_INDEX_TURN] & HEAD_MASK_TURN);
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
            int byte1 = ((int)(this.mData[HEAD_INDEX_USER] & 0b01111111) << 8) | bit1;
            int byte2 = ((int)(this.mData[HEAD_INDEX_USER + 1] & 0b01111111)) | bit2;
            
            return byte1 | byte2;
        }

        /**
         * Gets the length of the packet body
         * @return <code>int</code> length of packet body
         */
        public int getLength() {
            // Get bits to prevent negative shenanigans
            int bit1 = this.mData[HEAD_INDEX_LENGTH] & 0b10000000;
            int bit2 = this.mData[HEAD_INDEX_LENGTH + 1] & 0b10000000;

            // Get rest of byte
            int byte1 = ((int)(this.mData[HEAD_INDEX_LENGTH] & 0b01111111) << 8) | bit1;
            int byte2 = ((int)(this.mData[HEAD_INDEX_LENGTH + 1] & 0b01111111)) | bit2;

            return byte1 | byte2;
        }
        
        /**
         * Gets the header data
         * @return <code>byte[]</code> with header data
         */
        public byte[] getData() {
            return this.mData;
        }
    }

    // ----- Constants -----

    public static final byte PACKET_FLAG_NONE   = 0;

    public static final byte PACKET_TURN_PONE   = 0;
    public static final byte PACKET_TURN_PTWO   = 1;
    
    public static final byte PACKET_TYPE_NONE   = 0;
    public static final byte PACKET_TYPE_PING   = 1;
    public static final byte PACKET_TYPE_GAME   = 2;
    public static final int  PACKET_TAIL_SIZE   = 1;
    


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
     * @param None
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
        this.mHeader.addLength((short)this.mBody.length);
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
            FileLogger.logError(Packet.class, "setByte(int, byte)",  "Body out of bounds");
            System.err.println("Body out of bounds");
        }
    }

    /**
     * Adds body into the packet
     */
    private void setBody() {
        System.arraycopy(this.mBody, 0, this.mData, this.mHeader.getLength(), this.mTail.length);
    }

    /**
     * Adds the tail onto the end of the packet
     */
    private void setTail() {
        System.arraycopy(this.mTail, 0, this.mData, this.mHeader.getLength(), this.mTail.length);
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
     * @param None
     */
    public void serialize() {
        // Setup header
        this.mHeader.addType(PACKET_TYPE_PING);
        
        // Setup body, empty but not null
        this.mBody = new byte[1];
        this.setByte(0, (byte)0);
        
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

        // Get body setup
        this.mBody = new byte[this.mHeader.getLength()];
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
     * @return which players turn it is. Check with PACKET_TURN_P(ONE/TWO)
     */
    public int getTurn() {
        return this.mHeader.getTurn();
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
}
