import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // --- Board settings (everything is stored in grid cells, not pixels) ---
    private static final int PANEL_SIZE = 600;
    private static final int SQUARE_SIZE = 20;
    private static final int GRID_SIZE = PANEL_SIZE / SQUARE_SIZE; // 30 x 30 cells
    private static final int DELAY_MS = 80;

    // Four possible moves: right, left, down, up
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Snake body: index 0 is the head, snakeLength - 1 is the tail
    private final int[] snakeX = new int[GRID_SIZE * GRID_SIZE];
    private final int[] snakeY = new int[GRID_SIZE * GRID_SIZE];
    private int snakeLength;

    private int foodX;
    private int foodY;

    // Current direction in cells per tick
    private int xVelocity;
    private int yVelocity;

    private boolean autoPilot = true;
    private boolean gameOver = false;

    private final Timer timer;

    public GamePanel() {
        this.setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        resetGame();

        timer = new Timer(DELAY_MS, this);
        timer.start();
    }

    private void resetGame() {
        snakeLength = 3;
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = 5 - i;
            snakeY[i] = 5;
        }
        xVelocity = 1;
        yVelocity = 0;
        gameOver = false;
        placeFood();
    }

    // ------------------------------------------------------------------
    // Game loop
    // ------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (autoPilot) {
                autoPilotMove();
            }
            move();
        }
        repaint();
    }

    private void move() {
        int newX = snakeX[0] + xVelocity;
        int newY = snakeY[0] + yVelocity;

        if (hitsWall(newX, newY) || hitsBody(newX, newY)) {
            gameOver = true;
            return;
        }

        boolean ateFood = (newX == foodX && newY == foodY);
        if (ateFood) {
            snakeLength++;
        }

        // Shift every segment to the position of the one in front of it
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        snakeX[0] = newX;
        snakeY[0] = newY;

        if (ateFood) {
            placeFood();
        }
    }

    private boolean hitsWall(int x, int y) {
        return x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE;
    }

    // The tail is excluded because it moves out of the way on the same tick
    private boolean hitsBody(int x, int y) {
        for (int i = 0; i < snakeLength - 1; i++) {
            if (snakeX[i] == x && snakeY[i] == y) {
                return true;
            }
        }
        return false;
    }

    private void placeFood() {
        if (snakeLength >= GRID_SIZE * GRID_SIZE) {
            gameOver = true; // the snake fills the whole board
            return;
        }
        do {
            foodX = (int) (Math.random() * GRID_SIZE);
            foodY = (int) (Math.random() * GRID_SIZE);
        } while (isOnSnake(foodX, foodY));
    }

    private boolean isOnSnake(int x, int y) {
        for (int i = 0; i < snakeLength; i++) {
            if (snakeX[i] == x && snakeY[i] == y) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Autopilot: breadth-first search (BFS) on the grid
    // ------------------------------------------------------------------

    private void autoPilotMove() {
        int[] direction = bfsFirstStep();
        if (direction == null) {
            direction = anySafeStep(); // no route to the food: just stay alive
        }
        if (direction != null) {
            xVelocity = direction[0];
            yVelocity = direction[1];
        }
    }

    /**
     * Finds the shortest path from the head to the food that avoids walls and
     * the snake's body, and returns only the first step of that path.
     * Returns null if the food cannot be reached.
     */
    private int[] bfsFirstStep() {
        boolean[][] blocked = new boolean[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < snakeLength - 1; i++) { // tail will have moved by the time we get there
            blocked[snakeX[i]][snakeY[i]] = true;
        }

        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        int[][] firstStep = new int[GRID_SIZE][GRID_SIZE]; // which first move led to each cell
        Queue<int[]> queue = new ArrayDeque<>();

        visited[snakeX[0]][snakeY[0]] = true;

        // Seed the search with the head's neighbours, remembering the first move
        for (int d = 0; d < DIRECTIONS.length; d++) {
            int nx = snakeX[0] + DIRECTIONS[d][0];
            int ny = snakeY[0] + DIRECTIONS[d][1];
            if (isFree(nx, ny, blocked, visited)) {
                visited[nx][ny] = true;
                firstStep[nx][ny] = d;
                queue.add(new int[]{nx, ny});
            }
        }

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            if (cell[0] == foodX && cell[1] == foodY) {
                return DIRECTIONS[firstStep[cell[0]][cell[1]]];
            }
            for (int[] dir : DIRECTIONS) {
                int nx = cell[0] + dir[0];
                int ny = cell[1] + dir[1];
                if (isFree(nx, ny, blocked, visited)) {
                    visited[nx][ny] = true;
                    firstStep[nx][ny] = firstStep[cell[0]][cell[1]];
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return null;
    }

    private boolean isFree(int x, int y, boolean[][] blocked, boolean[][] visited) {
        return !hitsWall(x, y) && !blocked[x][y] && !visited[x][y];
    }

    // Fallback when BFS finds no path: take any move that doesn't kill the snake
    private int[] anySafeStep() {
        for (int[] dir : DIRECTIONS) {
            int nx = snakeX[0] + dir[0];
            int ny = snakeY[0] + dir[1];
            if (!hitsWall(nx, ny) && !hitsBody(nx, ny)) {
                return dir;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < snakeLength; i++) {
            g.setColor(i == 0 ? Color.GREEN : new Color(45, 180, 0));
            g.fillRect(snakeX[i] * SQUARE_SIZE, snakeY[i] * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }

        g.setColor(Color.RED);
        g.fillRect(foodX * SQUARE_SIZE, foodY * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Score: " + (snakeLength - 3)
                + "   Mode: " + (autoPilot ? "Autopilot (BFS)" : "Manual"), 10, 20);
        g.drawString("SPACE: toggle autopilot   Arrows: steer   R: restart", 10, PANEL_SIZE - 10);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over - press R", 110, PANEL_SIZE / 2);
        }
    }

    // ------------------------------------------------------------------
    // Keyboard input
    // ------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_R) {
            resetGame();
        } else if (key == KeyEvent.VK_SPACE) {
            autoPilot = !autoPilot;
        } else if (!autoPilot) {
            // Ignore a 180-degree turn, which would run the snake into its own neck
            if (key == KeyEvent.VK_LEFT && xVelocity != 1) {
                xVelocity = -1;
                yVelocity = 0;
            } else if (key == KeyEvent.VK_RIGHT && xVelocity != -1) {
                xVelocity = 1;
                yVelocity = 0;
            } else if (key == KeyEvent.VK_UP && yVelocity != 1) {
                xVelocity = 0;
                yVelocity = -1;
            } else if (key == KeyEvent.VK_DOWN && yVelocity != -1) {
                xVelocity = 0;
                yVelocity = 1;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}