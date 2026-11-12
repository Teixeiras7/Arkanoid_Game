package model;

import java.util.ArrayList;
import java.util.List;

// 1-3 = golpes, * = cápsula +2, % = cápsula x3, # = barreira, . = vazio
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
            "##.####.##",
        },
        {
            "3333333333",
            "#2*2222%2#",
            "#11111111#",
            "#11111111#",
            "###.##.###",
            "..........",
            "..##..##..",
        },
    };

    private static final int GAP = Brick.GAP, TOP = 80;

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
