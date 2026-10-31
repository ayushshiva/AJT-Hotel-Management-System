import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ReceiptHistoryFrame extends JFrame {
    private final JTextArea history = new JTextArea();

    public ReceiptHistoryFrame() {
        setTitle("File Management · Receipt History");
        setSize(700, 450);
        setLocationRelativeTo(null);
        history.setEditable(false);
        JButton refresh = new JButton("Refresh Receipt History");
        refresh.addActionListener(event -> loadHistory());
        add(new JScrollPane(history), BorderLayout.CENTER);
        add(refresh, BorderLayout.SOUTH);
        loadHistory();
        setVisible(true);
    }

    private void loadHistory() {
        try {
            String contents = FileManager.readAll();
            history.setText(contents.isBlank() ? "No receipt records found." : contents);
            history.setCaretPosition(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not read receipt history: " + e.getMessage(),
                    "File Management Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
