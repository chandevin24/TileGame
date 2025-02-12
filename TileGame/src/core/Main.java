package core;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import java.awt.*;
import java.io.File;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import edu.princeton.cs.algs4.In;


public class Main {
    //spotlight feature
    private static boolean visibilitySwitch = true;
    private static final int VISBILITY_RADIUS = 5;

    //new line for save.txt
    private static String newLine = System.lineSeparator();

    //proj directory for file saves and reads
    private static final String projectDir = System.getProperty("user.dir");

    //avatar coordinates
    private static boolean avatarPlaced = false;

    //center node
    private static int centerX;
    private static int centerY;

    //player position
    private static int playerX = 0;
    private static int playerY = 0;

    //window size
    private static final int WIDTH = 75;
    private static final int HEIGHT = 35;

    //ranges for room sizes
    private static final int LOWERBOUND = 2;
    private static final int UPPERBOUND = 20;

    //available tiles
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black, "floor", 2);
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing", 3);
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray, "wall", 1);
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "you", 0);

    //fonts
    public static final Font TITLE = new Font("Sans Serif", Font.BOLD, 60);
    public static final Font REGULAR = new Font("Sans Serif", Font.PLAIN, 30);

    public static void main(String[] args) {
        //set size of menu and scale
        StdDraw.setCanvasSize(550, 600);
        StdDraw.setXscale(0, 100);
        StdDraw.setYscale(0, 100);

        //main menu text
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(TITLE);
        StdDraw.text(50, 75, "CS61B: BYOW");
        StdDraw.setFont(REGULAR);
        StdDraw.rectangle(50, 55, 20, 5);
        StdDraw.text(50, 55, "(N) New Game");
        StdDraw.rectangle(50, 45, 20, 5);
        StdDraw.text(50, 45, "(L) Load Game");
        StdDraw.rectangle(50, 35, 20, 5);
        StdDraw.text(50, 35, "(Q) Quit Game");
        StdDraw.show();

        //button dimension
        final double BUTTON_WIDITH = 20;
        final double BUTTON_HEIGHT = 8;
        final double BUTTON_Y_START = 50;

        //checks for button presses or clicks to determine if to create a new game, load game, or quit the game
        while (true) {
            //Checks for mouse click
            if (StdDraw.isMousePressed()){
                double mouseX = StdDraw.mouseX();
                double mouseY = StdDraw.mouseY();
                //New game area
                if (mouseX >= 40 - (BUTTON_WIDITH / 2) && mouseX <= 60 + (BUTTON_WIDITH / 2) && mouseY >= BUTTON_Y_START && mouseY <= BUTTON_Y_START + BUTTON_HEIGHT) {
                    newGame();
                }
                //load game area
                else if (mouseX >= 40 - (BUTTON_WIDITH / 2) && mouseX <= 60 + (BUTTON_WIDITH / 2) && mouseY >= BUTTON_Y_START - 10 && mouseY <= BUTTON_Y_START - 10 + BUTTON_HEIGHT) {
                    loadGame();
                    System.out.println(2);
                }
                //quit game area
                else if (mouseX >= 40 - (BUTTON_WIDITH / 2) && mouseX <= 60 + (BUTTON_WIDITH / 2) && mouseY >= BUTTON_Y_START - 20 && mouseY <= BUTTON_Y_START - 20 + BUTTON_HEIGHT) {
                    System.out.println(1);
                    System.exit(0);
                }
            }
            //key presses for menu
            if (StdDraw.hasNextKeyTyped()) {
                char input = StdDraw.nextKeyTyped();
                if (input == 'q' || input == 'Q') {
                    System.exit(0);
                } else if (input == 'n' || input == 'N') {
                    newGame();
                } else if (input == 'L' || input == 'l') {
                    loadGame();
                }
            }
        }
    }

    //takes button press and moves avatar according to the direction
    public static void movePlayer(TETile[][] world,TETile[][] maskedWorld, char direction) {
        world[playerX][playerY] = FLOOR;
        maskedWorld[playerX][playerY] = FLOOR;

        if (direction == 'w' || direction == 'W') {
            if (world[playerX][playerY + 1] == FLOOR) {
                playerY += 1;
            }
        } else if (direction == 's' || direction == 'S') {
            if (world[playerX][playerY - 1] == FLOOR) {
                playerY -= 1;
            }
        } else if (direction == 'a' || direction == 'A') {
            if (world[playerX - 1][playerY] == FLOOR) {
                playerX -= 1;
            }
        } else if (direction == 'd' || direction == 'D') {
            if (world[playerX + 1][playerY] == FLOOR) {
                playerX += 1;
            }
        }
        world[playerX][playerY] = AVATAR;
        maskedWorld[playerX][playerY] = AVATAR;
        toggleVisibility(world, maskedWorld, playerX, playerY, !visibilitySwitch);

    }

    private static void loadGame() {
        In save = new In(projectDir + "/save.txt");
        if (!save.isEmpty()) {
            String seedLine = save.readLine();
            System.out.print(seedLine);
            if (!save.isEmpty()) {
                String coordLine = save.readLine();
                String[] coor = coordLine.split(" ");
                playerX = Integer.parseInt(coor[0]);
                playerY = Integer.parseInt(coor[1]);
                avatarPlaced = true;
            }
            createWorld(Long.parseLong(seedLine));
        }
    }

    private static void newGame() {
        //new menu text
        StdDraw.clear(Color.black);
        StdDraw.setFont(TITLE);
        StdDraw.text(50, 75, "CS61B: BYOW");
        StdDraw.setFont(REGULAR);
        StdDraw.text(50, 55, "Enter Seed Followed by S:");
        StringBuilder seedInput = new StringBuilder();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input2 = StdDraw.nextKeyTyped();
                if (input2 == 's' || input2 == 'S') {
                    //loads game with seed then exits menu
                    createWorld(Long.parseLong(seedInput.toString()));
                    break;
                }
                //records the seed input
                if (Character.isDigit(input2)) {
                    seedInput.append(input2);
                    StdDraw.clear(Color.black);
                    StdDraw.setPenColor(Color.white);
                    StdDraw.setFont(TITLE);
                    StdDraw.text(50, 75, "CS61B: BYOW");
                    StdDraw.setFont(REGULAR);
                    StdDraw.text(50, 55, "Enter Seed Followed by S:");

                    StdDraw.setPenColor(Color.yellow);
                    StdDraw.text(50, 45, seedInput.toString());
                }
                StdDraw.show();
            }
        }
    }
    private static void createWorld(Long seed) {
        Random r = new Random(seed);

        //make new world
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        TETile[][] maskedWorld = new TETile[WIDTH][HEIGHT];

        // Fill grid with NOTHING tiles.
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = NOTHING;
                maskedWorld[x][y] = NOTHING;
            }
        }

        //initialize rooms
        int placedRooms = 0;
        int maxRooms = r.nextInt(5) + 8; //random number of rooms between 8 and 12

        while (placedRooms < maxRooms) {
            int roomWidth = LOWERBOUND + r.nextInt(UPPERBOUND - LOWERBOUND);
            int roomHeight = LOWERBOUND + r.nextInt(UPPERBOUND - LOWERBOUND);
            //tiles are positioned at least 2 tiles away from the edge bounds
            int y = 2 + r.nextInt(HEIGHT - roomHeight - 4);
            int x = 2 + r.nextInt(WIDTH - roomWidth - 4);

            if (canPlace(world, x, y, roomWidth, roomHeight)) {

                //create walls
                createRoomWalls(world, x, y, roomWidth, roomHeight);
                createRoomWalls(maskedWorld, x, y, roomWidth, roomHeight);

                //creates floors
                for (int w = 0; w < roomWidth; w++) {
                    //adds floors from bottom to top
                    for (int h = 0; h < roomHeight; h++) {
                        world[x + w][y + h] = FLOOR;
                        maskedWorld[x + w][y + h] = FLOOR;
                    }
                }

                placedRooms++;

                //adds Avatar to the first room if it's the first time generating the world
                if (!avatarPlaced && placedRooms == 1) {
                    playerX = x + roomWidth / 2;
                    playerY = y + roomHeight / 2;
                    avatarPlaced = true;
                }

                if (placedRooms > 1) {
                    connect(centerX, centerY, x + (roomWidth / 2), y + (roomHeight / 2), world);
                    connect(centerX, centerY, x + (roomWidth / 2), y + (roomHeight / 2), maskedWorld);
                }
                centerX = x + (roomWidth / 2);
                centerY = y + (roomHeight / 2);
            }

        }
        world[playerX][playerY] = AVATAR;
        maskedWorld[playerX][playerY] = AVATAR;


        ter.renderFrame(maskedWorld);

        // Game loop
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = StdDraw.nextKeyTyped();
                //takes player input
                if (input == 'w' || input == 's' || input == 'a' || input == 'd' || input == 'W' || input == 'S' || input == 'A' || input == 'D') {
                    movePlayer(world, maskedWorld, input);
                    ter.renderFrame(maskedWorld);
                }
                if (input == ':') {
                    while (!StdDraw.hasNextKeyTyped()) {
                        // Waits for next key
                    }
                    char nextInput = StdDraw.nextKeyTyped();
                    if (nextInput == 'q' || nextInput == 'Q') {
                        File file = new File(projectDir + "/save.txt");
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(Long.toString(seed) + newLine);
                            writer.write(playerX + " " + playerY); //coordinate of player
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                    if (nextInput == 'w' || nextInput == 's' || nextInput == 'a' || nextInput == 'd' || nextInput == 'W' || nextInput == 'S' || nextInput == 'A' || nextInput == 'D') {
                        movePlayer(world, maskedWorld, nextInput);
                        ter.renderFrame(maskedWorld);
                    }
                }
                if (input == 't') {
                    visibilitySwitch = !visibilitySwitch; //toggles the state
                    toggleVisibility(world, maskedWorld, playerX, playerY, !visibilitySwitch);
                    ter.renderFrame(maskedWorld);
                }
            }
        }
    }
    private static void toggleVisibility(TETile[][] world, TETile[][]maskedWorld, int playerX, int playerY, boolean on) {
        if (on) {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    double distance = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));
                    if (distance > VISBILITY_RADIUS) {
                        maskedWorld[x][y] = NOTHING;
                    }
                    else {
                        maskedWorld[x][y] = world[x][y]; //make it clear
                    }
                }
            }

        } else {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    maskedWorld[x][y] = world[x][y]; //restore original world
                }
            }

        }

    }

    //adds hallway from 1 room to the existing connected rooms
    private static void connect(int x1, int y1, int x, int y, TETile[][] world) {
        //horizontal hallways
        while (x != x1) {
            if (x < x1) {
                x++;
            } else {
                x--;
            }
            placeHallwayTile(world, x, y);
        }
        //vertical hallways
        while (y != y1) {
            if (y < y1) {
                y++;
            } else {
                y--;
            }
            placeHallwayTile(world, x, y);
        }
    }

    private static void placeHallwayTile(TETile[][] world, int x, int y) {
        //Checks to see if there is a player
        if (!(avatarPlaced && x == playerX && y == playerY)) {
            world[x][y] = FLOOR;
        }

        //left wall
        if (isEmpty(world, x - 1, y)) {
            world[x - 1][y] = WALL;
        }
        //right wall
        if (isEmpty(world, x + 1, y)) {
            world[x + 1][y] = WALL;
        }
        //bottom wall
        if (isEmpty(world, x, y - 1)) {
            world[x][y - 1] = WALL;
        }
        //top wall
        if (isEmpty(world, x, y + 1)) {
            world[x][y + 1] = WALL;
        }
    }

    private static boolean canPlace(TETile[][] world, int x, int y, int roomWidth, int roomHeight) {
        for (int w = 0; w < roomWidth; w++) {
            for (int h = 0; h < roomHeight; h++) {
                if (world[x + w][y + h] != NOTHING) {
                    return false;
                }
            }
        }
        return true;
    }
    private static boolean isEmpty(TETile[][] world, int x, int y) {
        return world[x][y] == NOTHING;
    }
    private static void createRoomWalls(TETile[][] world, int x, int y, int roomWidth, int roomHeight) {
        for (int w = 0; w < roomWidth; w++) {
            // Bottom wall
            if (isEmpty(world, x + w, y - 1)) {
                world[x + w][y - 1] = WALL;
            }

            // Top wall
            if (isEmpty(world, x + w, y + roomHeight)) {
                world[x + w][y + roomHeight] = WALL;
            }
        }

        for (int h = 0; h < roomHeight; h++) {
            // Left wall
            if (isEmpty(world, x - 1, y + h)) {
                world[x - 1][y + h] = WALL;
            }
            // Right wall
            if (isEmpty(world, x + roomWidth, y + h)) {
                world[x + roomWidth][y + h] = WALL;
            }
        }
        //Bottom Left Diagonal Wall
        if (isEmpty(world, x - 1, y - 1)) {
            world[x - 1][y - 1] = WALL;
        }
        //Bottom Right Diagonal Wall
        if (isEmpty(world, x + roomWidth, y - 1)) {
            world[x + roomWidth][y - 1] = WALL;
        }
        //Top Left Diagonal Wall
        if (isEmpty(world, x - 1, y + roomHeight)) {
            world[x - 1][y + roomHeight] = WALL;
        }
        //Top Right Diagonal Wall
        if (isEmpty(world, x + roomWidth, y + roomHeight)) {
            world[x + roomWidth][y + roomHeight] = WALL;
        }

    }
}


