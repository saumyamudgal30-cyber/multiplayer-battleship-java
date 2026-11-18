# Multiplayer Battleship (Simplified) - Java

A two-player, turn-based Battleship game that runs in the terminal. Two players connect to a central **server** over TCP sockets; the server acts as the referee and keeps both boards.

Built for the Java Section C course project (Project 16) by **Saumya Mudgal**, B.Tech CSE, Banasthali Vidyapith.

## Features
- Two players connect through a server (`ServerSocket` / `Socket`)
- Each player has an 8x8 board; the opponent's ships stay hidden
- Ship placement (manual `row col H/V` or random `R`) with validation
- Turn management (server alternates players)
- Hit / miss detection
- Board synchronization (both players get fresh boards after every move)
- Score (hits per game, wins per session)
- Winner detection and final board reveal
- Rematch (both players must agree)

## Project structure
| File | Role |
|---|---|
| `Server.java` | Referee: accepts players, runs the game loop, turns, win check, rematch |
| `Client.java` | Thin terminal: prints server messages, sends keyboard input when asked |
| `Board.java` | One player's grid: placement, attack, win check, rendering |
| `Player.java` | A connected player: socket streams, board, score |

# How it works
The server sends plain text lines to each client. When the server sends the special line `INPUT`, the client reads a line from the keyboard and sends it back. All game rules live on the server, so a client cannot cheat.

# Requirements
- JDK 8 or newer (`javac -version`)

## How to run
```bash
javac *.java

# Terminal 1
java Server

# Terminal 2 and Terminal 3 (one per player)
java Client
# On another computer: java Client <server-ip>
```

## Playing
- Placement: type `row col H` or `row col V` (example `2 3 H`), or `R` for a random spot. Ship lengths: 4, 3, 2.
- Attack: type `row col` (example `3 4`). Rows and columns are 0-7.
- Symbols: `~` water, `S` your ship, `X` hit, `O` miss.

