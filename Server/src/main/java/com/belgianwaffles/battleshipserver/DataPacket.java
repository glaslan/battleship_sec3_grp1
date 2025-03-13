package com.belgianwaffles.battleshipserver;

public final class DataPacket {

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
        private byte[] mData;
        
        
        
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
        
        public void addFlag(byte flag) {
            this.bitManipulate(HEAD_INDEX_FLAGS, HEAD_MASK_FLAGS, flag);
        }
        
        public void addType(byte type) {
            this.bitManipulate(HEAD_INDEX_TYPE, HEAD_MASK_TYPE, (byte)(type << 1));
        }
        
        public void addTurn(byte turn) {
            this.bitManipulate(HEAD_INDEX_TURN, HEAD_MASK_TURN, turn);
        }
        
        public void addUser(short user) {
            this.bitManipulate(HEAD_INDEX_USER, HEAD_MASK_USER, (byte)((user & 0xff00) >> 8));
            this.bitManipulate(HEAD_INDEX_USER + 1, HEAD_MASK_USER, (byte)(user & 0x00ff));
        }
        
        public void addLength(short length) {
            this.bitManipulate(HEAD_INDEX_LENGTH, HEAD_MASK_LENGTH, (byte)((length & 0xff00) >> 8));
            this.bitManipulate(HEAD_INDEX_LENGTH + 1, HEAD_MASK_LENGTH, (byte)(length & 0x00ff));
        }
        
        public int getType() {
            return ((this.mData[HEAD_INDEX_TYPE] & HEAD_MASK_TYPE) >> 1);
        }
        
        public int getLength() {
            return ((this.mData[HEAD_INDEX_LENGTH] << 8) | this.mData[HEAD_INDEX_LENGTH + 1]);
        }
        
        public byte[] getData() {
            return this.mData;
        }
    }

    // ----- Constants -----
    
    public static final byte PACKET_TYPE_NONE   = 0;
    public static final byte PACKET_TYPE_PING   = 1;
    public static final byte PACKET_TYPE_GAME   = 2;
    public static final int  PACKET_TAIL_SIZE   = 1;
    


    // ----- Data -----
    
    // Data packet header
    private Header mHeader;
    
    // Body of packet
    private byte[] mBody;

    // Tail (newline)
    private byte[] mTail;

    // Packet whole
    private byte[] mData;



    // ----- Methods -----

    // ----- Constructors -----

    /**
     * Creates an empty data packet
     * @param None
     */
    public DataPacket() {
        this.mHeader = new Header();
        this.mBody = new byte[1];
        this.mTail = new byte[]{'\n'};
    }



    // ----- Setters -----

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
            Log.error(DataPacket.class, "setByte(int, byte)",  "Body out of bounds");
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



    // ----- Getters -----

    /**
     * Gets the length of the body for the packet
     * @return packet body length
     */
    public int getLength() {
        return this.mHeader.getLength();
    }
    
    /**
     * Gets the type of data packet
     * @return packet type
     */
    public int getType() {
        return this.mHeader.getType();
    }
    
    /**
     * Gets the databuffer, should make a call to serialize before to pack specific data
     * @return serialized data buffer, null if never serialized
     */
    public byte[] getBuffer() {
        return this.mData;
    }
}
