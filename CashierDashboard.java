import java.awt.*;
import javax.swing.*;

public class CashierDashboard extends JFrame {
    public CashierDashboard() {
        setTitle("Cashier Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        // Set the frame content pane background to a light orange shade
        getContentPane().setBackground(new Color(255, 235, 179)); // Light orange
        
        // Create main panel with BorderLayout and set its background to light orange
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 235, 179)); // Light orange
        
        // Header label with a slightly darker light orange background and black text
        JLabel label = new JLabel("Cashier Dashboard", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setOpaque(true);
        label.setBackground(new Color(255, 213, 128)); // Slightly darker light orange
        label.setForeground(Color.BLACK);
        panel.add(label, BorderLayout.NORTH);
        
        // Buttons for cashier actions remain unchanged
        JButton btnNewOrder = new JButton("New Order");
        JButton btnOrderHistory = new JButton("Order History");
        
        btnNewOrder.addActionListener(e -> new OrderPage(rootPaneCheckingEnabled).setVisible(true));
        btnOrderHistory.addActionListener(e -> new OrderHistoryPage().setVisible(true));
        
        // Button panel with the same light orange background
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(255, 235, 179)); // Light orange
        btnPanel.add(btnNewOrder);
        btnPanel.add(btnOrderHistory);
        
        panel.add(btnPanel, BorderLayout.CENTER);
        
        add(panel);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CashierDashboard().setVisible(true));
    }
}
