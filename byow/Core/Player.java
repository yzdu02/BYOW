package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {

    private Position currentPos;
    private TETile logo;
    private TETile previousStep;
    private static final int SPEED = 1;

    public Player () {
        this.logo = Tileset.OURPLAYER;
        this.previousStep = Tileset.OURFLOOR;
    }

    // place the player in a new position -- used in engine initialization and loading back
    public void place(Position currentPos) {
        this.currentPos = currentPos;
    }

    // get the current position of the player -- used in saving
    public Position getCurrentPos() {
        return currentPos;
    }

    // makes the player to remember where it is before it moves -- used in movement
    public void setStep(TETile currentStep) {
        previousStep = currentStep;
    }

    // get where the player has left of -- used in movement
    public TETile getPreviousStep() {
        return previousStep;
    }

    // make sure that only WASD makes the move
    public Position checkMove(char way) {
        switch (way) {
            case 'w':
                return currentPos.shift(0, SPEED);
            case 'a':
                return currentPos.shift(-SPEED, 0);
            case 's':
                return currentPos.shift(0, -SPEED);
            case 'd':
                return currentPos.shift(SPEED, 0);
        }
        return null;
    }

    // move the player to a new position
    public void move (char way) {
        switch (way) {
            case 'w' -> currentPos = currentPos.shift(0, SPEED);
            case 'a' -> currentPos = currentPos.shift(-SPEED, 0);
            case 's' -> currentPos = currentPos.shift(0, -SPEED);
            case 'd' -> currentPos = currentPos.shift(SPEED, 0);
        }
    }

    // draw the player in the new position -- used in movement
    public void drawMyself (TETile[][] tiles) {
        tiles[currentPos.x][currentPos.y] = logo;
    }
}
