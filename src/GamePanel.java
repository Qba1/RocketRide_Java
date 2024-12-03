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
    private Map<String, Image> levelBackgrounds = new HashMap<>();

    private int rocketX = 0;
    private int rocketY = 600;
    private final int ROCKET_WIDTH = 200;
    private final int ROCKET_HEIGHT = 100;

    private final int ASTEROID_WIDTH = 100;
    private final int ASTEROID_HEIGHT = 100;

    static int WIDTH = 1280;
    static int HEIGHT = 1024;

    static int SPEED = 15;
    private int asteroidSpeed = 15;

    private Timer timer;

    private boolean inGame = false;
    private int currentLevel = 1;
    private int asteroidCount = 0;

    private final String[] levelNames = {
            "Merkury", "Wenus", "Ziemia", "Mars", "Jowisz", "Saturn", "Uran", "Neptun"
    };

    private List<List<Integer>> levelSequences = new ArrayList<>();
    private List<Integer> asteroidSequence = new ArrayList<>();
    private int sequenceIndex = 0;

    private List<Asteroid> asteroids = new ArrayList<>();
    private int spawnDelay = 25;
    private int spawnTimer = 0;

    private final int[] asteroidsPerLevel = {10, 14, 18, 22, 26, 30, 34, 38};

    public GamePanel() {
        try {
            rocketImage = ImageIO.read(getClass().getClassLoader().getResource("resources/rocket.png"));
            asteroidImage = ImageIO.read(getClass().getClassLoader().getResource("resources/pixelmeteor.png"));

            for (String levelName : levelNames) {
                levelBackgrounds.put(levelName, ImageIO.read(getClass().getClassLoader().getResource("resources/" + levelName.toLowerCase() + ".png")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);

        levelSequences.add(List.of(1, 2, 1, 7, 1, 4, 2, 1));
        levelSequences.add(List.of(0, 2, 4, 6, 1, 3, 5, 7));
        levelSequences.add(List.of(7, 1, 5, 4, 1, 5, 1, 0));
        levelSequences.add(List.of(0, 3, 5, 1, 6, 4, 7, 2));
        levelSequences.add(List.of(0, 0, 1, 1, 2, 2, 3, 3));
        levelSequences.add(List.of(7, 7, 6, 6, 5, 5, 4, 4));
        levelSequences.add(List.of(0, 7, 1, 6, 2, 5, 3, 4));
        levelSequences.add(List.of(4, 2, 6, 0, 7, 1, 3, 5));

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

        g.drawImage(levelBackgrounds.get(levelNames[currentLevel - 1]), 0, 0, getWidth(), getHeight(), null);
        g.drawImage(rocketImage, rocketX, rocketY, ROCKET_WIDTH, ROCKET_HEIGHT, null);

        for (Asteroid asteroid : asteroids) {
            g.drawImage(asteroidImage, asteroid.x, asteroid.y, ASTEROID_WIDTH, ASTEROID_HEIGHT, null);
        }

        drawProgressBar(g);

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
        g.fillRect(50, HEIGHT - 50, (int) ((WIDTH - 100) * (asteroidCount / (float) asteroidsPerLevel[currentLevel - 1])), 20);

        g.setColor(Color.WHITE);
        g.drawRect(50, HEIGHT - 50, WIDTH - 100, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!inGame) {
            repaint();
            return;
        }

        spawnTimer++;
        if (spawnTimer >= spawnDelay) {
            spawnTimer = 0;
            spawnAsteroid();
        }

        List<Asteroid> toRemove = new ArrayList<>();
        for (Asteroid asteroid : asteroids) {
            asteroid.x -= asteroidSpeed;

            if (checkCollision(asteroid)) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Game Over!");
                System.exit(0);
            }

            if (asteroid.x + ASTEROID_WIDTH < 0) {
                asteroidCount++;
                toRemove.add(asteroid);
            }
        }

        asteroids.removeAll(toRemove);

        if (asteroidCount >= asteroidsPerLevel[currentLevel - 1]) {
            asteroidCount = 0;
            showLevelCompleteDialog();
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
        Rectangle rocketBounds = new Rectangle(rocketX, rocketY, ROCKET_WIDTH - 20, ROCKET_HEIGHT - 20);
        Rectangle asteroidBounds = new Rectangle(asteroid.x, asteroid.y, ASTEROID_WIDTH - 10, ASTEROID_HEIGHT - 10);

        return rocketBounds.intersects(asteroidBounds);
    }

    private void showLevelCompleteDialog() {
        int option = JOptionPane.showOptionDialog(
                this,
                "Gratulacje! Ukończyłeś poziom " + levelNames[currentLevel - 1] + "!\nNastępny poziom?",
                "Poziom ukończony",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"TAK", "MENU GŁÓWNE"},
                "TAK"
        );

        if (option == JOptionPane.YES_OPTION) {
            nextLevel();
        } else {
            inGame = false;
            repaint();
        }
    }

    private void nextLevel() {
        currentLevel++;
        if (currentLevel > levelNames.length) {
            JOptionPane.showMessageDialog(this, "Gratulacje! Ukończyłeś całą grę!");
            System.exit(0);
        }
        asteroidSequence = new ArrayList<>(levelSequences.get(currentLevel - 1));
        sequenceIndex = 0;
        asteroidSpeed += 5; // Przyspieszenie asteroid
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
