import java.util.*;
import Deck.*;

public class Game {
  private boolean isOver = false;
  private boolean playerTurn = true;
  private List<Card> playerHand = new LinkedList<Card>();
  private List<Card> dealerHand = new LinkedList<Card>();
  private int playerScore = 0;
  private int dealerScore = 0;

  public Game() { }

  public boolean isOver() {
    return isOver;
  }

  public void setGameOver() {
    isOver = true;
  }

  public int getPlayerScore() {
    return playerScore;
  }

  public int getDealerScore() {
    return dealerScore;
  }

  public List<Card> getPlayerHand() {
    return playerHand;
  }

  public List<Card> getDealerHand() {
    return dealerHand;
  }

  public void addPlayerCard(Card card) {
    playerHand.add(card);
    playerScore += card.getValue();
  }

  public void addDealerCard(Card card) {
    dealerHand.add(card);
    dealerScore += card.getValue();
  }

  public boolean isPlayerTurn() {
    return playerTurn;
  }

  public void endPlayerTurn() {
    playerTurn = false;
  }
}
