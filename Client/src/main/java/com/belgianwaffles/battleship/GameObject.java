package com.belgianwaffles.battleship;

import javax.swing.ImageIcon;

public abstract class GameObject {
    public abstract ImageIcon getImage();
    public abstract void setImage(ImageIcon img);
    public abstract Vector2 getPosition();
    public abstract void setPosition(Vector2 pos);
    public abstract int getWidth();
    public abstract int getHeight();
}
