package com.belgianwaffles.battleshipserver;

public final class DataPacket {

    // ----- Subclasses -----
    
    public final class Header {
        
        // ----- Constants -----
        
        public static final int HEADER_SIZE       = 5;

        public static final byte HEAD_TYPE_PING   = 1;
        public static final byte HEAD_TYPE_GAME   = 2;

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

    // ----- Data -----
    
    // Data packet header
    private Header mHeader;
    
    // Body of packet
    private byte[] mBody;

    // ----- Methods -----
    
    public DataPacket() {
        this.mHeader = new Header();
    }

    // ----- Serialization -----

    private void setByte(int index, byte data) {
        this.mBody[index] = data;
    }

    // Sets up data with game state data
    public void serializeData(Grid grid) {
        // Add length for header info
        this.mHeader.addType(Header.HEAD_TYPE_GAME);
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
        this.mHeader.addType(Header.HEAD_TYPE_PING);
        this.mHeader.addLength((short)0);

        // Empty body, but not null
        this.mBody = new byte[1];
        this.setByte(0, (byte)0);
    }

    public byte[] getBuffer() {
        return this.mBody;
    }
}

/*

public class DataPacket {
    final int gridLength = 10;
    class Header {
        // first 4 bits contain the flag of what is sent
        // last 4 contain flags on whos turn it is
        byte itemSentAndTurn;
        short userKey;
        // might not be needed??? java tcp seems to handle everything without needing a size
        int length;

        Header() {
            this.itemSentAndTurn = 0;
            this.userKey = 0;
            this.length = 0;
        }
        // adjust these depending on the flag and how we make things work
        public void setItemSent(int someFlag) {
            this.itemSentAndTurn = (byte) (this.itemSentAndTurn & 0b00011111);
        }

        public void setTurn(int someFlag) {
            this.itemSentAndTurn = (byte) (this.itemSentAndTurn & 0b11110001);
        }

        public void setUserKey(short userKey) {
            this.userKey = userKey;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Override
        public String toString() {
            return String.valueOf(this.itemSentAndTurn) + String.valueOf(this.userKey) + String.valueOf(this.length);
        }
        
    }

    Header head;
    GridCell [][] boardMatrix;
    String buffer;
    public DataPacket() {
        this.head = new Header();
        this.boardMatrix = new GridCell[gridLength][gridLength];
    }

    public DataPacket(String data) {
        // somehow parse the string data into usable data
    }

    public void setBoardMatrix(GridCell[][] matrix) {
        this.boardMatrix = matrix;
    }

    private void serializeData() {
        this.buffer = this.head.toString();
        for (int i = 0; i < gridLength; i++) {
            for (int j = 0; j < gridLength; j++) {
                this.buffer += this.boardMatrix[i][j];
            }
        }
    }

    public byte[] getBuffer() {
        serializeData();
        return this.buffer;
    }
}
    
*/