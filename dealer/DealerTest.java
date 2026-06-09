import java.io.*;
import java.util.*;
import java.net.Socket;
import Deck.*;

class DealerTest {
  private static final String HOST = "localhost";
  private static final int PORT = 2121;
  public static final boolean DEBUG = false;
  private boolean gameOver = false;
  private PrintWriter out;
  private BufferedReader in;
  private Deck deck = new Deck();
  private int score = 0;

  public DealerTest() {
    try (
      Socket socket = new Socket(HOST, PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
      this.out = out;
      this.in = in;

      if (requestRole()) {
        // successfully connected
        System.out.println("connected to server " + HOST + ":" + PORT + " as dealer");

        // wait for server to start game
        System.out.println("waiting for game to start...");
        waitForGameStart();

        // initial deal
        getInitialCards();

        // game loop
        while (!gameOver) {
          handleConnection();
        }
      } else {
        // failed to connect
        System.out.println("failed to connect to " + HOST + ":" + PORT + " as dealer");
        socket.close();
      }
    } catch (IOException e) {
    }
  }

  /**
   * requests server to be a dealer
   */
  private boolean requestRole() throws IOException {
    System.out.println("requesting dealer role...");
    out.println(
      "REQ ASRO\n" +
      "dealer\n"
    );

    String[] message = readMessage();

    System.out.println(message[1] + ": " + message[2]);

    if (!message[0].split(" ")[0].equals("RES")
      || !message[0].split(" ")[1].equals("ASRO")
      || !message[1].equals("success")
      || !message[2].equals("dealer")
    ) {
      return false;
    }
    return true;
  }

  /**
   * waits for game start message from server then confirms
   */
  private void waitForGameStart() throws IOException {
    String[] message = readMessage();

    if (!message[0].split(" ")[0].equals("REQ")
      || !message[0].split(" ")[1].equals("GMST")
    ) {
      return;
    }

    System.out.println("confirming\n");
    sendMessage("RES GMST\n");
  }

  /**
   * get 2 starting cards
   */
  private void getInitialCards() throws IOException {
    for (int i = 0; i < 2; i++) {
      dealSelfCard();

      // recieve card
      handleAction(readMessage());
    }
  }

  private void handleConnection() throws IOException {
    String[] message = readMessage();

    handleAction(message);
  }

  private void handleAction(String[] message) {
    String action = message[0].split(" ")[1];

    switch (action) {
      case "DLCD":
        handleDealCard(message[1]);
        break;
      case "DLTN":
        System.out.println("\ndealer turn");
        System.out.println("score: " + score);

        while (score < 17) {
          try {
            dealSelfCard();
            handleAction(readMessage());
            System.out.println("score: " + score);
          } catch (IOException e) {
          }
        }
        sendMessage("REQ DLFI\n");
        break;
      case "GMFI":
        String winner = message[1];
        endGame(winner);
        break;
    }
  }

  /**
   * deals a random card to a client
   */
  private void handleDealCard(String recipient) {
    Card card = deck.drawCard();

    if (recipient.equals("dealer")) {
      System.out.println("got " + card.toString());
      score += card.getValue();
    } else {
      System.out.println("dealt " + card.toString());
    }

    sendMessage(
      "RES DLCD\n" +
      recipient + "\n" +
      card.toMessageString() + "\n"
    );
  }

  /**
   * sends a request for a card to the server
   * and listens for the server to respond to request
   */
  private void dealSelfCard() throws IOException {
      // request card
      sendMessage("REQ DRCD\n");

      // deal card
      handleAction(readMessage());
  }

  /**
   * handles game over and declares winner / loser
   */
  private void endGame(String winner) {
    if (winner.equals("dealer")) {
      System.out.println("\nyou win!");
    } else {
      System.out.println("\nyou lose");
    }

    sendMessage("RES GMFI\n");
    gameOver = true;
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
    new DealerTest();
  }
}
