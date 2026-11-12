package app;

import controller.GameController;
import model.GameModel;
import view.GameView;
import view.GameWindow;

import javax.swing.SwingUtilities;

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
