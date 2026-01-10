package cardgameics; // wont work without this for some reason (program did it for me automatically)
/*
    Aiden Forder, Dec 4, 2025
    ICS3U1 - Card Game (Methods and Arrays assignment)

    simple card game with 2 players that deals random cards, shows scores,
    and uses an extra round if the first scores are tied
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ilovecards implements ActionListener {

    //array that stores all 52 card pictures
    ImageIcon[] deck;

    // labels that will show the cards for each player
    JLabel[] aCards;
    JLabel[] bCards;

    // labels to display scores and messages
    JLabel aScoreLabel;
    JLabel bScoreLabel;
    JLabel messageLabel;
    JLabel totalLabel;

    // buttons
    JButton playAgainButton;
    JButton exitButton;

    // description: set up arrays and window for the card game
    // parameters: none
    // return: nothing
    public ilovecards() {
        loadDeckImages();

        aCards = new JLabel[5];
        bCards = new JLabel[5];

        for (int i = 0; i < 5; i++) {
            aCards[i] = new JLabel();
            bCards[i] = new JLabel();
        }

        //hide the possible tiebreaker cards at the start
        for (int i = 2; i < 5; i++) {
            aCards[i].setVisible(false);
            bCards[i].setVisible(false);
        }

        aScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        bScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        messageLabel = new JLabel("Click PLAY AGAIN to start", JLabel.CENTER);
        totalLabel = new JLabel("", JLabel.CENTER);

        // build the frame
        JFrame frame = new JFrame("Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 5));
        mainPanel.setPreferredSize(new Dimension(900, 600));

        //panel that holds both players (top = player b, bottom = player a)
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new GridLayout(2, 1, 5, 5));

        // top player - player b
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(5, 5));

        JLabel bTitle = new JLabel("Player B", JLabel.CENTER);
        topPanel.add(bTitle, BorderLayout.NORTH);

        JPanel bCardsPanel = new JPanel();
        bCardsPanel.setLayout(new FlowLayout());
        for (int i = 0; i < 5; i++) {
            bCardsPanel.add(bCards[i]);
        }
        topPanel.add(bCardsPanel, BorderLayout.CENTER);
        topPanel.add(bScoreLabel, BorderLayout.SOUTH);

        // bottom player - player a
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(5, 5));

        JLabel aTitle = new JLabel("Player A", JLabel.CENTER);
        bottomPanel.add(aTitle, BorderLayout.NORTH);

        JPanel aCardsPanel = new JPanel();
        aCardsPanel.setLayout(new FlowLayout());
        for (int i = 0; i < 5; i++) {
            aCardsPanel.add(aCards[i]);
        }
        bottomPanel.add(aCardsPanel, BorderLayout.CENTER);
        bottomPanel.add(aScoreLabel, BorderLayout.SOUTH);

        playersPanel.add(topPanel);
        playersPanel.add(bottomPanel);

        // information labels for winner and total
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.add(messageLabel);
        infoPanel.add(totalLabel);

        //bottom - buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        playAgainButton = new JButton("PLAY AGAIN");
        playAgainButton.setActionCommand("play");
        playAgainButton.addActionListener(this);
        buttonPanel.add(playAgainButton);

        exitButton = new JButton("EXIT");
        exitButton.setActionCommand("exit");
        exitButton.addActionListener(this);
        buttonPanel.add(exitButton);

        // add everything to the frame (top = info, middle = players, bottom = buttons)
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(playersPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // description: load all card images into the deck array
    //parameters: none
    // return: nothing
    void loadDeckImages() {
        deck = new ImageIcon[52];

        char[] suits = { 'c', 'd', 'h', 's' };

        int index = 0;
        for (int s = 0; s < 4; s++) {
            for (int value = 1; value <= 13; value++) {
                String numberPart;
                if (value < 10) {
                    numberPart = "0" + value;
                } else {
                    numberPart = "" + value;
                }

                // try loading from java folder (cardgameics/Cards) first
                String fileName1 = "cardgameics/Cards/" + numberPart + suits[s] + ".gif";
                ImageIcon icon = new ImageIcon(fileName1);

                // if that fails (for example if program started inside cardgameics), try Cards/ directly
                if (icon.getIconWidth() <= 0) {
                    String fileName2 = "Cards/" + numberPart + suits[s] + ".gif";
                    icon = new ImageIcon(fileName2);
                }

                deck[index] = icon;
                index++;
            }
        }
    }

    //work out the score of a card based on its index in the deck
    // parameters: cardIndex - position in the deck
    //return: value of the card
    int scoreOfCard(int cardIndex) {
        int rank = cardIndex % 13 + 1; // 1 = ace, 11 = jack, 12 = queen, 13 = king

        if (rank == 1) {
            return 11; // ace
        } else if (rank >= 11) {
            return 10; // jack, queen, king
        } else {
            return rank; // 2..10
        }
    }

    // choose a random card, show it on a label, and return its score
    //parameters: label - where to draw the card picture
    // return: score value of the card
    int dealOneCard(JLabel label) {
        int index = (int) (Math.random() * 52);
        label.setIcon(deck[index]);
        return scoreOfCard(index);
    }

    // play a full round - 2 cards each, and possibly 3 more if tie
    // parameters: none
    //return: nothing
    void playRound() {
        //hide tie breaker cards before starting the round
        for (int i = 2; i < 5; i++) {
            aCards[i].setVisible(false);
            bCards[i].setVisible(false);
        }

        // first two cards for each player
        int aTotal = dealOneCard(aCards[0]) + dealOneCard(aCards[1]);
        int bTotal = dealOneCard(bCards[0]) + dealOneCard(bCards[1]);

        //update labels for the first part
        aScoreLabel.setText("Score: " + aTotal);
        bScoreLabel.setText("Score: " + bTotal);
        totalLabel.setText("Total: " + (aTotal + bTotal));

        // check if there is a clear winner already
        if (aTotal > bTotal) {
            messageLabel.setText("Player A wins!");
            messageLabel.setForeground(Color.BLUE);
        } else if (bTotal > aTotal) {
            messageLabel.setText("Player B wins!");
            messageLabel.setForeground(Color.RED);
        } else {
            // bonus starts here
            //tie - deal three extra cards to each player
            for (int i = 2; i < 5; i++) {
                aCards[i].setVisible(true);
                bCards[i].setVisible(true);
            }

            for (int i = 2; i < 5; i++) {
                aTotal += dealOneCard(aCards[i]);
                bTotal += dealOneCard(bCards[i]);
            }

            // update scores and total after tie breaker
            aScoreLabel.setText("Score: " + aTotal);
            bScoreLabel.setText("Score: " + bTotal);
            totalLabel.setText("Total: " + (aTotal + bTotal));

            if (aTotal > bTotal) {
                messageLabel.setText("Player A wins (tiebreaker)!");
                messageLabel.setForeground(Color.BLUE);
            } else if (bTotal > aTotal) {
                messageLabel.setText("Player B wins (tiebreaker)!");
                messageLabel.setForeground(Color.RED);
            } else {
                messageLabel.setText("Tie game!");
                messageLabel.setForeground(Color.BLACK);
            }
        }
    }

    // respond to button clicks
    //parameters: e - the action event from the button
    // return: nothing
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("play")) {
            playRound();
        } else if (command.equals("exit")) {
            System.exit(0);
        }
    }

    // main method
    public static void main(String[] args) {
        new ilovecards();
    }
}
