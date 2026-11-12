package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

public class GameModel {
    public static final int WIDTH = 800, HEIGHT = 600, HUD_HEIGHT = 40;

    public static final int MENU_PLAY = 0, MENU_OPTIONS = 1, MENU_EXIT = 2, MENU_SIZE = 3;

    public static final int LEVEL_GRID_COLUMNS = 5;

    public static final String[] DIFFICULTY_NAMES = {"FÁCIL", "MÉDIO", "DIFÍCIL"};
    private static final double[] DIFFICULTY_SPEEDS = {5, 6.5, 8};
    private static final int START_LIVES = 3;
    private static final int MAX_BALLS = 15;

    private static final Preferences PREFS = Preferences.userNodeForPackage(GameModel.class);
    private static final String PREF_LEVELS_CLEARED = "levelsCleared";

    private GameState state = GameState.MENU;
    private int menuIndex, selectedLevel, difficulty;
    private int levelsCleared = Math.min(PREFS.getInt(PREF_LEVELS_CLEARED, 0), Levels.count());

    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Capsule> capsules = new ArrayList<>();
    private boolean ballOnPaddle;
    private int score, highScore, lives, level;

    public void moveMenuSelection(int delta) {
        menuIndex = Math.floorMod(menuIndex + delta, MENU_SIZE);
    }

    public void openOptions() { state = GameState.OPTIONS; }
    public void closeOptions() { state = GameState.MENU; }

    public void changeDifficulty(int delta) {
        difficulty = Math.floorMod(difficulty + delta, DIFFICULTY_NAMES.length);
    }

    public void openLevelSelect() {
        selectedLevel = Math.min(levelsCleared, Levels.count() - 1);
        state = GameState.LEVEL_SELECT;
    }

    public void closeLevelSelect() {
        state = GameState.MENU;
    }

    public void moveLevelSelection(int delta) {
        int target = selectedLevel + delta;
        if (target >= 0 && target < Levels.count()) selectedLevel = target;
    }

    public void startSelectedLevel() {
        if (isLevelUnlocked(selectedLevel)) newGame(selectedLevel);
    }

    public boolean isLevelUnlocked(int index) {
        return index <= levelsCleared;
    }

    public boolean isLevelCompleted(int index) {
        return index < levelsCleared;
    }

    private void markLevelCleared(int index) {
        if (index + 1 <= levelsCleared) return;
        levelsCleared = index + 1;
        PREFS.putInt(PREF_LEVELS_CLEARED, levelsCleared);
    }

    private void newGame(int startLevel) {
        score = 0;
        lives = START_LIVES;
        level = startLevel;
        loadLevel();
    }

    public void loadLevel() {
        bricks = Levels.build(level, WIDTH);
        paddle = new Paddle((WIDTH - 100) / 2.0, HEIGHT - 50);
        resetBall();
        state = GameState.PLAYING;
    }

    public void launchBall() {
        if (!ballOnPaddle) return;
        balls.get(0).launch(20);
        ballOnPaddle = false;
    }

    public void pause() { state = GameState.PAUSED; }
    public void resume() { state = GameState.PLAYING; }
    public void backToMenu() { endGame(GameState.MENU); }

    private void resetBall() {
        balls = new ArrayList<>();
        balls.add(new Ball(0, 0, ballSpeed()));
        balls.get(0).stickTo(paddle);
        capsules = new ArrayList<>();
        ballOnPaddle = true;
    }

    private double ballSpeed() {
        return DIFFICULTY_SPEEDS[difficulty] + level;
    }

    private void endGame(GameState finalState) {
        highScore = Math.max(highScore, score);
        state = finalState;
    }

    public void update(boolean left, boolean right) {
        paddle.move(left, right, WIDTH);

        if (ballOnPaddle) {
            balls.get(0).stickTo(paddle);
            return;
        }

        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball ball = it.next();
            if (moveBall(ball)) {
                it.remove();
                continue;
            }
            if (ball.isFalling() && ball.collidesWith(paddle)) {
                ball.bounceOffPaddle(paddle);
            }
        }

        updateCapsules();

        if (balls.isEmpty()) {
            lives--;
            if (lives == 0) endGame(GameState.GAME_OVER);
            else resetBall();
            return;
        }

        if (bricks.stream().noneMatch(Brick::isBreakable)) {
            markLevelCleared(level);
            level++;
            if (level == Levels.count()) endGame(GameState.VICTORY);
            else state = GameState.LEVEL_CLEAR;
        }
    }

    // Move a bolinha em passos pequenos, um eixo por vez. Retorna true se ela caiu da tela.
    private boolean moveBall(Ball ball) {
        int steps = ball.steps();
        for (int i = 0; i < steps; i++) {
            ball.moveX(1.0 / steps);
            checkBrickCollisions(ball, true);
            ball.moveY(1.0 / steps);
            checkBrickCollisions(ball, false);
            if (ball.bounceOffWalls(HUD_HEIGHT, WIDTH, HEIGHT)) return true;
        }
        return false;
    }

    private void checkBrickCollisions(Ball ball, boolean horizontal) {
        Brick hit = null;
        double best = 0;
        for (Brick brick : bricks) {
            double area = ball.overlapArea(brick);
            if (area > best) {
                best = area;
                hit = brick;
            }
        }
        if (hit == null) return;

        if (horizontal) ball.bounceOffBrickX(hit);
        else ball.bounceOffBrickY(hit);

        score += hit.hit();
        if (hit.isDestroyed()) {
            bricks.remove(hit);
            if (hit.getType() == Brick.Type.MULTIBALL) capsules.add(new Capsule(hit.centerX(), hit.y, Capsule.Power.EXTRA_BALLS));
            if (hit.getType() == Brick.Type.SPLIT) capsules.add(new Capsule(hit.centerX(), hit.y, Capsule.Power.SPLIT));
        }
    }

    private void updateCapsules() {
        Iterator<Capsule> it = capsules.iterator();
        while (it.hasNext()) {
            Capsule capsule = it.next();
            capsule.move();
            if (capsule.collidesWith(paddle)) {
                it.remove();
                score += 100;
                if (capsule.getPower() == Capsule.Power.SPLIT) splitBalls();
                else spawnExtraBalls();
            } else if (capsule.isOffScreen(HEIGHT)) {
                it.remove();
            }
        }
    }

    private void spawnExtraBalls() {
        for (int angle : new int[] {-35, 35}) {
            if (balls.size() >= MAX_BALLS) return;
            Ball extra = new Ball(paddle.centerX() - Ball.SIZE / 2.0, paddle.y - Ball.SIZE, ballSpeed());
            extra.launch(angle);
            balls.add(extra);
        }
    }

    private void splitBalls() {
        List<Ball> copies = new ArrayList<>();
        for (Ball ball : balls) copies.addAll(ball.split());
        for (Ball copy : copies) {
            if (balls.size() >= MAX_BALLS) return;
            balls.add(copy);
        }
    }

    public GameState getState() { return state; }
    public int getMenuIndex() { return menuIndex; }
    public int getSelectedLevel() { return selectedLevel; }
    public int getLevelCount() { return Levels.count(); }
    public int getDifficulty() { return difficulty; }
    public String getDifficultyName() { return DIFFICULTY_NAMES[difficulty]; }
    public Paddle getPaddle() { return paddle; }
    public List<Ball> getBalls() { return Collections.unmodifiableList(balls); }
    public List<Brick> getBricks() { return Collections.unmodifiableList(bricks); }
    public List<Capsule> getCapsules() { return Collections.unmodifiableList(capsules); }
    public boolean isBallOnPaddle() { return ballOnPaddle; }
    public int getScore() { return score; }
    public int getHighScore() { return highScore; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
}
