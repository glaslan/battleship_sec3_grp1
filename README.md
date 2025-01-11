# Battleship

## Class Plans
 Not set in stone, just general ideas.

### Managers
 - BoardManager
 - WindowManager
 - SocketManager

### Subclasses
 - DataTransferClass: BoardManager -> DataTransferClass -> SocketManager
 - GridCell: GridCell -> BoardManager
 - Ship: Ship -> BoardManager
