import javax.swing.*;

public class BillManager extends JFrame {

    BillManager(int total) {

        double gst = total * 0.18;
        double finalBill = total + gst;

        JOptionPane.showMessageDialog(this,
            "Bill = " + total +
            "\nGST = " + gst +
            "\nFinal Bill = " + finalBill);
    }
}