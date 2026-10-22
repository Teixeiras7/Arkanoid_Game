package model;

import java.util.ArrayList;
import java.util.List;

// Mapas das fases.
//   '1'..'3' = tijolo que aguenta esse número de golpes
//   '*'      = tijolo multibola (solta a cápsula +2)
//   '%'      = tijolo divisor (solta a cápsula x3: cada bola vira 3)
//   '#'      = barreira indestrutível
//   '.'      = vazio
public class Levels {
    private static final String[][] MAPS = {
        {
            "1111111111",
            "11*1111%11",
            "1111111111",
            "1111111111",
        },
        {
            "2222222222",
            "11*1111%11",
            "1111111111",
            "2222222222",
            "..........",
            "##.####.##", // só duas passagens: precisa mirar
        },
        {
            "3333333333",
            "#2*2222%2#",
            "#11111111#",
            "#11111111#",
            "###.##.###", // duas passagens estreitas...
            "..........",
            "..##..##..", // ...e uma segunda camada desencontrada: só entra de lado
        },
    };

    private static final int GAP = 4, TOP = 80;

    public static int count() {
        return MAPS.length;
    }

    public static List<Brick> build(int level, int screenWidth) {
        List<Brick> bricks = new ArrayList<>();
        String[] map = MAPS[level];
        int cols = map[0].length();
        int left = (screenWidth - cols * (Brick.WIDTH + GAP) + GAP) / 2;
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < cols; col++) {
                char c = map[row].charAt(col);
                if (c == '.') continue;
                Brick.Type type = switch (c) {
                    case '#' -> Brick.Type.BARRIER;
                    case '*' -> Brick.Type.MULTIBALL;
                    case '%' -> Brick.Type.SPLIT;
                    default -> Brick.Type.NORMAL;
                };
                int hits = type == Brick.Type.NORMAL ? c - '0' : 1;
                bricks.add(new Brick(left + col * (Brick.WIDTH + GAP), TOP + row * (Brick.HEIGHT + GAP), hits, row, type));
            }
        }
        return bricks;
    }
}
