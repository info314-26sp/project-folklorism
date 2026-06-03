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
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        Random random = new Random();

        while (true) {
            Message message = (Message) in.readObject();

            if (message.getType() == MessageType.Request_Card) {
                Rank[] ranks = Rank.values();
                Suit[] suits = Suit.values();
                Rank rank = ranks[random.nextInt(ranks.length)];
                Suit suit = suits[random.nextInt(suits.length)];
                Card card = new Card(rank, suit);

                Message response = new Message(MessageType.Deal_Card, "Dealer", message.getSender(),
                                                card.toString());
                out.writeObject(response);
                out.flush();
            }
        }
        socket.close();
    }
}