package Deck;

public class Card {
  private final Rank rank;
  private final Suit suit;

  public Card(Rank rank, Suit suit) {
    this.rank = rank;
    this.suit = suit;
  }

  public Card(String messageString) {
    this.rank = Rank.fromAbbreviation(messageString.split("-")[0]);
    this.suit = Suit.valueOf(messageString.split("-")[1]);
  }

  public String toMessageString() {
    return rank.abbreviation + "-" + suit;
  }

  @Override
  public String toString() {
    return rank + " of " + suit.name;
  }

  public int getValue() {
    return rank.value;
  }
}
