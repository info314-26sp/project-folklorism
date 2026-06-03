import java.io.*;
import java.net.Socket;
import java.util.*;

class PlayerTest {
  private static final String HOST = "localhost";
  private static final int PORT = 2121;
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

      System.out.println("player connected to server " + HOST + ":" + PORT);

      while (true) {
        handleConnection();
      }
    } catch (IOException e) {
    }
  }

  private void handleConnection() throws IOException {
    boolean validInput = false;
    while (!validInput) {
      System.out.println("input 'hit' or 'stand':");
      String userInput = stdIn.readLine();

      switch (userInput) {
        case "hit":
          System.out.println();
          validInput = true;
          sendHit();
          break;
        default:
          System.out.print("please select a valid action: 'hit' or 'stand':");
      }
    }

    String[] message = readMessage();
    String type = message[1].split("\"")[3];

    // handle action
    switch (type) {
      case "DEALER_CARD":
        String card = message[6].split("\"")[3];
        handleDealerCard(card);
        break;
      default:
    }
  }

  private void sendHit() {
    System.out.println("(SENT) hit");
    out.println("{\n"
      + "  \"type\": \"PLAYER_ACTION\",\n"
      + "  \"msg_id\": \"103\",\n"
      + "  \"sender\": \"PLAYER\",\n"
      + "  \"payload\": {\n"
      + "    \"action\": \"HIT\"\n"
      + "  }\n"
      + "}"
    );
  }

  private void handleDealerCard(String card) {
    System.out.println("(RECIEVED) " + card + "\n");
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
    new PlayerTest();
  }
}
