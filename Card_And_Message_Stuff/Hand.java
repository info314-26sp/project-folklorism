package Card_And_Message_Stuff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public int getValue() {
        int value = 0;
        int aceCount = 0;

        for (Card card : cards) {
            value += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        while (value > 21 && aceCount > 0) {
            value -= 10; 
            aceCount--;
        }

        return value;
    }

        public boolean isBust() {
        return getValue() > 21;
    }

    public boolean isBlackjack() {
        return size() == 2 && getValue() == 21;
    }

    public boolean canSplit() {
        return size() == 2 && cards.get(0).getValue() == cards.get(1).getValue();
    }
}

