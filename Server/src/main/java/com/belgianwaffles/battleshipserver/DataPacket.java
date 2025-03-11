package com.belgianwaffles.battleshipserver;

public final class DataPacket {

    // ----- Subclasses -----
    
    public final class Header {
        
        // ----- Constants -----
        
        public static final int HEADER_SIZE       = 5;

        private final byte HEAD_MASK_FLAGS   = (byte)0b11110000;
        private final byte HEAD_MASK_TYPE    = (byte)0b00001110;
        private final byte HEAD_MASK_TURN    = (byte)0b00000001;
        private final byte HEAD_MASK_USER    = (byte)0b11111111;
        private final byte HEAD_MASK_LENGTH  = (byte)0b11111111;
        
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
        
        /**
         * Creates header with a given type
         * @param type type of header to prepare
         */
        public Header(int type) {
            this.mData = new byte[HEADER_SIZE];
            switch(type) {
                case DataPacket.PACKET_TYPE_PING:
                    this.addType(PACKET_TYPE_PING);
                    this.addLength((short)0);
                    break;
                case DataPacket.PACKET_TYPE_GAME:
                    this.addType(PACKET_TYPE_GAME);
                    break;
            }
        }

        /**
         * Creates header from received <code>byte[]</code>
         * @param bytes data received over socket
         */
        public Header(byte[] bytes) {
            this.mData = new byte[HEADER_SIZE];
            System.out.println(bytes[0]);
            System.arraycopy(bytes, 0, this.mData, 0, HEADER_SIZE);
            System.out.println(this.getType());
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
    
    public static final byte PACKET_TYPE_PING   = 1;
    public static final byte PACKET_TYPE_GAME   = 2;
    private static final int PACKET_TAIL_SIZE   = 1;
    

    
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
    
    /**
     * Creates an empty, untyped packet
     * @param none idk, you didn't put anything in there so you get a packet of you-can-do-stuff-to-it-able
     */
    public DataPacket() {
        this.mHeader = new Header();
        this.mBody = new byte[1];
        this.mTail = new byte[PACKET_TAIL_SIZE];
        this.mTail[0] = '\n';
    }
    
    /**
     * Creates a packet with a header of specified type
     * @param type type of header to initialize with
     */
    public DataPacket(int type) {
        this.mHeader = new Header(type);
        this.mBody = new byte[1];
        this.mTail = new byte[PACKET_TAIL_SIZE];
        this.mTail[0] = '\n';
    }

    /**
     * Creates a packet from a received <code>byte[]</code>
     * @param bytes received buffer of bytes from socket
     */
    public DataPacket(byte[] bytes) {
        this.mHeader = new Header(bytes);
        this.mBody = new byte[this.mHeader.getLength() + PACKET_TAIL_SIZE];
        System.arraycopy(bytes, Header.HEADER_SIZE, this.mBody, 0, this.mHeader.getLength());
        this.mData = new byte[1];
    }
    
    public int getType() {
        return this.mHeader.getType();
    }

    public int getLength() {
        return this.mHeader.getLength();
    }
    
    public byte[] getBuffer() {
        return this.mData;
    }
    
    
    
    // ----- Serialization -----

    /**
     * Requires a length to be set in header for the size of the body before the call to this function
     */
    private void setHeader() {
        this.mData = new byte[Header.HEADER_SIZE + this.mHeader.getLength() + PACKET_TAIL_SIZE];
        System.arraycopy(this.mHeader.getData(), 0, this.mData, 0, Header.HEADER_SIZE);
    }
    
    private void setByte(int index, byte data) {
        this.mBody[index + Header.HEADER_SIZE - 1] = data;
    }

    private void setTail() {
        System.arraycopy(this.mTail, 0, this.mData, Header.HEADER_SIZE + this.mHeader.getLength(), PACKET_TAIL_SIZE);
    }
    
    /**
     *  Sets up packet with ping data
     * @param none Creates a ping packet 
     */
    public void serializeData() {
        // Add header
        this.mHeader = new Header(PACKET_TYPE_PING);
        this.mHeader.addLength((short)0);
        this.setHeader();
        
        // Empty body, but not null
        this.mBody = new byte[Header.HEADER_SIZE];
        this.setByte(0, (byte)0);
        
        // Add tail
        this.setTail();
    }
    
    /**
     *  Sets up data with game state data
     * @param grid Uses the given grid and adds its data to the packet
     */
    public void serializeData(Grid grid) {
        // Add header info
        this.mHeader.addType(PACKET_TYPE_GAME);
        this.mHeader.addLength((short)(Grid.GRID_SIZE * Grid.GRID_SIZE));
        this.setHeader();
        
        // Copy cell data into packet body
        var cells = grid.getCells();
        for (int i = 0; i < Grid.GRID_SIZE; i++) {
            for (int j = 0; j < Grid.GRID_SIZE; j++) {
                int index = i * Grid.GRID_SIZE + j;
                this.setByte(index, cells[i][j].getCell());
            }
        }

        // Add tail
        this.setTail();
    }



    // ----- Deserialization -----

    public Grid deserialize() {
        Grid grid = new Grid(this.mBody);
        return grid;
    }
}
