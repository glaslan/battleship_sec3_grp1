package com.belgianwaffles.battleship;

import javax.swing.JButton;
import javax.swing.JLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class WindowComponentTest {

    @Test public void testGetters() {

        JLabel componentExpected = new JLabel();

        WindowComponent w = new WindowComponent(componentExpected, 250, 700, 0.7, 0.35);

        // expected values
        int xExpected = 250;
        int yExpected = 700;
        double widthExpected = 0.7;
        double heightExpected = 0.35;

        // assert values
        assertEquals(w.getComponent(), componentExpected);
        assertEquals(w.getBoundX(), xExpected);
        assertEquals(w.getBoundY(), yExpected);
        assertEquals(w.getWidth(), widthExpected);
        assertEquals(w.getHeight(), heightExpected);
    }

    @Test
    public void correctSizeAfterResize() {

        JButton b = new JButton();

        // create window component of size 0.5/0.5 (50% width and 50% height) 
        WindowComponent w = new WindowComponent(b, 400, 400, 0.5, 0.5);

        // resize component assuming screen is 800x600
        w.resize(800, 600);
        int widthExpected = 400;
        int heightExpected = 300;

        // check to see if component is 50% width and height of screen (400, 300)
        assertEquals(w.getComponent().getWidth(), widthExpected);
        assertEquals(w.getComponent().getHeight(), heightExpected);

    }

}