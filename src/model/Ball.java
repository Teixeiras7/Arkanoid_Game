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

    public void move() {
        x += dx;
        y += dy;
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
