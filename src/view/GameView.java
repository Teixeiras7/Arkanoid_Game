package view;

import model.Ball;
import model.Brick;
import model.Capsule;
import model.GameModel;
import model.GameState;
import model.Levels;
import model.Paddle;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class GameView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int WIDTH = GameModel.WIDTH, HEIGHT = GameModel.HEIGHT;

    private static final Color BG_TOP = new Color(8, 8, 28);
    private static final Color BG_BOTTOM = new Color(30, 10, 50);
    private static final Color PANEL = new Color(22, 22, 52);
    private static final Color PANEL_BORDER = new Color(80, 80, 130);
    private static final Color ORANGE = new Color(240, 140, 30);
    private static final Color YELLOW = new Color(240, 220, 40);
    private static final Color GREEN = new Color(60, 200, 80);
    private static final Color RED = new Color(220, 50, 50);
    private static final Color PINK = new Color(250, 120, 200);
    private static final Color CYAN = new Color(40, 210, 220);
    private static final Color SILVER = new Color(170, 170, 185);
    private static final Color GOLD = new Color(230, 170, 40);
    private static final Color STEEL = new Color(80, 85, 100);
    private static final Color[] ROW_COLORS = {
        RED, ORANGE, YELLOW, GREEN, new Color(50, 150, 240), new Color(180, 70, 220),
    };

    private static final Color[] DIFFICULTY_COLORS = {GREEN, YELLOW, RED};
    private static final String[] DIFFICULTY_DESCRIPTIONS = {"Velocidade normal", "Bola mais rápida", "Bola muito rápida"};

    private final GameModel model;
    private final int[][] stars = new int[90][3];

    public GameView(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        Random random = new Random(7);
        for (int[] star : stars) {
            star[0] = random.nextInt(WIDTH);
            star[1] = random.nextInt(HEIGHT);
            star[2] = random.nextInt(628);
        }
    }

    private static double time() {
        return System.nanoTime() / 1e9;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g);
        switch (model.getState()) {
            case MENU -> drawMenu(g);
            case LEVEL_SELECT -> {
                drawMenu(g);
                drawLevelSelect(g);
            }
            case OPTIONS -> drawOptions(g);
            default -> drawGame(g);
        }
    }

    private void drawBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, BG_TOP, 0, HEIGHT, BG_BOTTOM));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        double t = time();
        for (int[] star : stars) {
            int alpha = (int) (130 + 110 * Math.sin(t * 2 + star[2] / 100.0));
            g.setColor(new Color(255, 255, 255, alpha));
            int size = star[2] % 3 == 0 ? 2 : 1;
            g.fillRect(star[0], star[1], size, size);
        }
    }

    private void drawMenu(Graphics2D g) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 10; col++) {
                drawBrickShape(g, Brick.Type.NORMAL, 1, row, 22 + col * 76, 20 + row * 22, 72, 18);
            }
        }

        drawTitle(g, "ARKANOID", 84, 175);
        drawCentered(g, "-  BREAKOUT RETRÔ  -", 14, 205, new Color(180, 160, 220));

        String[] items = new String[GameModel.MENU_SIZE];
        items[GameModel.MENU_PLAY] = "JOGAR";
        items[GameModel.MENU_OPTIONS] = "OPÇÕES";
        items[GameModel.MENU_EXIT] = "SAIR";
        for (int i = 0; i < items.length; i++) {
            drawButton(g, items[i], 245 + i * 68, i == model.getMenuIndex());
        }

        drawMenuDemo(g);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.setColor(YELLOW);
        String record = "RECORDE: " + model.getHighScore();
        g.drawString(record, WIDTH - 20 - g.getFontMetrics().stringWidth(record), HEIGHT - 15);
    }

    private void drawTitle(Graphics2D g, String text, int size, int y) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        g.setColor(new Color(90, 30, 0));
        g.drawString(text, x + 5, y + 5);
        g.setPaint(new GradientPaint(0, y - fm.getAscent(), YELLOW, 0, y, ORANGE));
        g.drawString(text, x, y);
    }

    private void drawButton(Graphics2D g, String text, int y, boolean selected) {
        int w = 300, h = 50, x = (WIDTH - w) / 2;
        if (selected) {
            double pulse = (Math.sin(time() * 5) + 1) / 2;
            for (int k = 3; k >= 1; k--) {
                g.setColor(new Color(240, 140, 30, (int) (25 + 25 * pulse)));
                g.fillRoundRect(x - k * 4, y - k * 4, w + k * 8, h + k * 8, 24 + k * 4, 24 + k * 4);
            }
            g.setPaint(new GradientPaint(0, y, YELLOW, 0, y + h, ORANGE));
            g.fillRoundRect(x, y, w, h, 20, 20);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, w, h, 20, 20);
            g.setColor(new Color(90, 30, 0));
            g.fillPolygon(new int[] {x + 22, x + 34, x + 22}, new int[] {y + 16, y + 25, y + 34}, 3);
            g.fillPolygon(new int[] {x + w - 22, x + w - 34, x + w - 22}, new int[] {y + 16, y + 25, y + 34}, 3);
        } else {
            g.setColor(PANEL);
            g.fillRoundRect(x, y, w, h, 20, 20);
            g.setColor(PANEL_BORDER);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, w, h, 20, 20);
        }
        g.setStroke(new BasicStroke(1));
        drawTextInBox(g, text, 26, WIDTH / 2, y, h, selected ? new Color(60, 20, 0) : new Color(180, 180, 200));
    }

    private void drawMenuDemo(Graphics2D g) {
        int paddleX = (int) (WIDTH / 2 + Math.sin(time() * 1.1) * 220) - 50;
        drawPaddleShape(g, paddleX, 545, 100, 14);
    }

    private void drawLevelSelect(Graphics2D g) {
        int count = model.getLevelCount();
        int cols = Math.min(count, GameModel.LEVEL_GRID_COLUMNS);
        int rows = (count + cols - 1) / cols;
        int size = 80, gap = 24;
        int gridW = cols * size + (cols - 1) * gap;
        int panelW = Math.max(gridW + 80, 480), panelH = 125 + rows * (size + gap);
        int panelX = (WIDTH - panelW) / 2, panelY = (HEIGHT - panelH) / 2;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        drawPanel(g, panelX, panelY, panelW, panelH, ORANGE);
        drawCentered(g, "ESCOLHA A FASE", 28, panelY + 50, ORANGE);

        int gridX = (WIDTH - gridW) / 2, gridY = panelY + 75;
        for (int i = 0; i < count; i++) {
            drawLevelSquare(g, i, gridX + (i % cols) * (size + gap), gridY + (i / cols) * (size + gap), size);
        }

        String hint = model.isLevelUnlocked(model.getSelectedLevel())
                ? "ENTER joga  |  ESC volta"
                : "Fase bloqueada: passe da fase anterior";
        drawCentered(g, hint, 14, panelY + panelH - 25, Color.LIGHT_GRAY);
    }

    private void drawLevelSquare(Graphics2D g, int index, int x, int y, int size) {
        boolean unlocked = model.isLevelUnlocked(index);
        boolean completed = model.isLevelCompleted(index);

        g.setColor(!unlocked ? new Color(60, 60, 75) : completed ? new Color(50, 170, 80) : new Color(50, 120, 220));
        g.fillRoundRect(x, y, size, size, 14, 14);

        if (index == model.getSelectedLevel()) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(4));
            g.drawRoundRect(x - 4, y - 4, size + 8, size + 8, 18, 18);
            g.setStroke(new BasicStroke(1));
        }

        if (!unlocked) {
            drawPadlock(g, x + size / 2, y + size / 2);
            return;
        }

        drawTextInBox(g, String.valueOf(index + 1), 36, x + size / 2, y, size, Color.WHITE);
        if (completed) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            g.setColor(YELLOW);
            g.drawString("OK", x + size - 24, y + 16);
        }
    }

    private void drawPadlock(Graphics2D g, int cx, int cy) {
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(4));
        g.drawArc(cx - 10, cy - 20, 20, 24, 0, 180);
        g.setStroke(new BasicStroke(1));
        g.fillRoundRect(cx - 15, cy - 8, 30, 24, 6, 6);
        g.setColor(new Color(60, 60, 75));
        g.fillOval(cx - 3, cy - 1, 6, 6);
        g.fillRect(cx - 1, cy + 3, 3, 7);
    }

    private void drawOptions(Graphics2D g) {
        drawTitle(g, "OPÇÕES", 44, 65);

        drawPanel(g, 40, 90, 720, 175, PANEL_BORDER);
        drawSectionTitle(g, "DIFICULDADE", 60, 118);
        for (int i = 0; i < GameModel.DIFFICULTY_NAMES.length; i++) {
            drawDifficultyCard(g, i, 75 + i * 225, 135);
        }
        drawCentered(g, "use Esquerda / Direita para mudar", 12, 254, new Color(150, 150, 180));

        drawPanel(g, 40, 285, 350, 260, PANEL_BORDER);
        drawSectionTitle(g, "CONTROLES", 60, 313);
        int y = 335;
        int x = drawKey(g, "LEFT", 60, y);
        x = drawKey(g, "RIGHT", x + 4, y);
        x = drawOrText(g, x, y);
        x = drawKey(g, "A", x, y);
        drawKey(g, "D", x + 4, y);
        drawControlText(g, "Mover", y);

        y += 48;
        x = drawKey(g, "ESPAÇO", 60, y);
        x = drawOrText(g, x, y);
        drawKey(g, "UP", x, y);
        drawControlText(g, "Lançar a bola", y);

        y += 48;
        drawKey(g, "ESC", 60, y);
        drawControlText(g, "Pausar", y);

        y += 48;
        drawKey(g, "ENTER", 60, y);
        drawControlText(g, "Confirmar", y);

        drawPanel(g, 410, 285, 350, 260, PANEL_BORDER);
        drawSectionTitle(g, "LEGENDA", 430, 313);
        drawLegendRow(g, Brick.Type.MULTIBALL, 1, "Cápsula +2 bolas", 330);
        drawLegendRow(g, Brick.Type.SPLIT, 1, "Cápsula x3 (cada bola vira 3)", 372);
        drawLegendRow(g, Brick.Type.BARRIER, 1, "Barreira: não quebra", 414);
        drawLegendRow(g, Brick.Type.NORMAL, 2, "Aguenta 2 golpes", 456);
        drawLegendRow(g, Brick.Type.NORMAL, 3, "Aguenta 3 golpes", 498);

        drawCentered(g, "ESC ou ENTER para voltar", 14, 578, Color.LIGHT_GRAY);
    }

    private void drawDifficultyCard(Graphics2D g, int index, int x, int y) {
        int w = 200, h = 100;
        boolean selected = index == model.getDifficulty();
        Color color = DIFFICULTY_COLORS[index];

        if (selected) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g.fillRoundRect(x - 6, y - 6, w + 12, h + 12, 22, 22);
        }
        g.setColor(selected ? new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4 + 20) : PANEL);
        g.fillRoundRect(x, y, w, h, 16, 16);
        g.setColor(selected ? color : PANEL_BORDER);
        g.setStroke(new BasicStroke(selected ? 3 : 1));
        g.drawRoundRect(x, y, w, h, 16, 16);
        g.setStroke(new BasicStroke(1));

        Color text = selected ? Color.WHITE : Color.GRAY;
        drawTextInBox(g, GameModel.DIFFICULTY_NAMES[index], 22, x + w / 2, y + 6, 30, text);

        for (int b = 0; b < 3; b++) {
            int bx = x + w / 2 - 33 + b * 24;
            g.setColor(b <= index ? (selected ? color : color.darker().darker()) : new Color(40, 40, 60));
            g.fillRoundRect(bx, y + 62 - b * 6, 18, 12 + b * 6, 4, 4);
        }
        drawTextInBox(g, DIFFICULTY_DESCRIPTIONS[index], 12, x + w / 2, y + 76, 20, text);
    }

    private int drawKey(Graphics2D g, String label, int x, int y) {
        boolean arrow = label.equals("LEFT") || label.equals("RIGHT") || label.equals("UP");
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        int w = arrow ? 30 : Math.max(30, g.getFontMetrics().stringWidth(label) + 16), h = 28;

        g.setColor(new Color(20, 20, 35));
        g.fillRoundRect(x, y + 3, w, h, 8, 8);
        g.setColor(new Color(65, 65, 100));
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(new Color(130, 130, 170));
        g.drawRoundRect(x, y, w, h, 8, 8);

        g.setColor(Color.WHITE);
        int cx = x + w / 2, cy = y + h / 2;
        switch (label) {
            case "LEFT" -> g.fillPolygon(new int[] {cx + 5, cx - 6, cx + 5}, new int[] {cy - 7, cy, cy + 7}, 3);
            case "RIGHT" -> g.fillPolygon(new int[] {cx - 5, cx + 6, cx - 5}, new int[] {cy - 7, cy, cy + 7}, 3);
            case "UP" -> g.fillPolygon(new int[] {cx - 7, cx, cx + 7}, new int[] {cy + 5, cy - 6, cy + 5}, 3);
            default -> drawTextInBox(g, label, 13, cx, y, h, Color.WHITE);
        }
        return x + w;
    }

    private int drawOrText(Graphics2D g, int x, int y) {
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.setColor(Color.GRAY);
        g.drawString("ou", x + 8, y + 19);
        return x + 32;
    }

    private void drawControlText(Graphics2D g, String text, int y) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.setColor(new Color(210, 210, 230));
        g.drawString(text, 255, y + 19);
    }

    private void drawLegendRow(Graphics2D g, Brick.Type type, int hits, String text, int y) {
        drawBrickShape(g, type, hits, 0, 430, y, 56, 22);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.setColor(new Color(210, 210, 230));
        g.drawString(text, 500, y + 16);
    }

    private void drawSectionTitle(Graphics2D g, String text, int x, int y) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.setColor(ORANGE);
        g.drawString(text, x, y);
    }

    private void drawGame(Graphics2D g) {
        for (Brick brick : model.getBricks()) drawBrick(g, brick);
        Paddle p = model.getPaddle();
        drawPaddleShape(g, p.getX(), p.getY(), p.getWidth(), p.getHeight());
        for (Capsule capsule : model.getCapsules()) drawCapsule(g, capsule);
        for (Ball ball : model.getBalls()) {
            g.setColor(Color.WHITE);
            g.fillOval(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
        }
        drawHud(g);

        int score = model.getScore(), level = model.getLevel();
        switch (model.getState()) {
            case PLAYING -> {
                if (model.isBallOnPaddle()) drawCentered(g, "ESPAÇO para lançar", 18, HEIGHT / 2 + 80, Color.LIGHT_GRAY);
            }
            case PAUSED -> drawOverlay(g, "PAUSADO", "ESC continua  |  Q volta ao menu");
            case LEVEL_CLEAR -> drawOverlay(g, "FASE " + level + " COMPLETA!", "ENTER fase " + (level + 1) + "  |  ESC volta ao menu");
            case GAME_OVER -> drawOverlay(g, "GAME OVER", "Pontos: " + score + "  |  ENTER volta ao menu");
            case VICTORY -> drawOverlay(g, "VOCÊ VENCEU!", "Pontos: " + score + "  |  ENTER volta ao menu");
            default -> { }
        }
    }

    private void drawPaddleShape(Graphics2D g, int x, int y, int w, int h) {
        g.setPaint(new GradientPaint(0, y, new Color(230, 230, 240), 0, y + h, new Color(140, 140, 160)));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(new Color(220, 40, 40));
        g.fillRect(x + 6, y + 3, 12, h - 6);
        g.fillRect(x + w - 18, y + 3, 12, h - 6);
    }

    private void drawCapsule(Graphics2D g, Capsule c) {
        boolean split = c.getPower() == Capsule.Power.SPLIT;
        g.setColor(split ? CYAN : PINK);
        g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 16, 16);
        g.setColor(Color.WHITE);
        g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 16, 16);
        drawTextInBox(g, split ? "x3" : "+2", 13, c.getX() + c.getWidth() / 2, c.getY(), c.getHeight(), Color.WHITE);
    }

    private void drawBrick(Graphics2D g, Brick brick) {
        drawBrickShape(g, brick.getType(), brick.getHits(), brick.getRow(), brick.getX(), brick.getY(), Brick.WIDTH, Brick.HEIGHT);
    }

    private void drawBrickShape(Graphics2D g, Brick.Type type, int hits, int row, int x, int y, int w, int h) {
        Color c = switch (type) {
            case BARRIER -> STEEL;
            case MULTIBALL -> PINK;
            case SPLIT -> CYAN;
            default -> hits == 3 ? GOLD : hits == 2 ? SILVER : ROW_COLORS[row % ROW_COLORS.length];
        };
        g.setColor(c);
        g.fillRect(x, y, w, h);
        g.setColor(c.brighter());
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x, y, x, y + h - 1);
        g.setColor(c.darker().darker());
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);

        g.setColor(type == Brick.Type.BARRIER ? Color.LIGHT_GRAY : Color.WHITE);
        if (type == Brick.Type.BARRIER) {
            for (int rx : new int[] {5, w - 9}) {
                g.fillOval(x + rx, y + 3, 4, 4);
                g.fillOval(x + rx, y + h - 7, 4, 4);
            }
        } else if (type == Brick.Type.MULTIBALL) {
            int d = h / 3;
            for (int i = -1; i <= 1; i++) g.fillOval(x + w / 2 - d / 2 + i * (d + 5), y + h / 2 - d / 2, d, d);
        } else if (type == Brick.Type.SPLIT) {
            int cx = x + w / 2, cy = y + h - 6, arm = h / 2;
            g.fillOval(cx - 4, cy - 4, 8, 8);
            g.drawLine(cx, cy, cx - arm, cy - arm);
            g.drawLine(cx, cy, cx + arm, cy - arm);
            g.drawLine(cx, cy, cx, cy - arm);
        }
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(20, 20, 50, 230));
        g.fillRect(0, 0, WIDTH, GameModel.HUD_HEIGHT);
        g.setColor(ORANGE);
        g.drawLine(0, GameModel.HUD_HEIGHT - 1, WIDTH, GameModel.HUD_HEIGHT - 1);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("PONTOS: " + model.getScore(), 15, 26);
        g.drawString("FASE: " + Math.min(model.getLevel() + 1, Levels.count()), 230, 26);
        g.setColor(DIFFICULTY_COLORS[model.getDifficulty()]);
        g.drawString(model.getDifficultyName(), 380, 26);
        g.setColor(RED);
        g.drawString("♥".repeat(Math.max(0, model.getLives())), WIDTH - 90, 26);
        g.setColor(Color.WHITE);
        g.drawString("VIDAS:", WIDTH - 170, 26);
    }

    private void drawPanel(Graphics2D g, int x, int y, int w, int h, Color border) {
        g.setColor(new Color(PANEL.getRed(), PANEL.getGreen(), PANEL.getBlue(), 230));
        g.fillRoundRect(x, y, w, h, 20, 20);
        g.setColor(border);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 20, 20);
        g.setStroke(new BasicStroke(1));
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
        g.drawString(text, (WIDTH - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private void drawTextInBox(Graphics2D g, String text, int size, int cx, int top, int h, Color color) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, top + (h - fm.getHeight()) / 2 + fm.getAscent());
    }
}
