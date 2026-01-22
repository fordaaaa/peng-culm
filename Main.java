import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;          // leaderboard file I/O
import javax.swing.*;        // sorting leaderboard
import javax.swing.border.*;

public class Main extends JFrame implements ActionListener {

    //game settings
    private int rows;               // number of rows in the grid
    private int cols;               // number of columns in the grid
    private int moves;              // move limit (-1 means unlimited moves)
    private String diffname;        // name of difficulty chosen

    // view settings for fog-of-war
    private static final int VIEW_SIZE = 10;       // 10x10 area visible on screen
    private static final int VIEW_RADIUS = VIEW_SIZE / 2; // tiles in each direction from player

    //board state
    private boolean[][] visited;    // tracks which cells were visited
    private int row;                // current player row
    private int col;                // current player column
    private int wolfrow;            // wolfy row position
    private int wolfcol;            // wolfy column position

    // world tiles and bushes
    private int[][] tileType;       // 0 = grass, 1 = sand, 2 = path
    private int[][] bushIndex;      // -1 = no bush, 0..9 = bush sprite index

    //player state
    private String name;            // player name
    private int steps;              // steps taken
    private int movesleft;          // remaining moves if limited

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
    private JLabel moveTrackerLabel;// short summary of steps/moves
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

    public Main() {
        super("Rescue Wolfy!");

        // use CardLayout to switch between title screen and game screen
        cardlayout = new CardLayout();
        mainPanel = new JPanel(cardlayout);

        // build title screen and game screen
        JPanel titlepanel = createtitlepanel();
        JPanel gamepanel = creategamepanel();

        mainPanel.add(titlepanel, card_title);
        mainPanel.add(gamepanel, card_game);

        setContentPane(mainPanel);

        // menu bar with Instructions and About
        setJMenuBar(createmenubar());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 900);
        setLocationRelativeTo(null);

        // show title screen first
        cardlayout.show(mainPanel, card_title);
        setVisible(true);
    }

    // build the title screen panel
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
        center.setLayout(new GridLayout(3, 1, 12, 12));

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

    // create the menu bar with Instructions and About
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
    private void startgame() {
        // ask for name using a simple dialog
        playername = JOptionPane.showInputDialog(this, "Please enter your name: ");
        if (playername == null) playername = "player";
        playername = playername.trim();
        if (playername.length() == 0) playername = "player";
        name = playername;

        // ask for difficulty using a number: 1 easy, 2 medium, 3 hard
        guidiff = askDifficulty();
        setup(guidiff);
        init();

        // start game sounds (intro + background ambience)
        SoundHandling.playstartupsound();
        SoundHandling.startBackgroundLoop();

        SoundHandling.playpageturn();
        JOptionPane.showMessageDialog(this,
                "Rules\n\n" +
                "• The number at the top of each square shows how close the bomb is.\n" +
                "• Press F to scan how far away Wolfy is.\n" +
                "• The leaderboard appears after the game ends.",
                "Rules",
                JOptionPane.INFORMATION_MESSAGE);

        done = false;
        win = false;

        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();

        // switch to game screen
        cardlayout.show(mainPanel, card_game);
    }

    // show instructions dialog
    private void showinstructions() {
        String msg = "HOW TO PLAY RESCUE WOLFY\n\n" +
                "Goal: Move through the jungle to find Wolfy while avoiding hidden bombs.\n" +
                "You can only clearly see a 10x10 area around your current position.\n\n" +
                "Controls:\n" +
                "- W / Up Arrow    : move up\n" +
                "- S / Down Arrow  : move down\n" +
                "- A / Left Arrow  : move left\n" +
                "- D / Right Arrow : move right\n" +
                "- F               : scan distance to Wolfy\n" +
                "- G               : give up\n\n" +
                "Difficulty:\n" +
                "- Easy   : 1 bomb, 100 moves\n" +
                "- Medium : 2 bombs, 75 moves\n" +
                "- Hard   : 2 bombs, 60 moves, bombs move more often\n\n" +
                "Heat numbers on visited tiles show how close the nearest bomb is:\n" +
                "higher numbers mean closer (hotter).";
        JOptionPane.showMessageDialog(this, msg, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    // show About dialog
    private void showabout() {
        String msg = "Rescue Wolfy\n" +
                "Author: [Your Name Here]\n" +
                "Course: ICS3U1\n" +
                "Description: Jungle-themed grid rescue game with fog-of-war,\n" +
                "moving bombs, heat hints, and a leaderboard.";
        JOptionPane.showMessageDialog(this, msg, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // simple styled button used for in-game UI
    private JButton makeButton(String text, String command) {
        JButton b = makebuttonstyled(text);
        b.setActionCommand(command);
        b.addActionListener(this);
        return b;
    }

    // base style for all buttons (title + game)
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
        return b;
    }

    private void updateInfo() {
        String movesText;
        if (moves > -1) movesText = "moves left " + movesleft; 
        else movesText = "moves left unlimited";
        infoLabel.setText("player " + name + "  difficulty " + diffname +
                "  steps " + steps + "  " + movesText);

        moveTrackerLabel.setText("<html>steps: " + steps + "<br>" + movesText + "</html>");
    }
    // ask the user for difficulty using a simple number input dialog
    private int askDifficulty() {
        int d = 0;
        while (d < 1 || d > 3) {
            String input = JOptionPane.showInputDialog(this,
                    "choose difficulty:\n" +
                    "1 easy (40 moves limit)\n" +
                    "2 medium (30 moves limit)\n" +
                    "3 hard (20 moves limit)");
            if (input == null) {
                // deafult is to just use medium
                d = 2;
                break;
            }
            input = input.trim();
            if (input.length() > 0) {
                char ch = input.charAt(0);
                if (ch == '1') d = 1;
                else if (ch == '2') d = 2;
                else if (ch == '3') d = 3;
                else d = 0; // invalid, ask again
            } else {
                d = 0; // empty, ask again
            }
        }
        return d;
    }
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
            int dr = row - wolfrow;
            if (dr < 0) dr = -dr;
            int dc = col - wolfcol;
            if (dc < 0) dc = -dc;
            int distance = dr + dc;
            statusLabel.setText("scanner: wolfy is " + distance + " squares away");
        } else if (cmd.equals("giveup")) {
            done = true;
            win = false;
            statusLabel.setText(name + " gave up");
        } else if (cmd.equals("newgame")) {
            startNewGame();
        } else if (cmd.equals("menu")) {
            // stop sounds and go back to title screen
            SoundHandling.stopBackgroundLoop();
            done = true;
            cardlayout.show(mainPanel, card_title);
        } else if (cmd.equals("quitapp")) {
            System.exit(0);
        }

        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }
    private void setup(int d) {
        // use a 72x72 world so the diagonal distance is about 100 tiles
        // (from (0,0) to (71,71) is ~100.4 tiles)
        if (d == 1) {
            rows = 72;
            cols = 72;
            moves = 100;         // easy: 100 moves
            diffname = "easy";
        } else if (d == 2) {
            rows = 72;
            cols = 72;
            moves = 75;          // medium: 75 moves
            diffname = "medium";
        } else {
            rows = 72;
            cols = 72;
            moves = 60;          // hard: 60 moves
            diffname = "hard";
        }
    }
    // create game screen panel (layout around RescuePanel)
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

        moveTrackerLabel = new JLabel("", JLabel.CENTER);
        moveTrackerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

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
                    // scan distance to wolfy
                    int dr = row - wolfrow;
                    if (dr < 0) dr = -dr;
                    int dc = col - wolfcol;
                    if (dc < 0) dc = -dc;
                    int distance = dr + dc;
                    statusLabel.setText("scanner: wolfy is " + distance + " squares away");
                    logMove("used scan (distance " + distance + ")");
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

        // side panel on the right: short tracker + detailed move log
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout(5, 5));
        sidePanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        sidePanel.setBackground(new Color(0, 55, 70));
        moveTrackerLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        sidePanel.add(moveTrackerLabel, BorderLayout.NORTH);

        JScrollPane movescroll = new JScrollPane(moveLogArea);
        movescroll.setPreferredSize(new Dimension(230, 0));
        movescroll.setBorder(new TitledBorder("Move Log"));
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

    private void init() {
        visited = new boolean[rows][cols];
        row = 0;
        col = 0;
        visited[row][col] = true;

        // generate terrain and bushes
        tileType = WorldGeneration.generatetiles(rows, cols);
        bushIndex = WorldGeneration.generatebushes(tileType);

        // starting tile is always path (clear) with no bush
        tileType[row][col] = WorldGeneration.TILE_PATH;
        bushIndex[row][col] = -1;

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

    private void domove(char m) {
        if (m == 'w') row--;
        else if (m == 's') row++;
        else if (m == 'a') col--;
        else if (m == 'd') col++;
    }

    private void doMove(char m) {
        if (done) return;
        if (canmove(m)) {
            // actually move the player
            domove(m);
            steps++;
            visited[row][col] = true;

            // mark the current tile as player path and clear any bush here
            if (tileType != null) {
                tileType[row][col] = WorldGeneration.TILE_PATH;
            }
            if (bushIndex != null) {
                bushIndex[row][col] = -1;
            }

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

                JOptionPane.showMessageDialog(this,
                        "BOOM! You hit a mine!",
                        "Explosion!",
                        JOptionPane.ERROR_MESSAGE);
                
                // leaderboard AFTER explosion (no saving on loss)
                SoundHandling.playpageturn();
                JOptionPane.showMessageDialog(this,
                        getLeaderboardText(),
                        "Leaderboard",
                        JOptionPane.INFORMATION_MESSAGE);
                
                done = true;
                win = false;
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
    private String describeMove(char m) {
        if (m == 'w') return "moved up";
        else if (m == 's') return "moved down";
        else if (m == 'a') return "moved left";
        else if (m == 'd') return "moved right";
        return "moved";
    }
    private void logMove(String action) {
        moveLogArea.append(name + " " + action + " (" + steps + ")\n");
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }
    // leaderboard save - only keeps top 5 scores
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

        Image playerPic;  // util/player.png
        Image wolfyPic;   // util/wolfy.png
        Image bombPic;    // util/bomb.png

        public RescuePanel() {
            playerPic = new ImageIcon("util/player.png").getImage();
            wolfyPic  = new ImageIcon("util/wolfy.png").getImage();
            bombPic   = new ImageIcon("util/bomb.png").getImage();
        }

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

                    // outside the world: draw dark fog tile
                    if (wr < 0 || wr >= rows || wc < 0 || wc >= cols) {
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(x, y, cellSize, cellSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, cellSize, cellSize);
                        continue;
                    }

                    // base floor tile based on generated terrain
                    int baseType = (tileType != null ? tileType[wr][wc] : WorldGeneration.TILE_GRASS);
                    if (baseType == WorldGeneration.TILE_GRASS) {
                        g.setColor(new Color(0, 100, 0)); // grass
                    } else if (baseType == WorldGeneration.TILE_SAND) {
                        g.setColor(new Color(170, 150, 80)); // sand
                    } else { // path
                        g.setColor(new Color(120, 80, 40)); // path
                    }
                    g.fillRect(x, y, cellSize, cellSize);

                    // simple bush overlay: only on grass tiles, no collision
                    if (bushIndex != null && baseType == WorldGeneration.TILE_GRASS && bushIndex[wr][wc] >= 0) {
                        g.setColor(new Color(0, 140, 0));
                        int inset = cellSize / 6;
                        g.fillOval(x + inset, y + inset, cellSize - 2 * inset, cellSize - 2 * inset);
                    }

                    // draw player sprite
                    if (wr == row && wc == col && playerPic != null) {
                        g.drawImage(playerPic, x, y, cellSize, cellSize, this);
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

    public static void main(String[] args) {
        new Main();
    }
}