import javax.swing.*;

public class FoodOrder {

    public static int orderFood() {

        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");

        if (customerName == null || customerName.trim().isEmpty()) {
            return 0;
        }

        String[] foodItems = {
                "North Indian - Paneer Butter Masala - 250",
                "South Indian - Masala Dosa - 120",
                "International - Pizza - 300"
        };

        JComboBox<String> foodBox = new JComboBox<>(foodItems);

        int result = JOptionPane.showConfirmDialog(
                null,
                foodBox,
                "Select Food",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return 0;

        String selectedFood = (String) foodBox.getSelectedItem();

        int price = 0;

        if (selectedFood.contains("250")) price = 250;
        else if (selectedFood.contains("120")) price = 120;
        else if (selectedFood.contains("300")) price = 300;

        // CUSTOMER TABLE ME ADD
        DataStore.customers.add(new String[]{
                customerName,
                "Food Order",
                selectedFood
        });

        FileManager.save("Food Order: " + customerName + " | " + selectedFood);

        JOptionPane.showMessageDialog(null,
                "Customer: " + customerName +
                "\nFood: " + selectedFood +
                "\nPrice = " + price);

        return price;
    }
}