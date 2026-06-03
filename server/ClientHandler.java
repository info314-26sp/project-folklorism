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

      // dummy way of setting player / dealer
       if (server.getPlayer() == null) {
        server.setPlayer(this);
        out.println("player");
      } else if (server.getDealer() == null) {
        server.setDealer(this);
        out.println("dealer");
      }

      // separate logic handlers
      if (server.getDealer() == this) {
        while (true) {
          handleDealer();
        }
      } else if (server.getPlayer() == this) {
        while (true) {
          handlePlayer();
        }
      }
    } catch (IOException e) {
    }
  }

  private void handlePlayer() throws IOException {
    String[] message = readMessage();
    String type = message[1].split("\"")[3];

    // handle action
    switch (type) {
      case "PLAYER_ACTION":
        String action = message[5].split("\"")[3];
        server.handlePlayerAction(action);
        break;
      default:
    }
  }

  private void handleDealer() throws IOException {
    String[] message = readMessage();
    String type = message[1].split("\"")[3];

    // handle action
    switch (type) {
      case "DEALER_CARD":
        String target = message[5].split("\"")[3];
        String card = message[6].split("\"")[3];
        server.handleDealerCard(target, card);
        break;
      default:
    }
  }

  private String[] readMessage() throws IOException {
    List<String> message = new ArrayList<String>();
    String line;

    // stop reading at closing '}' line
    while ((line = in.readLine()) != null && !line.equals("}")) {
      message.add(line);
    }
    message.add("}");

    return message.toArray(new String[0]);
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}
