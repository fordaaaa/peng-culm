import java.util.Scanner;

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

    //bomb state    
    public static boolean[][] bombs; // true if there is a bomb in the specific cell
    public static int bombcount; // number of bombs on the grid for difficulty settings

    public static void main(String[] args) {
        run();                     // start the game program
    }
//ivan
    //main game loop
    public static void run() {
        Scanner in = new Scanner(System.in);  // scanner for all user input   (scanner is already defined here and we are borrowing it for the other methods, if i define scanner again in another method the code with get condused)
        boolean play = true;                  // loop control for replaying game

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

            } else { if (canmove(m)) {             // verify move is inside grid
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
                    } else {
                    System.out.println("cant move outside the grid");
                }
            }
        }

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

                if (r == row && c == col) {        // player position
                    System.out.print(" p ");

                } else if (visited[r][c]) {       // visited square
                    System.out.print(" . ");

                } else {                           // unexplored
                    System.out.print(" - ");
                }
            }
            System.out.println();
        }
    }
//jaden
    //get user input
    public static char getmove(Scanner in) {
        System.out.print("enter move w a s d or g to give up ");
        String s = in.nextLine().trim().toLowerCase();

        // validate input until correct
        while (s.length() == 0 || "wasdg".indexOf(s.charAt(0)) == -1) {
            System.out.print("please enter w a s d or g ");
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
}
