package com.belgianwaffles.battleship;

import javax.swing.ImageIcon;

public class Segment extends GameObject {
    
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

    public Segment(ImageIcon img, Vector2 position, int width, int height) {
        this.img = img;
        this.position = position;
        this.width = width;
        this.height = height;
    }

    @Override
    public ImageIcon getImage() {
        return this.img;
    }

    /**
     * Set the image of the segment.
     * @param img the new ImageIcon for the segment
     */
    @Override
    public void setImage(ImageIcon img) {
        this.img = img;
    }

    @Override
    public Vector2 getPosition() {
        return this.position;
    }

    /**
     * Returns the width of the segment.
     *
     * @return the width of the segment as an integer
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the segment.
     *
     * @return the height of the segment as an integer
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setPosition(Vector2 pos) {
        this.position = pos;
    }

}
