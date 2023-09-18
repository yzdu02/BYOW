package byow.Core;

public class Position {
   public int x;
   public int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** takes your position and gets a new position after change. */
    public Position shift(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }
}