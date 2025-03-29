package com.belgianwaffles.battleship;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

public class SegmentTest {
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void segmentInitializationWithAssetImage() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        assertInstanceOf(Segment.class, segment);
    }
    @Test
    public void segmentInitializationWithImageIcon() {
        ImageIcon img = new ImageIcon("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png");
        Segment segment = new Segment(img, new Vector2(1,1), 3, 1);
        assertInstanceOf(Segment.class, segment);
    }
    @Test
    public void getImageReturnsIcon() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        ImageIcon img = segment.getImage();
        assertInstanceOf(ImageIcon.class, img);
        assertNotNull(img);
    }

    @Test
    public void setImage() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        segment.setImage(new ImageIcon("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipHit.png"));
        ImageIcon img = segment.getImage();
        assertInstanceOf(ImageIcon.class, img);
        assertNotNull(img);
    }

    @Test
    public void getPositionReturnsVector2() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        Vector2 pos = segment.getPosition();
        assertInstanceOf(Vector2.class, pos);
        assertNotNull(pos);
        assertTrue(pos.x == 1 && pos.y == 1);
    }

    @Test
    public void setPosition() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        segment.setPosition(new Vector2(6,6));
        Vector2 pos = segment.getPosition();
        assertInstanceOf(Vector2.class, pos);
        assertNotNull(pos);
        assertTrue(pos.x == 6 && pos.y == 6);
    }

    @Test
    public void getHeightReturns1() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        int height = segment.getHeight();
        assertTrue(height == 1);
    }

    @Test
    public void getWidthReturns3() {
        Segment segment = new Segment("../../../../../main/java/com/belgianwaffles/battleship/assets/ShipTile.png", new Vector2(1,1), 3, 1);
        int width = segment.getWidth();
        assertTrue(width == 3);
    }
}
