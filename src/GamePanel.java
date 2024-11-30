import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Image rocketImage;
    private Image asteroidImage;
    private Map<String, Image> levelBackgrounds = new HashMap<>(); // Mapowanie poziom -> tło

    private int rocketX = 50;
    private int rocketY = 250;
    private final int ROCKET_WIDTH = 200;
    private final int ROCKET_HEIGHT = 100;

    private final int ASTEROID_WIDTH = 100;
    private final int ASTEROID_HEIGHT = 100;

    static int WIDTH = 1280;
    static int HEIGHT = 1024;

    static int SPEED = 10; // Rakieta szybkość
    private final int ASTEROID_SPEED = 5; // Asteroidy szybkość

    private Timer timer;

    // Menu i poziomy
    private boolean inGame = false; // Czy gra jest aktywna
    private int currentLevel = 1;  // Aktualny poziom
    private int asteroidCount = 0; // Liczba asteroid, które przeszły ekran

    // Nazwy poziomów
    private final String[] levelNames = {
            "Merkury", "Wenus", "Ziemia", "Mars", "Jowisz", "Saturn", "Uran", "Neptun"
    };

    // Sekwencje poziomów
    private List<List<Integer>> levelSequences = new ArrayList<>();
    private List<Integer> asteroidSequence = new ArrayList<>();
    private int sequenceIndex = 0;

    // Lista asteroid w grze
    private List<Asteroid> asteroids = new ArrayList<>();
    private int spawnDelay = 50; // Odstęp między asteroidami w tickach
    private int spawnTimer = 0;

    public GamePanel() {
        try {
            // Wczytywanie obrazów
            rocketImage = ImageIO.read(getClass().getClassLoader().getResource("resources/rocket.png"));
            asteroidImage = ImageIO.read(getClass().getClassLoader().getResource("resources/pixelmeteor.png"));

            // Wczytywanie teł
            for (String levelName : levelNames) {
                levelBackgrounds.put(levelName, ImageIO.read(getClass().getClassLoader().getResource("resources/" + levelName.toLowerCase() + ".png")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);

        // Definiowanie sekwencji poziomów
        levelSequences.add(List.of(0, 1, 2, 3, 4, 5, 6, 7)); // Poziom 1
        levelSequences.add(List.of(0, 2, 4, 6, 1, 3, 5, 7)); // Poziom 2
        levelSequences.add(List.of(7, 6, 5, 4, 3, 2, 1, 0)); // Poziom 3
        levelSequences.add(List.of(0, 3, 5, 1, 6, 4, 7, 2)); // Poziom 4
        levelSequences.add(List.of(0, 0, 1, 1, 2, 2, 3, 3)); // Poziom 5
        levelSequences.add(List.of(7, 7, 6, 6, 5, 5, 4, 4)); // Poziom 6
        levelSequences.add(List.of(0, 7, 1, 6, 2, 5, 3, 4)); // Poziom 7
        levelSequences.add(List.of(4, 2, 6, 0, 7, 1, 3, 5)); // Poziom 8

        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!inGame) {
            drawMenu(g);
            return;
        }

        // Rysowanie tła dla aktualnego poziomu
        g.drawImage(levelBackgrounds.get(levelNames[currentLevel - 1]), 0, 0, getWidth(), getHeight(), null);

        // Rysowanie rakiety
        g.drawImage(rocketImage, rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT, null);

        // Rysowanie asteroid
        for (Asteroid asteroid : asteroids) {
            g.drawImage(asteroidImage, asteroid.x, asteroid.y, ASTEROID_WIDTH, ASTEROID_HEIGHT, null);
        }

        // Rysowanie paska progresu
        drawProgressBar(g);

        // Rysowanie nazwy poziomu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Poziom: " + levelNames[currentLevel - 1], WIDTH - 300, 50);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Rocket Ride", WIDTH / 2 - 150, HEIGHT / 2 - 100);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        for (int i = 1; i <= 8; i++) {
            g.drawString("Level " + i + ": " + levelNames[i - 1], WIDTH / 2 - 200, HEIGHT / 2 + i * 30 - 80);
        }
    }

    private void drawProgressBar(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(50, HEIGHT - 50, WIDTH - 100, 20);

        g.setColor(Color.GREEN);
        g.fillRect(50, HEIGHT - 50, (int) ((WIDTH - 100) * (asteroidCount / 20.0)), 20);

        g.setColor(Color.WHITE);
        g.drawRect(50, HEIGHT - 50, WIDTH - 100, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!inGame) {
            repaint();
            return;
        }

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
                asteroidCount++;
                toRemove.add(asteroid);
            }
        }

        asteroids.removeAll(toRemove);

        // Sprawdzenie, czy poziom został ukończony
        if (asteroidCount >= 20) {
            asteroidCount = 0;
            nextLevel();
        }

        repaint();
    }

    private void spawnAsteroid() {
        int segmentHeight = HEIGHT / 8;
        int yPosition = asteroidSequence.get(sequenceIndex) * segmentHeight + (segmentHeight - ASTEROID_HEIGHT) / 2;

        asteroids.add(new Asteroid(WIDTH, yPosition));

        sequenceIndex = (sequenceIndex + 1) % asteroidSequence.size();
    }

    private boolean checkCollision(Asteroid asteroid) {
        Rectangle rocketBounds = new Rectangle(rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT);
        Rectangle asteroidBounds = new Rectangle(asteroid.x, asteroid.y, ASTEROID_WIDTH, ASTEROID_HEIGHT);

        return rocketBounds.intersects(asteroidBounds);
    }

    private void nextLevel() {
        currentLevel++;
        if (currentLevel > levelNames.length) {
            JOptionPane.showMessageDialog(this, "You completed the game!");
            System.exit(0);
        }
        asteroidSequence = new ArrayList<>(levelSequences.get(currentLevel - 1));
        sequenceIndex = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!inGame) {
            if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_8) {
                currentLevel = e.getKeyCode() - KeyEvent.VK_0;
                asteroidSequence = new ArrayList<>(levelSequences.get(currentLevel - 1));
                inGame = true;
            }
        } else {
            if (e.getKeyCode() == KeyEvent.VK_UP && rocketY > 0) {
                rocketY -= SPEED;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && rocketY + ROCKET_HEIGHT < getHeight()) {
                rocketY += SPEED;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private static class Asteroid {
        int x, y;

        public Asteroid(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
