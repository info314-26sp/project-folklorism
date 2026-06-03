import java.io.*;
import java.net.Socket;

// methods to implement for ts to work brah:
// - run() --> the method that runs the actual game with the client/server connection for blackjack

// SEND METHODS w/ JSON:

// - sendGameStart() --> sends info to the server that the client/game is ready! based on jason paylod of game_start
// - sendPlayerAction() --> the HIT or STAND to server based on player's input 
// - send() --> builds the message wrapper and writes it to the socket in one line

// RECIEVE METHOD w/ JSON (reads the line from socket, and prases as JSON):
// - receive() --> reads one line from socket and returns the message
public class Player {

    // variables for the server's host and port (edit)
    static final String HOST = "localhost";
    static final int PORT = 2121;

    //socket to connect
    Socket socket;
    //reader that RECEIVES FROM server
    BufferedReader reader;
    //writer that SENDS TO server
    PrintWriter writer;
    // // scanner for user input
    // Scanner input = new Scanner(System.in);


    // mainnn method!
    public static void main(String[] args) throws IOException {
        new Player().run();
    }

    // method that runs the client/blackjack player game
    void run() throws IOException {
        // connect to the server with the open TCP connection
        socket = new Socket(HOST, PORT);

        // getInputStream() --> bytes come IN FROM server (we're absorbing information FROM server)
        // getOutputStream() --> bytes go OUT TO server (we're sending information TO the server)
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        System.out.println("Connected to server at " + HOST + ": " + PORT);

        // #1 let the server know the game is ready to play after the connection to 
        // the server is set

       writer.println("START");
       System.out.println("(Sent) START");

       writer.print("HIT");
       System.out.println("(Sent) HIT");

       String card = reader.readLine();
       System.out.println("(RECIEVED) Dealer has given: " + card);

       System.out.print("Finished!");
       socket.close();
    }
}
