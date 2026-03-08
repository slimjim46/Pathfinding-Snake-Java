import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    
    // Snake position
    private int[] snakeX = new int[10000];
    private int[] snakeY = new int[10000];
    private int squareSize = 20;
    private int snakeLength = 3;

    private boolean autoPilot = true; 

    private int foodX;
    private int foodY;
    private int foodSize = 20;
    
    // Movement direction
    private int xVelocity = 0;  // Change in X per move
    private int yVelocity = 0;  // Change in Y per move
    private int SPEED = 20;
    
    // Timer for game loop
    private Timer timer;
    
    public GamePanel() {
        this.setPreferredSize(new Dimension(600, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        
        // Start the game loop (moves every 100ms)
        timer = new Timer(10, this);
        timer.start();

        for(int i = 0; i < snakeLength; ++i){
            snakeX[i] = 100 - (i * squareSize);
            snakeY[i] = 100;
        }

        placeFood();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw the snake (just a square for now)
        for (int i = 0; i < snakeLength; i++) {
            if (i == 0) {
                g.setColor(Color.GREEN);  // Head is bright green
            } else {
                g.setColor(new Color(45, 180, 0));  // Body is darker green
            }
            g.fillRect(snakeX[i], snakeY[i], squareSize, squareSize);
        }

        g.setColor(Color.RED);
        g.fillRect(foodX, foodY, foodSize, foodSize);
        
        // Draw instructions
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Use arrow keys to move", 20, 30);
    }
    
    // This runs every time the timer ticks (every 100ms)
    @Override
    public void actionPerformed(ActionEvent e) {
        if (autoPilot) {
            autoPilotMove();  // Let computer decide direction
        }
        // Update position based on velocity
        for (int i = snakeLength; i > 0; i--){
            snakeX[i] = snakeX[i-1];
            snakeY[i] = snakeY[i-1];
        }
        
        snakeX[0] += xVelocity;
        snakeY[0] += yVelocity;

        // Ask the panel to redraw itself
        checkFood();
        repaint();
    }
    

    public void placeFood(){
        foodX = (int)(Math.random() * (500 / foodSize)) * foodSize;
        foodY = (int)(Math.random() * (500 / foodSize)) * foodSize;
    }

    public void checkFood() {
        // Check if snake head overlaps with food
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            snakeLength++;  // Make snake longer
            
            // Move food to new random location
            placeFood();
            
            System.out.println("Snake length: " + snakeLength);
        }
    }

    public void autoPilotMove() {
        // Find direction to food
        if (snakeX[0] < foodX) {
            // Food is to the right
            xVelocity = squareSize;
            yVelocity = 0;
        } else if (snakeX[0] > foodX) {
            // Food is to the left
            xVelocity = -squareSize;
            yVelocity = 0;
        } else if (snakeY[0] < foodY) {
            // Food is below (Y increases going down)
            xVelocity = 0;
            yVelocity = squareSize;
        } else if (snakeY[0] > foodY) {
            // Food is above
            xVelocity = 0;
            yVelocity = -squareSize;
        }
    }
    
    // Called when a key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            xVelocity = -SPEED;
            yVelocity = 0;
        } else if (key == KeyEvent.VK_RIGHT) {
            xVelocity = SPEED;
            yVelocity = 0;
        } else if (key == KeyEvent.VK_UP) {
            xVelocity = 0;
            yVelocity = -SPEED;
        } else if (key == KeyEvent.VK_DOWN) {
            xVelocity = 0;
            yVelocity = SPEED;
        }
    }
    
    // Required by KeyListener (not used)
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
}