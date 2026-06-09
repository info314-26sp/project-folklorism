import java.io.*;
import java.net.Socket;
import java.util.*;
import Deck.*;

class PlayerTest {
  public static final boolean DEBUG = false;
  private static final String HOST = "localhost";
  private static final int PORT = 2121;
  private boolean gameOver = false;
  private boolean playerTurn = true;
  private boolean skipInput = false;
  private int score = 0;
  private PrintWriter out;
  private BufferedReader in;
  private BufferedReader stdIn;

  public PlayerTest() {
    try (
      Socket socket = new Socket(HOST, PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
    ) {
      this.out = out;
      this.in = in;
      this.stdIn = stdIn;

      if (requestRole()) {
        // successfully connected
        System.out.println("connected to server " + HOST + ":" + PORT + " as player");

        // wait for server to start game
        System.out.println("waiting for game to start...");
        waitForGameStart();

        // clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // initial deal
        getInitialCards();

        // player game loop
        while (!gameOver) {
          handleConnection();
        }
      } else {
        // failed to connect
        System.out.println("failed to connect to " + HOST + ":" + PORT + " as player");
      }
      socket.close();
    } catch (IOException e) {
    }
  }

  private boolean requestRole() throws IOException {
    System.out.println("requesting player role...");
    sendMessage(
      "REQ ASRO\n" +
      "player\n"
    );

    String[] message = readMessage();

    System.out.println(message[1] + ": " + message[2]);

    if (!message[0].split(" ")[0].equals("RES")
      || !message[0].split(" ")[1].equals("ASRO")
      || !message[1].equals("success")
      || !message[2].equals("player")
    ) {
      return false;
    }
    return true;
  }

  private void waitForGameStart() throws IOException {
    String[] message = readMessage();

    if (!message[0].split(" ")[0].equals("REQ")
      || !message[0].split(" ")[1].equals("GMST")
    ) {
      return;
    }

    System.out.println("game started");
    sendMessage("RES GMST\n");
  }

  private void getInitialCards() throws IOException {
    for (int i = 0; i < 2; i++) {
      sendHit();
      handleAction(readMessage());
    }
    System.out.println();

    // show dealer hand
    sendMessage("REQ DLHD\n");
    handleAction(readMessage());
  }

  private void handleConnection() throws IOException {
    if (playerTurn && !skipInput) {
      boolean validInput = false;
      while (!validInput) {
        System.out.println("\ninput 'hit' 'stand' or 'view':");
        String userInput = stdIn.readLine();

        switch (userInput) {
          case "hit":
            System.out.println();
            validInput = true;
            sendHit();
            break;
          case "stand":
            System.out.println();
            validInput = true;
            sendMessage(
              "REQ PLAC\n" +
              "stand\n"
            );
            break;
          case "view":
            System.out.println();
            validInput = true;
            sendMessage("REQ DLHD\n");
            break;
          default:
            System.out.print("please select a valid action: 'hit' 'stand' or 'view':");
        }
      }
    }

    String[] message = readMessage();
    handleAction(message);
  }

  private void handleAction(String[] message) throws IOException {
    String action = message[0].split(" ")[1];

    switch (action) {
      case "PLAC":
        // player action
        String body = message[1];

        if (body.equals("stand")) {
          // stand
          playerTurn = false;
          return;
        } else {
          // hit
          handleDealerCard(body);

          // blackjack or bust triggered
          if (message.length > 2) {
            System.out.println(message[2]);
            skipInput = true;
          }
        }
        break;
      case "DLHD":
        // dealer hand
        System.out.println("dealer hand:");
        int dealerScore = 0;
        for (int i = 1; i < message.length; i++) {
          Card card = new Card(message[i]);
          dealerScore += card.getValue();
          System.out.println("  " + card.toString());
        }
        if (message.length < 3) {
          // hidden card
          System.out.println("  hidden hole card");
          System.out.println("total (" + dealerScore + " + ?)");
        } else {
          System.out.println("total (" + dealerScore + ")");
        }

        break;
      case "DLTN":
        // dealer turn
        // view dealer's hand
        sendMessage("REQ DLHD\n");
        handleAction(readMessage());

        if (Integer.parseInt(message[1]) < 17) {
          System.out.println("\ndealer drawing...");
        }
        System.out.print("\nfinal ");
        break;
      case "DLFI":
        // get final dealer hand
        sendMessage("REQ DLHD\n");
        handleAction(readMessage());

        sendMessage("RES DLFI\n");
        break;
      case "GMFI":
        // game final
        String winner = message[1];
        endGame(winner);
        break;
      default:
    }
  }

  private void sendHit() {
    sendMessage(
      "REQ PLAC\n" +
      "hit\n"
    );
  }

  private void endGame(String winner) {
    switch (winner) {
      case "player":
      System.out.println("\nyou win!");
      break;
      case "dealer":
      System.out.println("\nyou lose");
      break;
      case "push":
      System.out.println("\npush");
    }

    sendMessage("RES GMFI\n");
    gameOver = true;
  }

  private void handleDealerCard(String cardString) {
    Card card = new Card(cardString);
    score += card.getValue();
    System.out.println("recieved: " + card.toString() + " (" + score + ")");
  }

  /**
   * read message from server
   */
  private String[] readMessage() throws IOException {
    List<String> message = new ArrayList<String>();
    String line;

    if (DEBUG) System.out.println("\ndebug (read):");
    while ((line = in.readLine()) != null && !line.isEmpty()) {
      if (DEBUG) System.out.println(line);
      message.add(line);
    }
    if (DEBUG) System.out.println();

    return message.toArray(new String[0]);
  }

  /**
   * send message to server
   */
  private void sendMessage(String message) {
    if (DEBUG) System.out.println("\ndebug (send):\n" + message);
    out.println(message);
  }

  public static void main(String[] args) {
    new PlayerTest();
  }
}
