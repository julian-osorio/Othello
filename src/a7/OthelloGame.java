package a7;

import javax.swing.*;
import java.awt.*;

public class OthelloGame {
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Othello");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        mainFrame.setContentPane(topPanel);

        OthelloWidget ow = new OthelloWidget();
        topPanel.add(ow, BorderLayout.CENTER);

        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
