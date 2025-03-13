package com.belgianwaffles.battleshipserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GridTest {
    /**
     * Tests that a blank grid is created with cells having no values.
     * This will be used by the server to start a new game
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
}
