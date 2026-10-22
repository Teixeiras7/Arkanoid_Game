package app;

import controller.GameController;
import model.GameModel;
import view.GameView;
import view.GameWindow;

import javax.swing.SwingUtilities;

// Ponto de entrada: monta o MVC (Model, View e Controller) e inicia o jogo.
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            GameView view = new GameView(model);
            GameController controller = new GameController(model, view);
            new GameWindow(view);
            controller.start();
        });
    }
}
