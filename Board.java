import java.util.Random;

/**
 * Board = one player's 8x8 grid of ocean.
 * Cell symbols:  ~ water   S ship   X hit   O miss
 */
public class Board {

    public static final int SIZE = 8;
    public static final char WATER = '~';
    public static final char SHIP = 'S';
    public static final char HIT = 'X';
    public static final char MISS = 'O';

    private char[][] grid = new char[SIZE][SIZE];
    private int shipCellsLeft = 0;          // how many ship cells are still unhit
    private Random random = new Random();

    // Constructor: runs when we write "new Board()". Fills the grid with water.
    public Board() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = WATER;
            }
        }
    }

    // Tries to put a ship on the board. Returns true if it fit, false if not.
    public boolean placeShip(int row, int col, int length, boolean horizontal) {
        int endRow = horizontal ? row : row + length - 1;
        int endCol = horizontal ? col + length - 1 : col;

        // 1) must stay inside the board
        if (row < 0 || col < 0 || endRow >= SIZE || endCol >= SIZE) {
            return false;
        }
        // 2) every cell it needs must be empty water
        for (int i = 0; i < length; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (grid[r][c] != WATER) {
                return false;
            }
        }
        // 3) all good -> actually place it
        for (int i = 0; i < length; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            grid[r][c] = SHIP;
        }
        shipCellsLeft += length;
        return true;
    }

    // Keeps trying random positions until the ship fits.
    public void placeRandomly(int length) {
        while (true) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            boolean horizontal = random.nextBoolean();
            if (placeShip(row, col, length, horizontal)) {
                return;
            }
        }
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    // True if this cell was already shot at (X or O).
    public boolean alreadyTried(int row, int col) {
        return grid[row][col] == HIT || grid[row][col] == MISS;
    }

    // Fires at a cell. Returns true for a hit, false for a miss.
    // (Caller must check isInside and alreadyTried first.)
    public boolean attack(int row, int col) {
        if (grid[row][col] == SHIP) {
            grid[row][col] = HIT;
            shipCellsLeft--;
            return true;
        }
        grid[row][col] = MISS;
        return false;
    }

    // The game is over for this board when no ship cells remain.
    public boolean allSunk() {
        return shipCellsLeft == 0;
    }

    // Turns the grid into printable text.
    // hideShips = true  -> enemy view (S shown as water)
    // hideShips = false -> owner view
    public String render(boolean hideShips) {
        StringBuilder sb = new StringBuilder("   ");
        for (int c = 0; c < SIZE; c++) {
            sb.append(c).append(' ');
        }
        sb.append('\n');
        for (int r = 0; r < SIZE; r++) {
            sb.append(r).append("  ");
            for (int c = 0; c < SIZE; c++) {
                char cell = grid[r][c];
                if (hideShips && cell == SHIP) {
                    cell = WATER;
                }
                sb.append(cell).append(' ');
            }
            if (r < SIZE - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
