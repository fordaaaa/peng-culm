import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;          // leaderboard file I/O
import javax.swing.*;        // sorting leaderboard

public class Main extends JFrame implements ActionListener {

    //game settings
    private int rows;               // number of rows in the grid
    private int cols;               // number of columns in the grid
    private int moves;              // move limit (-1 means unlimited moves)
    private String diffname;        // name of difficulty chosen

    //board state
    private boolean[][] visited;    // tracks which cells were visited
    private int row;                // current player row
    private int col;                // current player column
    private int wolfrow;            // wolfy row position
    private int wolfcol;            // wolfy column position

    //player state
    private String name;            // player name
    private int steps;              // steps taken
    private int movesleft;          // remaining moves if limited

    //bomb state 
    private int bombRow;
    private int bombCol;

    // leaderboard
    private static final String LEADERBOARD_FILE = "util/leaderboard.txt";


    // GUI components
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

    private String playerName;      // stored name for GUI runs
    private int guiDifficulty;      // difficulty chosen for GUI (1,2,3)
    //ivan
    public Main() {
        super("Rescue Wolfy!");

        // ask for name using a simple dialog
        playerName = JOptionPane.showInputDialog(this, "Please enter your name: ");
        if (playerName == null) playerName = "player";
        playerName = playerName.trim();
        if (playerName.length() == 0) playerName = "player";
        name = playerName;

        // ask for difficulty using a number: 1 easy, 2 medium, 3 hard
        guiDifficulty = askDifficulty();
        setup(guiDifficulty);
        init();

        // start game sounds (intro + background ambience)
        SoundHandling.playGameStart();
        SoundHandling.startBackgroundLoop();

        SoundHandling.playPageturn();
        JOptionPane.showMessageDialog(this,
                "Rules\n\n" +
                "• The number at the top of each square shows how close the bomb is.\n" +
                "• Press F to scan how far away Wolfy is.\n" +
                "• The leaderboard appears after the game ends.",
                "Rules",
                JOptionPane.INFORMATION_MESSAGE);

        done = false;
        win = false;

        // set up labels
        infoLabel = new JLabel("", JLabel.CENTER);
        statusLabel = new JLabel("use the buttons or WASD/arrow keys to move, f to use the hidden feature..", JLabel.CENTER);
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
        //jaden
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
        buttons.add(new JLabel("")); // spacer

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
        setSize(800, 800);
        setLocationRelativeTo(null);

        updateInfo();

        setVisible(true);
        panel.requestFocusInWindow();
    }
    //ivan
    private JButton makeButton(String text, String command) {
        JButton b = new JButton(text);
        b.setActionCommand(command);
        b.addActionListener(this);
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
    //jaden
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
    //jaden
    private void startNewGame() {
        name = playerName;
        setup(guiDifficulty);
        init();
        done = false;
        win = false;
        statusLabel.setText("new game started");

        // restart intro sound and background ambience for the new game
        SoundHandling.playGameStart();
        SoundHandling.startBackgroundLoop();
        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }
    //ivan
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
        }

        updateInfo();
        panel.repaint();
        panel.requestFocusInWindow();
    }
    //ivan
    private void setup(int d) {
        if (d == 1) {
            rows = 10;
            cols = 10;
            moves = 40;          // easy: 40 moves
            diffname = "easy";
        } else if (d == 2) {
            rows = 10;
            cols = 10;
            moves = 30;          // medium: 25 moves
            diffname = "medium";
        } else {
            rows = 12;
            cols = 12;
            moves = 20;          // hard: 15 moves
            diffname = "hard";
        }
    }
    //ivan
    private void init() {
        visited = new boolean[rows][cols];
        row = 0;
        col = 0;
        visited[row][col] = true;

        // randomly place wolfy somewhere not equal to player start
        wolfrow = (int)(Math.random() * rows);
        wolfcol = (int)(Math.random() * cols);
        while (wolfrow == row && wolfcol == col) {
            wolfrow = (int)(Math.random() * rows);
            wolfcol = (int)(Math.random() * cols);
        }

        // single bomb setup 
        bombRow = (int)(Math.random() * rows);
        bombCol = (int)(Math.random() * cols);
        while ((bombRow == row && bombCol == col) || (bombRow == wolfrow && bombCol == wolfcol)) { // check to make sure that bomb isnt on player/wolfy
            bombRow = (int)(Math.random() * rows);
            bombCol = (int)(Math.random() * cols);
        }

        steps = 0;
        movesleft = moves;
    }
    //ivan
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
            domove(m);
            steps++;
            visited[row][col] = true;
            logMove(describeMove(m));

            // play movement sound each ti            double chance = Math.random(); // 50% chance to move bomb
            if (chance < 0.50) {
                moveBombTowardPlayer();
            }

            // heartbeat / breathing based on how close the bomb is
            int dr = row - bombRo            if (row == bombRow && col == bombCol) {
                statusLabel.setText("boom! " + name + " stepped on a mine");
                logMove("hit a mine");

                // explosion sound and stop ambience
                SoundHandling.playExplosion();
                SoundHandling.stopBackgroundLoop();

                JOptionPane.showMessageDialog(this,
                        "BOOM! You hit a mine!",
                        "Explosion!",
                        JOptionPane.ERROR_MESSAGE);
                
                // leaderboard AFTER explosion (no saving on loss)
                SoundHandling.playPageturn();
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
                SoundHandling.playVictory();
                SoundHandling.stopBackgroundLoop();

                done = true;
                win = true;
                saveToLeaderboard();

                SoundHandling.playPageturn();
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
    //jaden
    // move the bomb one step closer to the player plus-type moves only
    private void moveBombTowardPlayer() {
        if (done) return;
        if (bombRow < row) bombRow++;
        else if (bombRow > row) bombRow--;
        else if (bombCol < col) bombCol++;
        else if (bombCol > col) bombCol--;
    }
    //jaden
    private String describeMove(char m) {
        if (m == 'w') return "moved up";
        else if (m == 's') return "moved down";
        else if (m == 'a') return "moved left";
        else if (m == 'd') return "moved right";
        return "moved";
    }
    //jaden
    private void logMove(String action) {
        moveLogArea.append(name + " " + action + " (" + steps + ")\n");
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }
    //ivan
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
    //ivan
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
    //ivan
    //return a simple "heat" level for a cell based on distance from the bomb
    //higher number = closer (hotter)
    private int getHeatLevelForCell(int r, int c) {
        int dr = r - bombRow;
        if (dr < 0) dr = -dr;
        int dc = c - bombCol;
        if (dc < 0) dc = -dc;
        int dist = dr + dc; // manhattan distance to bomb

        if (dist == 0) return 0;
        else if (dist <= 2) return 6;
        else if (dist <= 4) return 5;
        else if (dist <= 6) return 4;
        else if (dist <= 9) return 3;
        else if (dist <= 13) return 2;
        else return 1;
    }
    //ivan
    // panel that draws the grid
    class RescuePanel extends JPanel {

        Image playerPic;  // util/player.png
        Image wolfyPic;   // util/wolfy.png
        Image bombPic;    // util/bomb.png
        Image floorPic;   // util/grass.png

        public RescuePanel() {
            playerPic = new ImageIcon("util/player.png").getImage();
            wolfyPic  = new ImageIcon("util/wolfy.png").getImage();
            bombPic   = new ImageIcon("util/bomb.png").getImage();
            floorPic  = new ImageIcon("util/grass.png").getImage();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (rows <= 0 || cols <= 0) return;

            int cellSize = Math.min(getWidth() / cols, getHeight() / rows);
            int offsetX = (getWidth() - cellSize * cols) / 2;
            int offsetY = (getHeight() - cellSize * rows) / 2;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = offsetX + c * cellSize;
                    int y = offsetY + r * cellSize;

                    if (floorPic != null) g.drawImage(floorPic, x, y, cellSize, cellSize, this);

                    if (r == row && c == col && playerPic != null) {
                        g.drawImage(playerPic, x, y, cellSize, cellSize, this);
                    }

                    if (done && !win && r == bombRow && c == bombCol && bombPic != null) {
                        g.drawImage(bombPic, x, y, cellSize, cellSize, this);
                    }

                    if (visited != null && visited[r][c] && !(r == bombRow && c == bombCol)) {
                        int heat = getHeatLevelForCell(r, c);
                        if (heat > 0) {
                            g.setColor(Color.WHITE);
                            String text = Integer.toString(heat);
                            FontMetrics fm = g.getFontMetrics();
                            int textX = x + cellSize - fm.stringWidth(text) - 4;
                            int textY = y + fm.getAscent() + 2;
                            g.drawString(text, textX, textY);
                        }
                    }

                    if (done && r == wolfrow && c == wolfcol && wolfyPic != null) {
                        g.drawImage(wolfyPic, x, y, cellSize, cellSize, this);
                    }

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