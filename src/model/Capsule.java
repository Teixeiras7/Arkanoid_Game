package model;

public class Capsule extends GameObject {
    public enum Power { EXTRA_BALLS, SPLIT }

    private static final double FALL_SPEED = 3;
    private final Power power;

    public Capsule(double centerX, double y, Power power) {
        super(centerX - 20, y, 40, 16);
        this.power = power;
    }

    public void move() {
        y += FALL_SPEED;
    }

    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    public Power getPower() {
        return power;
    }
}
