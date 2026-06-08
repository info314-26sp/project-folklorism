package Card_And_Message_Stuff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BlackJackRound implements Serializable {
    private final Deck deck = new Deck();
    private final List<Hand> playerHands = new ArrayList<>();
    private final Hand dealerHand = new Hand();
    private int activeHandIndex = 0;

    public void startRound() {
        deck.reset();
        playerHands.clear();
        dealerHand.getCards().clear();
        activeHandIndex = 0;

        Hand player = new Hand();
        player.addCard(deck.draw());
        dealerHand.addCard(deck.draw());
        player.addCard(deck.draw());
        dealerHand.addCard(deck.draw());
        playerHands.add(player);
    }

    public Hand getCurrentPlayerHand() {
        return playerHands.get(activeHandIndex);
    }
    public List<Hand> getPlayerHands() {
        return playerHands;
    }
    public Hand getDealerHand() {
        return dealerHand;
    }

    public Card playerHit() {
        Card card = deck.draw();
        getCurrentPlayerHand().addCard(card);
        return card;
    }

    public Card dealerHit() {
        Card card = deck.draw();
        dealerHand.addCard(card);
        return card;
    }

    public boolean dealerShouldHit() {
        return dealerHand.getValue() < playerHands.stream().mapToInt(Hand::getValue).max().orElse(0) && !dealerHand.isBust();
    }

    public boolean canSplit() {
        return getCurrentPlayerHand().canSplit() && playerHands.size() == 1;
    }

    public void split() {
        if (!canSplit()) return;

        Hand original = playerHands.get(0);
        Card secondCard = original.getCards().remove(1);

        Hand second = new Hand();
        second.addCard(secondCard);

        original.addCard(deck.draw());
        second.addCard(deck.draw());

        playerHands.add(second);
        activeHandIndex = 0;
    }

    public void nextHand() {
        if (activeHandIndex < playerHands.size() - 1) {
            activeHandIndex++;
        }
    }
}
