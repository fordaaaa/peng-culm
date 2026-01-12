import java.util.Scanner;
import javax.swing.*;           // for GUI (JFrame, JPanel, JButton, etc.)
import java.awt.*;              // for drawing the grid
import java.awt.event.*;        // for button events
import java.io.*;               // for leaderboard file saving/loading (used later)

public class Main {

    //game settings
    public static int rows;               // number of rows in the grid
    public static int cols;               // number of columns in the grid
    public static int moves;              // move limit (-1 means unlimited moves)
    public static String diffname;        // name of difficulty chosen

    //board state
    public static boolean[][] visited;    // tracks which cells were visited
    public static int row;                // current player row
    public static int col;                // current player column
    public static int wolfrow;            // wolfy row position
    public static int wolfcol;            // wolfy column position

    //player state
    public static String name;            // player name
    public static int steps;              // steps taken
    public static int movesleft;          // remaining moves if limited

    //leaderboard (top 5) - will be filled in later
    public static String[] bestnames = new String[5];
    public static int[] bestscores = new int[5];

    //bomb state    
    public static boolean[][] bombs; // true if there is a bomb in the specific cell
    public static int bombcount; // number of bombs on the grid for difficulty settings

    // public static void main(String[] args) { - only used to debug game logic originally
    //     run();                     // start the game program (console version)
    // }
//ivan
    //main game loop
    public static void run() {
        Scanner in = new Scanner(System.in);  // scanner for all user input   (scanner is already defined here and we are borrowing it for the other methods, if i define scanner again in another method the code with get condused)
        boolean play = true;                  // loop control for replaying game

        // load any saved scores from file before the first game
        loadLeaderboard();

        while (play) {                        // repeat until player quits   
            openingscreen();                  // display intro text    (scan)
            getname(in);                      // ask player name
            int diff = getdifficulty(in);     // choose difficulty
            setup(diff);                      // configure settings based on difficulty
            init();                           // initialize board and variables
            game(in);                         // run one game session
            play = again(in);                 // ask if player wants to play again
        }

        in.close();                           // close input scanner
        System.out.println("thanks for playing"); // goodbye message
    }
//jaden
    //intro test
    public static void openingscreen() {
        System.out.println("===================================");
        System.out.println("          rescue wolfy");
        System.out.println("===================================");
        System.out.println("you are a medic trying to reach wolfy in a grid");
        System.out.println("move around and try to find wolfy");
        System.out.println();
        System.out.println("controls");
        System.out.println("w up   s down   a left   d right");
        System.out.println("f scanner (shows how far from wolfy)");
        System.out.println("g give up");
        System.out.println();
    }
//ivan
    //player name
    public static void getname(Scanner in) {
        System.out.print("enter your name ");
        name = in.nextLine().trim();          // read name and trim spaces

        if (name.length() == 0) {             // if empty, default to “player”
            name = "player";
        }

        System.out.println("welcome " + name);
        System.out.println();
        
    }

//jaden
    // difficulty setting
    public static int getdifficulty(Scanner in) {
        int d = 0;                            // placeholder until valid input

        while (d < 1 || d > 3) {              // keep asking until valid option
            System.out.println("choose difficulty");
            System.out.println("1 easy no move limit");
            System.out.println("2 medium 60 moves");
            System.out.println("3 hard 40 moves");
            System.out.print("enter 1 2 or 3 ");

            String s = in.nextLine().trim();  // read input as string

            try {// try prevents the code from crashing because .parseInt(s) can only handle a whole number
                d = Integer.parseInt(s);      // convert to integer
            } catch (NumberFormatException e) {
                d = 0;                         // invalid result forces loop
            }

            if (d < 1 || d > 3) {
                System.out.println("invalid choice");
            }
        }

        System.out.println();
        return d;                             // return difficulty number
    }
//jaden
    //game difficulty settings 
    public static void setup(int d) {
        if (d == 1) {                         // easy mode settings
            rows = 10;
            cols = 10;
            moves = -1;                       // unlimited moves - might change to have SOME challenge idk
            diffname = "easy";
            bombcount = 8;

        } else if (d == 2) {                  // medium mode
            rows = 10;
            cols = 10;
            moves = 60;
            diffname = "medium";
            bombcount = 15;


        } else {                              // hard mode
            rows = 12;
            cols = 12;
            moves = 40;
            diffname = "hard";
            bombcount = 25;
        }
    }
//ivan
    //initionalize the board and the player
    public static void init() {
        visited = new boolean[rows][cols];    // create visited grid
        row = 0;                              // player starts at top left (0,0)
        col = 0;                                    
        visited[row][col] = true;             // mark start as visited

        // randomly place wolfy somewhere not equal to player start
        wolfrow = (int)(Math.random() * rows);
        wolfcol = (int)(Math.random() * cols);

        while (wolfrow == row && wolfcol == col) {
            wolfrow = (int)(Math.random() * rows);
            wolfcol = (int)(Math.random() * cols);
        }
            // bomb setup
        bombs = new boolean[rows][cols];      // init bomb grid
        int placed = 0;
        while (placed < bombcount) {
            int br = (int)(Math.random() * rows);
            int bc = (int)(Math.random() * cols);

            // dont put bomb on player start or on wolfy or on an existing bomb
            if ((br == row && bc == col) || (br == wolfrow && bc == wolfcol)) {
                continue;
            }
            if (!bombs[br][bc]) {             // only place if empty
                bombs[br][bc] = true;
                placed++;
            }
        }
            steps = 0;                            // reset counters
            movesleft = moves;                    // initialize move counter
    }
//Ivan
    //code to run the main game
    public static void game(Scanner in) {
        boolean done = false;                 // flag when game ends
        boolean win = false;                  // track win state

        while (!done) {                       // repeat until game finished
            showscreen();                     // display grid + info
            char m = getmove(in);             // read a move from player

            if (m == 'g') {                   // player gives up
                System.out.println(name + " gave up");
                done = true;
                win = false;

            } else if (m == 'f') {            // use the radar, and doesnt move (takes a turn)
                showdistance();

            } else {
                if (canmove(m)) {             // verify move is inside grid
                    domove(m);                // update coordinates
                    steps++;                  // counter increases
                    visited[row][col] = true; // mark cell visited

                    if (moves > -1) {         // if moves are limited
                        movesleft--;          // subtracts one from the amount of steps left
                    }

                    // check if player stepped on a bomb
                    if (bombs[row][col]) {
                        System.out.println("boom! " + name + " stepped on a mine");
                        done = true;
                        win = false;

                    // check if player reached wolfy
                    } else if (row == wolfrow && col == wolfcol) {
                        win = true;
                        done = true;

                    // check if ran out of moves
                    } else if (moves > -1 && movesleft < 0) {
                        System.out.println("you ran out of moves");
                        done = true;
                        win = false;
                    }
                } else {
                    System.out.println("cant move outside the grid");
                }
            }
        }

        // show final grid state so bombs and wolfy position are visible
        showscreen();

        endscreen(win);                        // show end result
    }
//ivan
    //prints the status and the grid
    public static void showscreen() {
        System.out.println();
        System.out.println("===================================");
        System.out.println("player " + name + "  difficulty " + diffname);
        System.out.println("grid " + rows + " x " + cols);
        System.out.println("steps used " + steps);

        if (moves > -1) {                      // limited moves case
            System.out.println("moves left " + movesleft);
        } else {
            System.out.println("moves left unlimited");
        }

        System.out.println("-----------------------------------");
        showgrid();                            // actually draw the grid
        System.out.println("-----------------------------------");
    }
//ivan
    //render the screen
    public static void showgrid() {

        // column number labels
        System.out.print("   ");
        for (int c = 0; c < cols; c++) {
            if (c < 10) System.out.print(" " + c + " ");
            else System.out.print(c + " ");
        }
        System.out.println();

        // render each row
        for (int r = 0; r < rows; r++) {

            // print row number label
            if (r < 10) System.out.print(" " + r + " ");
            else System.out.print(r + " ");

            for (int c = 0; c < cols; c++) {

                if (r == row && c == col) {              // player position
                    System.out.print(" p ");

                } else if (visited[r][c] && bombs[r][c]) { // stepped on a bomb here
                    System.out.print(" X ");

                } else if (visited[r][c]) {              // visited safe square
                    int heat = getHeatLevelForCell(r, c);
                    if (heat > 0) {
                        // print heat number for hot/cold hint
                        System.out.print(" " + heat + " ");
                    } else {
                        System.out.print(" . ");
                    }

                } else {                                 // unexplored
                    System.out.print(" - ");
                }
            }
            System.out.println();
        }
    }

    // cheat/twist part where the player is able to scan how far they are from wolfy
    public static void showdistance() { // method to find distance between player and wolfy
        int dr = row - wolfrow;         // difference in rows
        if (dr < 0) dr = -dr;           // could use math.abs in its stead
        int dc = col - wolfcol;
        if (dc < 0) dc = -dc;         
        
        int distance = dr + dc;
        System.out.println("radar signal: wolfy is " + distance + " units away");

    }

    //return a simple "heat" level for a cell based on distance from wolfy
    //higher number = closer (hotter)
    public static int getHeatLevelForCell(int r, int c) {
        int dr = r - wolfrow;
        if (dr < 0) dr = -dr;
        int dc = c - wolfcol;
        if (dc < 0) dc = -dc;
        int dist = dr + dc; // manhattan distance

        if ("easy".equals(diffname)) {
            if (dist == 0) return 0;        // on wolfy
            else if (dist <= 2) return 5;   // very hot
            else if (dist <= 4) return 4;   // hot
            else if (dist <= 6) return 3;   // warm
            else if (dist <= 9) return 2;   // cool
            else return 1;                  // cold

        } else if ("medium".equals(diffname)) {
            if (dist == 0) return 0;
            else if (dist <= 1) return 7;   // closest
            else if (dist <= 3) return 6;
            else if (dist <= 5) return 5;
            else if (dist <= 7) return 4;
            else if (dist <= 9) return 3;
            else if (dist <= 12) return 2;
            else return 1;

        } else { // hard - only hot or cold
            if (dist == 0) return 0;
            else if (dist <= 4) return 2;   // hot
            else return 1;                  // cold
        }
    }
//jaden
    //get user input
    public static char getmove(Scanner in) {
        System.out.print("enter move w a s d, f to scan, or g to give up ");
        String s = in.nextLine().trim().toLowerCase();

        // validate input until correct
        while (s.length() == 0 || "wasdgf".indexOf(s.charAt(0)) == -1) {
            System.out.print("please enter w a s d, f or g ");
            s = in.nextLine().trim().toLowerCase();
        }

        return s.charAt(0);                     // return first character
    }
//ivan
    //check if the move is legal
    public static boolean canmove(char m) {
        int nr = row;                           // next row
        int nc = col;                           // next column

        if (m == 'w') nr--;                     // move up
        else if (m == 's') nr++;                // move down
        else if (m == 'a') nc--;                // move left
        else if (m == 'd') nc++;                // move right
        else return false;                      // invalid key

        // check bounds
        if (nr < 0 || nr >= rows) return false;
        if (nc < 0 || nc >= cols) return false;

        return true;                            // move is valid
    }
//jaden
    //do the move
    public static void domove(char m) {
        if (m == 'w') row--;                   // go up
        else if (m == 's') row++;              // go down
        else if (m == 'a') col--;              // go left
        else if (m == 'd') col++;              // go right
    }
//ivan
    //final game summary
    public static void endscreen(boolean win) {
        System.out.println();
        System.out.println("========== game over ==========");

        if (win) {                              // winning case
            System.out.println("you rescued wolfy " + name);
            System.out.println("you used " + steps + " moves");
        } else {                                // losing case
            System.out.println("mission failed " + name);
            System.out.println("you did not reach wolfy");
        }

        System.out.println("wolfy was at row " + wolfrow + " col " + wolfcol);

        // simple scoring + leaderboard update
        int score = calculateScore(win);
        System.out.println("your score: " + score);

        updateLeaderboard(name, score);
        saveLeaderboard();
        showLeaderboard();

        System.out.println("================================");
        System.out.println();
    }
    //ivan
    //ask player if they want to replay
    public static boolean again(Scanner in) {
        System.out.print("play again y or n ");
        String s = in.nextLine().trim().toLowerCase();

        while (s.length() == 0 || (s.charAt(0) != 'y' && s.charAt(0) != 'n')) {
            System.out.print("please enter y or n ");
            s = in.nextLine().trim().toLowerCase();
        }

        return s.charAt(0) == 'y';              // true if yes
    }

    // leaderboard methods - simple but working
    public static int calculateScore(boolean win) {
        // base score depends on win/lose
        if (!win) {
            return 0;
        }

        int base = 1000;           // starting points
        int stepPenalty = steps * 5;   // lose 5 points per step

        int difficultyBonus = 0;
        if ("medium".equals(diffname)) {
            difficultyBonus = 100;
        } else if ("hard".equals(diffname)) {
            difficultyBonus = 250;
        }

        int score = base + difficultyBonus - stepPenalty;
        if (score < 0) score = 0;
        return score;
    }

    public static void updateLeaderboard(String playerName, int score) {
        if (score <= 0) return;  // dont store zero or negative scores

        // find insert position (higher score is better)
        int pos = -1;
        for (int i = 0; i < bestscores.length; i++) {
            if (score > bestscores[i]) {
                pos = i;
                break;
            }
        }

        if (pos == -1) return; // not good enough for top 5

        // shift lower scores down
        for (int i = bestscores.length - 1; i > pos; i--) {
            bestscores[i] = bestscores[i - 1];
            bestnames[i] = bestnames[i - 1];
        }

        // insert new score
        bestscores[pos] = score;
        bestnames[pos] = playerName;
    }

    public static void showLeaderboard() {
        System.out.println("----- leaderboard (top 5) -----");
        for (int i = 0; i < bestscores.length; i++) {
            if (bestscores[i] > 0 && bestnames[i] != null) {
                System.out.println((i + 1) + ". " + bestnames[i] + " - " + bestscores[i]);
            }
        }
        System.out.println("--------------------------------");
    }

    public static void loadLeaderboard() {
        // start with empty scores
        for (int i = 0; i < bestscores.length; i++) {
            bestscores[i] = 0;
            bestnames[i] = null;
        }

        File file = new File("util/leaderboard.txt");
        if (!file.exists()) {
            return; // nothing to load yet
        }

        Scanner in = null;
        try {
            in = new Scanner(file);
            int index = 0;
            while (in.hasNextLine() && index < bestscores.length) {
                String line = in.nextLine().trim();
                if (line.length() == 0) continue;

                int comma = line.lastIndexOf(',');
                if (comma == -1) continue;   // bad line, skip

                String player = line.substring(0, comma);
                String scoreText = line.substring(comma + 1);

                int s;
                try {
                    s = Integer.parseInt(scoreText.trim());
                } catch (NumberFormatException e) {
                    continue;               // skip if score is not a number
                }

                bestnames[index] = player;
                bestscores[index] = s;
                index++;
            }
        } catch (IOException e) {
            // ignore file errors for now
        } finally {
            if (in != null) in.close();
        }
    }

    public static void saveLeaderboard() {
        File file = new File("util/leaderboard.txt");
        PrintWriter out = null;

        try {
            out = new PrintWriter(file);
            for (int i = 0; i < bestscores.length; i++) {
                if (bestscores[i] > 0 && bestnames[i] != null) {
                    out.println(bestnames[i] + "," + bestscores[i]);
                }
            }
        } catch (IOException e) {
            // ignore write errors for now
        } finally {
            if (out != null) out.close();
        }
    }
}

// simple GUI version of rescue wolfy using buttons and a drawing panel
// run this class with: java GuiWolfy (after compiling Main.java)
class GuiWolfy extends JFrame implements ActionListener {

    private RescuePanel panel;        // draws the grid
    private JLabel infoLabel;         // shows name, difficulty, steps, moves
    private JLabel statusLabel;       // shows messages (bomb, win, etc.)
    private JLabel moveTrackerLabel;  // short summary of steps/moves
    private JTextArea moveLogArea;    // detailed move log ("name moved right (3)")

    private JButton upButton;
    private JButton downButton;
    private JButton leftButton;
    private JButton rightButton;
    private JButton scanButton;
    private JButton giveUpButton;
    private JButton newGameButton;

    private boolean done;             // if the game is finished in GUI
    private boolean win;              // win/lose for this GUI round

    private String playerName;        // stored name for GUI runs
    private int guiDifficulty;        // difficulty chosen for GUI (1,2,3)

    public GuiWolfy() {
        super("Rescue Wolfy!");

        // load saved scores before starting GUI game
        Main.loadLeaderboard();

        // ask for name using a simple dialog (like scanner but in GUI)
        playerName = JOptionPane.showInputDialog(this, "Please enter your name: ");
        if (playerName == null) {
            playerName = "player";
        }
        playerName = playerName.trim();
        if (playerName.length() == 0) {
            playerName = "player";
        }

        Main.name = playerName;   // share with the console game variables

        // ask for difficulty using a number: 1 easy, 2 medium, 3 hard
        guiDifficulty = askDifficulty();
        Main.setup(guiDifficulty);
        Main.init();

        done = false;
        win = false;

        // set up labels
        infoLabel = new JLabel("", JLabel.CENTER);
        statusLabel = new JLabel("use the buttons to move", JLabel.CENTER);
        moveTrackerLabel = new JLabel("", JLabel.CENTER);

        // move log area on the side
        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // drawing panel
        panel = new RescuePanel();

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
                    // same as scan button
                    int dr = Main.row - Main.wolfrow;
                    if (dr < 0) dr = -dr;
                    int dc = Main.col - Main.wolfcol;
                    if (dc < 0) dc = -dc;
                    int distance = dr + dc;
                    statusLabel.setText("scanner: wolfy is " + distance + " squares away");
                    logMove("used scan (distance " + distance + ")");
                    updateInfo();
                    panel.repaint();

                } else if (ch == 'g') {
                    // same as give up button
                    done = true;
                    win = false;
                    statusLabel.setText(Main.name + " gave up");
                    logMove("gave up");
                    updateInfo();
                    panel.repaint();
                }
            }
        });

        // buttons panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(2, 4, 5, 5));

        upButton = makeButton("UP", "up");
        downButton = makeButton("DOWN", "down");
        leftButton = makeButton("LEFT", "left");
        rightButton = makeButton("RIGHT", "right");
        scanButton = makeButton("SCAN", "scan");
        giveUpButton = makeButton("GIVE UP", "giveup");
        newGameButton = makeButton("NEW GAME", "newgame");

        buttons.add(upButton);
        buttons.add(downButton);
        buttons.add(leftButton);
        buttons.add(rightButton);
        buttons.add(scanButton);
        buttons.add(giveUpButton);
        buttons.add(newGameButton);
        // one empty cell to keep it simple
        buttons.add(new JLabel(""));

        // side panel on the right: short tracker + detailed move log
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout(5, 5));
        sidePanel.add(moveTrackerLabel, BorderLayout.NORTH);
        JScrollPane moveScroll = new JScrollPane(moveLogArea);
        moveScroll.setPreferredSize(new Dimension(220, 0));
        sidePanel.add(moveScroll, BorderLayout.CENTER);

        // main layout
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout(5, 5));
        main.add(infoLabel, BorderLayout.NORTH);
        main.add(panel, BorderLayout.CENTER);
        main.add(statusLabel, BorderLayout.SOUTH);
        main.add(sidePanel, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800); // slightly larger window so pictures and tracker fit nicely
        setLocationRelativeTo(null);

        updateInfo();

        setVisible(true);
        // make sure panel has focus so WASD keys work immediately
        panel.requestFocusInWindow();
    }

    private JButton makeButton(String text, String command) {
        JButton b = new JButton(text);
        b.setActionCommand(command);
        b.addActionListener(this);
        return b;
    }

    private void updateInfo() {
        String movesText;
        if (Main.moves > -1) {
            movesText = "moves left " + Main.movesleft;
        } else {
            movesText = "moves left unlimited";
        }
        infoLabel.setText("player " + Main.name + "  difficulty " + Main.diffname +
                "  steps " + Main.steps + "  " + movesText);

        // simple move tracker on the side using HTML for two lines
        moveTrackerLabel.setText("<html>steps: " + Main.steps + "<br>" + movesText + "</html>");
    }

    // ask the user for difficulty using a simple number input dialog
    private int askDifficulty() {
        int d = 0;
        while (d < 1 || d > 3) {
            String input = JOptionPane.showInputDialog(this,
                    "choose difficulty:\n" +
                    "1 easy (no move limit)\n" +
                    "2 medium (60 moves)\n" +
                    "3 hard (40 moves)");
            if (input == null) {
                // if they cancel, default to medium
                d = 2;
                break;
            }
            input = input.trim();
            try {
                d = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                d = 0; // force loop again
            }
        }
        return d;
    }

    private void startNewGame() {
        // keep same name and same difficulty as before
        Main.name = playerName;
        Main.setup(guiDifficulty);
        Main.init();
        done = false;
        win = false;
        statusLabel.setText("new game started");
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
            // same distance as console version but shown here
            int dr = Main.row - Main.wolfrow;
            if (dr < 0) dr = -dr;
            int dc = Main.col - Main.wolfcol;
            if (dc < 0) dc = -dc;
            int distance = dr + dc;
            statusLabel.setText("scanner: wolfy is " + distance + " squares away");
        } else if (cmd.equals("giveup")) {
            done = true;
            win = false;
            statusLabel.setText(Main.name + " gave up");
        } else if (cmd.equals("newgame")) {
            startNewGame();
        }

        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }

    private void doMove(char m) {
        if (done) return;     // ignore moves after game over

        if (Main.canmove(m)) {
            Main.domove(m);
            Main.steps++;
            Main.visited[Main.row][Main.col] = true;
            logMove(describeMove(m));

            if (Main.moves > -1) {
                Main.movesleft--;
            }

            if (Main.bombs[Main.row][Main.col]) {
                statusLabel.setText("boom! " + Main.name + " stepped on a mine");
                logMove("hit a mine");
                JOptionPane.showMessageDialog(this,
                        "BOOM! You hit a mine!",
                        "Explosion",
                        JOptionPane.ERROR_MESSAGE);
                done = true;
                win = false;

            } else if (Main.row == Main.wolfrow && Main.col == Main.wolfcol) {
                statusLabel.setText("you rescued wolfy!");
                logMove("rescued wolfy");
                done = true;
                win = true;

            } else if (Main.moves > -1 && Main.movesleft < 0) {
                statusLabel.setText("you ran out of moves");
                logMove("ran out of moves");
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

    // helper to describe a movement direction as text
    private String describeMove(char m) {
        if (m == 'w') return "moved up";
        else if (m == 's') return "moved down";
        else if (m == 'a') return "moved left";
        else if (m == 'd') return "moved right";
        return "moved";
    }

    // add a line to the move log, including current step number
    private void logMove(String action) {
        moveLogArea.append(Main.name + " " + action + " (" + Main.steps + ")\n");
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }

    // small panel that draws the grid using the same data as the console version
    class RescuePanel extends JPanel {

        // picture variables loaded from ./util
        Image playerPic;  // util/player.png
        Image wolfyPic;   // util/wolfy.png
        Image bombPic;    // util/bomb.png
        Image floorPic;   // util/grass.png (background tile)

        public RescuePanel() {
            playerPic = new ImageIcon("util/player.png").getImage();
            wolfyPic  = new ImageIcon("util/wolfy.png").getImage();
            bombPic   = new ImageIcon("util/bomb.png").getImage();
            floorPic  = new ImageIcon("util/grass.png").getImage();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (Main.rows <= 0 || Main.cols <= 0) {
                return;
            }

            int cellSize = Math.min(getWidth() / Main.cols, getHeight() / Main.rows);
            int offsetX = (getWidth() - cellSize * Main.cols) / 2;
            int offsetY = (getHeight() - cellSize * Main.rows) / 2;

            for (int r = 0; r < Main.rows; r++) {
                for (int c = 0; c < Main.cols; c++) {
                    int x = offsetX + c * cellSize;
                    int y = offsetY + r * cellSize;

                    // draw floor tile if available
                    if (floorPic != null) {
                        g.drawImage(floorPic, x, y, cellSize, cellSize, this);
                    }

                    // draw player first
                    if (r == Main.row && c == Main.col && playerPic != null) {
                        g.drawImage(playerPic, x, y, cellSize, cellSize, this);
                    }

                    // draw bomb if this cell has one and has been visited
                    if (Main.visited != null && Main.visited[r][c] && Main.bombs != null && Main.bombs[r][c] && bombPic != null) {
                        // bomb image on top (acts like explosion when you step on it)
                        g.drawImage(bombPic, x, y, cellSize, cellSize, this);
                    }

                    // draw hot/cold number in the middle for visited safe squares
                    if (Main.visited != null && Main.visited[r][c]
                            && (Main.bombs == null || !Main.bombs[r][c])
                            && !(r == Main.row && c == Main.col)) {
                        int heat = Main.getHeatLevelForCell(r, c);
                        if (heat > 0) {
                            g.setColor(Color.WHITE);
                            String text = Integer.toString(heat);
                            FontMetrics fm = g.getFontMetrics();
                            // move number slightly up and right inside the cell
                            int textX = x + cellSize - fm.stringWidth(text) - 4;
                            int textY = y + fm.getAscent() + 2;
                            g.drawString(text, textX, textY);
                        }
                    }

                    // if game done, show where wolfy is
                    if (done && r == Main.wolfrow && c == Main.wolfcol && wolfyPic != null) {
                        g.drawImage(wolfyPic, x, y, cellSize, cellSize, this);
                    }

                    // optional grid lines so you can still see the board layout
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

    // entry point for the GUI only
    public static void main(String[] args) {
        new GuiWolfy();
    }
}
