import javax.swing.*;

public class LaundryService {

    public static int orderLaundry() {

        // Step 1: Customer Name
        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");

        if (customerName == null || customerName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Customer name required");
            return 0;
        }

        // Step 2: Cloth Selection
        String[] cloths = {"Shirt", "Pant", "Saree", "Blanket"};

        String choice = (String) JOptionPane.showInputDialog(
                null,
                "Select Cloth Type:",
                "Laundry Service",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cloths,
                cloths[0]
        );

        if (choice == null) return 0;

        // Step 3: Quantity
        String qtyStr = JOptionPane.showInputDialog("Enter Quantity:");

        int qty;

        try {
            qty = Integer.parseInt(qtyStr);

            if (qty <= 0) {
                JOptionPane.showMessageDialog(null, "Invalid Quantity");
                return 0;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Enter valid number");
            return 0;
        }

        // Step 4: Price
        int price = 0;

        switch (choice) {
            case "Shirt":
                price = 10;
                break;
            case "Pant":
                price = 15;
                break;
            case "Saree":
                price = 20;
                break;
            case "Blanket":
                price = 50;
                break;
        }

        int total = price * qty;

        // Step 5: Bill
        JOptionPane.showMessageDialog(null,
                "Customer Name: " + customerName +
                "\nCloth: " + choice +
                "\nQuantity: " + qty +
                "\nPrice: ₹" + price +
                "\nTotal: ₹" + total);

        return total;
    }
}