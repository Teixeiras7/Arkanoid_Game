package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// MODEL: guarda todo o estado do jogo e aplica as regras.
// Não sabe nada de Swing, teclado ou desenho.
public class GameModel {
    public static final int WIDTH = 800, HEIGHT = 600, HUD_HEIGHT = 40;

    // Itens do menu inicial
    public static final int MENU_PLAY = 0, MENU_LEVEL = 1, MENU_DIFFICULTY = 2, MENU_EXIT = 3, MENU_SIZE = 4;

    // Dificuldade = velocidade base da bola (cada fase ainda soma +1)
    public static final String[] DIFFICULTY_NAMES = {"FÁCIL", "MÉDIO", "DIFÍCIL"};
    private static final double[] DIFFICULTY_SPEEDS = {5, 6.5, 8};
    private static final int START_LIVES = 3;
    private static final int MAX_BALLS = 15; // limite para o x3 não multiplicar sem fim

    private GameState state = GameState.MENU;
    private int menuIndex, startLevel, difficulty;

    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Capsule> capsules = new ArrayList<>();
    private boolean ballOnPaddle;
    private int score, highScore, lives, level;

    // ---------- Menu ----------

    public void moveMenuSelection(int delta) {
        menuIndex = Math.floorMod(menuIndex + delta, MENU_SIZE);
    }

    // Esquerda/direita mudam a fase inicial ou a dificuldade, conforme o item selecionado.
    public void changeMenuOption(int delta) {
        if (menuIndex == MENU_LEVEL) startLevel = Math.floorMod(startLevel + delta, Levels.count());
        if (menuIndex == MENU_DIFFICULTY) difficulty = Math.floorMod(difficulty + delta, DIFFICULTY_NAMES.length);
    }

    // ---------- Fluxo da partida ----------

    public void newGame() {
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
        capsules = new ArrayList<>();
        ballOnPaddle = true;
    }

    private double ballSpeed() {
        return DIFFICULTY_SPEEDS[difficulty] + level; // cada fase deixa a bola mais rápida
    }

    private void endGame(GameState finalState) {
        highScore = Math.max(highScore, score);
        state = finalState;
    }

    // ---------- Atualização (um quadro do jogo) ----------

    public void update(boolean left, boolean right) {
        paddle.move(left, right, WIDTH);

        if (ballOnPaddle) {
            balls.get(0).stickTo(paddle);
            return;
        }

        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball ball = it.next();
            ball.move();
            if (ball.bounceOffWalls(HUD_HEIGHT, WIDTH, HEIGHT)) {
                it.remove(); // caiu: some do jogo
                continue;
            }
            if (ball.isFalling() && ball.collidesWith(paddle)) {
                ball.bounceOffPaddle(paddle);
            }
            checkBrickCollisions(ball);
        }

        updateCapsules();

        // Só perde vida quando TODAS as bolas caírem
        if (balls.isEmpty()) {
            lives--;
            if (lives == 0) endGame(GameState.GAME_OVER);
            else resetBall();
            return;
        }

        // Barreiras não contam: a fase acaba quando não sobra tijolo quebrável
        if (bricks.stream().noneMatch(Brick::isBreakable)) {
            level++;
            if (level == Levels.count()) endGame(GameState.VICTORY);
            else state = GameState.LEVEL_CLEAR;
        }
    }

    private void checkBrickCollisions(Ball ball) {
        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick brick = it.next();
            if (ball.collidesWith(brick)) {
                ball.bounceOffBrick(brick);
                score += brick.hit();
                if (brick.isDestroyed()) {
                    it.remove();
                    if (brick.getType() == Brick.Type.MULTIBALL) capsules.add(new Capsule(brick.centerX(), brick.y, Capsule.Power.EXTRA_BALLS));
                    if (brick.getType() == Brick.Type.SPLIT) capsules.add(new Capsule(brick.centerX(), brick.y, Capsule.Power.SPLIT));
                }
                return; // um tijolo por quadro evita quiques duplos
            }
        }
    }

    // Cápsulas caem; se a raquete pegar, ativa o poder. Se cair no fundo, perdeu.
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

    // Solta duas bolas novas de cima da raquete.
    private void spawnExtraBalls() {
        for (int angle : new int[] {-35, 35}) {
            if (balls.size() >= MAX_BALLS) return;
            Ball extra = new Ball(paddle.centerX() - Ball.SIZE / 2.0, paddle.y - Ball.SIZE, ballSpeed());
            extra.launch(angle);
            balls.add(extra);
        }
    }

    // Cada bola em jogo vira 3, no lugar onde ela está.
    private void splitBalls() {
        List<Ball> copies = new ArrayList<>();
        for (Ball ball : balls) copies.addAll(ball.split());
        for (Ball copy : copies) {
            if (balls.size() >= MAX_BALLS) return;
            balls.add(copy);
        }
    }

    // ---------- Leitura do estado (usada pela View e pelo Controller) ----------

    public GameState getState() { return state; }
    public int getMenuIndex() { return menuIndex; }
    public int getStartLevel() { return startLevel; }
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
