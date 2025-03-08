package com.belgianwaffles.battleshipserver;

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
    public String getBuffer() {
        serializeData();
        return this.buffer;
    }
}
    
