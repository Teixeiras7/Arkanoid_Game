package controller;

import model.GameModel;
import model.GameState;
import view.GameView;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameController implements KeyListener, ActionListener {
    private final GameModel model;
    private final GameView view;
    private final Timer timer = new Timer(16, this);
    private boolean left, right;

    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        view.addKeyListener(this);
    }

    public void start() {
        timer.start();
    }

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
                if (confirm) {
                    if (model.getMenuIndex() == GameModel.MENU_PLAY) model.openLevelSelect();
                    if (model.getMenuIndex() == GameModel.MENU_OPTIONS) model.openOptions();
                    if (model.getMenuIndex() == GameModel.MENU_EXIT) System.exit(0);
                }
                if (escape) System.exit(0);
            }
            case OPTIONS -> {
                if (leftKey) model.changeDifficulty(-1);
                if (rightKey) model.changeDifficulty(1);
                if (confirm || escape) model.closeOptions();
            }
            case LEVEL_SELECT -> {
                if (leftKey) model.moveLevelSelection(-1);
                if (rightKey) model.moveLevelSelection(1);
                if (up) model.moveLevelSelection(-GameModel.LEVEL_GRID_COLUMNS);
                if (down) model.moveLevelSelection(GameModel.LEVEL_GRID_COLUMNS);
                if (confirm) model.startSelectedLevel();
                if (escape) model.closeLevelSelect();
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
                if (escape) model.backToMenu();
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
