# Blackjack

## Running

### General

When starting the nodes, make sure that the server is started first, otherwise the clients will not be able to connect and will exit immediately.

All nodes are written in java and should be able to be run by executing the main class for each:

```sh
java server/GameServer.java
```
```sh
java player/PlayerClient.java
```
```sh
java dealer/DealerClient.java
```

Shared packages, i.e. `Deck`, are found under the `common` directory, but are also symlinked into each node's directory to avoid the user having to worry about manually copying and compiling them.

If for some reason you are unable to run the nodes like this, copy the `Deck` directory into the same directory as the classes for each node:
```
.
├── Deck
│   ├── Card.java
│   ├── Deck.java
│   ├── Rank.java
│   └── Suit.java
├── DealerClient.java
```

If you are still unable to run, try compiling the files before running:
```sh
javac --source-path . DealerClient.java
```

### Nix

On systems with nix installed, you can use the repo's flake to build and run each node. The following commands will run each node respectively:

```sh
nix run .#server
```
```sh
nix run .#player
```
```sh
nix run .#dealer
```

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

## Testing

While implementing this system, we went through multipple different iterations. With every new step we saw available paths to make the communication protocol, the communication between the components and the logic itself a lot simpler. 

Therefore testing was very important starting with the components themselves:
- Dealer logic tests: deck initialization, shuffling, card uniqueness, hit/stand rules, dealer bust logic, Ace soft/hard handling.
- Player logic tests — hand evaluation, Ace logic, decision logic (hit/stand).
- Server state tests — game state transitions, turn ordering, win/loss evaluation, message routing logic.

The protocol also went through different testing to accunt for:
- Message format validation 
- Invalid message handling 
- Oredering gurantees (the game flow)

We also needed to test each pair individually, mainly for debugging pourposes:
- Player <-> Server: Player registration, action forwarding, result delivery
- Dealer <-> Server: Dealer registration, card request, deck managment.

Of course we also had to do Full System Tests:
- Start‑to‑finish game tests — START → initial deal → HIT/ STAND → dealer turn → RESULT.
- Multiple rounds tests — ensure state resets correctly.

To check the user experience from the teminal, we also tested for:
- Readable output tests — Player sees cards, totals, and results clearly.
- Error message clarity tests — TIMEOUT, INVALID_ACTION, DEALER_DISCONNECTED.

For overall integrity and to keep track of the game, a debug mode was included that logs everything in case future problems arise.


## Changes Since Proposal

Since the original topic proposal and protocol prototyping, there have been a lot of changes to the details of the protocol as thing started to be implemented.

First, we made the decision to switch from a json like format to a cleaner and simpler format more similar to http, with a main line determining specific actions and details in the following lines. Message names were also shortend to 4 characters so line length was more standardized (nicer to look at!) and also much nicer to write out than json.

We also made a lot of changes in which messages are included in the protocol, dropping some, such as `GAME_CONFIRM`, which became redundant when we adopted a `REQ`/`RES` based header line, which allowed the game start message to act as both intiation and confirmation.

Balancing this out, lots of message types were added as we discovered the need for them, mainly interacting with the dealer and the game lifecycle to trigger events. These include dealer hand, dealer turn, and dealer finished.
