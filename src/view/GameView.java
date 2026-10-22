package view;

import model.Ball;
import model.Brick;
import model.Capsule;
import model.GameModel;
import model.GameState;
import model.Levels;
import model.Paddle;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// VIEW: só desenha o que está no Model. Não muda nada do jogo.
public class GameView extends JPanel {
    private static final int WIDTH = GameModel.WIDTH, HEIGHT = GameModel.HEIGHT;

    private static final Color BACKGROUND = new Color(10, 10, 30);
    private static final Color ORANGE = new Color(240, 140, 30);
    private static final Color YELLOW = new Color(240, 220, 40);
    private static final Color PINK = new Color(250, 120, 200);
    private static final Color CYAN = new Color(40, 210, 220);
    private static final Color SILVER = new Color(170, 170, 185);
    private static final Color GOLD = new Color(230, 170, 40);
    private static final Color STEEL = new Color(80, 85, 100);
    private static final Color[] ROW_COLORS = {
        new Color(220, 50, 50), ORANGE, YELLOW,
        new Color(60, 200, 80), new Color(50, 150, 240), new Color(180, 70, 220),
    };

    private final GameModel model;

    public GameView(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (model.getState() == GameState.MENU) {
            drawMenu(g);
            return;
        }

        for (Brick brick : model.getBricks()) drawBrick(g, brick);
        drawPaddle(g, model.getPaddle());
        for (Capsule capsule : model.getCapsules()) drawCapsule(g, capsule);
        for (Ball ball : model.getBalls()) drawBall(g, ball);
        drawHud(g);

        int score = model.getScore(), level = model.getLevel();
        switch (model.getState()) {
            case PLAYING -> {
                if (model.isBallOnPaddle()) drawCentered(g, "ESPAÇO para lançar", 18, HEIGHT / 2 + 80, Color.LIGHT_GRAY);
            }
            case PAUSED -> drawOverlay(g, "PAUSADO", "ESC continua  |  Q volta ao menu");
            case LEVEL_CLEAR -> drawOverlay(g, "FASE " + level + " COMPLETA!", "ENTER para a fase " + (level + 1));
            case GAME_OVER -> drawOverlay(g, "GAME OVER", "Pontos: " + score + "  |  ENTER volta ao menu");
            case VICTORY -> drawOverlay(g, "VOCÊ VENCEU!", "Pontos: " + score + "  |  ENTER volta ao menu");
            default -> { }
        }
    }

    // ---------- Elementos do jogo ----------

    private void drawPaddle(Graphics2D g, Paddle p) {
        g.setColor(new Color(200, 200, 210));
        g.fillRoundRect(p.getX(), p.getY(), p.getWidth(), p.getHeight(), 10, 10);
        g.setColor(new Color(220, 40, 40));
        g.fillRect(p.getX() + 6, p.getY() + 3, 12, p.getHeight() - 6);
        g.fillRect(p.getX() + p.getWidth() - 18, p.getY() + 3, 12, p.getHeight() - 6);
    }

    private void drawBall(Graphics2D g, Ball b) {
        g.setColor(Color.WHITE);
        g.fillOval(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }

    private void drawCapsule(Graphics2D g, Capsule c) {
        boolean split = c.getPower() == Capsule.Power.SPLIT;
        g.setColor(split ? CYAN : PINK);
        g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 16, 16);
        g.setColor(Color.WHITE);
        g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 16, 16);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.drawString(split ? "x3" : "+2", c.getX() + 13, c.getY() + 12);
    }

    private void drawBrick(Graphics2D g, Brick brick) {
        int x = brick.getX(), y = brick.getY(), w = Brick.WIDTH, h = Brick.HEIGHT;
        Color c = switch (brick.getType()) {
            case BARRIER -> STEEL;
            case MULTIBALL -> PINK;
            case SPLIT -> CYAN;
            default -> brick.getHits() == 3 ? GOLD : brick.getHits() == 2 ? SILVER : ROW_COLORS[brick.getRow() % ROW_COLORS.length];
        };
        g.setColor(c);
        g.fillRect(x, y, w, h);
        g.setColor(c.brighter());
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x, y, x, y + h - 1);
        g.setColor(c.darker().darker());
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);

        if (brick.getType() == Brick.Type.BARRIER) {
            // rebites para parecer metal
            g.setColor(Color.LIGHT_GRAY);
            for (int rx : new int[] {6, w - 10}) {
                g.fillOval(x + rx, y + 4, 4, 4);
                g.fillOval(x + rx, y + h - 8, 4, 4);
            }
        } else if (brick.getType() == Brick.Type.MULTIBALL) {
            // três bolinhas indicando o poder
            g.setColor(Color.WHITE);
            for (int i = -1; i <= 1; i++) g.fillOval(x + w / 2 - 4 + i * 14, y + h / 2 - 4, 8, 8);
        } else if (brick.getType() == Brick.Type.SPLIT) {
            // uma bolinha que se abre em leque
            g.setColor(Color.WHITE);
            int cx = x + w / 2, cy = y + h - 7;
            g.fillOval(cx - 4, cy - 4, 8, 8);
            g.drawLine(cx, cy, cx - 12, cy - 12);
            g.drawLine(cx, cy, cx + 12, cy - 12);
            g.drawLine(cx, cy, cx, cy - 13);
        }
    }

    // ---------- Telas e textos ----------

    private void drawMenu(Graphics2D g) {
        drawCentered(g, "ARKANOID", 72, 140, ORANGE);

        String[] items = new String[GameModel.MENU_SIZE];
        items[GameModel.MENU_PLAY] = "JOGAR";
        items[GameModel.MENU_LEVEL] = "FASE: < " + (model.getStartLevel() + 1) + " >";
        items[GameModel.MENU_DIFFICULTY] = "DIFICULDADE: < " + model.getDifficultyName() + " >";
        items[GameModel.MENU_EXIT] = "SAIR";

        for (int i = 0; i < items.length; i++) {
            boolean selected = i == model.getMenuIndex();
            drawCentered(g, selected ? "> " + items[i] + " <" : items[i], 28, 230 + i * 50, selected ? Color.WHITE : Color.GRAY);
        }

        drawCentered(g, "Cima/Baixo escolhe  |  Esquerda/Direita altera  |  ENTER confirma", 14, 450, Color.LIGHT_GRAY);
        drawCentered(g, "No jogo: Setas/WASD movem  |  ESPAÇO lança  |  ESC pausa", 14, 475, Color.LIGHT_GRAY);
        drawCentered(g, "Rosa: cápsula +2 bolas  |  Ciano: cápsula x3 (cada bola vira 3)", 14, 500, PINK);
        drawCentered(g, "Cinza com rebites = barreira (não quebra)", 14, 520, Color.LIGHT_GRAY);
        drawCentered(g, "RECORDE: " + model.getHighScore(), 20, 550, YELLOW);
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(30, 30, 60));
        g.fillRect(0, 0, WIDTH, GameModel.HUD_HEIGHT);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("PONTOS: " + model.getScore(), 15, 26);
        g.drawString("FASE: " + Math.min(model.getLevel() + 1, Levels.count()), 230, 26);
        g.drawString(model.getDifficultyName(), 380, 26);
        g.drawString("VIDAS: " + "♥".repeat(Math.max(0, model.getLives())), WIDTH - 180, 26);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        drawCentered(g, title, 48, HEIGHT / 2 - 10, Color.WHITE);
        drawCentered(g, subtitle, 18, HEIGHT / 2 + 40, Color.LIGHT_GRAY);
    }

    private void drawCentered(Graphics2D g, String text, int size, int y, Color color) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        g.setColor(color);
        int x = (WIDTH - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
