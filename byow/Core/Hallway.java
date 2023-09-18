package byow.Core;

/**
 * A hallway is a 3 tile wide object with no ends
 * Looks like this: [! = wall tiles, # = floor]
 * !!!!!
 * #####
 * !!!!!
 *
 *
 * NOTE: tbh this class may not be necessary? What it seeks to solve can probably just be done
 * in either Room or WorldGenerator
 * */

public class Hallway {

    private int length;
    /** Although a hallway is technically 3 tiles wide, width is in terms of floor tiles*/
    private final static int WIDTH = 1;
    private Position startPos;
    private Position endPos;

    /** Connects two rooms together */
    public Hallway(Position src, Position dst) {
        this.startPos = src;
        this.endPos = dst;
    }

    /** creates a 1x1 room */
    public Room anchorRoom() {
        return null;
    }

    public void drawVertical(){

    }

}