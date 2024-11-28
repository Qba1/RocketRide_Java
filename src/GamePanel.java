import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Image background;
    private Image rocketImage;
    private Image asteroidImage;

    private int rocketX = 50;
    private int rocketY = 250;
    private final int ROCKET_WIDTH = 200;
    private final int ROCKET_HEIGHT = 100;

    private final int ASTEROID_WIDTH = 100;
    private final int ASTEROID_HEIGHT = 100;

    static int WIDTH = 1280;
    static int HEIGHT = 1024;

    static int SPEED = 10; // Rakieta szybkość
    private final int ASTEROID_SPEED = 15; // Asteroidy szybkość

    private Timer timer;

    // Sekwencja asteroid
    private List<Integer> asteroidSequence = new ArrayList<>();
    private int sequenceIndex = 0;

    // Lista asteroid w grze
    private List<Asteroid> asteroids = new ArrayList<>();
    private int spawnDelay = 25; // Odstęp między asteroidami w tickach
    private int spawnTimer = 0;

    public GamePanel() {
        try {
            // Wczytywanie obrazów
            background = ImageIO.read(getClass().getClassLoader().getResource("resources/pixelearth.png"));
            rocketImage = ImageIO.read(getClass().getClassLoader().getResource("resources/rocket.png"));
            asteroidImage = ImageIO.read(getClass().getClassLoader().getResource("resources/pixelmeteor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);

        // Tworzenie sekwencji asteroid (0-7 dla miejsc wylotowych)
        asteroidSequence.add(0);
        asteroidSequence.add(2);
        asteroidSequence.add(5);
        asteroidSequence.add(3);
        asteroidSequence.add(7);

        // Inicjalizacja timera
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Rysowanie tła
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        // Rysowanie rakiety
        g.drawImage(rocketImage, rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT, null);

        // Rysowanie asteroid
        for (Asteroid asteroid : asteroids) {
            g.drawImage(asteroidImage, asteroid.x, asteroid.y, ASTEROID_WIDTH, ASTEROID_HEIGHT, null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Obsługa spawnowania asteroid
        spawnTimer++;
        if (spawnTimer >= spawnDelay) {
            spawnTimer = 0;
            spawnAsteroid();
        }

        // Aktualizacja pozycji asteroid
        List<Asteroid> toRemove = new ArrayList<>();
        for (Asteroid asteroid : asteroids) {
            asteroid.x -= ASTEROID_SPEED;

            // Sprawdzanie kolizji
            if (checkCollision(asteroid)) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Game Over!");
                System.exit(0);
            }

            // Usuwanie asteroidy, gdy wyleci poza ekran
            if (asteroid.x + ASTEROID_WIDTH < 0) {
                toRemove.add(asteroid);
            }
        }

        asteroids.removeAll(toRemove);
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && rocketY > 0) {
            rocketY -= SPEED;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && rocketY + ROCKET_HEIGHT < getHeight()) {
            rocketY += SPEED;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Spawnowanie nowej asteroidy na podstawie sekwencji.
     */
    private void spawnAsteroid() {
        int segmentHeight = HEIGHT / 8;
        int yPosition = asteroidSequence.get(sequenceIndex) * segmentHeight + (segmentHeight - ASTEROID_HEIGHT) / 2;

        asteroids.add(new Asteroid(WIDTH, yPosition));

        sequenceIndex = (sequenceIndex + 1) % asteroidSequence.size();
    }

    /**
     * Sprawdza kolizję między rakietą a asteroidą.
     */
    private boolean checkCollision(Asteroid asteroid) {
        Rectangle rocketBounds = new Rectangle(rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT);
        Rectangle asteroidBounds = new Rectangle(asteroid.x, asteroid.y, ASTEROID_WIDTH, ASTEROID_HEIGHT);

        return rocketBounds.intersects(asteroidBounds);
    }

    /**
     * Klasa reprezentująca asteroidę.
     */
    private static class Asteroid {
        int x, y;

        public Asteroid(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
