# Pathfinding Snake (Java)

A Snake game built in Java with Swing. It can be played manually or left on autopilot, where the snake uses breadth-first search (BFS) to find the shortest path to the food while avoiding walls and its own body.

## Features

- Timer-driven game loop with custom rendering (`paintComponent`)
- Keyboard controls using `KeyListener`
- **Autopilot mode:** BFS on the 30x30 grid finds the shortest safe route to the food
- Wall and self-collision detection with game over and restart
- Score and mode shown on screen

## Controls

| Key | Action |
| --- | --- |
| Arrow keys | Steer (manual mode) |
| Space | Toggle autopilot / manual |
| R | Restart |

## Run it

Requires JDK 11 or newer.

```
javac *.java
java SnakeGame
```

## Project structure

- `SnakeGame.java`: entry point
- `GameFrame.java`: window setup
- `GamePanel.java`: game state, game loop, BFS autopilot and rendering

## How the autopilot works

Each tick, BFS starts from the snake's head and explores neighbouring cells outward, skipping walls and body segments. Each cell remembers which first move led to it, so when BFS reaches the food it returns that first move. If no path exists, the snake falls back to any move that doesn't kill it.

## Known limitations

- Plain BFS can steer the snake into a dead end as it grows, so the autopilot still dies sometimes.
- Possible improvement: after choosing a path, check the snake can still reach its tail, or pick the move with the most open space when no path exists.
