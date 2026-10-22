package model;

// Tijolo: pode precisar de mais de um golpe, ser uma barreira indestrutível
// ou soltar uma cápsula de poder quando quebrado.
public class Brick extends GameObject {
    public enum Type { NORMAL, BARRIER, MULTIBALL, SPLIT }

    public static final int WIDTH = 72, HEIGHT = 24;

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

    // Leva um golpe e devolve os pontos ganhos. Barreiras não sofrem dano.
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
