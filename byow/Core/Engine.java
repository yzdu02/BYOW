/**
 * Unimplemented Features before Thanksgiving:
 * [CHECK] 1. Set Player and Move
 * [CHECK] 2. Save Game
 * [CHECK] 3. Read Game
 * [CHECK] 4. Load Game
 * [CHECK] 5. BIG Ambition: making lights to turn on
 * [CHECK] 6. SMALL Ambition: Real Date & Time in HUD
 * [CHECK] 7. Load Game while there is no game
 * [CHECK 12/5 final fix] 8. font problem?
 * [CHECK 12/5 final fix] 9. UI aesthetic staff
 * [CHECK 12/5 final fix] 10. rearrange, comment, and cleanup -- ready to submit
 */

package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;

import java.awt.*;
import java.util.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    // the size of the tiles world
    public static final int WIDTH = 60;
    public static final int HEIGHT = 40;
    // the size of the canvas
    private static final int CANVASWIDTH = 960;
    private static final int CANVASHEIGHT = 640;
    // the only player -- that we could set its place and move, etc.
    private static final Player player = new Player();
    // the file used to save the world using in/out
    public static final String filename = "savefile.txt";
    // the string used to recreate/reload the world
    private String reloadSeedFactor;
    // the string used to mimic the movement -- no longer needed cuz we have the player's position saved
    // private String reloadActionFactor;
    // all the realRooms in the world, used to set player & turn on/off the lights
    private Set<Room> realRoomSet;


    // Constructor: every new engine will record a unique factor for the sake of saving/reloading the world (aka the "pure" seed)
    public Engine() {
        this.reloadSeedFactor = "";
        // this.reloadActionFactor = "";
    }


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        // This might solve the highlight problem?
        Font font = new Font("Monaco", Font.PLAIN, 30);
        StdDraw.setFont(font);
        char action = drawFirstScreenAndCatchAction();
        switch (action){
            case 'n' -> newWithKeyboard();
            case 'l' -> loadWithKeyboard();
            case 'q' -> quit();
        }
    }

    // BELOW ARE KEYBOARD METHODS

    /**
     * KEYBOARD METHOD
     * starts a new game to interact with keyboard
     * no need to return -- entering the catchAction Phase
     */
    public void newWithKeyboard() {
        String seed = drawSeedScreenAndReadSeed();
        reloadSeedFactor = seed;
        ter.initialize(WIDTH, HEIGHT + 2);
        WorldGenerator generatedWorld = new WorldGenerator(seed);
        generatedWorld.fillBoardWithNothing();
        generatedWorld.generateRooms();
        realRoomSet = generatedWorld.getRealRooms();
        generatedWorld.generateHallways();
        Position startPos = generatedWorld.getRandomAnchorPoint();
        player.place(startPos);
        TETile[][] worldFrame = generatedWorld.getTiles();
        player.drawMyself(worldFrame);
        Font font = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(font);
        ter.renderFrame(worldFrame);
        catchActionWithKeyboard(worldFrame);
    }

    /**
     * KEYBOARD METHOD
     * reload an existing world to interact with keyboard -- read the player's positionX, positionY, and the seedFactor from an infile
     * quit if nothing is in the infile
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2093022?comment=4898827">...</a>
     * Reference: <a href="https://gist.github.com/sberkun/4c5c5de5fd6a617c8bb84d4ba188f484">...</a>
     * Reference: <a href="https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/In.html">...</a>
     */
    public void loadWithKeyboard() {
        In in = new In(filename);
        if (in.isEmpty()){
            quit();
        }
        // read the saved position of the player
        int savedXPos = Integer.parseInt(in.readLine());
        int savedYPos = Integer.parseInt(in.readLine());
        // retrieve the previous worldFrame by seed (and action?)
        reloadSeedFactor = in.readLine();
        // reloadActionFactor = in.readLine();
        WorldGenerator retrievedWorld = new WorldGenerator(reloadSeedFactor);
        retrievedWorld.fillBoardWithNothing();
        retrievedWorld.generateRooms();
        realRoomSet = retrievedWorld.getRealRooms();
        retrievedWorld.generateHallways();
        TETile[][] retrievedFrame = retrievedWorld.getTiles();
        // set the currentPos of the player back to the saved one and draw it
        player.place(new Position(savedXPos, savedYPos));
        player.drawMyself(retrievedFrame);
        ter.initialize(WIDTH, HEIGHT + 2);
        Font font = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(font);
        ter.renderFrame(retrievedFrame);
        catchActionWithKeyboard(retrievedFrame);
    }

    /**
     * KEYBOARD METHOD
     * catch the movement of the player
     * NOTICE: the HUD function could be infused in here
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2168007">...</a>
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2158028">...</a>
     * Reference: <a href="https://www.tutorialspoint.com/java/lang/character_tolowercase.htm">...</a>
     * Reference: Professor Hug's Demo: InputDemo.KeyboardInputSource
     */
    private void catchActionWithKeyboard(TETile[][] tiles) {
        // check if light is turned on
        int lit = 0;

        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                presentHUD(tiles); // present the HUD if no key is typed -- must have this flickering or the date/time will not refresh
                continue;
            }
            char press = Character.toLowerCase(StdDraw.nextKeyTyped());

            // turn the light on and off with 'l'
            if (press == 't') {
                if (lit == 0) {
                    lightOn(tiles);
                    lit = 1;
                }
                else {
                    lightOff(tiles);
                    lit = 0;
                }
                continue;
            }

            // quit and save the world with ':' and 'q'
            if (press == ':') {
                while (true) {
                    // do nothing but present HUD if it is not a digit
                    if (!StdDraw.hasNextKeyTyped()) {
                        presentHUD(tiles); // present the HUD if no key is typed -- must have this flickering or the date/time will not refresh
                        continue;
                    }
                    char nextPress = Character.toLowerCase(StdDraw.nextKeyTyped());
                    if (nextPress == 'q'){
                        saveWorld(tiles);
                        quit();
                    }
                    else {
                        break; //move out from this "quitting mode"while loop and back to normal movement
                    }
                }
            }

//            // if it is not :q, add this movement to the action factor
//            else {
//                // reloadActionFactor += press;
//            }

            // to check if the move is WASD and the position the player want to move does not hit wall
            if (player.checkMove(press) != null && tiles[player.checkMove(press).x][player.checkMove(press).y] != Tileset.OURWALL){
                retrieveStep(player.getCurrentPos(), tiles);
                player.move(press);
                player.setStep(tiles[player.getCurrentPos().x][player.getCurrentPos().y]);
                player.drawMyself(tiles);
            }
            Font font = new Font("Monaco", Font.PLAIN, 20);
            StdDraw.setFont(font);
            ter.renderFrame(tiles);
        }
    }

    /**
     * KEYBOARD METHOD
     * draw the first/welcoming page when the game starts, also catch which character is being pressed (only n/l/q will work) and return this character
     * @return the character that is caught on the first screen
     * Reference: Professor Hug's Demo: InputDemo.KeyboardInputSource
     * Reference: Lab 12's Staff live-coding solution (in Yunze's repo): <a href="https://github.com/Berkeley-CS61B-Student/fa22-s930/blob/main/lab12/MemoryGame/MemoryGame.java">...</a>
     */
    public char drawFirstScreenAndCatchAction() {
        StdDraw.setCanvasSize(CANVASWIDTH, CANVASHEIGHT);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        Font font = new Font("Monaco", Font.PLAIN, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.text(WIDTH/2, 22, "New Game (N)");
        StdDraw.text(WIDTH/2, 18, "Load Game (L)");
        StdDraw.text(WIDTH/2, 14, "Quit (Q) ");
        StdDraw.text(WIDTH/2, 30, "Aiden and Yunze's Ice World");
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char nextPress = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (nextPress == 'n') {
                    return nextPress;
                }
                else if (nextPress == 'l') {
                    return nextPress;
                }
                else if (nextPress == 'q') {
                    return nextPress;
                }
                else {
                    continue;
                }
            }
        }
    }

    /**
     * KEYBOARD METHOD
     * draw the screen to let the user type in a seed and extract it out as a seed
     * end when a 's' is detected
     * @return the seed as a string
     * Reference: <a href="https://www.geeksforgeeks.org/character-isdigit-method-in-java-with-examples/">...</a>
     * Reference: Lab 12's Staff live-coding solution (in Yunze's repo): <a href="https://github.com/Berkeley-CS61B-Student/fa22-s930/blob/main/lab12/MemoryGame/MemoryGame.java">...</a>
     * Reference: Professor Hug's Demo: InputDemo.KeyboardInputSource
     */
    public String drawSeedScreenAndReadSeed() {
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering(); // should we use d-b here?
        Font font = new Font("Monaco", Font.PLAIN, 30);
        String seed = "";

        while (true) {
            StdDraw.clear(Color.BLACK);
            StdDraw.enableDoubleBuffering();
            StdDraw.setFont(font);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 + 6, "Type some digits as seed; press 'S' to start game!:");
            StdDraw.show();

            if (StdDraw.hasNextKeyTyped()) {
                char nextPress = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (nextPress == 's') {
                    return seed; // stop and return when an 'S' is detected
                }
                else {
                    // input can only be digits
                    if (Character.isDigit(nextPress)) {
                        seed += nextPress;
                        StdDraw.setFont(font);
                        StdDraw.setPenColor(Color.WHITE);
                        StdDraw.text(WIDTH / 2, HEIGHT / 4 + 6,  seed);
                        StdDraw.show();
                        StdDraw.pause(500); // the digits will last for a while
                    }
                }
            }
        }
    }

    /**
     * KEYBOARD METHOD
     * present the HUD on the top-left of the canvas
     * and the real day and time on the top-mid of the canvas
     * Reference: <a href="https://stackoverflow.com/questions/23463793/getting-coordinates-of-mouse-click-on-grid-easy-java-stddraw">...</a>
     */
    public void presentHUD(TETile[][] tiles){
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        // the if-statement solves the problem that HUD freezes when the mouse is moved out from the frame
        if (mouseX < WIDTH && mouseY < HEIGHT){
            TETile mousePos = tiles[mouseX][mouseY];
            Font font = new Font("Monaco", Font.PLAIN, 20);
            StdDraw.setFont(font);
            StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
            StdDraw.text(4, HEIGHT + 1, mousePos.description());
            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
            // secondary feature: real date/time on the HUD
            StdDraw.text(WIDTH / 2, HEIGHT + 1, java.time.LocalDateTime.now().toString());
            StdDraw.show();
            StdDraw.setFont(font);
            ter.renderFrame(tiles);
        }
    }

    /**
     * KEYBOARD METHOD
     * light up the rooms
     */
    public void lightOn(TETile[][] tiles) {
        for (Room room: realRoomSet){
            room.drawLight(tiles);
        }
        if(tiles[player.getCurrentPos().x][player.getCurrentPos().y] == Tileset.OURPLAYER) {
            player.setStep(Tileset.OURFLOOR);
        }
        else {
            player.setStep(tiles[player.getCurrentPos().x][player.getCurrentPos().y]);
        }
        player.drawMyself(tiles);
    }

    /**
     * KEYBOARD METHOD
     * turn the lights off
     */
    public void lightOff(TETile[][] tiles) {
        for (Room room: realRoomSet){
            room.drawFloors(tiles);
        }
        player.setStep(Tileset.OURFLOOR);
        player.drawMyself(tiles);
    }



    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Yunze： Note that the seed always start with an N and ends with an S for every seed
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        String lowerInput = input.toLowerCase();
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        char[] lowerInputInChar = lowerInput.toCharArray();
        switch (lowerInputInChar[0]){
            case 'n' -> finalWorldFrame = newWithString(input);
            case 'l' -> finalWorldFrame = loadWithString(input);
            case 'q' -> quit();
        }

        // the rendering processed below is temporary -- should be comment out when submitted
//        ter.initialize(WIDTH, HEIGHT);
//        ter.renderFrame(finalWorldFrame);

        return finalWorldFrame;
    }

    // BELOW ARE STRING METHODS

    /**
     * STRING METHOD
     * starts a new game and interact it with the string input like N8965SWWDDSA
     * @return the tiles world after the game is played with the string input
     */
    public TETile[][] newWithString(String input){
        String seed = readSeed(input);
        String action = readAction(input);
        reloadSeedFactor = seed;
        // reloadActionFactor = action;
        WorldGenerator generatedWorld = new WorldGenerator(seed);
        generatedWorld.fillBoardWithNothing();
        generatedWorld.generateRooms();
        realRoomSet = generatedWorld.getRealRooms();
        generatedWorld.generateHallways();
        Position startPos = generatedWorld.getRandomAnchorPoint();
        player.place(startPos);
        TETile[][] worldFrame = generatedWorld.getTiles();
        player.drawMyself(worldFrame);
        catchActionWithString(worldFrame, action);
        return worldFrame;
    }

    /**
     * STRING METHOD
     * reload an existing world and interact it with the string input -- read the player's positionX, positionY, and the seedFactor from an infile
     * quit if nothing is in the infile
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2093022?comment=4898827">...</a>
     * Reference: <a href="https://gist.github.com/sberkun/4c5c5de5fd6a617c8bb84d4ba188f484">...</a>
     * Reference: <a href="https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/In.html">...</a>
     * @return the tiles world after the game is reloaded and played with the string input
     */
    public TETile[][] loadWithString(String input){
        In in = new In(filename);
        if (in.isEmpty()){
            quit();
        }
        // read the saved position of the player
        int savedXPos = Integer.parseInt(in.readLine());
        int savedYPos = Integer.parseInt(in.readLine());
        // retrieve the previous worldFrame by seed (and action?)
        reloadSeedFactor = in.readLine();
        // reloadActionFactor = in.readLine();
        WorldGenerator retrievedWorld = new WorldGenerator(reloadSeedFactor);
        retrievedWorld.fillBoardWithNothing();
        retrievedWorld.generateRooms();
        realRoomSet = retrievedWorld.getRealRooms();
        retrievedWorld.generateHallways();
        TETile[][] retrievedFrame = retrievedWorld.getTiles();
        // set the currentPos of the player back to the saved one and draw it
        player.place(new Position(savedXPos, savedYPos));
        player.drawMyself(retrievedFrame);
        String lowerInput = input.toLowerCase();
        int startIndex = lowerInput.indexOf('l') + 1; // get rid of the first 'L'
        String realInput = lowerInput.substring(startIndex);
        catchActionWithString(retrievedFrame, realInput);
        return retrievedFrame;
    }

    /**
     * STRING METHOD
     * catch the movement of the player
     */
    private void catchActionWithString(TETile[][] tiles, String operation) {
        char[] pressList = operation.toLowerCase().toCharArray();
        for (int i = 0; i < pressList.length; i++) {
            if (pressList[i] == ':'){
                if ((i < pressList.length - 1) && (pressList[i+1] == 'q')) {
                    // if the action string ends with :q, erase these two characters
                    // reloadActionFactor = reloadActionFactor.substring(0, reloadActionFactor.length() - 2);
                    saveWorld(tiles);
                    quit();
                }
            }
            else {
                // to check if the move is WASD and the position the player want to move does not hit wall
                if (player.checkMove(pressList[i]) != null && tiles[player.checkMove(pressList[i]).x][player.checkMove(pressList[i]).y] != Tileset.OURWALL){
                    retrieveStep(player.getCurrentPos(), tiles);
                    player.move(pressList[i]);
                    player.setStep(tiles[player.getCurrentPos().x][player.getCurrentPos().y]);
                    player.drawMyself(tiles);
                }
            }
        }
    }

    /**
     * STRING METHOD
     * read the string and extract the seed out as a substring
     * @return the seed as a string
     * reference: <a href="https://www.javatpoint.com/java-string-substring">...</a>
     */
    public String readSeed(String input){
        String lowerInput = input.toLowerCase();
        int startIndex = 1;
        int endIndex = lowerInput.indexOf('s');
        String seed = lowerInput.substring(startIndex, endIndex);
        return seed;
    }

    /**
     * STRING METHOD
     * read the string and extract the action out as a substring
     * @return the action as a string
     * reference: <a href="https://www.javatpoint.com/java-string-substring">...</a>
     */
    public String readAction(String input){
        String lowerInput = input.toLowerCase();
        int startIndex = lowerInput.indexOf('s') + 1;
        int endIndex = lowerInput.length() - 1;
        String action = lowerInput.substring(startIndex, endIndex);
        return action;
    }



    // BELOW ARE UNIVERSAL METHODS

    /**
     * save the world into a file, but how? -- writing the player's positionX, positionY, and the seedFactor into an outfile
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2093022?comment=4898827">...</a>
     * Reference: <a href="https://gist.github.com/sberkun/4c5c5de5fd6a617c8bb84d4ba188f484">...</a>
     * Reference: <a href="https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/Out.html">...</a>
     */
    public void saveWorld(TETile[][] tiles) {
        Out out = new Out(filename);
        out.println(player.getCurrentPos().x); // x-position of the player
        out.println(player.getCurrentPos().y); // y-position of the player
        out.println(reloadSeedFactor); // factor to reconstruct the world
        // out.println(reloadActionFactor); // factor to mimic the action
    }

    /**
     * quit the game, no matter interacting with keyboard or string
     * Reference: <a href="https://edstem.org/us/courses/25377/discussion/2181192">...</a>
     */
    public void quit(){
        System.exit(0);
    }

    /**
     * draw the position back to floor, used in player movement
     */
    public void retrieveStep(Position pos, TETile[][] tiles) {
        tiles[pos.x][pos.y] = player.getPreviousStep();
    }


//    main-method for testing
//    public static void main(String[] args){
//        //test a generated game
//        Engine newEng = new Engine();
//        newEng.interactWithInputString("N8982SDD:Q");
//        newEng.interactWithInputString("LWWSSDD");
//
//        newEng.interactWithKeyboard();
//        System.out.println(new In(filename).readAll());
//    }
//

}
