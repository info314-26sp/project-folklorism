# Blackjack

## Message Reference

#### GAME_CONFIRM
This is sent by the Player that confirms GAME_START and signals readiness that begins the play.
The game start is sent by the server to the Player and Dealer, which initates a new session/game.

Fields: 
- game_id: string (game session ID from GAME_START)
- player_id: string (player ID to verify ID)
- timestamp: string (server time when game starts)

Response: 
- Player: GAME_CONFIRM
- Dealer: DECK_READY

Side Effects:
- server initializes game state
- hands need to be empty to start

#### GAME_CONFIRM
This is sent by the Player that confirms GAME_START and signals readiness that begins the play.

Fields: 
- game_id: string (ID's game session)
- player_id: string (ID's player in session)
- ready: boolean (needs to be true and false is ERROR)

Response: 
- Server: ACK
- when GAME_CONFIRM and DECK_READY are recieved, Server will get DEAL_CARDS

Side Effects:
- Server marks Player as READY
- Game doesn't proceed to deal until Player and Dealer confirms

#### HAND_UPDATE_PLAYER
Delivers current hand state of Player, omit hidden dealer cards
Fields: 
- game_id: string (ID's game session)
- score: int (best score for visible cards only)
- owner: string (player or dealer with updates describes)
Response: 
- Server: ACK

Side Effects:
- Player updates the local display
- Server logs player is notified

### Player_Action
Description: Player chooses Hit | Stand | Split
Sender: Player
Fields:
Required:
  action (“Hit | Stand | Split”)
Optional
  hand_id()
Expected response:
  If Hit: Server - Dealers_Card
  If Stand: Server - Dealer - Dealers_Card | Game_Result
  If Split: Server - Dealer - new cards
Side effect:
  Server validates action and updates game state. 
Example:
```
{
  "type": "PLAYER_ACTION",
  "msg_id": "103",
  "sender": "PLAYER",
  "payload": {
     "action": "HIT"
  }
}
```

### Dealers_Card
Description: Dealer provides a new card to either themselves or the player
Sender: Dealer
Fields:
Required:
  target(“Dealer | Player”)
  card(“String”)
  new_total(“int”)
  is_bust(“true | false”)
  is_ blackjack(“true | false”)
Expected response:
  If target = Player: Server - Updating_Hand_To_Player
	  If Bust | Blackjack: Server - Game_Result’
	  Else: Server - Player_Action
	
  If target = Dealer: Server - Game_Result
Side effect:
  Servers updates totals 
Example:
```
{
  "type": "DEALER_CARD",
  "msg_id": "104",
  "sender": "DEALER",
  "payload": {
    "target": "PLAYER",
    "card": "5S",
    "new_total": 20,
    "is_bust": false,
    "is_blackjack": false
  }
}
```
### Error
Description: In case of invalid arguments, 
Sender: Any
Fields:
Required:
  msg_id()
  error_code(4xx | 5xx)
  error_mgs()
Expected response:
  None
Side effect:
  Prevents violations
```
Example:
{
  "type": "ERROR",
  "msg_id": "105",
  "sender": "GAME_SERVER",
  "payload": {
    "original_msg_id": "103",
    "error_code": "ILLEGAL_ACTION",
    “error_message": "Cannot HIT after bust."
  }
}
```
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
