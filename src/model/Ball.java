package model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// A bola: anda sozinha e quica em paredes, raquete e tijolos.
public class Ball extends GameObject {
    public static final int SIZE = 12;
    private static final double MAX_ANGLE = Math.toRadians(60);
    private final double speed;
    private double dx, dy;

    public Ball(double x, double y, double speed) {
        super(x, y, SIZE, SIZE);
        this.speed = speed;
    }

    // Dispara a bola num ângulo (0 = reto para cima, positivo = para a direita).
    public void launch(double angleDegrees) {
        double angle = Math.toRadians(angleDegrees);
        dx = speed * Math.sin(angle);
        dy = -speed * Math.cos(angle);
    }

    public void move() {
        x += dx;
        y += dy;
    }

    // Deixa a bola parada em cima da raquete (antes do lançamento).
    public void stickTo(Paddle paddle) {
        x = paddle.centerX() - width / 2;
        y = paddle.y - height;
    }

    // Quica nas paredes laterais e no teto. Retorna true se caiu pelo fundo.
    public boolean bounceOffWalls(int top, int screenWidth, int screenHeight) {
        if (x < 0) { x = 0; dx = Math.abs(dx); }
        if (x + width > screenWidth) { x = screenWidth - width; dx = -Math.abs(dx); }
        if (y < top) { y = top; dy = Math.abs(dy); }
        return y > screenHeight;
    }

    // O ângulo de saída depende de onde a bola bate na raquete (como no original).
    public void bounceOffPaddle(Paddle paddle) {
        double rel = (centerX() - paddle.centerX()) / (paddle.width / 2);
        double angle = Math.max(-1, Math.min(1, rel)) * MAX_ANGLE;
        dx = speed * Math.sin(angle);
        dy = -speed * Math.cos(angle);
        y = paddle.y - height;
    }

    // Decide se bateu pela lateral ou por cima/baixo pelo tamanho da sobreposição.
    public void bounceOffBrick(Brick brick) {
        Rectangle2D overlap = getBounds().createIntersection(brick.getBounds());
        if (overlap.getWidth() < overlap.getHeight()) {
            dx = -dx;
            x += Math.signum(dx) * overlap.getWidth();
        } else {
            dy = -dy;
            y += Math.signum(dy) * overlap.getHeight();
        }
    }

    // Cria duas cópias na mesma posição, abrindo em leque.
    // Mantém o sentido (subindo ou descendo) da bola original.
    public List<Ball> split() {
        List<Ball> copies = new ArrayList<>();
        for (int angle : new int[] {-40, 40}) {
            Ball copy = new Ball(x, y, speed);
            copy.launch(angle);
            if (isFalling()) copy.dy = -copy.dy;
            copies.add(copy);
        }
        return copies;
    }

    public boolean isFalling() {
        return dy > 0;
    }
}
