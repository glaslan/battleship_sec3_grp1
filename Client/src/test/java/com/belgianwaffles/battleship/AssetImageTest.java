package com.belgianwaffles.battleship;

import javax.swing.ImageIcon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AssetImageTest {

    @Test
    public void imageSizeTest() {


        ImageIcon img = new ImageIcon(Constants.ASSET_PATH + "Miss.png");
        AssetImage aImg = new AssetImage(img, 0.5, 0.25, 100, 100);

        // check dimensions of initial image
        int initialWidthExpected = 50;
        int initialHeightExpected = 25;



        assertEquals(aImg.getIconWidth(), initialWidthExpected);
        assertEquals(aImg.getIconHeight(), initialHeightExpected);

        // resize and check dimenions after resizing 
        aImg.resizeImage(300, 300);

        // check dimensions of initial image
        int resizeWidthExpected = 150;
        int resizeHeightExpected = 75;

        assertEquals(aImg.getIconWidth(), resizeWidthExpected);
        assertEquals(aImg.getIconHeight(), resizeHeightExpected);
        
    }

    /// No need to test anything else since AssetImage extends from ImageIcon ///
    /// Only has 1 function outside of extended functions ///

}