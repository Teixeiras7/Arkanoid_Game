package model;

// A raquete controlada pelo jogador.
public class Paddle extends GameObject {
    private static final double SPEED = 8;

    public Paddle(double x, double y) {
        super(x, y, 100, 14);
    }

    public void move(boolean left, boolean right, int screenWidth) {
        if (left) x -= SPEED;
        if (right) x += SPEED;
        x = Math.max(0, Math.min(x, screenWidth - width));
    }
}
