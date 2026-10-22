package view;

import javax.swing.JFrame;

// VIEW: a janela do jogo, que mostra o painel GameView.
public class GameWindow extends JFrame {

    public GameWindow(GameView view) {
        super("Arkanoid");
        add(view);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        view.requestFocusInWindow(); // o painel precisa do foco para receber o teclado
    }
}
