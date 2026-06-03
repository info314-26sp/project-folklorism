import java.io.*;
import java.util.*;
import java.net.Socket;
import java.util.Random;
import Card_And_Message_Stuff.*;

class DealerTest {
  private static final String HOST = "localhost";
  private static final int PORT = 2121;
  private PrintWriter out;
  private BufferedReader in;

  public DealerTest() {
    try (
      Socket socket = new Socket(HOST, PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
      this.out = out;
      this.in = in;

      System.out.println("dealer connected to server " + HOST + ":" + PORT);

      while (true) {
        handleConnection();
      }
      // socket.close();
    } catch (IOException e) {
    }
  }

  private void handleConnection() throws IOException {
    String[] message = readMessage();

    String type = message[1].split("\"")[3];

    // handle action
    switch (type) {
      case "PLAYER_ACTION":
        handleHit();
        break;
      default:
    }
  }

  private void handleHit() throws IOException {
    Random random = new Random();

    Rank[] ranks = Rank.values();
    Suit[] suits = Suit.values();
    Rank rank = ranks[random.nextInt(ranks.length)];
    Suit suit = suits[random.nextInt(suits.length)];

    Card card = new Card(rank, suit);

    String response = "{\n" +
            "  \"type\": \"DEALER_CARD\",\n" +
            "  \"msg_id\": \"104\",\n" +
            "  \"sender\": \"DEALER\",\n" +
            "  \"payload\": {\n" +
            "    \"target\": \"PLAYER\",\n" +
            "    \"card\": \"" + card.toString() + "\"\n" +
            "  }\n" +
            "}";

    System.out.println("drew " + card.toString());
    out.println(response);
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

  public static void main(String[] args) {
    new DealerTest();
  }
}
