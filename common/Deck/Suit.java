package Deck;

public enum Suit {
  c("clubs"),
  h("hearts"),
  s("spades"),
  d("diamonds"),
  ;

  public final String name;

  private Suit(String name) {
    this.name = name;
  }
}
