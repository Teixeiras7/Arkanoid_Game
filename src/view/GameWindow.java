package view;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    public GameWindow(GameView view) {
        super("Arkanoid");
        add(view);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        view.requestFocusInWindow();
    }
}
