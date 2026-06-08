import java.io.*;
import java.util.*;
import java.net.Socket;

import Card_And_Message_Stuff.*;

class DealerTest {
  private static final String HOST = "localhost";
  private static final int PORT = 2121;
  private PrintWriter out;
  private BufferedReader in;
  private BlackJackRound round = new BlackJackRound();
  public DealerTest() {
    try (
      Socket socket = new Socket(HOST, PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) 
    {
      this.out = out;
      this.in = in;

      out.println("{\"type\":\"CONNECT\",\"sender\":\"DEALER\"}");
      System.out.println("dealer connected to server " + HOST + ":" + PORT);

      while (true) {
        handleConnection();
      }
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleConnection() throws IOException {
    String message = readMessage();
    if (message == null) return;

    if (message.contains("\"START_ROUND\"")) {
        startRound();
    } else if (message.contains("\"PLAYER_HIT\"")) {
        handlePlayerHit();
    } else if (message.contains("\"PLAYER_STAND\"")) {
        handlePlayerStand();
    } else if (message.contains("\"PLAYER_SPLIT\"")) {
        handlePlayerSplit();
    } else if (message.contains("\"DEALER_TURN\"")) {
        handleDealerTurn();
    }
  }

  private void startRound() {
    round = new BlackJackRound();
    round.startRound();

    send("{\"typre:DEALER_UPCARD\",\"sender\":\"DEALER\",\"payload\":{\"card\":\"" + round.getDealerHand().getCard(0).toString() + "\"}}");
    send("{\"type\":\"PLAYER_INITIAL\",\"sender\":\"DEALER\",\"payload\":{"
                + "\"card1\":\"" + round.getCurrentPlayerHand().getCard(0).toString() + "\","
                + "\"card2\":\"" + round.getCurrentPlayerHand().getCard(1).toString() + "\","
                + "\"total\":\"" + round.getCurrentPlayerHand().getValue() + "\","
                + "\"blackjack\":\"" + round.getCurrentPlayerHand().isBlackjack() + "\""
                + "}}");
  }

  private void handlePlayerHit(){
    Card card = round.playerHit();
    Hand hand = round.getCurrentPlayerHand();

    System.out.println("Player hits and receives: " + card);

    send("{\"type\":\"PLAYER_HIT_CARD\",\"sender\":\"DEALER\",\"payload\":{"
                + "\"card\":\"" + card.toString() + "\","
                + "\"total\":\"" + hand.getValue() + "\","
                + "\"bust\":\"" + hand.isBust() + "\","
                + "\"blackjack\":\"" + hand.isBlackjack() + "\""
                + "}}");
    if (hand.isBust()) {
      send("{\"type\":\"PLAYER_BUST\",\"sender\":\"DEALER\",\"payload\":{"
              + "\"total\":\"" + hand.getValue() + "\""
              + "}}");
    }
  }

  private void handlePlayerStand() {
    System.out.println("Player stands with total: " + round.getCurrentPlayerHand().getValue());
    send("{\"type\":\"PLAYER_STAND\",\"sender\":\"DEALER\"}");
  }

  private void handlePlayerSplit() {
    if (round.canSplit()) {
        round.split();
        send("{\"type\":\"PLAYER_INITIAL\",\"sender\":\"DEALER\",\"payload\":{"
                + "\"hand_count\":\"2\","
                + "\"hand1_total\":\"" + round.getPlayerHands().get(0).getValue() + "\","
                + "\"hand2_total\":\"" + round.getPlayerHands().get(1).getValue() + "\""
                + "}}");
    } else {
        send("{\"type\":\"PLAYER_INITIAL\",\"sender\":\"DEALER\",\"payload\":{"
                + "\"error\":\"SPLIT_NOT_ALLOWED\""
                + "}}");
      }
  }

  private void handleDealerTurn() {
    send("{\"type\":\"DEALER_REVEAL\",\"sender\":\"DEALER\",\"payload\":{"
            + "\"card1\":\"" + round.getDealerHand().getCard(0).toString() + "\","
            + "\"card2\":\"" + round.getDealerHand().getCard(1).toString() + "\","
            + "\"total\":\"" + round.getDealerHand().getValue() + "\""
            + "}}");
    
    while(round.dealerShouldHit()) {
      Card card = round.dealerHit();
      System.out.println("Dealer hits and receives: " + card);
      send("{\"type\":\"DEALER_HIT_CARD\",\"sender\":\"DEALER\",\"payload\":{"
                + "\"card\":\"" + card.toString() + "\","
                + "\"total\":\"" + round.getDealerHand().getValue() + "\","
                + "\"bust\":\"" + round.getDealerHand().isBust() + "\""
                + "}}");
    }

    if (round.getDealerHand().isBust()) {
      send("{\"type\":\"DEALER_BUST\",\"sender\":\"DEALER\",\"payload\":{"
              + "\"total\":\"" + round.getDealerHand().getValue() + "\""
              + "}}");
    } else {
      send("{\"type\":\"ROUND_END\",\"sender\":\"DEALER\",\"payload\":{"
              + "\"dealer_total\":\"" + round.getDealerHand().getValue() + "\""
              + "}}");
    }
  }


  private void send(String response) {
        out.println(response);
  }

  private String readMessage() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);
            if (line.contains("}")) break;
        }

        if (sb.length() == 0) return null;
        return sb.toString();
  }

  public static void main(String[] args) {
    new DealerTest();
  }
}
