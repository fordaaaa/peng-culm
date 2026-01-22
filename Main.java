import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;  
import javax.swing.border.*;

public class Main extends JFrame implements ActionListener {
    private static final int TILE_GRASS = 0;
    private static final int TILE_SAND  = 1;
    private static final int TILE_PATH  = 2;

    //game settings
    private int rows;               // number of rows in the grid
    private int cols;               // number of columns in the grid
    private int moves;              // move limit (-1 means unlimited moves)
    private String diffname;        // name of difficulty chosen

    // view settings 
    private static final int VIEW_SIZE = 10;       // 10x10 area visible
    private static final int VIEW_RADIUS = VIEW_SIZE / 2; // tiles in each direction from player

    //board state
    private boolean[][] visited;    // tracks which cells were visited
    private int row;                // current player row
    private int col;                // current player column
    private int wolfrow;            // wolfy row position
    private int wolfcol;            // wolfy column position

    // world tiles
    private int[][] tileType;       // 0 = grass, 1 = sand, 2 = path

    //player state
    private String name;            // player name
    private int steps;              // steps taken
    private int movesleft;          // remaining moves if limited
    private char lastMove;          // last movement direction ('w','s','a','d')
    private int animFrame;          // current animation frame index
    private int selectedCharacter;  // 0-3 for character selection
    private long lastMoveTime;      // for rate limiting moves
    private boolean dying;          // death cutscene flag
    private int deathRow;           // where explosion happens
    private int deathCol;
    private int deathFrame;         // 0..6
    private javax.swing.Timer deathTimer;       // advances explosion frames

    //bomb state 
    private int bombRow;
    private int bombCol;

    // second bomb state (used for medium and hard)
    private int bombRow2;
    private int bombCol2;

    // leaderboard
    private static final String LEADERBOARD_FILE = "util/leaderboard.txt";


    // GUI components
    private CardLayout cardlayout;  // switches between title screen and game screen
    private JPanel mainPanel;       // root panel with CardLayout

    private RescuePanel panel;      // draws the grid
    private JLabel infoLabel;       // shows name, difficulty, steps, moves
    private JLabel statusLabel;     // shows messages (bomb, win, etc.)
    private JTextArea moveLogArea;  // detailed move log ("name moved right (3)")

    private JButton upButton;
    private JButton downButton;
    private JButton leftButton;
    private JButton rightButton;
    private JButton scanButton;
    private JButton giveUpButton;
    private JButton newGameButton;

    private boolean done;           // if the game is finished in GUI
    private boolean win;            // win/lose for this GUI round

    private String playername;      // stored name for GUI runs
    private int guidiff;            // difficulty chosen for GUI (1,2,3)

    // card names for CardLayout
    private static final String card_title = "title";
    private static final String card_game  = "game";
    private static final String card_instructions = "instructions";
    private static final String card_setup = "setup";

    // instructions UI state
    private int instructionsPage = 0;
    private JLabel instructionsText;
    
    // setup UI components
    private JTextField nameField;
    private JRadioButton easyButton, mediumButton, hardButton;
    private JRadioButton char0Button, char1Button, char2Button, char3Button;
    private JPanel sidePanel;       // side panel with move logger
    private JButton toggleLogButton; // button to show/hide move logger

    // description: create the main window and build all screens
    // parms: none
    // returns: none
    public Main() {
        super("Rescue Wolfy!");

        // use CardLayout to switch between title screen and game screen
        cardlayout = new CardLayout();
        mainPanel = new JPanel(cardlayout);

        // build title screen, instructions screen, setup screen, and game screen
        JPanel titlepanel = createtitlepanel();
        JPanel instructionspanel = createinstructionspanel();
        JPanel setuppanel = createsetuppanel();
        JPanel gamepanel = creategamepanel();

        mainPanel.add(titlepanel, card_title);
        mainPanel.add(instructionspanel, card_instructions);
        mainPanel.add(setuppanel, card_setup);
        mainPanel.add(gamepanel, card_game);

        setContentPane(mainPanel);

        // menu bar with Instructions and About
        setJMenuBar(createmenubar());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 900);
        setLocationRelativeTo(null);

        // show title screen first
        cardlayout.show(mainPanel, card_title);

        // start menu music on title screen
        SoundHandling.startmenumusic();

        setVisible(true);
    }

    // build the title screen panel
    // parms: none
    // returns: title panel
    // description: create the title screen panel
    // parms: none
    // returns: title panel
    private JPanel createtitlepanel() {
        JPanel title = new JPanel(new BorderLayout(10, 10)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // simple vertical gradient for jungle sky/forest
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                Color top = new Color(0, 80, 90);
                Color bottom = new Color(0, 50, 40);
                GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }
        };

        JPanel inner = new JPanel(new BorderLayout(10, 10));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(40, 80, 60, 80));

        JLabel titlelabel = new JLabel("Rescue Wolfy", JLabel.CENTER);
        titlelabel.setFont(new Font("Serif", Font.BOLD, 48));
        titlelabel.setForeground(new Color(210, 255, 210));
        titlelabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel subtitle = new JLabel("Find Wolfy and avoid the hidden jungle mines", JLabel.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(new Color(190, 235, 230));
        subtitle.setBorder(new EmptyBorder(0, 10, 20, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titlelabel, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new GridLayout(4, 1, 12, 12));

        JButton playbutton = makebuttonstyled("Play");
        playbutton.setFont(new Font("SansSerif", Font.BOLD, 20));
        playbutton.addActionListener(e -> startgame());

        JButton instrbutton = makebuttonstyled("Instructions");
        instrbutton.setFont(new Font("SansSerif", Font.BOLD, 20));
        instrbutton.addActionListener(e -> showinstructions());

        JButton quitbutton = makebuttonstyled("Quit");
        quitbutton.setFont(new Font("SansSerif", Font.BOLD, 20));
        quitbutton.addActionListener(e -> System.exit(0));

        center.add(playbutton);
        center.add(instrbutton);
        JButton aboutbutton = makebuttonstyled("About");
        aboutbutton.setFont(new Font("SansSerif", Font.BOLD, 20));
        aboutbutton.addActionListener(e -> showabout());
        center.add(aboutbutton);
        center.add(quitbutton);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 0, 0, 90), 2, true),
                new EmptyBorder(20, 30, 30, 30)));
        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        inner.add(card, BorderLayout.CENTER);
        title.add(inner, BorderLayout.CENTER);

        return title;
    }

    // build the instructions screen panel with paged text and sounds
    // description: create the instructions screen panel
    // parms: none
    // returns: instructions panel
    private JPanel createinstructionspanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBackground(new Color(0, 45, 50));
        outer.setBorder(new EmptyBorder(40, 80, 60, 80));

        JLabel title = new JLabel("Instructions", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(new Color(220, 255, 240));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        instructionsText = new JLabel("", JLabel.CENTER);
        instructionsText.setFont(new Font("SansSerif", Font.PLAIN, 18));
        instructionsText.setForeground(new Color(210, 240, 235));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(instructionsText, BorderLayout.CENTER);

        JButton back = makebuttonstyled("Back");
        JButton next = makebuttonstyled("Next");
        JButton done = makebuttonstyled("Done");

        back.addActionListener(e -> {
            if (instructionsPage > 0) {
                instructionsPage--;
                updateinstructionstext();
            }
        });

        next.addActionListener(e -> {
            // advance page with click + page turn sound
            if (instructionsPage < 3) {
                instructionsPage++;
                SoundHandling.playpageturn();
                updateinstructionstext();
            }
        });

        done.addActionListener(e -> {
            cardlayout.show(mainPanel, card_title);
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);
        bottom.add(back);
        bottom.add(next);
        bottom.add(done);

        outer.add(title, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    // update instructions text per page (small chunks)
    // description: update the instructions text for the current page
    // parms: none
    // returns: none
    private void updateinstructionstext() {
        switch (instructionsPage) {
            case 0:
                instructionsText.setText("<html>Use <b>WASD</b> or the <b>arrow keys</b> to move your character around the jungle.</html>");
                break;
            case 1:
                instructionsText.setText("<html>Press <b>F</b> to use the radar. It scans for <b>Wolfy</b> and the <b>nearest bomb</b> and shows their distances.</html>");
                break;
            case 2:
                instructionsText.setText("<html>Heat numbers on visited tiles show how close the nearest bomb is. Higher numbers mean the mine is <b>closer</b>.</html>");
                break;
            default:
                instructionsText.setText("<html>Avoid the hidden mines and find Wolfy! Use the menu buttons to start a new game or change difficulty.</html>");
                break;
        }
    }

    // build the setup screen panel with name input and difficulty selection
    // description: create the setup screen (name, difficulty, character)
    // parms: none
    // returns: setup panel
    private JPanel createsetuppanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBackground(new Color(0, 45, 50));
        outer.setBorder(new EmptyBorder(40, 80, 60, 80));

        JLabel title = new JLabel("Game Setup", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(new Color(220, 255, 240));
        title.setBorder(new EmptyBorder(0, 0, 30, 0));

        // name input section
        JPanel namePanel = new JPanel(new BorderLayout(10, 10));
        namePanel.setOpaque(false);
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        nameLabel.setForeground(new Color(210, 240, 235));
        nameField = new JTextField(20);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        nameField.setBorder(new EmptyBorder(5, 10, 5, 10));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);

        // difficulty selection section
        JPanel diffPanel = new JPanel(new BorderLayout(10, 10));
        diffPanel.setOpaque(false);
        JLabel diffLabel = new JLabel("Choose difficulty:");
        diffLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        diffLabel.setForeground(new Color(210, 240, 235));
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        radioPanel.setOpaque(false);
        ButtonGroup diffGroup = new ButtonGroup();
        
        easyButton = new JRadioButton("Easy (100 moves)", true);
        mediumButton = new JRadioButton("Medium (75 moves)", false);
        hardButton = new JRadioButton("Hard (60 moves)", false);
        
        easyButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        mediumButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        hardButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        easyButton.setForeground(new Color(210, 240, 235));
        mediumButton.setForeground(new Color(210, 240, 235));
        hardButton.setForeground(new Color(210, 240, 235));
        easyButton.setOpaque(false);
        mediumButton.setOpaque(false);
        hardButton.setOpaque(false);
        
        diffGroup.add(easyButton);
        diffGroup.add(mediumButton);
        diffGroup.add(hardButton);
        radioPanel.add(easyButton);
        radioPanel.add(mediumButton);
        radioPanel.add(hardButton);
        
        diffPanel.add(diffLabel, BorderLayout.WEST);
        diffPanel.add(radioPanel, BorderLayout.CENTER);

        // character selection section
        JPanel charPanel = new JPanel(new BorderLayout(10, 10));
        charPanel.setOpaque(false);
        JLabel charLabel = new JLabel("Choose character:");
        charLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        charLabel.setForeground(new Color(210, 240, 235));
        
        JPanel charRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        charRadioPanel.setOpaque(false);
        ButtonGroup charGroup = new ButtonGroup();
        
        char0Button = new JRadioButton("Doux", true);
        char1Button = new JRadioButton("Mort", false);
        char2Button = new JRadioButton("Tard", false);
        char3Button = new JRadioButton("Vita", false);
        
        char0Button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        char1Button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        char2Button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        char3Button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        char0Button.setForeground(new Color(210, 240, 235));
        char1Button.setForeground(new Color(210, 240, 235));
        char2Button.setForeground(new Color(210, 240, 235));
        char3Button.setForeground(new Color(210, 240, 235));
        char0Button.setOpaque(false);
        char1Button.setOpaque(false);
        char2Button.setOpaque(false);
        char3Button.setOpaque(false);
        
        charGroup.add(char0Button);
        charGroup.add(char1Button);
        charGroup.add(char2Button);
        charGroup.add(char3Button);
        charRadioPanel.add(char0Button);
        charRadioPanel.add(char1Button);
        charRadioPanel.add(char2Button);
        charRadioPanel.add(char3Button);
        
        charPanel.add(charLabel, BorderLayout.WEST);
        charPanel.add(charRadioPanel, BorderLayout.CENTER);

        // center content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        center.add(namePanel);
        center.add(Box.createVerticalStrut(30));
        center.add(diffPanel);
        center.add(Box.createVerticalStrut(30));
        center.add(charPanel);

        // buttons
        JButton back = makebuttonstyled("Back");
        JButton start = makebuttonstyled("Start Game");
        
        back.addActionListener(e -> {
            cardlayout.show(mainPanel, card_title);
        });
        
        start.addActionListener(e -> {
            // get name
            String enteredName = nameField.getText().trim();
            if (enteredName.length() == 0) {
                enteredName = "player";
            }
            playername = enteredName;
            name = playername;
            
            // get difficulty
            if (easyButton.isSelected()) {
                guidiff = 1;
            } else if (mediumButton.isSelected()) {
                guidiff = 2;
            } else {
                guidiff = 3;
            }
            
            // get character selection
            if (char0Button.isSelected()) {
                selectedCharacter = 0;
            } else if (char1Button.isSelected()) {
                selectedCharacter = 1;
            } else if (char2Button.isSelected()) {
                selectedCharacter = 2;
            } else {
                selectedCharacter = 3;
            }
            
            // start the game
            setup(guidiff);
            init();
            
            // start game sounds
            SoundHandling.playstartupsound();
            SoundHandling.startBackgroundLoop();
            
            // initialize animation state
            lastMove = 's';
            animFrame = 0;
            lastMoveTime = 0;
            
            done = false;
            win = false;
            
            updateInfo();
            panel.repaint();
            
            // switch to game screen and request focus
            cardlayout.show(mainPanel, card_game);
            panel.requestFocusInWindow();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);
        bottom.add(back);
        bottom.add(start);

        outer.add(title, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    // create the menu bar with Instructions and About
    // description: create the menu bar
    // parms: none
    // returns: menu bar
    private JMenuBar createmenubar() {
        JMenuBar bar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newgameitem = new JMenuItem("New Game");
        newgameitem.addActionListener(e -> startgame());
        JMenuItem quititem = new JMenuItem("Quit");
        quititem.addActionListener(e -> System.exit(0));
        gameMenu.add(newgameitem);
        gameMenu.addSeparator();
        gameMenu.add(quititem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem instritem = new JMenuItem("Instructions");
        instritem.addActionListener(e -> showinstructions());
        JMenuItem aboutitem = new JMenuItem("About");
        aboutitem.addActionListener(e -> showabout());
        helpMenu.add(instritem);
        helpMenu.add(aboutitem);

        bar.add(gameMenu);
        bar.add(helpMenu);
        return bar;
    }

        // start the game when Play or New Game is chosen from title/menu
    // description: go from title to setup screen
    // parms: none
    // returns: none
    private void startgame() {
        // stop menu music (if playing) and show setup panel
        SoundHandling.stopBackgroundLoop(); // stops any current loop (menu or otherwise)
        
        // reset setup fields
        if (nameField != null) {
            nameField.setText("");
        }
        if (easyButton != null) {
            easyButton.setSelected(true);
        }
        
        // switch to setup screen
        cardlayout.show(mainPanel, card_setup);
    }

        // show instructions using custom paged UI instead of dialog
    // description: show the instructions screen
    // parms: none
    // returns: none
    private void showinstructions() {
        instructionsPage = 0;
        updateinstructionstext();
        cardlayout.show(mainPanel, card_instructions);
    }

    // show About dialog
    // parms: none
    // returns: none
    private void showabout() {
        String msg = "Rescue Wolfy\n" +
                "- Aiden F.\n" +
                "January 1 2026\n" +
                "ICS3U1 Culminating\n" +
                "Jungle grid game based on finding\nWolfy, where there are mines that move...";
        JOptionPane.showMessageDialog(this, msg, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // simple styled button used for in-game UI
    // description: create a styled button with an action command
    // parms: text, command
    // returns: button
    private JButton makeButton(String text, String command) {
        JButton b = makebuttonstyled(text);
        b.setActionCommand(command);
        b.addActionListener(this);
        return b;
    }

        // base style for all buttons (title + game)
        // description: create a styled button (no command)
        // parms: text
        // returns: button
        private JButton makebuttonstyled(String text) {
            JButton b = new JButton(text);
            b.setFocusPainted(false);
            b.setForeground(new Color(230, 250, 240));
            b.setBackground(new Color(0, 90, 80));
            b.setOpaque(true);
            b.setBorder(new CompoundBorder(
                    new LineBorder(new Color(0, 0, 0, 120), 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            b.setFont(new Font("SansSerif", Font.PLAIN, 14));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // play a click sound for all UI buttons (not WASD keys)
            b.addActionListener(ev -> SoundHandling.playbuttonclicked());

            return b;
        }

    // description: update the top info label
    // parms: none
    // returns: none
    private void updateInfo() {
        String movesText;
        if (moves > -1) movesText = "moves left " + movesleft; 
        else movesText = "moves left unlimited";
        infoLabel.setText("player " + name + "  difficulty " + diffname +
                "  steps " + steps + "  " + movesText);

    }
    // ask the user for difficulty using a simple number input dialog
    // description: start a new game with the same setup values
    // parms: none
    // returns: none
    private void startNewGame() {
        name = playername;
        setup(guidiff);
        init();
        done = false;
        win = false;
        statusLabel.setText("new game started");

        // restart intro sound and background ambience for the new game
        SoundHandling.playstartupsound();
        SoundHandling.startBackgroundLoop();
        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }
    // description: handle button/menu actions
    // parms: e
    // returns: none
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.equals("up")) {
            doMove('w');
        } else if (cmd.equals("down")) {
            doMove('s');
        } else if (cmd.equals("left")) {
            doMove('a');
        } else if (cmd.equals("right")) {
            doMove('d');
        } else if (cmd.equals("scan")) {
            SoundHandling.playsonarping();
            int wolfdist = Math.abs(row - wolfrow) + Math.abs(col - wolfcol);

            int bombdist = Integer.MAX_VALUE;
            int d1 = Math.abs(row - bombRow) + Math.abs(col - bombCol);
            bombdist = Math.min(bombdist, d1);
            if (bombRow2 >= 0 && bombCol2 >= 0) {
                int d2 = Math.abs(row - bombRow2) + Math.abs(col - bombCol2);
                bombdist = Math.min(bombdist, d2);
            }

            statusLabel.setText("scanner: wolfy " + wolfdist + " away, nearest bomb " + bombdist + " away");
        } else if (cmd.equals("giveup")) {
            done = true;
            win = false;
            statusLabel.setText(name + " gave up");
        } else if (cmd.equals("newgame")) {
            startNewGame();
        } else if (cmd.equals("menu")) {
            // stop in-game ambience and go back to title screen with menu music
            SoundHandling.stopBackgroundLoop();
            SoundHandling.startmenumusic();
            done = true;
            cardlayout.show(mainPanel, card_title);
        } else if (cmd.equals("quitapp")) {
            System.exit(0);
        } else if (cmd.equals("togglelog")) {
            boolean visible = !moveLogArea.isVisible();
            moveLogArea.setVisible(visible);
            JScrollPane scrollPane = (JScrollPane) moveLogArea.getParent().getParent();
            scrollPane.setVisible(visible);
            toggleLogButton.setText(visible ? "Hide Move Log" : "Show Move Log");
            sidePanel.revalidate();
            sidePanel.repaint();
        }

        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }
    // description: set world size and move limit based on difficulty
    // parms: d (1 easy, 2 medium, 3 hard)
    // returns: none
    private void setup(int d) {
        // 50x50 world for tighter gameplay
        if (d == 1) {
            rows = 50;
            cols = 50;
            moves = 100;         // easy: 100 moves
            diffname = "easy";
        } else if (d == 2) {
            rows = 50;
            cols = 50;
            moves = 75;          // medium: 75 moves
            diffname = "medium";
        } else {
            rows = 50;
            cols = 50;
            moves = 60;          // hard: 60 moves
            diffname = "hard";
        }
    }
    // create game screen panel (layout around RescuePanel)
    // description: create the main game ui panel
    // parms: none
    // returns: game panel
    private JPanel creategamepanel() {
        // set up labels
        infoLabel = new JLabel("", JLabel.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(0, 90, 70));
        infoLabel.setForeground(new Color(220, 255, 235));
        infoLabel.setBorder(new EmptyBorder(8, 8, 8, 8));

        statusLabel = new JLabel("use the buttons or WASD/arrow keys to move, f to use the hidden feature..", JLabel.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 70, 90));
        statusLabel.setForeground(new Color(220, 240, 255));
        statusLabel.setBorder(new EmptyBorder(6, 6, 6, 6));


        // move log area on the side
        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // drawing panel
        panel = new RescuePanel();
        panel.setBackground(new Color(0, 40, 40));

        // allow WASD keys and arrow keys to control movement as well as buttons
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                char ch = Character.toLowerCase(e.getKeyChar());
                int code = e.getKeyCode();

                // map keys to movement chars used by the game
                char move = 0;
                if (ch == 'w' || code == KeyEvent.VK_UP) move = 'w';
                else if (ch == 's' || code == KeyEvent.VK_DOWN) move = 's';
                else if (ch == 'a' || code == KeyEvent.VK_LEFT) move = 'a';
                else if (ch == 'd' || code == KeyEvent.VK_RIGHT) move = 'd';

                if (move == 'w' || move == 'a' || move == 's' || move == 'd') {
                    doMove(move);
                    updateInfo();
                    panel.repaint();

                } else if (ch == 'f') {
                    // scan distance to wolfy and bomb
                    SoundHandling.playsonarping();
                    int wolfdist = Math.abs(row - wolfrow) + Math.abs(col - wolfcol);

                    int bombdist = Integer.MAX_VALUE;
                    int d1 = Math.abs(row - bombRow) + Math.abs(col - bombCol);
                    bombdist = Math.min(bombdist, d1);
                    if (bombRow2 >= 0 && bombCol2 >= 0) {
                        int d2 = Math.abs(row - bombRow2) + Math.abs(col - bombCol2);
                        bombdist = Math.min(bombdist, d2);
                    }

                    statusLabel.setText("scanner: wolfy " + wolfdist + " away, nearest bomb " + bombdist + " away");
                    logMove("used scan (wolf " + wolfdist + ", bomb " + bombdist + ")");
                    updateInfo();
                    panel.repaint();

                } else if (ch == 'g') {
                    done = true;
                    win = false;
                    statusLabel.setText(name + " gave up");
                    logMove("gave up");
                    updateInfo();
                    panel.repaint();
                }
            }
        });

        // bottom controls
        JPanel buttons = new JPanel(new BorderLayout(10, 10));
        buttons.setBorder(new EmptyBorder(6, 6, 6, 6));
        buttons.setBackground(new Color(0, 55, 70));

        // movement buttons laid out like WASD on the left
        JPanel movepanel = new JPanel(new GridLayout(2, 3, 4, 4));
        movepanel.setOpaque(false);
        upButton = makeButton("W", "up");
        leftButton = makeButton("A", "left");
        downButton = makeButton("S", "down");
        rightButton = makeButton("D", "right");
        movepanel.add(new JLabel(""));
        movepanel.add(upButton);
        movepanel.add(new JLabel(""));
        movepanel.add(leftButton);
        movepanel.add(downButton);
        movepanel.add(rightButton);

        // center actions (scan + give up)
        JPanel centerbuttons = new JPanel(new GridLayout(2, 1, 4, 4));
        centerbuttons.setOpaque(false);
        scanButton = makeButton("SCAN", "scan");
        giveUpButton = makeButton("GIVE UP", "giveup");
        centerbuttons.add(scanButton);
        centerbuttons.add(giveUpButton);

        // right side game controls (new game, exit to menu, quit)
        JPanel gamectrl = new JPanel(new GridLayout(3, 1, 4, 4));
        gamectrl.setOpaque(false);
        newGameButton = makeButton("NEW GAME", "newgame");
        JButton menubutton = makeButton("MENU", "menu");
        JButton quitbutton = makeButton("QUIT", "quitapp");
        gamectrl.add(newGameButton);
        gamectrl.add(menubutton);
        gamectrl.add(quitbutton);

        buttons.add(movepanel, BorderLayout.WEST);
        buttons.add(centerbuttons, BorderLayout.CENTER);
        buttons.add(gamectrl, BorderLayout.EAST);

        // side panel on the right: toggle button for move log
        sidePanel = new JPanel(new BorderLayout(5, 5));
        sidePanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        sidePanel.setBackground(new Color(0, 55, 70));
        
        // toggle button to show/hide move log
        toggleLogButton = makeButton("Show Move Log", "togglelog");
        toggleLogButton.setPreferredSize(new Dimension(150, 30));
        sidePanel.add(toggleLogButton, BorderLayout.NORTH);
        
        // move log area (hidden by default)
        JScrollPane movescroll = new JScrollPane(moveLogArea);
        movescroll.setPreferredSize(new Dimension(230, 0));
        movescroll.setBorder(new TitledBorder("Move Log"));
        movescroll.setVisible(false);
        moveLogArea.setVisible(false);
        sidePanel.add(movescroll, BorderLayout.CENTER);

        // main layout
        JPanel game = new JPanel();
        game.setLayout(new BorderLayout(5, 5));
        game.setBackground(new Color(0, 45, 50));
        game.setBorder(new EmptyBorder(6, 6, 6, 6));
        game.add(infoLabel, BorderLayout.NORTH);
        game.add(panel, BorderLayout.CENTER);
        game.add(sidePanel, BorderLayout.EAST);

        // bottom area: status label on top of control buttons
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        game.add(bottom, BorderLayout.SOUTH);

        return game;
    }

    // description: initialize a new world and place player/wolfy/bombs
    // parms: none
    // returns: none
    private void init() {
        visited = new boolean[rows][cols];
        row = 0;
        col = 0;
        visited[row][col] = true;

        // generate terrain
        tileType = generatetiles(rows, cols);

        // randomly place wolfy somewhere not equal to player start
        wolfrow = (int)(Math.random() * rows);
        wolfcol = (int)(Math.random() * cols);
        while (wolfrow == row && wolfcol == col) {
            wolfrow = (int)(Math.random() * rows);
            wolfcol = (int)(Math.random() * cols);
        }

        // first bomb setup 
        bombRow = (int)(Math.random() * rows);
        bombCol = (int)(Math.random() * cols);
        while ((bombRow == row && bombCol == col) || (bombRow == wolfrow && bombCol == wolfcol)) { // check to make sure that bomb isnt on player/wolfy
            bombRow = (int)(Math.random() * rows);
            bombCol = (int)(Math.random() * cols);
        }

        // second bomb for medium and hard difficulties
        if ("medium".equals(diffname) || "hard".equals(diffname)) {
            bombRow2 = (int)(Math.random() * rows);
            bombCol2 = (int)(Math.random() * cols);
            while ((bombRow2 == row && bombCol2 == col) ||
                   (bombRow2 == wolfrow && bombCol2 == wolfcol) ||
                   (bombRow2 == bombRow && bombCol2 == bombCol)) {
                bombRow2 = (int)(Math.random() * rows);
                bombCol2 = (int)(Math.random() * cols);
            }
        } else {
            bombRow2 = -1;
            bombCol2 = -1;
        }

        steps = 0;
        movesleft = moves;
    }
    // description: check if a movement stays inside the world
    // parms: m (w/a/s/d)
    // returns: true if move is valid
    private boolean canmove(char m) {
        int nr = row;
        int nc = col;
        if (m == 'w') nr--;
        else if (m == 's') nr++;
        else if (m == 'a') nc--;
        else if (m == 'd') nc++;
        else return false;
        if (nr < 0 || nr >= rows) return false;
        if (nc < 0 || nc >= cols) return false;
        return true;
    }

    // description: apply a movement to the player position
    // parms: m (w/a/s/d)
    // returns: none
    private void domove(char m) {
        if (m == 'w') row--;
        else if (m == 's') row++;
        else if (m == 'a') col--;
        else if (m == 'd') col++;
    }

    // description: run a full move step (rate limit, move, sounds, win/lose)
    // parms: m (w/a/s/d)
    // returns: none
    private void doMove(char m) {
        if (done || dying) return;
        // rate limiting: max ~5 moves per second (200ms between moves)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < 200) return;
        lastMoveTime = currentTime;
        
        if (canmove(m)) {
            // actually move the player
            domove(m);
            steps++;
            visited[row][col] = true;
            
            // track direction and animation frame
            lastMove = m;
            animFrame = (animFrame + 1) % 10; // cycle through animation frames

            logMove(describeMove(m));

            // play movement sound each time the player moves
            SoundHandling.playmove();

            // decrease remaining moves (if limited)
            if (moves > -1) movesleft--;

            // chance to move the bomb closer to the player
            double chance = Math.random();
            double threshold;
            if ("hard".equals(diffname)) {
                threshold = 0.75;     // 75% on hard
            } else {
                threshold = 0.50;     // 50% on easy/medium
            }
            if (chance < threshold) {
                moveBombTowardPlayer();
            }

            // heartbeat / breathing based on how close the nearest bomb is
            int dr1 = row - bombRow;
            if (dr1 < 0) dr1 = -dr1;
            int dc1 = col - bombCol;
            if (dc1 < 0) dc1 = -dc1;
            int dist = dr1 + dc1; // distance to first bomb

            if (bombRow2 >= 0 && bombCol2 >= 0) {
                int dr2 = row - bombRow2;
                if (dr2 < 0) dr2 = -dr2;
                int dc2 = col - bombCol2;
                if (dc2 < 0) dc2 = -dc2;
                int dist2 = dr2 + dc2;
                if (dist2 < dist) dist = dist2; // use closest bomb
            }

            if (dist <= 4) {
                SoundHandling.playheartbeat2();
                SoundHandling.playbreathing();
            } else if (dist <= 8) {
                SoundHandling.playheartbeat1();
            }

            // check for hitting bomb, rescuing wolfy, or running out of moves
            if ((row == bombRow && col == bombCol) ||
                (bombRow2 >= 0 && row == bombRow2 && col == bombCol2)) {
                statusLabel.setText("boom! " + name + " stepped on a mine");
                logMove("hit a mine");

                // explosion sound and stop ambience
                SoundHandling.playboom();
                SoundHandling.stopBackgroundLoop();

                // play explosion cutscene first, then show dialogs
                done = true;
                win = false;
                dying = true;
                deathRow = row;
                deathCol = col;
                deathFrame = 0;

                if (deathTimer != null) deathTimer.stop();
                deathTimer = new javax.swing.Timer(100, ev -> {
                    deathFrame++;
                    panel.repaint();
                    if (deathFrame >= 6) {
                        ((javax.swing.Timer) ev.getSource()).stop();
                        dying = false;
                        panel.repaint();

                        JOptionPane.showMessageDialog(this,
                                "game over! you hit a mine",
                                "Game Over",
                                JOptionPane.ERROR_MESSAGE);

                        SoundHandling.playpageturn();
                        JOptionPane.showMessageDialog(this,
                                getLeaderboardText(),
                                "Leaderboard",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                deathTimer.start();
                panel.repaint();
                return;
            } else if (row == wolfrow && col == wolfcol) {
                statusLabel.setText("you rescued wolfy!");
                logMove("rescued wolfy");

                // victory sound and stop ambience
                SoundHandling.playwin();
                SoundHandling.stopBackgroundLoop();

                done = true;
                win = true;
                saveToLeaderboard();

                SoundHandling.playpageturn();
                JOptionPane.showMessageDialog(this,
                        getLeaderboardText(),
                        "Leaderboard",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (moves > -1 && movesleft < 0) {
                statusLabel.setText("you ran out of moves");
                logMove("ran out of moves");

                // stop ambience when the player runs out of moves
                SoundHandling.stopBackgroundLoop();

                done = true;
                win = false;
            } else {
                statusLabel.setText("moved");
            }
        } else {
            statusLabel.setText("cant move outside the grid");
            logMove("invalid move");
        }
    }
    // move the bomb one step closer to the player plus-type moves only
    // description: move bombs one step closer to the player
    // parms: none
    // returns: none
    private void moveBombTowardPlayer() {
        if (done) return;

        // move first bomb
        if (bombRow < row) bombRow++;
        else if (bombRow > row) bombRow--;
        else if (bombCol < col) bombCol++;
        else if (bombCol > col) bombCol--;

        // move second bomb if it exists (medium / hard)
        if (bombRow2 >= 0 && bombCol2 >= 0) {
            if (bombRow2 < row) bombRow2++;
            else if (bombRow2 > row) bombRow2--;
            else if (bombCol2 < col) bombCol2++;
            else if (bombCol2 > col) bombCol2--;
        }
    }
    // description: turn a movement char into a short message
    // parms: m (w/a/s/d)
    // returns: text description
    private String describeMove(char m) {
        if (m == 'w') return "moved up";
        else if (m == 's') return "moved down";
        else if (m == 'a') return "moved left";
        else if (m == 'd') return "moved right";
        return "moved";
    }
    // description: append a line to the move log
    // parms: action
    // returns: none
    private void logMove(String action) {
        moveLogArea.append(name + " " + action + " (" + steps + ")\n");
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }
    // leaderboard save - only keeps top 5 scores
    // description: save a win score to leaderboard
    // parms: none
    // returns: none
    private void saveToLeaderboard() {
        if (!win) return;
        
        File file = new File(LEADERBOARD_FILE);
        ArrayList<String> list = new ArrayList<>();
        
        // read existing scores
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
            } catch (IOException e) {
            }
        }
        
        // add new score
        list.add(name + "," + diffname + "," + steps);
        
        // sort by steps (ascending)
        list.sort((a, b) -> Integer.parseInt(a.split(",")[2]) - Integer.parseInt(b.split(",")[2]));
        
        // keep only top 5
        if (list.size() > 5) {
            list = new ArrayList<>(list.subList(0, 5));
        }
        
        // write back to file
        try (PrintWriter out = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (String score : list) {
                out.println(score);
            }
        } catch (IOException e) {
        }
    }
    // leaderboard read + sort
    // description: read leaderboard and format top scores
    // parms: none
    // returns: leaderboard text
    private String getLeaderboardText() {
        File file = new File(LEADERBOARD_FILE);
        if (!file.exists()) return "No scores yet.";

        ArrayList<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            return "Error reading leaderboard.";
        }

        list.sort((a, b) -> Integer.parseInt(a.split(",")[2]) - Integer.parseInt(b.split(",")[2]));

        StringBuilder sb = new StringBuilder("LEADERBOARD\n\n");
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            String[] p = list.get(i).split(",");
            sb.append(i + 1).append(". ")
              .append(p[0]).append(" | ")
              .append(p[1]).append(" | ")
              .append(p[2]).append(" steps\n");
        }
        return sb.toString();
    }
    //return a simple "heat" level for a cell based on distance from the nearest bomb
    //higher number = closer (hotter)
    // description: compute heat hint for a cell based on nearest bomb distance
    // parms: r, c
    // returns: heat level
    private int getHeatLevelForCell(int r, int c) {
        // distance to first bomb
        int dr1 = r - bombRow;
        if (dr1 < 0) dr1 = -dr1;
        int dc1 = c - bombCol;
        if (dc1 < 0) dc1 = -dc1;
        int dist = dr1 + dc1;

        // if second bomb exists, use the closer one
        if (bombRow2 >= 0 && bombCol2 >= 0) {
            int dr2 = r - bombRow2;
            if (dr2 < 0) dr2 = -dr2;
            int dc2 = c - bombCol2;
            if (dc2 < 0) dc2 = -dc2;
            int dist2 = dr2 + dc2;
            if (dist2 < dist) dist = dist2;
        }

        if (dist == 0) return 0;
        else if (dist <= 2) return 6;
        else if (dist <= 4) return 5;
        else if (dist <= 6) return 4;
        else if (dist <= 9) return 3;
        else if (dist <= 13) return 2;
        else return 1;
    }
    // panel that draws the grid
    class RescuePanel extends JPanel {

        Image wolfyPic;   // util/wolfy.png
        Image bombPic;    // util/bomb.png

        // description: create drawing panel and load simple images
        // parms: none
        // returns: none
        public RescuePanel() {
            // wolfy and bomb still use standalone images for now
            wolfyPic  = new ImageIcon("util/wolfy.png").getImage();
            bombPic   = new ImageIcon("util/bomb.png").getImage();
        }

        // description: draw the visible 10x10 view
        // parms: g
        // returns: none
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (rows <= 0 || cols <= 0) return;

            // each tile size is based on the 10x10 view, not the full 100x100 world
            int cellSize = Math.min(getWidth() / VIEW_SIZE, getHeight() / VIEW_SIZE);
            int offsetX = (getWidth() - cellSize * VIEW_SIZE) / 2;
            int offsetY = (getHeight() - cellSize * VIEW_SIZE) / 2;

            // world coordinates for the top-left of the visible window
            int startRow = row - VIEW_RADIUS;
            int startCol = col - VIEW_RADIUS;

            for (int vr = 0; vr < VIEW_SIZE; vr++) {
                for (int vc = 0; vc < VIEW_SIZE; vc++) {
                    int wr = startRow + vr; // world row
                    int wc = startCol + vc; // world col

                    int x = offsetX + vc * cellSize;
                    int y = offsetY + vr * cellSize;

                    // outside the world: draw sky border so player can't see beyond the map
                    if (wr < 0 || wr >= rows || wc < 0 || wc >= cols) {
                        Image border = Animations.getskyborder();
                        if (border != null) {
                            g.drawImage(border, x, y, cellSize, cellSize, this);
                        } else {
                            g.setColor(Color.DARK_GRAY);
                            g.fillRect(x, y, cellSize, cellSize);
                        }
                        continue;
                    }

                    // base floor tile based on generated terrain using tile sprites
                    int baseType = (tileType != null ? tileType[wr][wc] : TILE_GRASS);
                    Image tileImg = null;
                    if (baseType == TILE_GRASS) {
                        tileImg = Animations.gettile_grass(wr, wc);
                    } else if (baseType == TILE_SAND) {
                        tileImg = Animations.gettile_sand(wr, wc);
                    } else {
                        tileImg = Animations.gettile_path();
                    }

                    if (tileImg != null) {
                        g.drawImage(tileImg, x, y, cellSize, cellSize, this);
                    } else {
                        // fallback colors if sprites are missing
                        if (baseType == TILE_GRASS) {
                            g.setColor(new Color(0, 100, 0));
                        } else if (baseType == TILE_SAND) {
                            g.setColor(new Color(170, 150, 80));
                        } else {
                            g.setColor(new Color(120, 80, 40));
                        }
                        g.fillRect(x, y, cellSize, cellSize);
                    }

                    // draw player sprite from character sheet
                    if (wr == row && wc == col) {
                        Image playerFrame = null;
                        if (dying && wr == deathRow && wc == deathCol) {
                            playerFrame = Animations.getexplosion(Math.max(0, Math.min(deathFrame, 6)));
                        } else if (done && !win) {
                            // after death cutscene, player stays hidden
                            playerFrame = null;
                        } else {
                            if (lastMove == 'w' || lastMove == 'a' || lastMove == 's' || lastMove == 'd') {
                                playerFrame = Animations.getcharacter_walk(selectedCharacter, animFrame);
                            } else {
                                playerFrame = Animations.getcharacter_idle(selectedCharacter, animFrame);
                            }
                        }
                        if (playerFrame != null) {
                            g.drawImage(playerFrame, x, y, cellSize, cellSize, this);
                        }
                    }

                    // show bombs only when game is over and player lost
                    if (done && !win && bombPic != null) {
                        if (wr == bombRow && wc == bombCol) {
                            g.drawImage(bombPic, x, y, cellSize, cellSize, this);
                        } else if (bombRow2 >= 0 && wr == bombRow2 && wc == bombCol2) {
                            g.drawImage(bombPic, x, y, cellSize, cellSize, this);
                        }
                    }

                    // heat numbers on visited tiles (not including bomb tile)
                    if (visited != null && visited[wr][wc] && !(wr == bombRow && wc == bombCol)) {
                        int heat = getHeatLevelForCell(wr, wc);
                        if (heat > 0) {
                            g.setColor(Color.WHITE);
                            String text = Integer.toString(heat);
                            FontMetrics fm = g.getFontMetrics();
                            int textX = x + cellSize - fm.stringWidth(text) - 4;
                            int textY = y + fm.getAscent() + 2;
                            g.drawString(text, textX, textY);
                        }
                    }

                    // show wolfy when game is finished and position is known
                    if (done && wr == wolfrow && wc == wolfcol && wolfyPic != null) {
                        g.drawImage(wolfyPic, x, y, cellSize, cellSize, this);
                    }

                    // grid outline
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

// main method
    public static void main(String[] args) throws Exception {
        Animations.init();
        new Main();
    }

    private static int[][] generatetiles(int rows, int cols) {
        int[][] tiles = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = TILE_GRASS;
            }
        }

        Random rng = new Random();
        int numSandPatches = Math.max(10, (rows * cols) / 150);
        for (int i = 0; i < numSandPatches; i++) {
            int centerR = rng.nextInt(rows);
            int centerC = rng.nextInt(cols);
            int radius = 2 + rng.nextInt(2);
            for (int dr = -radius; dr <= radius; dr++) {
                for (int dc = -radius; dc <= radius; dc++) {
                    int rr = centerR + dr;
                    int cc = centerC + dc;
                    if (rr < 0 || rr >= rows || cc < 0 || cc >= cols) continue;
                    if (dr * dr + dc * dc <= radius * radius) {
                        tiles[rr][cc] = TILE_SAND;
                    }
                }
            }
        }
        return tiles;
    }
}