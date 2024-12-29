import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Image rocketImage;
    private Image asteroidImage;
    private Image menuBackground;
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
    private int lives = 3;
    private boolean quizUsed = false;

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
    private final int[] asteroidSpeedsPerLevel = {10, 12, 14, 16, 18, 20, 22, 24};

    private final Map<String, String[]> quizQuestions = Map.of(
            "Kto był pierwszym człowiekiem w kosmosie?",
            new String[]{"Yuri Gagarin", "Neil Armstrong", "Buzz Aldrin", "John Glenn"},

            "Która planeta jest największa w Układzie Słonecznym?",
            new String[]{"Jowisz", "Saturn", "Ziemia", "Mars"},

            "Jak nazywa się pierwszy sztuczny satelita Ziemi?",
            new String[]{"Sputnik 1", "Voyager 1", "Hubble", "Luna 2"}
    );

    private final Map<String, Integer> correctAnswers = Map.of(
            "Kto był pierwszym człowiekiem w kosmosie?", 0,
            "Która planeta jest największa w Układzie Słonecznym?", 0,
            "Jak nazywa się pierwszy sztuczny satelita Ziemi?", 0
    );

    private final Random random = new Random();

    public GamePanel() {
        try {
            rocketImage = ImageIO.read(getClass().getClassLoader().getResource("resources/rocket.png"));
            asteroidImage = ImageIO.read(getClass().getClassLoader().getResource("resources/pixelmeteor.png"));
            menuBackground = ImageIO.read(getClass().getClassLoader().getResource("resources/background.png"));

            for (String levelName : levelNames) {
                levelBackgrounds.put(levelName, ImageIO.read(getClass().getClassLoader().getResource("resources/" + levelName.toLowerCase() + ".png")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);

        levelSequences.add(List.of(1, 2, 1, 7, 1, 4, 2, 1,3,6));
        levelSequences.add(List.of(0, 2, 4, 6, 1, 3, 5, 7));
        levelSequences.add(List.of(7, 1, 5, 4, 1, 5, 1, 0));
        levelSequences.add(List.of(0, 3, 5, 1, 6, 4, 7, 2));
        levelSequences.add(List.of(0, 0, 1, 1, 2, 2, 3, 3));
        levelSequences.add(List.of(7, 7, 6, 6, 5, 5, 4, 4));
        levelSequences.add(List.of(0, 7, 1, 6, 2, 5, 3, 4));
        levelSequences.add(List.of(4, 2, 6, 0, 7, 1, 3, 5,1,2,3,5,6,7,8,1,2));

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
        g.drawString("Życia: " + lives, 50, 50);
    }

    private void drawMenu(Graphics g) {
        // Narysuj tło
        g.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);

        // Kolor tekstu i czcionka dla tytułu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 72));

        // Wyśrodkuj tytuł
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth("Rocket Ride");
        int titleHeight = fmTitle.getHeight();

        // Narysuj obramowanie wokół tytułu
        g.setColor(new Color(100, 150, 255, 200)); // Półprzezroczysty niebieski
        g.fillRoundRect((WIDTH - titleWidth) / 2 - 20, HEIGHT / 2 - 150 - titleHeight + 10,
                titleWidth + 40, titleHeight + 20, 20, 20);

        // Obramowanie
        g.setColor(new Color(255, 255, 255, 200)); // Białe, lekko przezroczyste
        g.drawRoundRect((WIDTH - titleWidth) / 2 - 20, HEIGHT / 2 - 150 - titleHeight + 10,
                titleWidth + 40, titleHeight + 20, 20, 20);

        // Narysuj tytuł
        g.setColor(Color.WHITE);
        g.drawString("Rocket Ride", (WIDTH - titleWidth) / 2, HEIGHT / 2 - 150);

        // Ustawienia czcionki dla poziomów
        g.setFont(new Font("Arial", Font.PLAIN, 36));

        // Ulepszone wyświetlanie poziomów
        int startY = HEIGHT / 2;
        for (int i = 1; i <= 8; i++) {
            String levelText = String.format("Level %d: %s", i, levelNames[i - 1]);

            // Oblicz szerokość tekstu, aby wyśrodkować
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(levelText);
            int textHeight = fm.getHeight();

            // Dodaj obramowanie dla poziomów
            g.setColor(new Color(100, 150, 255, 220)); // Półprzezroczysty niebieski
            g.fillRoundRect((WIDTH - textWidth) / 2 - 10, startY + (i - 1) * 50 - textHeight + 10,
                    textWidth + 20, textHeight + 10, 15, 15);

            // Biała obwódka
            g.setColor(new Color(255, 255, 255, 200)); // Białe, lekko przezroczyste
            g.drawRoundRect((WIDTH - textWidth) / 2 - 10, startY + (i - 1) * 50 - textHeight + 10,
                    textWidth + 20, textHeight + 10, 15, 15);

            // Kolor tekstu poziomów
//            if (i <= unlockedLevels) {
//                g.setColor(new Color(50, 50, 150)); // Ciemnoniebieski kolor tekstu
//            } else {
//                g.setColor(new Color(100, 100, 100)); // Szary kolor dla zablokowanych poziomów
//            }

            g.drawString(levelText, (WIDTH - textWidth) / 2, startY + (i - 1) * 50);
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
                lives--;
                toRemove.add(asteroid);
                if (lives == 0 && !quizUsed) {
                    showQuiz();
                } else if (lives == 0) {
                    gameOver();
                }
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

    private void showQuiz() {
        quizUsed = true;
        String question = new ArrayList<>(quizQuestions.keySet()).get(random.nextInt(quizQuestions.size()));
        String[] answers = quizQuestions.get(question);
        int correctAnswer = correctAnswers.get(question);

        String userAnswer = (String) JOptionPane.showInputDialog(this, question, "Quiz", JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
        if (userAnswer != null && userAnswer.equals(answers[correctAnswer])) {
            lives = 1;
            JOptionPane.showMessageDialog(this, "Poprawna odpowiedź! Zyskujesz dodatkowe życie.");
        } else {
            gameOver();
        }
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Gra zakończona! Wracasz do menu.");
        resetGame();
    }

    private void resetGame() {
        inGame = false;
        lives = 3;
        currentLevel = 1;
        asteroidCount = 0;
        asteroidSequence.clear();
        asteroids.clear();
        asteroidSpeed = asteroidSpeedsPerLevel[0];
        repaint();
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
            resetGame();
        }
        asteroidSequence = new ArrayList<>(levelSequences.get(currentLevel - 1));
        sequenceIndex = 0;
        asteroidSpeed = asteroidSpeedsPerLevel[currentLevel - 1];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!inGame) {
            if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_8) {
                currentLevel = e.getKeyCode() - KeyEvent.VK_0;
                asteroidSequence = new ArrayList<>(levelSequences.get(currentLevel - 1));
                asteroidSpeed = asteroidSpeedsPerLevel[currentLevel - 1];
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
