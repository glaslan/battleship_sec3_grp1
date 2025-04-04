package com.belgianwaffles.battleship;

import javax.swing.JComponent;

public class WindowComponent {

    private double width;
    private double height;
    private double x_bound;
    private double y_bound;
    private JComponent comp;

    public WindowComponent(JComponent comp, double x_bound, double y_bound, double width, double height) {
        this.width = width;
        this.height = height;
        this.x_bound = x_bound;
        this.y_bound = y_bound;
        this.comp = comp;
    }

    public void resize(int screen_width, int screen_height) {
        this.comp.setBounds((int)(this.x_bound * screen_width), (int)(this.y_bound * screen_height), (int)(this.width * screen_width), (int)(this.height * screen_height));
        this.comp.revalidate();
    }

    public double getBoundX() {
        return this.x_bound;
    }

    public double getBoundY() {
        return this.y_bound;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public JComponent getComponent() {
        return this.comp;
    }

    
    
    
}
