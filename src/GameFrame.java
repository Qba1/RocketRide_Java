import javax.swing.*;

public class GameFrame extends JFrame {


    public GameFrame() {
        this.add(new GamePanel());
        this.setTitle("Rocket Ride");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack(); // Dopasowanie rozmiaru do panelu
        this.setVisible(true);



    }
}
