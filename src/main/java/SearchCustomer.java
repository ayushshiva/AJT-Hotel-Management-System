import javax.swing.*;

public class SearchCustomer extends JFrame {

    SearchCustomer() {

        String name = JOptionPane.showInputDialog("Enter Customer Name");

        for (String[] c : DataStore.customers) {
            if (c[0].equals(name)) {
                JOptionPane.showMessageDialog(this,
                    "Customer: " + c[0] +
                    "\nRoom: " + c[1] +
                    "\nType: " + c[2]);
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Customer Not Found");
    }
}