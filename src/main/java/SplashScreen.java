import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame {

    private JProgressBar progress;

    public SplashScreen() {
        setTitle("Hotel Management System");
        setSize(600, 350);
        setLocationRelativeTo(null);
        setUndecorated(true);

        // App icon (optional image file)
        ImageIcon icon = new ImageIcon("library.png");
        setIconImage(icon.getImage());

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 60, 120));

        // Logo top left
        JLabel logo = new JLabel("📚");
        logo.setFont(new Font("Arial", Font.PLAIN, 40));
        logo.setBounds(20, 20, 60, 50);
        panel.add(logo);

        // Title
        JLabel title = new JLabel("Hotel Management System");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBounds(120, 100, 400, 40);
        panel.add(title);

        // Loading text
        JLabel loading = new JLabel("Loading...");
        loading.setForeground(Color.WHITE);
        loading.setBounds(260, 180, 100, 30);
        panel.add(loading);

        // Progress bar
        progress = new JProgressBar();
        progress.setBounds(100, 220, 400, 25);
        progress.setStringPainted(true);
        panel.add(progress);

        add(panel);
        setVisible(true);

        startLoading();
    }

    private void startLoading() {
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    progress.setValue(i);
                    Thread.sleep(30);
                }

                new LoginFrame();
                dispose();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}