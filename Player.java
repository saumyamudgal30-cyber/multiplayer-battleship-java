import java.io.*;
import java.net.Socket;

/**
 * Player = everything the server knows about one connected player:
 * their network connection, their board, and their score.
 */
public class Player {

    public String name;
    public Board board;
    public int hits = 0;     // hits scored in the current game
    public int wins = 0;     // games won in this session

    private Socket socket;
    private BufferedReader in;   // reads lines the player sends us
    private PrintWriter out;     // writes lines to the player

    public Player(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true); // true = auto-flush
        this.board = new Board();
    }

    // Send one message (may contain several lines) to this player's screen.
    public void send(String message) {
        out.println(message);
    }

    // Show a prompt, tell the client "now it's your turn to type", wait for the reply.
    public String ask(String prompt) throws IOException {
        out.println(prompt);
        out.println("INPUT");              // special signal understood by Client
        String answer = in.readLine();     // blocks until the player presses Enter
        if (answer == null) {              // null means the connection closed
            throw new IOException(name + " disconnected");
        }
        return answer;
    }

    // Fresh board and hit counter for a new round (wins are kept).
    public void newGame() {
        board = new Board();
        hits = 0;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // nothing useful to do if closing fails
        }
    }
}
