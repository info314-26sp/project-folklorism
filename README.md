# Blackjack

## Message Reference

Messages between nodes take the form of a newline separated plaintext strings.

The first line contains a 3 letter `REQ`/`RES` indicator distinguishing requests versus responses, followed by a 4 letter action as described below.

Optionally, the message can contain information in the body on following new lines as specified for each action.

### ASRO - Assign Role

Used to assign a client to a game role. The client sends a request to the server, which then attempts to give it the requested role and responds with success or failure.

#### Fields

- String role (`"player"`|`"dealer"`)

#### Responses

- Success:
  ```
  RES ASRO
  success
  {role}
  ```
- Failure:
  ```
  RES ASRO
  failure
  {reason}
  ```

#### Side Effects

- Server assigns the client to a role
- Server checks if roles are filled and game is ready to play

#### Example

```
REQ ASRO
player
```

### GMST - Game Start

Once both clients have joined, the server sends a message to both clients letting them know it is now okay to start the game.

#### Response

```
RES GMST
```

#### Side Effects
- Clients will stop waiting begin their game loop

#### Example

```
REQ GMST
```

### PLAC - Player Action

Represents the action a player takes on their turn. Sent by the player to the server.

#### Fields

- String action (`"hit"`|`"stand"`)
- (response) String card (`"{rank}-{suit}"`)
- (response) optional - String event (`"blackjack"`|`"bust"`)

#### Response

- Hit
  ```
  RES PLAC
  {card}
  {event?}
  ```
- Stand
  ```
  RES PLAC
  stand
  ```

#### Side Effects

- Hit
  - The server requests the dealer to deal a card for the player
  - If the card results in a blackjack or bust don't prompt the player for action and instead wait for the gameover message
- Stand
  - Stop prompting the player for action and reveal the dealer's hidden card
  - Initiate the dealer's turn

#### Example

```
REQ PLAC
hit
```

### DRCD - Draw Card

Represents the dealer dealing themselves a card, i.e. for the initial two cards dealt, or during the dealer's turn once the player turn has ended.

#### Fields

- (response) String card (`"{rank}-{suit}"`)

#### Response

```
RES DRCD
{card}
```

#### Side Effects

- The server requests the dealer to deal a card for themselves

#### Example

```
REQ DRCD
```

### DLCD - Deal Card

Represents a request to the dealer to be dealt a card. Can be sent by the server to the dealer on behalf of the player when they 'hit' or on behalf of the dealer when they deal themselves a card to keep track of their hand.

#### Fields

- String recipient (`"player"`|`"dealer"`)
- (response) String card (`"{rank}-{suit}"`)

#### Response

```
RES DLCD
{recipient}
{card}
```

#### Side Effects

- The dealer deals a random card from the deck
- The server checks whether the card causes a blackjack or bust before passing it to the recipient

#### Example

```
REQ DLCD
player
```

### DLHD - Dealer Hand

Sent from the player to the server to view the dealer's current hand

#### Fields

- (response) String card (`"{rank}-{suit}"`)

#### Response

```
RES DLHD
{card}
```

#### Side Effects

- Player is displayed with the dealer's current hand, minus the hole card if it is still the player's turn

#### Example

```
REQ DLHD
```

### DLTN - Dealer Turn

Sent by the server to both clients once the player stands to indicate it is now the dealer's turn

#### Fields

- int dealerScore

#### Side Effects

- Dealer
  - Will hit until thier score is at least 17
- Player
  - Will stop prompting for action input and instead fetch dealer hand

#### Example

```
REQ DLTN
13
```

### DLFI - Dealer Finished

Indicates that the dealer is finished performing additional hits on their turn. Sent by the dealer to the server to inform server, and also by the server to the player so the player knows to stop waiting.

#### Response

```
RES DLFI
```

#### Side Effects

- Server
  - Sends DLFI request to player
  - Initiates final comparison of scores
- Player
  - Fetches and displays final dealer hand

#### Example

```
REQ DLFI
```

### GMFI - Game Final

Sent by the server to indicate that the game has ended

#### Fields

- String winner (`"player"`|`"dealer"`)

#### Response

```
RES GMFI
```

#### Side Effects

- Clients will end their game loop, close their connection, and exit
- The server will end its game loop and exit

#### Example

```
REQ GMFI
player
```
