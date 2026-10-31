import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Hotel Manggement Login");
        setSize(650, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel bg = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(110, 230, 210),
                        getWidth(), getHeight(), new Color(140, 180, 255)
                );

                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        bg.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(420, 450));

        // Clean user icon
        JPanel circle = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(new Color(20, 40, 120));

                // Outer circle
                g2.drawOval(10, 10, 100, 100);

                // Head
                g2.drawOval(38, 25, 45, 45);

                // Body
                g2.drawArc(30, 65, 60, 50, 0, 180);
            }
        };

        circle.setOpaque(false);
        circle.setBounds(150, 10, 120, 120);
        card.add(circle);

        // Username field
        RoundedTextField userField = new RoundedTextField(20);
        userField.setBounds(20, 140, 380, 45);
        userField.setFont(new Font("Arial", Font.PLAIN, 18));
        userField.setMargin(new Insets(0, 15, 0, 15));
        card.add(userField);

        JLabel userLabel = new JLabel("👤  User Name");
        userLabel.setBounds(40, 150, 150, 20);
        userLabel.setForeground(new Color(20, 40, 120));
        card.add(userLabel);

        // Password field
        RoundedPasswordField passField = new RoundedPasswordField(20);
        passField.setBounds(20, 210, 380, 45);
        passField.setFont(new Font("Arial", Font.PLAIN, 18));
        passField.setMargin(new Insets(0, 15, 0, 15));
        card.add(passField);

        JLabel passLabel = new JLabel("🔒  Password");
        passLabel.setBounds(40, 220, 150, 20);
        passLabel.setForeground(new Color(20, 40, 120));
        card.add(passLabel);

        // Eye button
        JButton eye = new JButton("👁");
        eye.setBounds(350, 215, 40, 35);
        card.add(eye);

        // Remember me
        JCheckBox remember = new JCheckBox("Remember me");
        remember.setBounds(20, 280, 130, 25);
        remember.setOpaque(false);
        card.add(remember);

        // Login button
        JButton loginBtn = new JButton("LOG IN");
        loginBtn.setBounds(110, 340, 200, 50);
        loginBtn.setBackground(new Color(20, 90, 220));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 20));
        loginBtn.setFocusPainted(false);

        card.add(loginBtn);

        // Eye toggle
        eye.addActionListener(e -> {
            if (passField.getEchoChar() == 0) {
                passField.setEchoChar('•');
            } else {
                passField.setEchoChar((char) 0);
            }
        });

        // Login action
        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if (user.equals("admin") && pass.equals("1234")) {
                new DashboardFrame();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }
        });

        bg.add(card);
        add(bg);

        setVisible(true);
    }
}

// Rounded text field
class RoundedTextField extends JTextField {
    public RoundedTextField(int size) {
        super(size);
        setOpaque(false);
    }

    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
        g.setColor(new Color(20, 40, 120));
        g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
    }
}

// Rounded password field
class RoundedPasswordField extends JPasswordField {
    public RoundedPasswordField(int size) {
        super(size);
        setOpaque(false);
    }

    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
        g.setColor(new Color(20, 40, 120));
        g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
    }
}