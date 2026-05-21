# Blackjack

## Message Reference

### TMO

This message represents a timeout, and can be sent by any component following a message that expects a response, but has not been given one within a designated time period. The timed-out message is repeated within the timeout message following the fields.

Fields:
- timeout-count: int

Expected Response:
- inherited from contained message

Side effects:
- Previous instances of timed-out message are ignored in favor of most recent

Example:
```
TMO
timout-count: 3
CGA
```

### FIN

This message is sent from the server to the dealer and player clients, signaling the end of the game, and indicating the winner.

Fields:
- winner: string (one of "player" | "dealer")

Side effects:
- The game is concluded and the connection is closed

Example:
```
FIN
winner: player
```
