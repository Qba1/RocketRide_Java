import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Image background;
    private Image rocketImage;
    private Image asteroidImage;

    private int rocketX = 50;
    private int rocketY = 250;
    private final int ROCKET_WIDTH = 50;
    private final int ROCKET_HEIGHT = 100;

    private int asteroidX = 750;
    private int asteroidY = 200;
    private final int ASTEROID_WIDTH = 100;
    private final int ASTEROID_HEIGHT = 100;

    private Timer timer;

    public GamePanel() {
        try {


            // Load images correctly
            background = ImageIO.read(getClass().getClassLoader().getResource("\\resources\\pixelearth.png"));
            rocketImage = ImageIO.read(getClass().getClassLoader().getResource("\\resources\\rocket.png"));
            asteroidImage = ImageIO.read(getClass().getClassLoader().getResource("\\resources\\pixelmeteor.png"));
        } catch (IOException e) {
            e.printStackTrace();  // Print stack trace if an image is not found
        }

        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);

        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background, rocket, and asteroid
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(rocketImage, rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT, null);
        g.drawImage(asteroidImage, asteroidX, asteroidY, ASTEROID_WIDTH, ASTEROID_HEIGHT, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        asteroidX -= 5;

        if (asteroidX + ASTEROID_WIDTH < 0) {
            asteroidX = getWidth();
            asteroidY = (int) (Math.random() * (getHeight() - ASTEROID_HEIGHT));
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 10;

        if (e.getKeyCode() == KeyEvent.VK_UP && rocketY > 0) {
            rocketY -= speed;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && rocketY + ROCKET_HEIGHT < getHeight()) {
            rocketY += speed;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
