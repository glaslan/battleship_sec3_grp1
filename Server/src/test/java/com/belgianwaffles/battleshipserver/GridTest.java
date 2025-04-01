package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GridTest {
    /**
     * Tests that a blank grid is created with cells having no values.
     * This will be used by the server to start a new game
     * SVR-GRID-001
     */
    @Test
    public void CreateGridWithDefault() {
        // Arrange
        int expected = 0;
        
        // Act
        Grid grid = new Grid();
        var cells = grid.getCells();
        
        // Assert
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertEquals(expected, cell.getCell());
            }
        }
    }
    /**
     * Test that a grid can be made from a given byte array containing cell information
     * SVR-GRID-002
     */
    @Test
    public void CreateGridWithBytes() {
        // Arrange
        byte expected = 0b00101000;
        byte[] bytes = new byte[Grid.GRID_SIZE * Grid.GRID_SIZE];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = expected;
        }
        
        // Act
        Grid grid = new Grid(bytes);
        var cells = grid.getCells();
        
        // Assert
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertEquals(expected, cell.getCell());
            }
        }
    }
    /**
     * Tests that a grid can be made from another grids cell arrays
     * SVR-GRID-003
     */
    @Test
    public void CreateGridWithCells() {
        // Arrange
        var creationGrid = new Grid();
        var creationCells = creationGrid.getCells();
        for (Grid.GridCell[] subcells : creationCells) {
            for (Grid.GridCell cell : subcells) {
                cell.setSharkP1(true);
                cell.setShipP2(true);
            }
        }
        
        // Act
        Grid grid = new Grid(creationCells);
        var cells = grid.getCells();
        
        // Assert
        
        // Each cell is asserted for all flags
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertTrue(cell.hasSharkP1());
                assertTrue(cell.hasShipP2());
                assertFalse(cell.hasSharkP2());
                assertFalse(cell.hasShipP1());
                assertFalse(cell.hasShotP1());
                assertFalse(cell.hasShotP2());
            }
        }
    }
    /**
     * Tests that a grid can convert into only a p1 grid
     * SVR-GRID-004
     */
    @Test
    public void GetGridPlayer1() {
        // Arrange
        var grid = new Grid();
        var creationCells = grid.getCells();
        for (Grid.GridCell[] subcells : creationCells) {
            for (Grid.GridCell cell : subcells) {
                cell.setSharkP1(true);
                cell.setShipP2(true);
            }
        }
        
        // Act
        grid.getGridP1();
        var cells = grid.getCells();
        
        // Assert
        
        // Each cell is asserted for all flags
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertTrue(cell.hasSharkP1());
                assertFalse(cell.hasShipP2());
                assertFalse(cell.hasSharkP2());
                assertFalse(cell.hasShipP1());
                assertFalse(cell.hasShotP1());
                assertFalse(cell.hasShotP2());
            }
        }
    }
    /**
     * Tests that a grid can convert a p2 grid into only a p1 grid
     * SVR-GRID-005
     */
    @Test
    public void GetGridPlayer2() {
        // Arrange
        var grid = new Grid();
        var creationCells = grid.getCells();
        for (Grid.GridCell[] subcells : creationCells) {
            for (Grid.GridCell cell : subcells) {
                cell.setSharkP1(true);
                cell.setShipP2(true);
            }
        }
        
        // Act
        grid.getGridP2();
        var cells = grid.getCells();
        
        // Assert
        
        // Each cell is asserted for all flags
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertTrue(cell.hasShipP1());
                assertFalse(cell.hasSharkP1());
                assertFalse(cell.hasSharkP2());
                assertFalse(cell.hasShipP2());
                assertFalse(cell.hasShotP1());
                assertFalse(cell.hasShotP2());
            }
        }
    }
    /**
     * Tests that a grid can be translated into a p2 grid
     * SVR-GRID-006
     */
    @Test
    public void TranslateGridP1ToP2() {
        // Arrange
        var grid = new Grid();
        var creationCells = grid.getCells();
        for (Grid.GridCell[] subcells : creationCells) {
            for (Grid.GridCell cell : subcells) {
                cell.setSharkP1(true);
                cell.setShipP2(true);
            }
        }
        
        // Act
        grid.translateP1toP2();
        var cells = grid.getCells();
        
        // Assert
        
        // Each cell is asserted for all flags
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertTrue(cell.hasSharkP2());
                assertFalse(cell.hasSharkP1());
                assertFalse(cell.hasShipP2());
                assertFalse(cell.hasShipP1());
                assertFalse(cell.hasShotP1());
                assertFalse(cell.hasShotP2());
            }
        }
    }
    /**
     * Tests that a grid can translate a p2 grid into a p1 grid with important p2 data that the client will need
     * SVR-GRID-007
     */
    @Test
    public void TranslateGridP2ToP1() {
        // Arrange
        var grid = new Grid();
        var creationCells = grid.getCells();
        for (Grid.GridCell[] subcells : creationCells) {
            for (Grid.GridCell cell : subcells) {
                cell.setSharkP1(true);
                cell.setShotP2(true);
                cell.setShipP1(true);
            }
        }
        
        // Act
        grid.translateP2toP1();
        var cells = grid.getCells();
        
        // Assert
        
        // Each cell is asserted for all flags
        for (Grid.GridCell[] subcells : cells) {
            for (Grid.GridCell cell : subcells) {
                assertFalse(cell.hasSharkP1());
                assertFalse(cell.hasSharkP2());
                assertFalse(cell.hasShipP1());
                assertFalse(cell.hasShipP2());
                assertTrue(cell.hasShotP1());
                assertFalse(cell.hasShotP2());
            }
        }
    }
}
