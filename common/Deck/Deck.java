package Deck;

import java.util.*;

public class Deck {
  private List<Card> cards;

  /**
   * creates a new deck with one of each card
   */
  public Deck() {
    this.cards = new ArrayList<Card>();

    for (Suit s : Suit.values()) {
      for (Rank r : Rank.values()) {
        cards.add(new Card(r, s));
      }
    }
  }

  /**
   * draws a random card from the deck
   */
  public Card drawCard() {
    Random random = new Random();

    return cards.remove(random.nextInt(cards.size()));
  }
}
