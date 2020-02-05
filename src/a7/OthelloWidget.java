package a7;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class OthelloWidget extends JPanel implements ActionListener, SpotListener {
    private enum Player {BLACK, WHITE};
    private JSpotBoard board;
    private JLabel message;
    private boolean gameOver;
    private Player nextToPlay;

    public OthelloWidget() {
        this.board = new JSpotBoard(8, 8);
        message = new JLabel();
        setLayout(new BorderLayout());
        add(this.board, BorderLayout.CENTER);
        JPanel resetMessagePanel = new JPanel();
        resetMessagePanel.setLayout(new BorderLayout());
        JButton resetButton = new JButton("Restart");
        resetButton.addActionListener(this);
        resetMessagePanel.add(resetButton, BorderLayout.EAST);
        resetMessagePanel.add(this.message, BorderLayout.CENTER);
        add(resetMessagePanel, BorderLayout.SOUTH);
        board.addSpotListener(this);

        resetGame();
    }

    private void resetGame() {
        for (Spot s : board) {
            s.clearSpot();
            s.setSpotColor(new Color(0.1f, 0.1f, 0.1f));
        }

        // set starting spots
        board.getSpotAt(3, 3).setSpotColor(Color.WHITE);
        board.getSpotAt(3, 3).toggleSpot();
        board.getSpotAt(3, 4).setSpotColor(Color.BLACK);
        board.getSpotAt(3, 4).toggleSpot();
        board.getSpotAt(4, 3).setSpotColor(Color.BLACK);
        board.getSpotAt(4, 3).toggleSpot();
        board.getSpotAt(4, 4).setSpotColor(Color.WHITE);
        board.getSpotAt(4, 4).toggleSpot();

        gameOver = false;
        nextToPlay = Player.BLACK;

        message.setText("Welcome to Othello. Black to play.");
    }

    private int getScore(Color c) {
        int score = 0;
        for (Spot s : board) {
            if (s.getSpotColor() == c) {
                score += 1;
            }
        }
        return score;
    }

    private boolean hasValidMove(Color playerColor) {
        boolean flag = false;
        for (Spot s : board) {
            if (isValidSpot(s, playerColor)) {
                flag = true;
            }
        }
        return flag;
    }

    private boolean isValidSpot(Spot s, Color playerColor) {
        if (!s.isEmpty()) {
            return false;
        }

        // check for adjacent opponent color
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

        boolean hasAdjacent = false;

        for (int[] direction : directions) {
            int xMove = direction[0];
            int yMove = direction[1];

            int currentX = s.getSpotX() + xMove;
            int currentY = s.getSpotY() + yMove;

            if (currentX >= 0 && currentX < board.getSpotWidth() && currentY >= 0 && currentY < board.getSpotHeight()) {
                Color adjacentColor = board.getSpotAt(currentX, currentY).getSpotColor();
                if (adjacentColor != playerColor) {
                    hasAdjacent = true;
                }
            }
        }

        if (!hasAdjacent) {
            return false;
        }

        // check for endpoint
        if (spotsToFlip(s, playerColor).isEmpty()) {
            return false;
        }

        return true;
    }

    private List<Spot> spotsToFlip(Spot s, Color c) {
        List<Spot> spots = new ArrayList<Spot>();

        // all possible directions: up, down, right, left, up/right, up/left, down/right, down/left
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

        for (int[] direction : directions) {
            int xMove = direction[0];
            int yMove = direction[1];

            int currentX = s.getSpotX() + xMove;
            int currentY = s.getSpotY() + yMove;

            // walk in current direction until you fall off the edge of the board
            while (currentX >= 0 && currentX < board.getSpotWidth() && currentY >= 0 && currentY < board.getSpotHeight()) {

                // ran into an empty spot
                if (board.getSpotAt(currentX, currentY).isEmpty()) {
                    break;
                }

                if (board.getSpotAt(currentX, currentY).getSpotColor() == c) {
                    // trace back steps and add to spots List
                    currentX -= xMove;
                    currentY -= yMove;

                    while (currentX != s.getSpotX() || currentY != s.getSpotY()) {
//                        System.out.println("Spot added: x=" + s.getSpotX() + ", y=" + s.getSpotY());
                        spots.add(board.getSpotAt(currentX, currentY));
                        currentX -= xMove;
                        currentY -= yMove;
                    }

                    // done tracing back steps, move on to the next direction
                    break;
                }

                // next step in current direction
                currentX += xMove;
                currentY += yMove;
            }
        }
        return spots;
    }

    @Override
    public void spotClicked(Spot spot) {
        if (gameOver) {
            return;
        }

        // clicked an empty spot
        if (!isValidSpot(spot, nextToPlay == Player.BLACK ? Color.BLACK : Color.WHITE)) {
            return;
        }

        String playerName = null;
        String nextPlayerName = null;
        Color playerColor = null;

        if (nextToPlay == Player.BLACK) {
            playerColor = Color.BLACK;
            playerName = "Black";
            nextPlayerName = "White";
            nextToPlay = Player.WHITE;
        } else {
            playerColor = Color.WHITE;
            playerName = "White";
            nextPlayerName = "Black";
            nextToPlay = Player.BLACK;
        }

        List<Spot> toFlip = spotsToFlip(spot, playerColor);

        // set the selected spot color as well as that flipped
        spot.setSpotColor(playerColor);
        spot.setSpot();
        spot.unhighlightSpot();
        for (Spot s : toFlip) {
            s.setSpotColor(playerColor);
        }

        // If the next player has a move...
        if (hasValidMove(nextToPlay == Player.BLACK ? Color.BLACK : Color.WHITE)) {
            message.setText(nextPlayerName + " to play.");
        }
        // If neither player has a move...
        else if (!hasValidMove(Color.WHITE) && !hasValidMove(Color.BLACK)) {
            gameOver = true;

            int blackScore = getScore(Color.BLACK);
            int whiteScore = getScore(Color.WHITE);

            if (blackScore > whiteScore) {
                message.setText("Game over. Black wins. Score: " + blackScore + " to " + whiteScore);
            } else if (whiteScore > blackScore) {
                message.setText("Game over. White wins. Score: " + blackScore + " to " + whiteScore);
            } else {
                message.setText("Game over. Draw!");
            }

        }
        // If the current player still has moves...
        else {
            if (nextToPlay == Player.BLACK) {
                nextToPlay = Player.WHITE;
            } else {
                nextToPlay = Player.BLACK;
            }
        }

    }

    @Override
    public void spotEntered(Spot spot) {
        if (gameOver) {
            return;
        }
        if (isValidSpot(spot, (nextToPlay == Player.BLACK ? Color.BLACK : Color.WHITE))) {
            spot.highlightSpot();
        }
    }

    @Override
    public void spotExited(Spot spot) {
        spot.unhighlightSpot();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        resetGame();
    }
}
