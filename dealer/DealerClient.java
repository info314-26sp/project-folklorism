import java.io.*;
import java.net.Socket;
import java.util.Random;
import Card_And_Message_Stuff.Card;
import Card_And_Message_Stuff.Message;
import Card_And_Message_Stuff.MessageType;
import Card_And_Message_Stuff.Rank;
import Card_And_Message_Stuff.Suit;

public class DealerClient {

    public void start() throws IOException {
        Socket socket = new Socket("localhost", 2121);

        
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Random random = new Random();

       
        out.println("{\"sender\":\"DEALER\"}");

        while (true) {
            String line = in.readLine();
            if (line == null) break;

           
            if (line.contains("\"PLAYER_ACTION\"")) {

               
                Rank[] ranks = Rank.values();
                Suit[] suits = Suit.values();
                Rank rank = ranks[random.nextInt(ranks.length)];
                Suit suit = suits[random.nextInt(suits.length)];
                Card card = new Card(rank, suit);

                // Send JSON back to server
                String response = "{\n" +
                        "  \"type\": \"DEAL_CARD\",\n" +
                        "  \"sender\": \"DEALER\",\n" +
                        "  \"target\": \"PLAYER\",\n" +
                        "  \"payload\": {\n" +
                        "    \"card\": \"" + card.toString() + "\"\n" +
                        "  }\n" +
                        "}";

                out.println(response);
            }
        }

        socket.close();
    }
}
