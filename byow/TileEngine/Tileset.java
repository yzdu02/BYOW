package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "you");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "wall");
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "floor");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "tree");

    // our version of the aesthetic of the world
    public static final TETile OURWALL = new TETile('+', new Color(225, 237, 240), new Color(114, 146, 163),
            "wall");
    public static final TETile OURFLOOR = new TETile('·', Color.black, new Color(227, 231,232),
            "floor");
    public static final TETile OURPLAYER = new TETile('❀', new Color(255, 213,128), new Color(255, 83, 73),
            "player");

    // below are tiles used in the light-up feature in the engine
    public static final TETile LITFLOOR0 = new TETile('·', Color.white, new Color(153, 204, 255),
            "light");
    public static final TETile LITFLOOR1 = new TETile('·', Color.white, new Color(0, 128, 255),
            "floor");
    public static final TETile LITFLOOR2 = new TETile('·', Color.white, new Color(0, 102, 204),
            "floor");
    public static final TETile LITFLOOR3 = new TETile('·', Color.white, new Color(0, 76, 153),
            "floor");
}


