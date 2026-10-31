import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomManagementFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Room Number", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public RoomManagementFrame() {
        setTitle("Room Management");
        setSize(520, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton add = new JButton("Add Room");
        JButton edit = new JButton("Rename Room");
        JButton delete = new JButton("Delete Room");
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        add.addActionListener(event -> addRoom());
        edit.addActionListener(event -> renameRoom());
        delete.addActionListener(event -> deleteRoom());
        refresh();
        setVisible(true);
    }

    private void refresh() {
        model.setRowCount(0);
        for (int i = 0; i < DataStore.rooms.length; i++) {
            model.addRow(new Object[]{DataStore.rooms[i], DataStore.booked[i] ? "BOOKED" : "AVAILABLE"});
        }
    }

    private void addRoom() {
        String number = JOptionPane.showInputDialog(this, "Room number:");
        if (number == null) return;
        if (!DataStore.addRoom(number)) {
            JOptionPane.showMessageDialog(this, "Enter a unique room number up to 12 characters.");
            return;
        }
        FileManager.save("Added desktop room " + number.trim());
        refresh();
    }

    private void renameRoom() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a room first.");
            return;
        }
        String number = JOptionPane.showInputDialog(this, "New room number:", DataStore.rooms[selected]);
        if (number == null) return;
        if (!DataStore.renameRoom(selected, number)) {
            JOptionPane.showMessageDialog(this, "Booked rooms cannot be renamed. Room number must be unique.");
            return;
        }
        FileManager.save("Renamed desktop room to " + number.trim());
        refresh();
    }

    private void deleteRoom() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a room first.");
            return;
        }
        if (DataStore.removeRoom(selected)) {
            FileManager.save("Deleted desktop room " + model.getValueAt(selected, 0));
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Booked rooms cannot be deleted.");
        }
    }
}
