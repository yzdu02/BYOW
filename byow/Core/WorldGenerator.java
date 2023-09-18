package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

/** [Class def to be expanded upon lol]
 * Collects info needed to genWorld w/ seed
 *
 *
 * */
public class WorldGenerator {

    //I changed the world to be 60x40 bc it was kind of big
    private static final int WIDTH = 60;
    private static final int HEIGHT = 40;
    private static final int MIN_ROOMS = 10;
    private static final int MAX_ROOMS = 30;
    //the same seed will always return the same "randomly" generated world
    private final long seed;
    private final TETile[][] tiles;
    private final Random rand;

    /** the set of all the rooms in the world.
     * and a list to sort them for use in connection
     */
    private Set<Room> roomsInWorld;
    private List<Room> roomList;

    //World is supposed to be generated w/ just a seed
    public WorldGenerator(String StarterSeed) {
        this.seed = Long.parseLong(StarterSeed);
        this.tiles = new TETile[WIDTH][HEIGHT];
        rand = new Random(seed);
        roomsInWorld = new HashSet<>();
        roomList = new ArrayList<>();
        //now does stuff with methods below
    }

    /** Determines how many rooms should be generated in the world */
    private int howManyRooms() {
        int numRoomsInWorld = rand.nextInt(MAX_ROOMS);
        if (numRoomsInWorld >= MIN_ROOMS) {
            return numRoomsInWorld;
        }
        else {
            while (numRoomsInWorld < MIN_ROOMS){
                numRoomsInWorld = rand.nextInt(MAX_ROOMS);
            }
        }
        return numRoomsInWorld;
    }

    /** Using Random, finds the next start point for the next room
     * uses randomXCoordinate and randomYcoordinate, but avoiding the cases where one of the axises is 0
     * */
    private Position findNextStart() {
        Position nextStart = new Position(rand.nextInt(WIDTH - 1), rand.nextInt(HEIGHT - 1));
        if ((nextStart.x <= 0) || (nextStart.y <= 0)){
            nextStart = findNextStart();
        }
        return nextStart;
    }


    /** Instantiates a room. */
    private Room createRoom() {
        Position start = findNextStart();
        //randomly generated height or width, only odd numbers could make it (for the purpose of connecting rooms)
        int roomWidth = rand.nextInt(2,8);
        while (roomWidth % 2 == 0){
            roomWidth = rand.nextInt(2,8);
        }
        int roomHeight = rand.nextInt(2,8);
        while (roomHeight % 2 == 0){
            roomHeight = rand.nextInt(2,8);
        }
        Room currentRoom = new Room(roomWidth, roomHeight, start);

        //checks if room has valid positioning
        if(currentRoom.isOutOfBound(tiles)) {
            return createRoom(); //calls itself again and hopefully the next room is valid
        }
        for(Room room : roomsInWorld){
            if (currentRoom.isOverlapped(room) || room.isOverlapped(currentRoom)){
                return createRoom(); //calls itself again and hopefully the next room is valid
            }
        }
        return currentRoom;
    }

    /** Instantiates a room with height = 1 and width = 1, to turn the hallway. */
    private Room createTurningRoom() {
        Position start = findNextStart();
        //randomly generated height or width
        Room currentRoom = new Room(1, 1, start);

        //checks if room has valid positioning
        if(currentRoom.isOutOfBound(tiles)) {
            return createRoom(); //calls itself again and hopefully the next room is valid
        }
        for(Room room : roomsInWorld){
            if (currentRoom.isOverlapped(room) || room.isOverlapped(currentRoom)){
                return createRoom(); //calls itself again and hopefully the next room is valid
            }
        }
        return currentRoom;
    }


    /** Instantiates set of rooms and determines their locations */
    public void generateRooms() {
        int numRooms = howManyRooms();

        for (int i = 0; i < numRooms; i++) {
            int roomDeterminator = rand.nextInt(2);
            Room nextRoom = createRoom();
            if (roomDeterminator == 0) {
              nextRoom = createTurningRoom();
            }
            roomsInWorld.add(nextRoom);
            //then draws rooms into world
            for (Room room : roomsInWorld) {
                room.drawRoom(tiles);
            }
        }
    }


    //* draw vertical hallway from bottom to top -- it is a ROOM!
    public void drawVerticalHallway (int length, Position startPos) {
        Room veHallway = new Room(1, length, startPos);
        veHallway.drawRoom(tiles);
    }

    //* draw horizontal hallway from left to right -- it is a ROOM!
    public void drawHorizontalHallway (int length, Position startPos) {
        Room hoHallway = new Room (length, 1, startPos);
        hoHallway.drawRoom(tiles);
    }


    /**
     * Chooses one of Points randomly in all the floor points in this room -- might not be useful
     */
    private Position chooseAnchorPoint(Room room) {
        int xRangeLimit = room.getStartPos().x + room.getWidth(); // the rightmost wall tile
        int yRangeLimit = room.getStartPos().y + room.getHeight(); // the uppermost wall tile
        return new Position(rand.nextInt(room.getStartPos().x, xRangeLimit), rand.nextInt(room.getStartPos().y, yRangeLimit));
    }


    // used in the sortRoom method to sort the rooms in order using their anchor point (middle point in the room)
    private static class positionComparator implements Comparator<Room> {
        public int compare(Room r1, Room r2) {
            if (r1.getAnchorPoint().x < r2.getAnchorPoint().x) {
                return -1;
            }
            else if (r1.getAnchorPoint().x > r2.getAnchorPoint().x){
                return 1;
            }
            else {
                return Integer.compare(r1.getAnchorPoint().y, r2.getAnchorPoint().y);
            }
        }
    }
    /** sort all rooms in the roomList using the comparator we created*/
    private void sortRoom() {
        roomList.addAll(roomsInWorld);
        roomList.sort(new positionComparator());
    }

    // find the closest neighbour using the x, y position of the Anchor Point
    public Room findNeighbour (Room thisRoom, List<Room> candidates){
        double closestDistance = Double.MAX_VALUE;
        Room neighbour = null;
        for (Room otherRoom: candidates){
            // calculate out the distance
            double xDeviation = Math.pow(thisRoom.getAnchorPoint().x - otherRoom.getAnchorPoint().x, 2);
            double yDeviation = Math.pow(thisRoom.getAnchorPoint().y - otherRoom.getAnchorPoint().y, 2);
            double distance = Math.sqrt(xDeviation + yDeviation);
            if (distance < closestDistance){
                closestDistance = distance;
                neighbour = otherRoom;
            }
        }
        return neighbour;
    }

    /** connects the generated rooms by using hallways to connect their anchor Points.
     * first go horizontally, and then vertically
     * */
    private void connectRooms(Room r1, Room r2) {
        Position r1Anchor = r1.getAnchorPoint();
        Position r2Anchor = r2.getAnchorPoint();
        Position bridgePoint = null;

        if (r1Anchor.x < r2Anchor.x) { // r1.x < r2.x
            drawHorizontalHallway(r2Anchor.x - r1Anchor.x + 1, r1Anchor);
            bridgePoint = r1Anchor.shift(r2Anchor.x - r1Anchor.x,0);
            if (r1Anchor.y < r2Anchor.y) { // r1.y < r2.y
                drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, bridgePoint);
            }
            else if (r1Anchor.y > r2Anchor.y) { // r1.y > r2.y
                drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, r2Anchor);
            }
        }

        else if (r1Anchor.x > r2Anchor.x) { // r1.x > r2.x
            drawHorizontalHallway((r1Anchor.x - r2Anchor.x) + 1, r2Anchor);
            bridgePoint = r2Anchor.shift(r1Anchor.x - r2Anchor.x,0);
            if (r1Anchor.y < r2Anchor.y) { // r1.y < r2.y
                drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, r1Anchor);
            }
            else if (r1Anchor.y > r2Anchor.y) { // r1.y > r2.y
                drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, bridgePoint);
            }
        }

        else { // r1.x = r2.x
            if (r1Anchor.y < r2Anchor.y){// r1.y < r2.y
                bridgePoint = r1Anchor;
                drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, bridgePoint);
            }
            else if (r1Anchor.y > r2Anchor.y){// r1.y > r2.y
                bridgePoint = r2Anchor;
                drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, bridgePoint);
            }
        }

//        if (r1Anchor.y < r2Anchor.y) { // r1.y < r2.y
//            drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, bridgePoint);
//        }
//        else if (r1Anchor.y > r2Anchor.y) { // r1.y > r2.y
//            drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, r2Anchor);
//        }
    }

    // generate hallways between every room in the tiles using the sorted roomList and the connectRooms method
    public void generateHallways() {
        sortRoom();
//        for (int i = 0; i < roomList.size() -1; i++) {
//            connectRooms(roomList.get(i), roomList.get(i+1));
//        }
        LinkedList<Room> candidates = new LinkedList<>();
        candidates.addFirst(roomList.get(0));
        roomList.remove(0);
        while (!roomList.isEmpty()){
            Room r1 = candidates.getFirst();
            Room r2 = findNeighbour(r1, roomList);
            connectRooms(r1, r2);
            roomList.remove(r2);
            candidates.addFirst(r2);
        }
//        for (int i = 0; i < roomList.size() - 1; i++){
//            connectRooms(roomList.get(i), roomList.get(i+1));
//        }

//            Position r1Anchor = r1.getStartPos();
//            Position r2Anchor = r2.getanchorPoint();
//            //NOTE: r1 will never be on the right of r2 since they are sorted by x-position
//            // considering different column cases
//            if ((r2Anchor.x > r1Anchor.x) && (r2Anchor.y > r1Anchor.y)) { // r2 is on the right and upper of r1
//                drawHorizontalHallway(r2Anchor.x - r1Anchor.x + 1, r1Anchor);
//                Position midPoint = r1Anchor.shift(r2Anchor.x - r1Anchor.x,0);
//                drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, midPoint); // from the midpoint to r2Anchor
//            }
//            if ((r2Anchor.x > r1Anchor.x) && (r2Anchor.y < r1Anchor.y)) {  // r2 is on the right and lower of r1
//                drawHorizontalHallway(r2Anchor.x - r1Anchor.x + 1, r1Anchor);
//                Position midPoint = r1Anchor.shift(r2Anchor.x - r1Anchor.x,0);
//                drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, r2Anchor); // from r2Anchor to the midpoint
//            }
//            if ((r2Anchor.x > r1Anchor.x) && (r2Anchor.y == r1Anchor.y)) {  // r2 is on the right and same level of r1
//                drawHorizontalHallway(r2Anchor.x - r1Anchor.x + 1, r1Anchor);
//            }
//
//            // considering same column cases
//            if ((r2Anchor.x == r1Anchor.x) && (r2Anchor.y > r1Anchor.y)) { // r2 is on the same x position and upper of r1
//                drawVerticalHallway(r2Anchor.y - r1Anchor.y + 1, r1Anchor);
//            }
//            if ((r2Anchor.x == r1Anchor.x) &&(r2Anchor.y < r1Anchor.y)) {  // r2 is on the same x position and lower of r1
//                drawVerticalHallway(r1Anchor.y - r2Anchor.y + 1, r2Anchor);
//            }
//        }
    }


    /**
     * below are methods that are used to create the world
     */

    /**
     * @return the tiles
     */
    public TETile[][] getTiles() {
        return tiles;
    }

    /**
     * @return all the rooms in the world that are "real" rooms
     * i.e. the rooms with width and height greater than 1
     * for use in BIG Ambition
     */
    public Set<Room> getRealRooms() {
        Set<Room> realRooms = new HashSet<>();
        for (Room room: roomsInWorld){
            if (room.getHeight() > 1 && room.getWidth() > 1){
                realRooms.add(room);
            }
        }
        return realRooms;
    }

    /**
     * @return all the anchor points in the world
     * for use to place the player in engine
     */
    public Position getRandomAnchorPoint() {
        ArrayList<Position> anchors = new ArrayList<>();
        for (Room room: roomsInWorld) {
            anchors.add(room.getAnchorPoint());
        }
        int selector = rand.nextInt(anchors.size());
        return anchors.get(selector);
    }


    /** draw a single room into the tiles, for testing*/
    public void drawSingleRoom (int width, int height, Position startPos){
        Room newRoom = new Room(width, height, startPos);
        newRoom.drawRoom(tiles);
    }

    /**
     * Fills the given 2D array of tiles with NOTHING.
     */
    public void fillBoardWithNothing() {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }
}
