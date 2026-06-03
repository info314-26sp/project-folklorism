import java.io.*;
import java.net.*;

class GameServer {
  private static final int PORT = 2121;
  private ClientHandler player;
  private ClientHandler dealer;

  public GameServer() {
    try (
      ServerSocket serverSocket = new ServerSocket(PORT);
    ) {
      System.out.println("Server running on port " + PORT);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        (new Thread(new ClientHandler(clientSocket, this))).start();
      }
    } catch (IOException e) {
    }
  }

  public synchronized ClientHandler getPlayer() {
    return player;
  }

  public synchronized ClientHandler getDealer() {
    return dealer;
  }

  public synchronized void setPlayer(ClientHandler player) {
    System.out.println("player set");
    this.player = player;
  }

  public synchronized void setDealer(ClientHandler dealer) {
    System.out.println("dealer set");
    this.dealer = dealer;
  }

  public void handlePlayerAction(String action) {
    switch (action) {
      case "HIT":
        System.out.println("player: hit");
        dealer.sendMessage("{\n"
          + "  \"type\": \"PLAYER_ACTION\",\n"
          + "  \"msg_id\": \"103\",\n"
          + "  \"sender\": \"PLAYER\",\n"
          + "  \"payload\": {\n"
          + "    \"action\": \"HIT\"\n"
          + "  }\n"
          + "}"
        );
        break;
      case "STAND":
        break;
      case "SPLIT":
        break;
      default:
    }
  }

  public void handleDealerCard(String target, String card) {
    String message = "{\n"
      + "  \"type\": \"DEALER_CARD\",\n"
      + "  \"msg_id\": \"104\",\n"
      + "  \"sender\": \"DEALER\",\n"
      + "  \"payload\": {\n"
      + "    \"target\": \"" + target + "\",\n"
      + "    \"card\": \"" + card + "\",\n"
      + "    \"new_total\": 20,\n"
      + "    \"is_bust\": false,\n"
      + "    \"is_blackjack\": false\n"
      + "  }\n"
      + "}";

    switch (target) {
      case "PLAYER":
        System.out.println("player: drew " + card);
        // TODO calculate total and if bust or blackjack send to player
        player.sendMessage(message);
        break;
      case "DEALER":
        break;
      default:
    }
  }

  public static void main(String[] args) {
    new GameServer();
  }
}
