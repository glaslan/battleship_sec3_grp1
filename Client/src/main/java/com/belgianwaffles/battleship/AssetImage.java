package com.belgianwaffles.battleship;

import javax.swing.ImageIcon;

public class AssetImage extends ImageIcon {

    private double width;
    private double height;
    private ImageIcon img;

    public AssetImage(ImageIcon img, double width, double height, double screen_width, double screen_height) {

        this.img = img;
        this.width = width;
        this.height = height;

        try {
            this.setImage(img.getImage().getScaledInstance((int)(this.width * screen_width), (int)(this.height * screen_height), java.awt.Image.SCALE_SMOOTH));
        } catch (Exception e) {
            this.setImage(img.getImage());
        }
        
    }

    public void resizeImage(double screen_width, double screen_height) {
        try {
            this.setImage(img.getImage().getScaledInstance((int)(this.width * screen_width), (int)(this.height * screen_height), java.awt.Image.SCALE_SMOOTH));
        } catch (Exception e) {
            this.setImage(img.getImage());
        }
    }


    
}
