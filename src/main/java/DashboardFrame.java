import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class DashboardFrame extends JFrame {

    int totalBill = 0;
    LocalDateTime currentTime = LocalDateTime.now();

    DashboardFrame() {

        setTitle("Welcome To Aayush Hotel");
        setSize(900, 550);
        setLayout(new BorderLayout());

        // HEADER
        JLabel title = new JLabel("🏨 Welcome To Aayush Hotel", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.BLUE);

        add(title, BorderLayout.NORTH);

        // CENTER PANEL
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 3, 20, 20));
        panel.setBackground(Color.LIGHT_GRAY);
        
        JButton room = new JButton("Book Room");
        JButton food = new JButton("Food Order");
        JButton bill = new JButton("Show Bill");
        JButton table = new JButton("Customer Table");
        JButton payment = new JButton("Payment");
        JButton search = new JButton("Search Customer");
        JButton employee = new JButton("Employees");
        JButton laundry = new JButton("Laundry");
        JButton service = new JButton("Room Service");
        JButton roomManagement = new JButton("Room Management");
        JButton reports = new JButton("Reports");
        JButton fileManagement = new JButton("File Management");
        JButton logout = new JButton("Logout");
        JButton exit = new JButton("Exit");

        // BUTTON STYLE
        Font btnFont = new Font("Arial", Font.BOLD, 18);

        JButton[] buttons = {
                room, food, bill, table, payment,
                search, employee, laundry, service,
                roomManagement, reports, fileManagement, logout, exit
        };

        for (JButton b : buttons) {
            b.setFont(btnFont);
            panel.add(b);
        }

        add(panel, BorderLayout.CENTER);

        // FOOTER
        JLabel footer = new JLabel(
                "Date & Time: " + currentTime,
                JLabel.CENTER
        );

        footer.setFont(new Font("Arial", Font.PLAIN, 14));
        footer.setForeground(Color.DARK_GRAY);

        add(footer, BorderLayout.SOUTH);

        // ROOM BUTTON
        room.addActionListener(e -> {

            String name = JOptionPane.showInputDialog("Customer Name:");

            String[] roomCategory = {
                    "Single Room - 1000",
                    "Double Room - 2000",
                    "Deluxe Room - 3000",
                    "Four Bad - 400"
            };

            JComboBox<String> categoryBox = new JComboBox<>(roomCategory);

            int categoryResult = JOptionPane.showConfirmDialog(
                    null,
                    categoryBox,
                    "Select Room Category",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (categoryResult != JOptionPane.OK_OPTION) return;

            String selectedCategory = (String) categoryBox.getSelectedItem();

            String[] roomTypes = {"AC", "Non-AC"};

            JComboBox<String> typeBox = new JComboBox<>(roomTypes);

            int typeResult = JOptionPane.showConfirmDialog(
                    null,
                    typeBox,
                    "Select AC / Non-AC",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (typeResult != JOptionPane.OK_OPTION) return;

            String selectedType = (String) typeBox.getSelectedItem();

            String availableRooms = "";

            for (int i = 0; i < DataStore.rooms.length; i++) {
                if (!DataStore.booked[i]) {
                    availableRooms += DataStore.rooms[i] + "\n";
                }
            }

            String[] roomList = availableRooms.split("\n");

            JComboBox<String> roomBox = new JComboBox<>(roomList);

            int roomResult = JOptionPane.showConfirmDialog(
                    null,
                    roomBox,
                    "Select Room Number",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (roomResult != JOptionPane.OK_OPTION) return;

            String roomNo = (String) roomBox.getSelectedItem();

            for (int i = 0; i < DataStore.rooms.length; i++) {

                if (DataStore.rooms[i].equals(roomNo) && !DataStore.booked[i]) {

                    DataStore.booked[i] = true;

                    int price = 0;

                    if (selectedCategory.contains("Single")) price = 1000;
                    else if (selectedCategory.contains("Double")) price = 2000;
                    else price = 3000;

                    if (selectedType.equals("AC")) price += 500;

                    totalBill += price;

                    DataStore.customers.add(new String[]{
                            name, roomNo, selectedCategory + " " + selectedType
                    });

                    FileManager.save("Booked: " + name + " Room " + roomNo);

                    JOptionPane.showMessageDialog(this,
                            "Booked Successfully\nRoom: " + roomNo);

                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Room Not Available");
        });

        // FOOD
        food.addActionListener(e -> totalBill += FoodOrder.orderFood());

        // BILL
        bill.addActionListener(e -> new BillManager(totalBill));

        // TABLE
        table.addActionListener(e -> new CustomerTable());

        // PAYMENT
        payment.addActionListener(e -> {

    double gst = totalBill * 0.18;
    double finalBill = totalBill + gst;

    String paid = JOptionPane.showInputDialog(
            "Final Bill = " + finalBill + "\nEnter Payment:"
    );

    double paymentAmount = Double.parseDouble(paid);

    double balance = paymentAmount - finalBill;

    JOptionPane.showMessageDialog(this,
            "Balance = " + balance);

    // BILL RESET
    totalBill = 0;
});
        // SEARCH
        search.addActionListener(e -> new SearchCustomer());

        // EMPLOYEE
        employee.addActionListener(e -> EmployeeSection.showEmployees());

        // LAUNDRY
        laundry.addActionListener(e -> totalBill += LaundryService.orderLaundry());

        // ROOM SERVICE
        service.addActionListener(e -> totalBill += RoomService.service());

        roomManagement.addActionListener(e -> new RoomManagementFrame());

        reports.addActionListener(e -> showDesktopReport());

        fileManagement.addActionListener(e -> new ReceiptHistoryFrame());

        // LOGOUT
        logout.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Logout now?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if(choice == JOptionPane.YES_OPTION) {
                new LoginFrame();
                dispose();
            }
        });

        // EXIT
        exit.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure to exit?",
                    "Exit",
                    JOptionPane.YES_NO_OPTION
            );

            if(choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void showDesktopReport() {
        int available = 0;
        int activeBookings = 0;
        for (boolean isBooked : DataStore.booked) {
            if (isBooked) activeBookings++;
            else available++;
        }
        JOptionPane.showMessageDialog(this, "Desktop data summary\n\n"
                + "Customers: " + DataStore.customers.size() + "\n"
                + "Rooms: " + DataStore.rooms.length + "\n"
                + "Available rooms: " + available + "\n"
                + "Active room bookings: " + activeBookings + "\n"
                + "Current unbilled charges: ₹" + totalBill,
                "Reports", JOptionPane.INFORMATION_MESSAGE);
    }
}