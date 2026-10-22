package model;

import java.awt.geom.Rectangle2D;

// Base de tudo que existe no jogo: posição, tamanho e caixa de colisão.
public abstract class GameObject {
    protected double x, y, width, height;

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public boolean collidesWith(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }

    public double centerX() {
        return x + width / 2;
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return (int) width; }
    public int getHeight() { return (int) height; }
}
