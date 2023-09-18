package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

/**
 * Creates a room.
 * a 2x2 room would look like this: [! = walls, # = floor]
 * !!!!
 * !##!
 * !##!
 * !!!!
 * <p>
 * I was thinking that we could refer to a room in terms of its floorspace?
 * It just felt more natural to me as the square footage of a room in a house
 * is also in terms of its floor space.
 */

public class Room {

    private final int width;
    private final int height;
    private final Position startPos;
    private final Position topLeftCorner;
    private final Position topRightCorner;
    private final Position bottomLeftCorner;
    private final Position bottomRightCorner;

    /**
     * the four corners of the wall surrounding the room
     */
    private final ArrayList<Position> wallCornerPoints = new ArrayList<>();

// floor corner points
//    /**
//     * the corners of the floor surrounded by the wall
//     * If event width or height = 1, there are only two floor corner points
//     */
//    private final ArrayList<Position> floorCornerPoints = new ArrayList<>();

    /**
     * the position that will help WorldGenerator connect two rooms
     */
    private final Position anchorPoint;

    public Room(int width, int height, Position start) {
        this.width = width;
        this.height = height;
        this.startPos = start;
        // if the room is a single tile, then anchor is the startPos, if not, it is the middle point of the room
        if (width == 1 && height == 1){
            this.anchorPoint = this.startPos;
        }
        else{
            this.anchorPoint = startPos.shift((width - 1) / 2, (height - 1) / 2); // the center of the room
        }

        topLeftCorner = startPos.shift(-1, height);
        wallCornerPoints.add(topLeftCorner);

        topRightCorner = topLeftCorner.shift(width + 1, 0);
        wallCornerPoints.add(topRightCorner);

        bottomLeftCorner = startPos.shift(-1, -1);
        wallCornerPoints.add(bottomLeftCorner);

        bottomRightCorner = bottomLeftCorner.shift(width + 1, 0);
        wallCornerPoints.add(bottomRightCorner);
    }


    /**
     * draws a row in the room from left to right with certain length only on the tiles that have not been covered.
     */
    public void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            if (tiles[p.x + dx][p.y] == Tileset.NOTHING) {
                tiles[p.x + dx][p.y] = tile;
            }
        }
    }
    /**
     * draws a column from bottom to top with certain length only on the tiles that have not been covered.
     */
    public void drawColumn(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dy = 0; dy < length; dy++) {
            if (tiles[p.x][p.y + dy] == Tileset.NOTHING) {
                tiles[p.x][p.y + dy] = tile;
            }
        }

    }


    /**
     * draws walls around the corner floor tiles of the room.
     * <p>
     */
    public void drawWalls(TETile[][] tiles) {
        drawRow(tiles, topLeftCorner, Tileset.OURWALL, width + 2);
        drawRow(tiles, bottomLeftCorner, Tileset.OURWALL, width + 2);
        drawColumn(tiles, bottomLeftCorner, Tileset.OURWALL, height + 2);
        drawColumn(tiles, bottomRightCorner, Tileset.OURWALL, height + 2);
    }
    /**
     * draws the floors inside the room
     */
    public void drawFloors(TETile[][] tiles) {
        int startX = startPos.x;
        int startY = startPos.y;
        for (int i = startX; i < startX + width; i++) {
            for (int j = startY; j < startY + height; j++) {
                if (tiles[i][j] != Tileset.OURPLAYER){
                    tiles[i][j] = Tileset.OURFLOOR;
                }
            }
        }
    }
    /**
     * draws the full Room, will use drawFloors and drawWalls to do this
     */
    public void drawRoom(TETile[][] tiles) {
        drawFloors(tiles);
        drawWalls(tiles);
    }

    /**
     * check whether the certain point is within the room
     * used in drawLight
     * @return true -- in the room; false -- not in the room
     */
    public boolean withinTheRoom(Position pos){
        return (pos.x > getBottomLeftCorner().x && pos.x < getBottomRightCorner().x) &&
                (pos.y > getBottomLeftCorner().y && pos.y < getTopRightCorner().y);
    }


    /**
     * draws the light surrounding the anchor points with 3 rounds of lighting up
     * used in the engine
     */
    public void drawLight (TETile[][] tiles) {

        // light up the center && round 0
        Position center = getAnchorPoint();
        if (tiles[center.x][center.y] != Tileset.OURWALL && tiles[center.x][center.y] != Tileset.NOTHING){
            tiles[center.x][center.y] = Tileset.LITFLOOR0;
        }

        // light up round 1
        Position bottomLeftPoint1 = center.shift(-1, -1);
        for (int i = bottomLeftPoint1.x; i < bottomLeftPoint1.x+3; i++){
            for (int j = bottomLeftPoint1.y; j < bottomLeftPoint1.y+3; j++){
                Position pos = new Position(i, j);
                if (withinTheRoom(pos) && tiles[i][j] != Tileset.LITFLOOR0){
                    tiles[i][j] = Tileset.LITFLOOR1;
                }
            }
        }

        // light up round 2
        Position bottomLeftPoint2 = bottomLeftPoint1.shift(-1, -1);
        for (int i = bottomLeftPoint2.x; i < bottomLeftPoint2.x+5; i++){
            for (int j = bottomLeftPoint2.y; j < bottomLeftPoint2.y+5; j++){
                Position pos = new Position(i, j);
                if (withinTheRoom(pos) && tiles[i][j] != Tileset.LITFLOOR0 && tiles[i][j] != Tileset.LITFLOOR1){
                    tiles[i][j] = Tileset.LITFLOOR2;
                }
            }
        }

        // light up round 3
        Position bottomLeftPoint3 = bottomLeftPoint2.shift(-1, -1);
        for (int i = bottomLeftPoint3.x; i < bottomLeftPoint3.x+7; i++){
            for (int j = bottomLeftPoint3.y; j < bottomLeftPoint3.y+7; j++){
                Position pos = new Position(i, j);
                if (withinTheRoom(pos) && tiles[i][j] != Tileset.LITFLOOR0 && tiles[i][j] != Tileset.LITFLOOR1 && tiles[i][j] != Tileset.LITFLOOR2){
                    tiles[i][j] = Tileset.LITFLOOR3;
                }
            }
        }
    }


    /**
     * below methods are aimed to get the instance variables that would be useful in WorldGenerator
     */

    /**
     * returns the anchorPoint of this room
     * To be used by WorldGenerator in connectRooms method.
     */
    public Position getAnchorPoint() {
        return anchorPoint;
    }

    public ArrayList<Position> wallCornerPoints() {
        return wallCornerPoints;
    }

    public Position getTopLeftCorner() {
        return topLeftCorner;
    }

    public Position getTopRightCorner() {
        return topRightCorner;
    }

    public Position getBottomLeftCorner() {
        return bottomLeftCorner;
    }

    public Position getBottomRightCorner() {
        return bottomRightCorner;
    }

    public Position getStartPos() {
        return startPos;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * checks if this room will end up out of bounds of the tiles map
     */
    public boolean isOutOfBound(TETile[][] tiles) {
        return (startPos.x + width >= tiles.length - 1) || (startPos.y + height >= (tiles[0]).length - 1);
    }

    /**
     * Checks if the current room is overlapped with another room
     * NOTICE that two rooms will need to be checked with each other (check twice) to ensure that they are not overlapped
     */
    public boolean isOverlapped(Room otherRoom) {
        int otherLeftX = otherRoom.getTopLeftCorner().x;
        int otherRightX = otherRoom.getTopRightCorner().x;
        int otherTopY = otherRoom.getTopLeftCorner().y;
        int otherBottomY = otherRoom.getBottomLeftCorner().y;
        ArrayList<Position> checkingPoints = new ArrayList<>(this.wallCornerPoints);
        checkingPoints.add(anchorPoint); // adding the middle point of the room

        for (Position currentCornerPoint : checkingPoints) {
            //checks if the currentCornerPoint of this Room is in between the x values of
            //the room in the Set and also in between the y values
            //we are checking the four corners as well as the middle point of the room
            if (((currentCornerPoint.x >= otherLeftX) && (currentCornerPoint.x <= otherRightX)) && ((currentCornerPoint.y <= otherTopY) && (currentCornerPoint.y >= otherBottomY))) {
                return true;
            }
        }
        return false;
    }

}
