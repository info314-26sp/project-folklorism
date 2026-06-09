import java.util.*;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
  private final Socket clientSocket;
  private final GameServer server;
  private PrintWriter out;
  private BufferedReader in;

  public ClientHandler(Socket clientSocket, GameServer server) {
    this.clientSocket = clientSocket;
    this.server = server;
  }

  public void run() {
    try (
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    ) {
      this.out = out;
      this.in = in;

      // assign client to dealer or player
      if (!handleAssign()) {
        clientSocket.close();
        return;
      }

      // wait for all clients to join to start game
      handleGameStart();

      // separate logic handlers
      if (server.getDealer() == this) {
        while (!server.isGameOver()) {
          handleDealer();
        }
      } else if (server.getPlayer() == this) {
        while (!server.isGameOver()) {
          handlePlayer();
        }
      }
    } catch (IOException e) {
    }
  }

  /**
   * handles assigning the client as either a dealer or player
   */
  private boolean handleAssign() throws IOException {
    String[] message = readMessage();
    String type = message[0].split(" ")[0];
    String action = message[0].split(" ")[1];

    // abort if invalid message
    if (!type.equals("REQ") || !action.equals("ASRO")) {
    System.out.println("abort");
      return false;
    }

    String role = message[1];
    switch (role) {
      case "player":
        return server.assignPlayer(this);
      case "dealer":
        return server.assignDealer(this);
      default:
        sendMessage(
          "RES ASRO\n" +
          "failure\n" +
          "Invalid role requested\n"
        );
        return false;
    }
  }

  /**
   * waits for all clients to join before sending a game start message
   */
  private void handleGameStart() throws IOException {
    server.handleGameStart();

    // send gamestart message
    sendMessage(
      "REQ GMST\n"
    );
  }

  /**
   * player game loop logic
   */
  private void handlePlayer() throws IOException {
    String[] message = readMessage();
    String type = message[0].split(" ")[0];
    String action = message[0].split(" ")[1];

    // handle action
    switch (action) {
      case "PLAC":
        String body = message[1];
        server.handlePlayerAction(body);
        break;
      case "DLHD":
        server.handleDealerHand();
        break;
      case "DLFI":
        server.gotFinalDealerHand();
      case "GMFI":
        break;
      default:
    }
  }

  /**
   * dealer game loop logic
   */
  private void handleDealer() throws IOException {
    String[] message = readMessage();
    String type = message[0].split(" ")[0];
    String action = message[0].split(" ")[1];

    // handle action
    switch (action) {
      case "DLCD":
        String recipient = message[1];
        String card = message[2];
        server.handleDealerCard(recipient, card);
        break;
      case "DRCD":
        sendMessage(
          "REQ DLCD\n" +
          "dealer\n"
        );
        break;
      case "DLFI":
        sendMessage("RES DLFI\n");
        server.handleCompareScores();
        break;
      default:
    }
  }

  /**
   * read message from client
   */
  private String[] readMessage() throws IOException {
    List<String> message = new ArrayList<String>();
    String line;

    if (GameServer.DEBUG) System.out.println("\ndebug (read):");
    while ((line = in.readLine()) != null && !line.isEmpty()) {
      if (GameServer.DEBUG) System.out.println(line);
      message.add(line);
    }
    if (GameServer.DEBUG) System.out.println();

    return message.toArray(new String[0]);
  }

  /**
   * send message to client
   */
  public void sendMessage(String message) {
    if (GameServer.DEBUG) System.out.println("\ndebug (send):\n" + message);
    out.println(message);
  }
}
