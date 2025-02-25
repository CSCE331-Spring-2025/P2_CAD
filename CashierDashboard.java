import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CashierDashboard extends JFrame {
    public CashierDashboard() {
        setTitle("Cashier Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("Cashier Dashboard", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);
        
        // Buttons for cashier actions
        JButton btnNewOrder = new JButton("New Order");
        JButton btnOrderHistory = new JButton("Order History");
        
        btnNewOrder.addActionListener(e -> new OrderPage(rootPaneCheckingEnabled).setVisible(true));
        btnOrderHistory.addActionListener(e -> new OrderHistoryPage().setVisible(true));
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnNewOrder);
        btnPanel.add(btnOrderHistory);
        
        panel.add(btnPanel, BorderLayout.CENTER);
        
        add(panel);
    }
}
