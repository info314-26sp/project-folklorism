package Deck;

public enum Rank {
  one(1, "1"),
  two(2, "2"),
  three(3, "2"),
  four(4, "4"),
  five(5, "5"),
  six(6, "6"),
  seven(7, "7"),
  eight(8, "8"),
  nine(9, "9"),
  ten(10, "x"),
  jack(10, "j"),
  queen(10, "q"),
  king(10, "k"),
  ace(11, "a"),
  ;

  public final int value;
  public final String abbreviation;

  private Rank(int value, String abbreviation) {
    this.value = value;
    this. abbreviation = abbreviation;
  }

  public static Rank fromAbbreviation(String abbreviation) {
    for (Rank r : values()) {
      if (java.util.Objects.equals(r.abbreviation, abbreviation)) {
        return r;
      }
    }
    return null;
  }
}
