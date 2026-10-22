package controller;

import model.GameModel;
import model.GameState;
import view.GameView;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// CONTROLLER: recebe os eventos (teclado e Timer), manda o Model agir
// e pede para a View redesenhar.
public class GameController implements KeyListener, ActionListener {
    private final GameModel model;
    private final GameView view;
    private final Timer timer = new Timer(16, this); // ~60 FPS
    private boolean left, right;

    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        view.addKeyListener(this);
    }

    public void start() {
        timer.start();
    }

    // Cada "tique" do Timer = um quadro do jogo.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (model.getState() == GameState.PLAYING) model.update(left, right);
        view.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        boolean up = key == KeyEvent.VK_UP || key == KeyEvent.VK_W;
        boolean down = key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S;
        boolean leftKey = key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A;
        boolean rightKey = key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D;
        boolean confirm = key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE;
        boolean escape = key == KeyEvent.VK_ESCAPE;

        switch (model.getState()) {
            case MENU -> {
                if (up) model.moveMenuSelection(-1);
                if (down) model.moveMenuSelection(1);
                if (leftKey) model.changeMenuOption(-1);
                if (rightKey) model.changeMenuOption(1);
                if (confirm) {
                    if (model.getMenuIndex() == GameModel.MENU_EXIT) System.exit(0);
                    else model.newGame();
                }
                if (escape) System.exit(0);
            }
            case PLAYING -> {
                if (leftKey) left = true;
                if (rightKey) right = true;
                if (key == KeyEvent.VK_SPACE || up) model.launchBall();
                if (escape) model.pause();
            }
            case PAUSED -> {
                if (escape) model.resume();
                if (key == KeyEvent.VK_Q) model.backToMenu();
            }
            case LEVEL_CLEAR -> {
                if (confirm) model.loadLevel();
            }
            case GAME_OVER, VICTORY -> {
                if (confirm || escape) model.backToMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
