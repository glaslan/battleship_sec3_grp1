package com.belgianwaffles.battleship;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Window extends JFrame implements ActionListener {

    private JButton b_Connect;
    private JButton b_Exit;


    private ArrayList<WindowComponent> elements = new ArrayList<>();
    private ArrayList<AssetImage> loadedImages = new ArrayList<>();




    // for removing clutter in the constructor 
    private void buttonInit(JButton button, double x_bound, double y_bound, double width, double height) {

        button.setBounds((int)(x_bound*Constants.WINDOW_WIDTH),(int)(y_bound*Constants.WINDOW_HEIGHT),(int)(width*Constants.WINDOW_WIDTH),(int)(height*Constants.WINDOW_HEIGHT));
        button.addActionListener(this);
        button.setVisible(false);
        this.add(button);

        elements.add(new WindowComponent(button, x_bound, y_bound, width, height));
    }

    private void componentInit(JComponent component, double x_bound, double y_bound, double width, double height) {

        component.setBounds((int)(x_bound*Constants.WINDOW_WIDTH),(int)(y_bound*Constants.WINDOW_HEIGHT),(int)(width*Constants.WINDOW_WIDTH),(int)(height*Constants.WINDOW_HEIGHT));
        component.setVisible(false);
        this.add(component);

        elements.add(new WindowComponent(component, x_bound, y_bound, width, height));

    }


    // all images must use this function or else they will not be scaled when screen resized
    private void imageInit(AssetImage img) {
        loadedImages.add(img);
    }



    public void clearScreen() {
        for (WindowComponent c : elements) {
            c.setVisible(false);
        }
    }



    
    public Window() {

        this.setLayout(null);
        this.setVisible(true);
        this.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        // Buttons
        AssetImage test = new AssetImage(new ImageIcon("ThisBeAnAsset.png"), 0.6, 0.15, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        imageInit(test);

        b_Connect = new JButton("Connect");
        buttonInit(b_Connect, 0.2, 0.7, 0.6, 0.15);
        b_Connect.setVisible(true);

        b_Exit = new JButton("Exit");
        buttonInit(b_Exit, 0.2, 0.3, 0.6, 0.15);
        b_Exit.setVisible(true);




        getContentPane().addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e) {

                for (AssetImage img : loadedImages) {
                    img.resizeImage(getWidth(), getHeight());
                }
                for (WindowComponent comp : elements) {
                    comp.resize(getWidth(), getHeight());
                }
            }
         });


        
    }

    






    @Override
    public void actionPerformed(ActionEvent e) {

    }
    
}
