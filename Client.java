import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client = a "dumb terminal". It prints whatever the server sends,
 * and when the server sends the word INPUT it reads a line from the keyboard.
 * All game rules live on the server.
 */
public class Client {

    public static void main(String[] args) throws IOException {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = 5000;

        Socket socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner keyboard = new Scanner(System.in);

        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("INPUT")) {
                System.out.print("> ");
                out.println(keyboard.nextLine());
            } else {
                System.out.println(line);
            }
        }
        System.out.println("Disconnected from server.");
        socket.close();
    }
}
