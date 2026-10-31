import javax.swing.*;

public class EmployeeSection {

    public static void showEmployees() {

        String data =
                "EMPLOYEE LIST\n\n" +
                "1. Manager - Rahul\n" +
                "2. Receptionist - Priya\n" +
                "3. Chef - Amit\n" +
                "4. Cleaner - Suresh";

        JOptionPane.showMessageDialog(null, data);
    }
}