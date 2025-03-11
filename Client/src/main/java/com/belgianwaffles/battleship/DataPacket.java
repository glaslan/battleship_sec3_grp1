package com.belgianwaffles.battleship;

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
        
        public Header() {
            this.mData = new byte[HEADER_SIZE];
        }

        public Header(byte[] bytes) {
            this.mData = new byte[HEADER_SIZE];
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
    
    public static final byte PACKET_TYPE_PING   = 1;
    public static final byte PACKET_TYPE_GAME   = 2;
    

    
    // ----- Data -----
    
    // Data packet header
    private Header mHeader;
    
    // Body of packet
    private byte[] mBody;

    

    // ----- Methods -----
    
    public DataPacket() {
        this.mHeader = new Header();
    }

    public DataPacket(byte[] bytes) {
        this.mHeader = new Header(bytes);
        this.mBody = new byte[this.mHeader.getLength()];
        System.arraycopy(bytes, Header.HEADER_SIZE, this.mBody, 0, this.mHeader.getLength());
    }

    public int getType() {
        return this.mHeader.getType();
    }
    
    public byte[] getBuffer() {
        return this.mBody;
    }



    // ----- Serialization -----

    private void setByte(int index, byte data) {
        this.mBody[index] = data;
    }

    // Sets up data with game state data
    public void serializeData(Grid grid) {
        // Add length for header info
        this.mHeader.addType(PACKET_TYPE_GAME);
        this.mHeader.addLength((short)(Grid.GRID_SIZE * Grid.GRID_SIZE));
        
        // Copy header into packet
        this.mBody = new byte[Header.HEADER_SIZE + this.mHeader.getLength()];
        System.arraycopy(this.mHeader.getData(), 0, this.mBody, 0, Header.HEADER_SIZE);
        
        // Copy cell data into packet
        var cells = grid.getCells();
        for (int i = 0; i < Grid.GRID_SIZE; i++) {
            for (int j = 0; j < Grid.GRID_SIZE; j++) {
                int index = i * Grid.GRID_SIZE + j + Header.HEADER_SIZE;
                this.setByte(index, cells[i][j].getCell());
            }
        }
    }
    
    // Sets up packet with ping data
    public void serializeData() {
        this.mHeader.addType(PACKET_TYPE_PING);
        this.mHeader.addLength((short)0);

        // Empty body, but not null
        this.mBody = new byte[1];
        this.setByte(0, (byte)0);
    }
}
