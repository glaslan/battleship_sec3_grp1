package com.belgianwaffles.battleship;

import java.util.Vector;
import javax.swing.ImageIcon;

public class Segment {
    
    private String assetPath;
    private ImageIcon img;
    private Vector2 position;
    private int width;
    private int height;

    public Segment(String assetPath, Vector2 position, int width, int height) {
        this.assetPath = assetPath;
        this.img = new ImageIcon(assetPath);
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public ImageIcon getImage() {
        return this.img;
    }

    /**
     * Set the image of the segment.
     * @param img the new ImageIcon for the segment
     */
    public void setImage(ImageIcon img) {
        this.img = img;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    /**
     * Returns the width of the segment.
     *
     * @return the width of the segment as an integer
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the segment.
     *
     * @return the height of the segment as an integer
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Show the segment on screen.
     */
    public void show() {
        this.img.setVisible(true);
    }
    
    /**
     * Hide the segment on screen.
     */
    public void hide() {
        this.img.setVisible(false);
    }

}
