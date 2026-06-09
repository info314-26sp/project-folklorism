import java.util.*;
import java.io.*;
import java.net.*;
import Deck.*;

class GameServer {
  private static final int PORT = 2121;
  public static final boolean DEBUG = false;
  private ClientHandler player;
  private ClientHandler dealer;
  private Game game;

  public GameServer() {
    try (
      ServerSocket serverSocket = new ServerSocket(PORT);
    ) {
      System.out.println("Server running on port " + PORT + "\n");

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

  public synchronized boolean assignPlayer(ClientHandler player) {
    if (this.player == null) {
      this.player = player;
      System.out.println("(player) joined");
      player.sendMessage(
        "RES ASRO\n" +
        "success\n" +
        "player\n"
      );
      notifyAll();
      return true;
    }
    player.sendMessage(
      "RES ASRO\n" +
      "failure\n" +
      "Table is full\n"
    );
    return false;
  }

  public synchronized boolean assignDealer(ClientHandler dealer) {
    if (this.dealer == null) {
      this.dealer = dealer;
      System.out.println("(dealer) joined");
      dealer.sendMessage(
        "RES ASRO\n" +
        "success\n" +
        "dealer\n"
      );
      notifyAll();
      return true;
    }
    dealer.sendMessage(
      "RES ASRO\n" +
      "failure\n" +
      "Table is full\n"
    );
    return false;
  }

  public synchronized void handleGameStart() {
    // wait until both clients joined
    while (player == null || dealer == null) {
      try {
        System.out.println("waiting for clients...");
        wait();
        System.out.println("\nGAME START");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("thread interrupted");
      }
    }

    // init new game
    game = new Game();
  }

  public void handlePlayerAction(String action) {
    switch (action) {
      case "hit":
        System.out.println("(player) hit");
        dealer.sendMessage(
          "REQ DLCD\n" +
          "player\n"
        );
        break;
      case "stand":
        System.out.println("(player) stand");
        player.sendMessage(
          "RES PLAC\n" +
          "stand\n"
        );
        game.endPlayerTurn();
        handleDealerTurn();
        break;
      default:
    }
  }

  public void handleDealerCard(String recipient, String cardString) {
    Card card = new Card(cardString);

    int score;
    String message;

    switch (recipient) {
      case "player":
        System.out.println("(player) received: " + card.toString());
    
        // update player hand
        game.addPlayerCard(card);
        score = game.getPlayerScore();
        System.out.println("(player) hand: " + game.getPlayerHand());
        System.out.println("(player) total hand: " + score);

        message = 
          "RES PLAC\n" +
          cardString + "\n";

        // check if card triggers event
        if (score == 21) {
          // blackjack
          System.out.println("(player) blackjack");
          player.sendMessage(message + "blackjack\n");
          handleBlackjack();
        } else if (score > 21) {
          // bust
          System.out.println("(player) bust");
          player.sendMessage(message + "bust\n");
          handleGameOver("dealer");
        } else {
          player.sendMessage(message);
        }
        break;
      case "dealer":
        System.out.println("(dealer) received: " + card.toString());

        // update dealer hand
        game.addDealerCard(card);
        score = game.getDealerScore();
        System.out.println("(dealer) hand: " + game.getDealerHand());
        System.out.println("(dealer) total hand: " + score);

        dealer.sendMessage(
          "RES DRCD\n" +
          cardString + "\n"
        );
        break;
      default:
    }
  }

  public void handleDealerHand() {
    StringBuilder messageBuilder = new StringBuilder("RES DLHD\n");
    List<Card> hand = game.getDealerHand();

    if (game.isPlayerTurn()) {
      // only send first card if still player turn
      messageBuilder.append(hand.get(0).toMessageString() + "\n");
    } else {
      for (Card c : game.getDealerHand()) {
        messageBuilder.append(c.toMessageString() + "\n");
      }
    }
    player.sendMessage(messageBuilder.toString());
  }

  private void handleDealerTurn() {
    System.out.println("DEALER TURN");
    String message =
      "REQ DLTN\n"
      + game.getDealerScore() + "\n";

    // alert clients of dealer's turn
    player.sendMessage(message);
    dealer.sendMessage(message);
  }

  public synchronized void handleCompareScores() {
    // notify player that dealer is done
    player.sendMessage("REQ DLFI\n");

    try {
      wait();
    } catch (InterruptedException e) {
    }

    int dealerScore = game.getDealerScore();
    int playerScore = game.getPlayerScore();

    if (dealerScore <= 21) {
      if (21 - dealerScore > 21 - playerScore) {
        // player win
        handleGameOver("player");
      } else {
        handleGameOver("dealer");
      }
    } else {
      // dealer bust
      handleGameOver("player");
    }
  }

  public synchronized void gotFinalDealerHand() {
    notifyAll();
  }

  private void handleBlackjack() {
    int dealerScore = game.getDealerScore();

    if (dealerScore == 21) {
      handleGameOver("push");
    } else {
      handleGameOver("player");
    }
  }

  private void handleGameOver(String winner) {
    String message =
      "REQ GMFI\n" +
      winner + "\n";

    // alert clients of game over
    player.sendMessage(message);
    dealer.sendMessage(message);

    // set game over
    game.setGameOver();

    // clear clients
    player = null;
    dealer = null;

    System.out.println("GAME OVER\n");
  }

  public boolean isGameOver() {
    return game.isOver();
  }

  public boolean isPlayerTurn() {
    return game.isPlayerTurn();
  }

  public static void main(String[] args) {
    new GameServer();
  }
}
