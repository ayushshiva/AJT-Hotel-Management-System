import javax.swing.*;
import java.awt.*;

public class CustomerTable extends JFrame {

    CustomerTable() {

        String col[] = {"Customer", "Room", "Type"};

        String data[][] = new String[DataStore.customers.size()][3];

        for (int i = 0; i < DataStore.customers.size(); i++) {
            data[i] = DataStore.customers.get(i);
        }

        JTable table = new JTable(data, col);

        JButton delete = new JButton("Delete Selected");

        delete.addActionListener(e -> {

        int row = table.getSelectedRow();

        if (row >= 0) {

                String roomNo = DataStore.customers.get(row)[1];

                for (int i = 0; i < DataStore.rooms.length; i++) {
                    if (DataStore.rooms[i].equals(roomNo)) {
                        DataStore.booked[i] = false;
                    }
                }

                DataStore.customers.remove(row);

                dispose();
                new CustomerTable();
            }
        });

        setLayout(new BorderLayout());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(delete, BorderLayout.SOUTH);

        setSize(450, 250);
        setVisible(true);
    }
}