package com.belgianwaffles.battleship;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GameWindow extends JFrame implements ActionListener {

    private JLabel b_Connect;
    private JButton b_Exit;
    private JLabel[][] playerBoardButtons;
    private JLabel[][] opponentBoardButtons;

    private JLabel l_title;
    private AssetImage tileImg;

    private boolean clientTurn;
    private boolean inGame;
    private boolean isTurn;

    private ClientConnectionManager connection;

    private ArrayList<WindowComponent> elements = new ArrayList<>();
    private ArrayList<AssetImage> loadedImages = new ArrayList<>();

    // for removing clutter in the constructor
    private void buttonInit(JButton button, double x_bound, double y_bound, double width, double height) {

        button.setBounds((int) (x_bound * Constants.WINDOW_WIDTH), (int) (y_bound * Constants.WINDOW_HEIGHT),
                (int) (width * Constants.WINDOW_WIDTH), (int) (height * Constants.WINDOW_HEIGHT));
        button.addActionListener(this);
        button.setVisible(false);
        this.add(button);

        elements.add(new WindowComponent(button, x_bound, y_bound, width, height));
    }

    private void componentInit(JComponent component, double x_bound, double y_bound, double width, double height) {

        component.setBounds((int) (x_bound * Constants.WINDOW_WIDTH), (int) (y_bound * Constants.WINDOW_HEIGHT),
                (int) (width * Constants.WINDOW_WIDTH), (int) (height * Constants.WINDOW_HEIGHT));
        component.setVisible(false);
        this.add(component);

        elements.add(new WindowComponent(component, x_bound, y_bound, width, height));

    }

    private boolean clicked(WindowComponent element, Point p) {

        // check if within x
        if (p.getX() >= getWindowXPosition(element.getBoundX()) &&
                p.getX() <= getWindowXPosition(element.getBoundX() + element.getWidth()) &&
                p.getY() >= getWindowYPosition(element.getBoundY()) &&
                p.getY() <= getWindowYPosition(element.getBoundY() + element.getHeight())) {
            return true;

        }
        return false;
    }

    private int getWindowXPosition(double relative) {
        return (int) (relative * getWidth());
    }

    private int getWindowYPosition(double relative) {
        return (int) (relative * getHeight());
    }

    // all images must use this function or else they will not be scaled when screen
    // resized
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
        this.inGame = false;

        // Buttons
        AssetImage test = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "ThisBeAnAsset.png"), 0.6, 0.15,
                Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        imageInit(test);

        addBoardButtons();

        b_Connect = new JLabel(test);
        componentInit(b_Connect, 0.05, 0.8, 0.4, 0.15);
        b_Connect.setVisible(true);

        b_Exit = new JButton(test);
        buttonInit(b_Exit, 0.55, 0.8, 0.4, 0.15);
        b_Exit.setVisible(true);

        // JLabels
        l_title = new JLabel("Battleship");
        componentInit(l_title, 0.3, 0.7, 0.4, 0.1);
        l_title.setVisible(true);
        l_title.setBorder(BorderFactory.createLineBorder(Color.black));

        // Clickables
        b_Connect = new JLabel("Connect");
        componentInit(b_Connect, 0.2, 0.7, 0.6, 0.15);
        b_Connect.setBorder(BorderFactory.createLineBorder(Color.black));
        b_Connect.setBackground(Color.BLUE);
        b_Connect.setVisible(true);

        getContentPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resize();
            }
        });

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                if (clicked(getWindowComponent(b_Connect), e.getPoint())) {
                    connectToServer();
                }
                

                if (inGame) {
                    for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {
                        for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {
                            if (clicked(getWindowComponent(playerBoardButtons[x][y]), e.getPoint())) {
                                System.out.println("Player: " + x + "," + y);
                            } else if (clicked(getWindowComponent(opponentBoardButtons[x][y]), e.getPoint())) {
                                System.out.println("Opponent: " + x + "," + y);
                            }
                        }
                    }
                }

            }
        });

        clientTurn = false;
        this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
              (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public void startGame(boolean turn) {
        System.out.println("Game Started");
        clientTurn = false;
        inGame = true;
        isTurn = turn;
        addBoardButtons();
        resize();
    }

    public boolean isGameStarted() {
        return inGame;
    }

    public boolean isTurn() {
        return isTurn;
    }

    private void connectToServer() {

        // Establish connection thread
        System.out.println("Connect");
        connection = new ClientConnectionManager(this);

        Thread connectionThread = new Thread(connection);
        connectionThread.start();

    }

    private void addBoardButtons() {

        JPanel playerBoard = new JPanel();
        JPanel opBoard = new JPanel();

        float widthRatio = (10.0f / 16.0f);
        float boardHeight = 0.6f;
        float boardWidth = boardHeight * widthRatio;
        float boardXBound = 0.1f;
        float boardYBound = 0.1f;

        playerBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(playerBoard, boardXBound, boardYBound, boardWidth, boardHeight);
        playerBoard.setVisible(true);

        opBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(opBoard, 1 - boardXBound - boardWidth, boardYBound, boardWidth, boardHeight);
        opBoard.setVisible(true);


        playerBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];
        opponentBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];

        float firstBoardPosition = 0.05f;
        float secondBoardPosition = 0.55f;
        float offsetY = 0.05f;
        
        float tileHeight = 0.08f;
        float tileWidth = tileHeight * widthRatio;

        tileImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "CoffeeTile.png"), boardWidth / Constants.BOARD_DIMENSIONS,
                boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        imageInit(tileImg);

        // First board
        for (int i = 0; i < Constants.BOARD_DIMENSIONS; i++) {
            for (int j = 0; j < Constants.BOARD_DIMENSIONS; j++) {

                playerBoardButtons[i][j] = new JLabel(tileImg);
                playerBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                // componentInit(playerBoardButtons[i][j], boardXBound / Constants.BOARD_DIMENSIONS, boardYBound / Constants.BOARD_DIMENSIONS, boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS);
                playerBoardButtons[i][j].setVisible(true);
                playerBoard.add(playerBoardButtons[i][j]);

                opponentBoardButtons[i][j] = new JLabel(tileImg);
                opponentBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                // componentInit(playerBoardButtons[i][j], boardXBound / Constants.BOARD_DIMENSIONS, boardYBound / Constants.BOARD_DIMENSIONS, boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS);
                opponentBoardButtons[i][j].setVisible(true);
                opBoard.add(opponentBoardButtons[i][j]);

                /*
                // first board
                playerBoardButtons[i][j] = new JLabel(tileImg);
                componentInit(playerBoardButtons[i][j], firstBoardPosition + (double) j * tileWidth,
                        offsetY + (double) i * tileHeight,
                        tileWidth, tileHeight);
                playerBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black, 7));
                playerBoardButtons[i][j].setVisible(true);

                // second board
                opponentBoardButtons[i][j] = new JLabel(tileImg);
                componentInit(opponentBoardButtons[i][j], secondBoardPosition + (double) j * tileWidth,
                        offsetY + (double) i * tileHeight,
                        tileWidth, tileHeight);
                opponentBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black, 7));
                opponentBoardButtons[i][j].setVisible(true);
                */

            }
        }

        resize();
    }

    public void updatePlayerBoard(Grid grid) {
        Grid.GridCell[][] cells = grid.getCells();
        for (int i = 0; i < Constants.BOARD_DIMENSIONS; i++) {
            for (int j = 0; j < Constants.BOARD_DIMENSIONS; j++) {
                switch (cells[i][j].getCell()) {
                    case 0:
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(24f, 0.25f, 0.42f));
                        break;
                    case 1:
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(34f, 0.25f, 0.42f));
                        break;
                    case 2:
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(44f, 0.25f, 0.42f));
                        break;
                    case 3:
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(54f, 0.25f, 0.42f));
                        break;
                    case 4:
                        playerBoardButtons[i][j].setIcon(new ImageIcon(Constants.ASSET_PATH + "cup.svg"));
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(64f, 0.25f, 0.42f));
                        break;
                    default:
                        playerBoardButtons[i][j].setBackground(Color.getHSBColor(0f, 0.25f, 0f));
                }
            }
        }
    }

    private void resize() {
        // resize all images/assets
        for (AssetImage img : loadedImages) {
            img.resizeImage(getWidth(), getHeight());
        }

        // resize all screen components i.e. buttons, labels, text fields, etc.
        for (WindowComponent comp : elements) {
            comp.resize(getWidth(), getHeight());
        }

        revalidate();
    }

}
