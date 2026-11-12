package model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Ball extends GameObject {
    public static final int SIZE = 12;
    private static final double MAX_ANGLE = Math.toRadians(60);
    private final double speed;
    private double dx, dy;

    public Ball(double x, double y, double speed) {
        super(x, y, SIZE, SIZE);
        this.speed = speed;
    }

    public void launch(double angleDegrees) {
        double angle = Math.toRadians(angleDegrees);
        dx = speed * Math.sin(angle);
        dy = -speed * Math.cos(angle);
    }

    private static final double MAX_STEP = 4;

    // Quantidade de passos pequenos em que o movimento de um quadro é dividido (evita atravessar tijolos)
    public int steps() {
        return (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / MAX_STEP));
    }

    public void moveX(double fraction) {
        x += dx * fraction;
    }

    public void moveY(double fraction) {
        y += dy * fraction;
    }

    public void stickTo(Paddle paddle) {
        x = paddle.centerX() - width / 2;
        y = paddle.y - height;
    }

    public boolean bounceOffWalls(int top, int screenWidth, int screenHeight) {
        if (x < 0) { x = 0; dx = Math.abs(dx); }
        if (x + width > screenWidth) { x = screenWidth - width; dx = -Math.abs(dx); }
        if (y < top) { y = top; dy = Math.abs(dy); }
        return y > screenHeight;
    }

    public void bounceOffPaddle(Paddle paddle) {
        double rel = (centerX() - paddle.centerX()) / (paddle.width / 2);
        double angle = Math.max(-1, Math.min(1, rel)) * MAX_ANGLE;
        dx = speed * Math.sin(angle);
        dy = -speed * Math.cos(angle);
        y = paddle.y - height;
    }

    // Área da sobreposição entre a bolinha e um tijolo (0 se não houver colisão)
    public double overlapArea(Brick brick) {
        Rectangle2D overlap = getBounds().createIntersection(brick.getBounds());
        return overlap.getWidth() > 0 && overlap.getHeight() > 0 ? overlap.getWidth() * overlap.getHeight() : 0;
    }

    // Colisão horizontal: empurra a bolinha para fora do lado de onde ela veio
    public void bounceOffBrickX(Brick brick) {
        Rectangle2D b = brick.getBounds();
        x = dx > 0 ? b.getMinX() - width : b.getMaxX();
        dx = -dx;
    }

    // Colisão vertical: empurra a bolinha para fora do lado de onde ela veio
    public void bounceOffBrickY(Brick brick) {
        Rectangle2D b = brick.getBounds();
        y = dy > 0 ? b.getMinY() - height : b.getMaxY();
        dy = -dy;
    }

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
