import java.util.Scanner;

public class Main {

    // game settings
    static int rows;
    static int cols;
    static int moves;       // -1 means no limit
    static String diffname;

    // board state
    static boolean[][] visited;
    static int row;         // player row
    static int col;         // player col
    static int wolfrow;
    static int wolfcol;

    // player state
    static String name;
    static int steps;
    static int movesleft;

    public static void main(String[] args) {
        run();
    }

    // main loop for whole game
    static void run() {
        Scanner in = new Scanner(System.in);
        boolean play = true;

        while (play) {
            openingscreen();
            getname(in);
            int diff = getdifficulty(in);
            setup(diff);
            init();
            game(in);
            play = again(in);
        }

        in.close();
        System.out.println("thanks for playing");
    }

    // show basic info about game
    static void openingscreen() {
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
        // later add full rules and pictures
    }

    // get player name
    static void getname(Scanner in) {
        System.out.print("enter your name ");
        name = in.nextLine().trim();
        if (name.length() == 0) {
            name = "player";
        }
        System.out.println("welcome " + name);
        System.out.println();
    }

    // get difficulty from user
    static int getdifficulty(Scanner in) {
        int d = 0;
        while (d < 1 || d > 3) {
            System.out.println("choose difficulty");
            System.out.println("1 easy no move limit");
            System.out.println("2 medium 60 moves");
            System.out.println("3 hard 40 moves");
            System.out.print("enter 1 2 or 3 ");
            String s = in.nextLine().trim();
            try {
                d = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                d = 0;
            }
            if (d < 1 || d > 3) {
                System.out.println("invalid choice");
            }
        }
        System.out.println();
        return d;
    }

    // set rows cols and moves based on difficulty
    static void setup(int d) {
        if (d == 1) {         // easy
            rows = 10;
            cols = 10;
            moves = -1;       // no limit
            diffname = "easy";
        } else if (d == 2) {  // medium
            rows = 10;
            cols = 10;
            moves = 60;
            diffname = "medium";
        } else {              // hard
            rows = 12;
            cols = 12;
            moves = 40;
            diffname = "hard";
        }
    }

    // make arrays and random wolfy and reset counters
    static void init() {
        visited = new boolean[rows][cols];

        // starting position
        row = 0;
        col = 0;
        visited[row][col] = true;

        // random wolfy not at start
        wolfrow = (int)(Math.random() * rows);
        wolfcol = (int)(Math.random() * cols);
        while (wolfrow == row && wolfcol == col) {
            wolfrow = (int)(Math.random() * rows);
            wolfcol = (int)(Math.random() * cols);
        }

        steps = 0;
        movesleft = moves;
        // later add mines radar score etc
    }

    // one full game
    static void game(Scanner in) {
        boolean done = false;
        boolean win = false;

        while (!done) {
            showscreen();
            char m = getmove(in);

            if (m == 'g') {
                System.out.println(name + " gave up");
                done = true;
                win = false;
            } else {
                if (canmove(m)) {
                    domove(m);
                    steps++;
                    visited[row][col] = true;

                    if (moves > -1) {
                        movesleft--;
                    }

                    // check win
                    if (row == wolfrow && col == wolfcol) {
                        win = true;
                        done = true;
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

        // later add score before end screen
        endscreen(win);
    }

    // show info and grid
    static void showscreen() {
        System.out.println();
        System.out.println("===================================");
        System.out.println("player " + name + "  difficulty " + diffname);
        System.out.println("grid " + rows + " x " + cols);
        System.out.println("steps used " + steps);
        if (moves > -1) {
            System.out.println("moves left " + movesleft);
        } else {
            System.out.println("moves left unlimited");
        }
        System.out.println("-----------------------------------");
        showgrid();
        System.out.println("-----------------------------------");
        // later show mines around and radar signal
    }

    // draw the grid
    static void showgrid() {
        // column labels
        System.out.print("   ");
        for (int c = 0; c < cols; c++) {
            if (c < 10) {
                System.out.print(" " + c + " ");
            } else {
                System.out.print(c + " ");
            }
        }
        System.out.println();

        for (int r = 0; r < rows; r++) {
            if (r < 10) {
                System.out.print(" " + r + " ");
            } else {
                System.out.print(r + " ");
            }

            for (int c = 0; c < cols; c++) {
                if (r == row && c == col) {
                    System.out.print(" p ");
                } else if (visited[r][c]) {
                    System.out.print(" . ");
                } else {
                    System.out.print(" - ");
                }
                // later maybe show wolfy or mines for debug
            }
            System.out.println();
        }
    }

    // ask for move key
    static char getmove(Scanner in) {
        System.out.print("enter move w a s d or g to give up ");
        String s = in.nextLine().trim().toLowerCase();
        while (s.length() == 0 || "wasdg".indexOf(s.charAt(0)) == -1) {
            System.out.print("please enter w a s d or g ");
            s = in.nextLine().trim().toLowerCase();
        }
        return s.charAt(0);
    }

    // check if move stays inside grid
    static boolean canmove(char m) {
        int nr = row;
        int nc = col;

        if (m == 'w') {
            nr--;
        } else if (m == 's') {
            nr++;
        } else if (m == 'a') {
            nc--;
        } else if (m == 'd') {
            nc++;
        } else {
            return false;
        }

        if (nr < 0 || nr >= rows) return false;
        if (nc < 0 || nc >= cols) return false;
        return true;
    }

    // update player row and col
    static void domove(char m) {
        if (m == 'w') {
            row--;
        } else if (m == 's') {
            row++;
        } else if (m == 'a') {
            col--;
        } else if (m == 'd') {
            col++;
        }
        // later stop moves into walls or guards
    }

    // show result of game
    static void endscreen(boolean win) {
        System.out.println();
        System.out.println("========== game over ==========");
        if (win) {
            System.out.println("you rescued wolfy " + name);
            System.out.println("you used " + steps + " moves");
        } else {
            System.out.println("mission failed " + name);
            System.out.println("you did not reach wolfy");
        }
        System.out.println("wolfy was at row " + wolfrow + " col " + wolfcol);
        System.out.println("================================");
        System.out.println();
        // later add score and leaderboard
    }

    // ask if user wants to play again
    static boolean again(Scanner in) {
        System.out.print("play again y or n ");
        String s = in.nextLine().trim().toLowerCase();
        while (s.length() == 0 || (s.charAt(0) != 'y' && s.charAt(0) != 'n')) {
            System.out.print("please enter y or n ");
            s = in.nextLine().trim().toLowerCase();
        }
        return s.charAt(0) == 'y';
    }

    // later add methods for mines radar scoring gui drawing and files
}