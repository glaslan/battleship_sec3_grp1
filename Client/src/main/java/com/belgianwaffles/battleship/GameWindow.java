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
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.belgianwaffles.battleship.Grid.GridCell;

public class GameWindow extends JFrame implements ActionListener {

    private JLabel b_Connect;
    private JButton b_Exit;
    private JLabel[][] playerBoardButtons;
    private JLabel[][] opponentBoardButtons;

    private JLabel l_title;
    private Grid board;

    private AssetImage tileImg;
    private AssetImage shipImg;
    private AssetImage shipShotImg;
    private AssetImage missImg;
    private AssetImage sugarSharkImg;
    private AssetImage shotImg;

    private boolean clientTurn;
    private boolean inGame;
    private boolean isTurn;

    private ClientConnectionManager connection;

    private ArrayList<WindowComponent> elements = new ArrayList<>();
    private ArrayList<AssetImage> loadedImages = new ArrayList<>();

    private JPanel playerBoard = new JPanel();
    private JPanel opBoard = new JPanel();

    private float widthRatio = (10.0f / 16.0f);
    private float boardHeight = 0.6f;
    private float boardWidth = boardHeight * widthRatio;
    private float boardXBound = 0.1f;
    private float boardYBound = 0.1f;
    private float firstBoardPosition = 0.05f;
    private float secondBoardPosition = 0.55f;
    private float offsetY = 0.05f;

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
                

                if (inGame && isTurn) {
                    for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {
                        for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {

                            if (e.getPoint().getX() >= (boardXBound + (x * ((float)opBoard.size().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                                e.getPoint().getX() < (boardXBound + ((x + 1) * ((float)opBoard.size().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                                e.getPoint().getY() >= (boardYBound + (y * ((float)opBoard.size().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight() &&
                                e.getPoint().getY() < (boardYBound + ((y + 1) * ((float)opBoard.size().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight()) 
                            {
                                GridCell[][] cells = board.getCells();
                                if (!cells[x][y].hasShotP1()) {
                                    cells[x][y].setShotP1(true);
                                    Grid updated = new Grid(cells);
                                    try {
                                        connection.sendGridToServer(updated);
                                    } catch (IOException bozo) {}
                                    setTurn(false);
                                }
                                System.out.println(x+", "+y);
                            }
                        }
                    }
                }

            }
        });

        clientTurn = false;
        this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
            (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());


        this.startGame();

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public void startGame() {
        System.out.println("Game Started");
        clientTurn = false;
        inGame = true;
        addBoardButtons();
        resize();
    }

    public boolean isGameStarted() {
        return inGame;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean turn) {
        this.isTurn = turn;
    }

    private void connectToServer() {

        // Establish connection thread
        System.out.println("Connect");
        connection = new ClientConnectionManager(this);

        Thread connectionThread = new Thread(connection);
        connectionThread.start();

    }

    private void addBoardButtons() {

        playerBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(playerBoard, 1 - boardXBound - boardWidth, boardYBound, boardWidth, boardHeight);
        playerBoard.setVisible(true);

        opBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(opBoard, boardXBound, boardYBound, boardWidth, boardHeight);
        opBoard.setVisible(true);


        playerBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];
        opponentBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];

        tileImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "CoffeeTile.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        shipImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "Ship.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        shipShotImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "ShipShot.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        missImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "Miss.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        sugarSharkImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "SugarShark.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        shotImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "Hit.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());

        imageInit(tileImg);
        imageInit(shipImg);
        imageInit(shipShotImg);
        imageInit(missImg);
        imageInit(sugarSharkImg);
        imageInit(shotImg);

        // First board
        for (int i = 0; i < Constants.BOARD_DIMENSIONS; i++) {
            for (int j = 0; j < Constants.BOARD_DIMENSIONS; j++) {

                playerBoardButtons[i][j] = new JLabel(tileImg);
                playerBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                playerBoardButtons[i][j].setVisible(true);
                playerBoard.add(playerBoardButtons[i][j]);

                opponentBoardButtons[i][j] = new JLabel(tileImg);
                opponentBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                opponentBoardButtons[i][j].setVisible(true);
                opBoard.add(opponentBoardButtons[i][j]);

            }
        }

        resize();
    }

    public void updatePlayerBoard(Grid grid) {

        board = grid;
        Grid.GridCell[][] cells = grid.getCells();
        for (int i = 0; i < Constants.BOARD_DIMENSIONS; i++) {
            for (int j = 0; j < Constants.BOARD_DIMENSIONS; j++) {

                // change displays on the opponent board
                if (cells[i][j].hasSharkP1()) {
                    // sugar shark
                    opponentBoardButtons[i][j].setIcon(sugarSharkImg);
                }
                else if (cells[i][j].hasShipP2() && cells[i][j].hasShotP1()) {
                    // landed shot
                    opponentBoardButtons[i][j].setIcon(shotImg);
                }
                else if (cells[i][j].hasShotP1()) {
                    // missed shot
                    opponentBoardButtons[i][j].setIcon(missImg);
                }
                else {
                    // normal tile
                    opponentBoardButtons[i][j].setIcon(tileImg);
                }

                // change displays on the player's board
                if (cells[i][j].hasShipP1() && cells[i][j].hasShotP2()) {
                    // display shot ship
                    playerBoardButtons[i][j].setIcon(shipShotImg);
                }
                else if (cells[i][j].hasShipP1()) {
                    // display unhit ship
                    playerBoardButtons[i][j].setIcon(shipImg);
                }
                else if (cells[i][j].hasShotP2()) {
                    // display missed shot
                    playerBoardButtons[i][j].setIcon(missImg);
                }
                else {
                    // display normal tile
                    playerBoardButtons[i][j].setIcon(tileImg);
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
