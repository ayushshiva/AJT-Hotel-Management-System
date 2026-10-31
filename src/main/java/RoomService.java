import javax.swing.*;

public class RoomService {

    public static int service() {

        String[] services = {
                "Water Bottle - 30",
                "Extra Pillow - 50",
                "Cleaning - 100"
        };

        JComboBox<String> box = new JComboBox<>(services);

        int result = JOptionPane.showConfirmDialog(
                null,
                box,
                "Room Service",
                JOptionPane.OK_CANCEL_OPTION
        );

        if(result != JOptionPane.OK_OPTION) return 0;

        String selected = (String) box.getSelectedItem();

        int price = 0;

        if(selected.contains("30")) price = 30;
        else if(selected.contains("50")) price = 50;
        else if(selected.contains("100")) price = 100;

        JOptionPane.showMessageDialog(null,
                "Service Added\n" + selected);

        return price;
    }
}