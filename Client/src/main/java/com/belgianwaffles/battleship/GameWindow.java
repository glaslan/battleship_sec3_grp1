package com.belgianwaffles.battleship;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Canvas;
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
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.belgianwaffles.battleship.Grid.GridCell;

public class GameWindow extends JFrame implements ActionListener {

    // window specfic constants
    private int REFRESH_RATE = 10;
    private int LOADING_DISPLAY_DELAY = 50;
    private int SELECTED_BORDER_WIDTH = 4;

    // button definitions
    private JComponent b_Connect;
    private JComponent b_Exit;
    private JButton b_Refresh;
    private JButton b_Ready;

    // label definitions
    private JLabel[][] playerBoardButtons;
    private JLabel[][] opponentBoardButtons;
    private JLabel l_title;
    private JLabel l_wait;
    private JLabel l_turnDisplay;

    // board panel definitions
    private JPanel playerBoard = new JPanel();
    private JPanel opBoard = new JPanel();
    private JLabel background;


    // image definitions
    private AssetImage tileImg;
    private AssetImage shipImg;
    private AssetImage shipShotImg;
    private AssetImage missImg;
    private AssetImage sugarSharkImg;
    private AssetImage shotImg;

    // game variables
    private Grid board;
    private boolean inGame;
    private boolean isTurn;
    private ClientConnectionManager connection;
    private int frameCounter = 0;

    // game window component lists
    private ArrayList<WindowComponent> elements = new ArrayList<>();
    private ArrayList<AssetImage> loadedImages = new ArrayList<>();

    
    // component size definitions
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

    // for removing clutter in the constructor
    private void componentInit(JComponent component, double x_bound, double y_bound, double width, double height) {

        component.setBounds((int) (x_bound * Constants.WINDOW_WIDTH), (int) (y_bound * Constants.WINDOW_HEIGHT),
                (int) (width * Constants.WINDOW_WIDTH), (int) (height * Constants.WINDOW_HEIGHT));
        component.setVisible(false);
        this.add(component);

        elements.add(new WindowComponent(component, x_bound, y_bound, width, height));

    }

    // all images must use this function or else they will not be scaled when screen
    // resized
    private void imageInit(AssetImage img) {
        loadedImages.add(img);
    }

    // constructor
    public GameWindow() {

        this.setLayout(null);
        this.setUndecorated(true);
        this.setVisible(true);
        this.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.inGame = false;

        ImageIcon back = new ImageIcon(Constants.ASSET_PATH+"home_page.png");

        this.setGameBackground(back);
        this.setContentPane(this.background);

        /////// Buttons ////////
        AssetImage connectImage = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "start_button.png"), 0.5, 0.15, this.getWidth(), this.getHeight());
        AssetImage exitImage = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "exit_button.png"), 0.5, 0.15, this.getWidth(), this.getHeight());
        b_Connect = new JLabel(connectImage);
        // b_Connect.setBorder(BorderFactory.createLineBorder(Color.black, 10));
        b_Connect.setFont(new Font(Constants.FONT, Font.PLAIN, 60));
        componentInit(b_Connect, 0.25, 0.6, 0.5, 0.15);
        b_Connect.setVisible(true);
        
        b_Exit = new JLabel(exitImage);
        // b_Exit.setBorder(BorderFactory.createLineBorder(Color.black, 10));
        b_Exit.setFont(new Font(Constants.FONT, Font.PLAIN, 60));
        componentInit(b_Exit, 0.25, 0.8, 0.5, 0.15);
        b_Exit.setVisible(true);

        b_Refresh = new JButton("Refresh");
        b_Refresh.setBorder(BorderFactory.createLineBorder(Color.black, 10));
        b_Refresh.setFont(new Font(Constants.FONT, Font.PLAIN, 24));
        buttonInit(b_Refresh, boardXBound + (boardWidth/4), 0.75, boardWidth / 2, 0.1);

        b_Ready = new JButton("Ready");
        b_Ready.setBorder(BorderFactory.createLineBorder(Color.black, 10));
        b_Ready.setFont(new Font(Constants.FONT, Font.PLAIN, 24));
        buttonInit(b_Ready, 1 - (boardXBound + boardWidth) + (boardWidth/4), 0.75, boardWidth / 2, 0.1);


        playerBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];
        opponentBoardButtons = new JLabel[Constants.BOARD_DIMENSIONS][Constants.BOARD_DIMENSIONS];
        for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {
        for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {
            opponentBoardButtons[x][y] = new JLabel();
            playerBoardButtons[x][y] = new JLabel();
        }}



        /////// Player Boards /////////
        
        opBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(opBoard, boardXBound, boardYBound, boardWidth, boardHeight);

        playerBoard.setLayout(new GridLayout(Constants.BOARD_DIMENSIONS, Constants.BOARD_DIMENSIONS));
        componentInit(playerBoard, 1 - boardXBound - boardWidth, boardYBound, boardWidth, boardHeight);



        /////// Labels ////////
        l_title = new JLabel("Battleship", SwingConstants.CENTER);
        componentInit(l_title, 0.1, 0.1, 0.8, 0.2);
        l_title.setVisible(true);
        l_title.setFont(new Font(Constants.FONT, Font.BOLD, 100));

        l_wait = new JLabel("Waiting for Opponent", SwingConstants.CENTER);
        componentInit(l_wait, 0.1, 0.35, 0.8, 0.3);
        l_wait.setFont(new Font(Constants.FONT, Font.BOLD, 120));

        l_turnDisplay = new JLabel("Waiting for Opponent", SwingConstants.CENTER);
        componentInit(l_turnDisplay, 0.1, 0.75, 0.8, 0.1);
        l_turnDisplay.setFont(new Font(Constants.FONT, Font.BOLD, 100));

        getContentPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resize();
            }
        });

        /////// Mouse Listener ////////
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                // if player is in game and its their turn, check to see if they have clicked a tile that has not been shot to shoot
                if (inGame && isTurn) {
                    for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {
                    for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {

                        // get location of mouse click
                        if (e.getPoint().getX() >= (boardXBound + (x * ((float)opBoard.getSize().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                            e.getPoint().getX() < (boardXBound + ((x + 1) * ((float)opBoard.getSize().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                            e.getPoint().getY() >= (boardYBound + (y * ((float)opBoard.getSize().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight() &&
                            e.getPoint().getY() < (boardYBound + ((y + 1) * ((float)opBoard.getSize().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight()) 
                        {

                            //get gridcells
                            GridCell[][] cells = board.getCells();

                            // shoot grid and send to server, also setTurn to false
                            if (!cells[y][x].hasShotP1()) {
                                cells[y][x].setShotP1(true);
                                Grid updated = new Grid(cells);
                                try {
                                    connection.sendGridToServer(updated);
                                } catch (IOException bozo) {}
                                setTurn(false);
                            }
                            System.out.println(x+", "+y);
        }}}}}}); // end addMouseListener();

        Timer onRefresh;
        onRefresh = new Timer((int)(REFRESH_RATE), 
                
                // calls every time timer refreshes (once per refreshRate/1000)
                new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        // change waiting for opponent display
                        // makes for the tiny animation of the dots going from . .. ...
                        frameCounter++;
                        l_wait.setText("Waiting for Opponent.");
                        if (frameCounter >= LOADING_DISPLAY_DELAY) {
                            l_wait.setText("Waiting for Opponent..");
                        }
                        if (frameCounter >= LOADING_DISPLAY_DELAY * 2) {
                            l_wait.setText("Waiting for Opponent...");
                        }
                        if (frameCounter >= LOADING_DISPLAY_DELAY * 3) {
                            frameCounter = 0;
                        }


                        // highlight hovered tile
                        resetBorders();
                        if (true) {

                            // get mouses current position
                            Point mousePosition = MouseInfo.getPointerInfo().getLocation();
                            for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {
                                for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {

                                    // check if mouse hovered over tile
                                    if (mousePosition.getX() >= (boardXBound + (x * ((float)opBoard.getSize().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                                        mousePosition.getX() < (boardXBound + ((x + 1) * ((float)opBoard.getSize().getWidth() / getWidth() / Constants.BOARD_DIMENSIONS))) * getWidth() &&
                                        mousePosition.getY() >= (boardYBound + (y * ((float)opBoard.getSize().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight() &&
                                        mousePosition.getY() < (boardYBound + ((y + 1) * ((float)opBoard.getSize().getHeight() / getHeight() / Constants.BOARD_DIMENSIONS))) * getHeight()) 
                                    {
                                        // set border color to blue
                                        opponentBoardButtons[y][x].setBorder(BorderFactory.createLineBorder(Color.blue, SELECTED_BORDER_WIDTH));
                                    }
                                    // set border to default
                                    else {opponentBoardButtons[y][x].setBorder(BorderFactory.createLineBorder(Color.black));
        }}}}}}); // end timer();
        onRefresh.start();



        this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
            (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());

        resize();

    }

    public void setGameBackground(ImageIcon background) {

        ArrayList<WindowComponent> components = new ArrayList<WindowComponent>();

        for (WindowComponent comp : this.elements) {
            if (comp.getComponent().isVisible()) {
                components.add(comp);
            }
        }

        clearScreen();

        AssetImage back = new AssetImage(background, 1, 1, getWidth(), getHeight());
        imageInit(back);
        if (this.background == null) {
            this.background = new JLabel();
        }
        this.background.setIcon(back);
    }


    // used to check if a JLabel has been clicked on
    private boolean clicked(WindowComponent element, Point p) {

        // check if within labels area
        if (p.getX() >= getWindowXPosition(element.getBoundX()) &&
                p.getX() <= getWindowXPosition(element.getBoundX() + element.getWidth()) &&
                p.getY() >= getWindowYPosition(element.getBoundY()) &&
                p.getY() <= getWindowYPosition(element.getBoundY() + element.getHeight())) {
            return true;

        }
        return false;
    }



    // called whenever a JButton is clicked
    @Override
    public void actionPerformed(ActionEvent e) {

        // Connect
        if (e.getSource().equals(b_Connect)) {
            connectToServer();
        }

        // Exit
        else if (e.getSource().equals(b_Exit)) {
            System.exit(1);
        }

        // Refresh
        else if (e.getSource().equals(b_Refresh)) {
            try {
                connection.sendGridRefreshRequest(board);
            } catch (IOException bozo) {System.out.println("Too bad you're stuck with it");}
        }

        // Ready
        else if (e.getSource().equals(b_Ready)) {
            try {
                l_turnDisplay.setText("Waiting for opponent to confirm");
                connection.sendGridReadyRequest(board);
                b_Ready.setVisible(false);
                b_Refresh.setVisible(false);
                l_turnDisplay.setVisible(true);
            } catch (IOException bozo) {System.out.println("Not ready");}
        }
    }

    

    

    

    // sets game to board screen
    private void setBoardScreen(Grid grid) {

        // remove all elements from screen and add ready and refresh buttons
        clearScreen();
        b_Refresh.setVisible(true);
        b_Ready.setVisible(true);

        // add the player and opponent boards in the correct positions
        playerBoard.setVisible(true);
        opBoard.setVisible(true);

        // load images for tile assets
        tileImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "CoffeeTile.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        shipImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "ShipTile.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
        shipShotImg = new AssetImage(new ImageIcon(Constants.ASSET_PATH + "ShipHit.png"), boardWidth / Constants.BOARD_DIMENSIONS, boardHeight / Constants.BOARD_DIMENSIONS, getWidth(), getHeight());
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

                // initialize player board tiles
                playerBoardButtons[i][j] = new JLabel(tileImg);
                playerBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                playerBoardButtons[i][j].setVisible(true);
                playerBoard.add(playerBoardButtons[i][j]);

                // initialize opponent board tiles
                opponentBoardButtons[i][j] = new JLabel(tileImg);
                opponentBoardButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                opponentBoardButtons[i][j].setVisible(true);
                opBoard.add(opponentBoardButtons[i][j]);

            }
        }

        updatePlayerBoard(grid);
        resize();
    }

    // updates the board displays based on the state of the grid passed 
    public void updatePlayerBoard(Grid grid) {

        // set the board to the new grid passed in
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



    // attempts to make a connection to the server
    private void connectToServer() {

        // Establish connection thread
        connection = new ClientConnectionManager(this);
        Thread connectionThread = new Thread(connection);
        connectionThread.start();

    }

    

    // returns JComponent passed in as a WindowComponent
    private WindowComponent getWindowComponent(JComponent element) {
        for (WindowComponent e : elements) {
            if (e.getComponent().equals(element)) {
                return e;
            }
        }

        return null;
    }

    // starts game
    public void startGame(Grid grid) {
        isTurn = false;
        inGame = true;
        setBoardScreen(grid);
        resize();
    }

    // ends game
    public void endGame(boolean winner) {

        this.inGame=false;
        if (winner) {
            l_turnDisplay.setText("You Win!");
        }
        else {
            l_turnDisplay.setText("You Lose!");
        }

        playerBoard.removeAll();
        opBoard.removeAll();
        
        Timer onRefresh;
        onRefresh = new Timer((int)(3000), 
                
                // calls every time timer refreshes (once per refreshRate/1000)
                new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        clearScreen();
                        l_title.setVisible(true);
                        b_Connect.setVisible(true);
                        b_Exit.setVisible(true);

        }}); // end timer();

        onRefresh.setRepeats(false);
        onRefresh.start();
       
        
    }

    // returns if client is in game
    public boolean isGameStarted() {
        return inGame;
    }
    

    // return whether or not its the clients turn
    public boolean isTurn() {
        return isTurn;
    }

    // sets clients turn and changes the display of whose turn it is
    public void setTurn(boolean turn) {
        this.isTurn = turn;

        if (isTurn) {
            l_turnDisplay.setText("Your Turn");
        }
        else {
            l_turnDisplay.setText("Opponents Turn");
        }
    }

    // gets x pixel value of double location
    private int getWindowXPosition(double relative) {
        return (int) (relative * getWidth());
    }

    // gets y pixel value of double location
    private int getWindowYPosition(double relative) {
        return (int) (relative * getHeight());
    }
    

    // removes all components that have been init'ed from the screen
    public void clearScreen() {
        for (WindowComponent c : elements) {
            c.getComponent().setVisible(false);
        }
        revalidate();
    }

    // sets screen to waiting for opponent screen
    public void waitScreen() {
        clearScreen();
        l_wait.setVisible(true);

    }

    // called when screen is resized, resizes all components according to there relative size
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

    // resets all the boarders of boards to there default
    public void resetBorders() {

        // gor through each board tile
        for (int x = 0; x < Constants.BOARD_DIMENSIONS; x++) {
        for (int y = 0; y < Constants.BOARD_DIMENSIONS; y++) {
            opponentBoardButtons[x][y].setBorder(BorderFactory.createLineBorder(Color.black));
            playerBoardButtons[x][y].setBorder(BorderFactory.createLineBorder(Color.black));
        }}

    }

}
