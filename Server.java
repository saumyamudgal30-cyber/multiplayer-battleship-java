import java.io.*;
import java.net.*;

/**
 * Server = the referee. It owns both boards, enforces turns,
 * decides hit/miss, keeps score, and handles rematches.
 */
public class Server {

    static final int PORT = 5000;
    static final int[] FLEET = {4, 3, 2};   // ship lengths each player places

    public static void main(String[] args) {
        Player p1 = null;
        Player p2 = null;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + ". Waiting for players...");

            p1 = new Player(serverSocket.accept(), "Player 1");
            System.out.println("Player 1 connected.");
            p1.send("Welcome Player 1! Waiting for Player 2 to join...");

            p2 = new Player(serverSocket.accept(), "Player 2");
            System.out.println("Player 2 connected.");
            p2.send("Welcome Player 2!");
            p1.send("Player 2 has joined. Let's play!");

            boolean playAgain = true;
            while (playAgain) {
                playGame(p1, p2);
                playAgain = askRematch(p1, p2);
            }
            p1.send("Thanks for playing. Goodbye!");
            p2.send("Thanks for playing. Goodbye!");

        } catch (IOException e) {
            System.out.println("Game stopped: " + e.getMessage());
        } finally {
            if (p1 != null) p1.close();
            if (p2 != null) p2.close();
        }
    }

    // ---------- one full game ----------
    static void playGame(Player p1, Player p2) throws IOException {
        p1.newGame();
        p2.newGame();

        placeShips(p1, p2);
        placeShips(p2, p1);

        Player current = p1;
        Player other = p2;

        while (true) {
            // board synchronization: both players get a fresh view every turn
            showBoards(current, other);
            showBoards(other, current);
            current.send(">>> YOUR TURN <<<");
            other.send("Waiting for " + current.name + " to attack...");

            int[] target = askAttack(current, other);
            int row = target[0];
            int col = target[1];

            boolean hit = other.board.attack(row, col);
            if (hit) {
                current.hits++;
                current.send("HIT at (" + row + ", " + col + ")!");
                other.send(current.name + " attacked (" + row + ", " + col + ") and HIT your ship!");
            } else {
                current.send("MISS at (" + row + ", " + col + ").");
                other.send(current.name + " attacked (" + row + ", " + col + ") and missed.");
            }

            if (other.board.allSunk()) {
                current.wins++;
                announceWinner(current, other);
                return;
            }

            // swap turns
            Player temp = current;
            current = other;
            other = temp;
        }
    }

    // ---------- ship placement ----------
    static void placeShips(Player p, Player waiting) throws IOException {
        waiting.send(p.name + " is placing ships. Please wait...");
        p.send("=== SHIP PLACEMENT ===");
        p.send("Type:  row col H   (horizontal)   or   row col V   (vertical).  Example: 2 3 H");
        p.send("Or type R to place the ship at a random spot.");

        for (int length : FLEET) {
            boolean placed = false;
            while (!placed) {
                p.send(p.board.render(false));
                String line = p.ask("Place your ship of length " + length + ":");
                placed = tryPlace(p, line, length);
            }
        }
        p.send("All ships placed!");
    }

    static boolean tryPlace(Player p, String line, int length) {
        String[] parts = line.trim().toUpperCase().split("\\s+");

        if (parts.length == 1 && parts[0].equals("R")) {
            p.board.placeRandomly(length);
            return true;
        }
        if (parts.length != 3) {
            p.send("Wrong format. Use: row col H/V   or   R");
            return false;
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            boolean horizontal;
            if (parts[2].equals("H")) {
                horizontal = true;
            } else if (parts[2].equals("V")) {
                horizontal = false;
            } else {
                p.send("Direction must be H or V.");
                return false;
            }
            if (p.board.placeShip(row, col, length, horizontal)) {
                return true;
            }
            p.send("It doesn't fit there (off the board or overlapping). Try again.");
        } catch (NumberFormatException e) {
            p.send("Row and column must be numbers.");
        }
        return false;
    }

    // ---------- attacking ----------
    static int[] askAttack(Player current, Player other) throws IOException {
        while (true) {
            String line = current.ask("Enter attack as: row col   (example: 3 4)");
            String[] parts = line.trim().split("\\s+");

            if (parts.length != 2) {
                current.send("Please enter exactly two numbers.");
                continue;
            }
            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);

                if (!other.board.isInside(row, col)) {
                    current.send("That's outside the board. Use 0 to " + (Board.SIZE - 1) + ".");
                } else if (other.board.alreadyTried(row, col)) {
                    current.send("You already fired there. Pick another cell.");
                } else {
                    return new int[]{row, col};
                }
            } catch (NumberFormatException e) {
                current.send("Those aren't numbers. Try again.");
            }
        }
    }

    // ---------- display & score ----------
    static void showBoards(Player viewer, Player opponent) {
        viewer.send("\n----------------------------------------");
        viewer.send("YOUR BOARD:");
        viewer.send(viewer.board.render(false));
        viewer.send("\n" + opponent.name.toUpperCase() + "'S BOARD (hidden ships):");
        viewer.send(opponent.board.render(true));
        viewer.send("\nScore -> your hits this game: " + viewer.hits
                + " | your wins: " + viewer.wins);
    }

    static void announceWinner(Player winner, Player loser) {
        for (Player p : new Player[]{winner, loser}) {
            p.send("\n========== GAME OVER ==========");
            p.send(winner.name + "'s final board:");
            p.send(winner.board.render(false));
            p.send("\n" + loser.name + "'s final board:");
            p.send(loser.board.render(false));
        }
        winner.send("\n*** YOU WIN! ***");
        loser.send("\n*** You lost. Better luck next time! ***");
        winner.send("Total wins -> " + winner.name + ": " + winner.wins
                + " | " + loser.name + ": " + loser.wins);
        loser.send("Total wins -> " + winner.name + ": " + winner.wins
                + " | " + loser.name + ": " + loser.wins);
    }

    // ---------- rematch ----------
    static boolean askRematch(Player p1, Player p2) throws IOException {
        p2.send("Waiting for " + p1.name + " to answer the rematch question...");
        boolean a = p1.ask("Play again? (y/n)").trim().equalsIgnoreCase("y");
        p1.send("Waiting for " + p2.name + " to answer...");
        boolean b = p2.ask("Play again? (y/n)").trim().equalsIgnoreCase("y");

        if (a && b) {
            p1.send("Rematch! Get ready...");
            p2.send("Rematch! Get ready...");
            return true;
        }
        p1.send("Rematch cancelled.");
        p2.send("Rematch cancelled.");
        return false;
    }
}
