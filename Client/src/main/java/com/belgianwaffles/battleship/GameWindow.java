package com.belgianwaffles.battleship;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLDocument;

public class GameWindow extends JFrame implements ActionListener {

    private JButton b_Connect;
    private JButton b_Exit;

    private JLabel l_title;

    private boolean clientTurn;

    private ClientConnectionManager connection;


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

    private boolean clicked(WindowComponent element, Point p) {

        // check if within x
        if (p.getX() >= getWindowXPosition(element.getBoundX()) && 
            p.getX() <= getWindowXPosition(element.getBoundX() + element.getWidth()) &&
            p.getY() >= getWindowYPosition(element.getBoundY()) && 
            p.getY() <= getWindowYPosition(element.getBoundY() + element.getHeight())) 
        {
            System.out.println(getWindowXPosition(element.getBoundX()));
            System.out.println(getWindowXPosition(element.getBoundX() + element.getWidth()));
            System.out.println(getWindowYPosition(element.getBoundY()));
            System.out.println(getWindowYPosition(element.getBoundY() + element.getHeight()));
            return true;
            
        }   return false;
    }

    private int getWindowXPosition(double relative) {
        return (int)(relative * getWidth());
    }

    private int getWindowYPosition(double relative) {
        return (int)(relative * getHeight());
    }


    // all images must use this function or else they will not be scaled when screen resized
    private void imageInit(AssetImage img) {
        loadedImages.add(img);
    }

    private WindowComponent getWindowComponent(JComponent element) {
        for (WindowComponent e : elements) {
            if (e.getComponent().equals(element)) {
                return e;
            }
        }

        return null;
    }



    public void clearScreen() {
        for (WindowComponent c : elements) {
            c.setVisible(false);
        }
    }



    
    public GameWindow() {

        this.setLayout(null);
        this.setUndecorated(true);
        this.setVisible(true);
        this.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        


        // Buttons
        AssetImage test = new AssetImage(new ImageIcon("assets/ThisBeAnAsset.png"), 0.6, 0.15, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        imageInit(test);

        b_Connect = new JButton("Connect");
        buttonInit(b_Connect, 0.2, 0.7, 0.6, 0.15);
        b_Connect.setVisible(true);

        b_Exit = new JButton(test);
        buttonInit(b_Exit, 0.2, 0.3, 0.6, 0.15);
        b_Exit.setVisible(true);

        // JLabels
        l_title = new JLabel("Battleship");
        componentInit(l_title, 0.3, 0.1, 0.4, 0.2);
        l_title.setVisible(true);
        l_title.setBorder(BorderFactory.createLineBorder(Color.black));
        

        




        getContentPane().addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e) {

                // resize all images/assets
                for (AssetImage img : loadedImages) {
                    img.resizeImage(getWidth(), getHeight());
                }

                // resize all screen components i.e. buttons, labels, text fields, etc.
                for (WindowComponent comp : elements) {
                    comp.resize(getWidth(), getHeight());
                    System.out.println(comp.getBoundY() * getHeight());
                    System.out.println(comp.getBoundX() * getWidth());
                }
            }
         });


         this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                
                System.out.println("Mouse clicked at: "+e.getPoint());

                if (clicked(getWindowComponent(l_title), e.getPoint())) {
                    System.out.println("Im a title");
                }
            }
         });




         clientTurn = false;
         this.setSize((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());

         

         

        
    }

    






    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == b_Connect) {
            connectToServer();
        }   

    }

    private void startGame() {
        clientTurn = false;
    }

    private void connectToServer() {

        // Establish connection thread 
        connection = new ClientConnectionManager(this);

        Thread connectionThread = new Thread(connection);
        connectionThread.start();

    }
    
}
