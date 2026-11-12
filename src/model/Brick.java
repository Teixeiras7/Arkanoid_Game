package model;

import java.awt.geom.Rectangle2D;

public class Brick extends GameObject {
    public enum Type { NORMAL, BARRIER, MULTIBALL, SPLIT }

    public static final int WIDTH = 72, HEIGHT = 24;
    public static final int GAP = 4;

    private final Type type;
    private final int row;
    private final int maxHits;
    private int hits;

    public Brick(double x, double y, int maxHits, int row, Type type) {
        super(x, y, WIDTH, HEIGHT);
        this.maxHits = maxHits;
        this.hits = maxHits;
        this.row = row;
        this.type = type;
    }

    // A área de colisão cobre metade do espaço visual entre os tijolos, assim não existem frestas
    @Override
    public Rectangle2D.Double getBounds() {
        double pad = GAP / 2.0;
        return new Rectangle2D.Double(x - pad, y - pad, width + GAP, height + GAP);
    }

    public int hit() {
        if (!isBreakable()) return 0;
        hits--;
        return isDestroyed() ? 50 * maxHits : 10;
    }

    public boolean isDestroyed() {
        return hits <= 0;
    }

    public boolean isBreakable() {
        return type != Type.BARRIER;
    }

    public Type getType() { return type; }
    public int getRow() { return row; }
    public int getHits() { return hits; }
}
