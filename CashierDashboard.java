import java.awt.*;
import javax.swing.*;

/**
 * The CashierDashboard class represents the main interface for cashiers,
 * providing options to create new orders and view order history.
 * <p>
 * This JFrame contains buttons for placing new orders and viewing order history.
 * Clicking these buttons will navigate to the respective pages.
 *
 * @author Sareem Mominkhoja, Rayan Ali, Chloe Lee, Chase Varghese
 * @version 1.0
 * @see OrderPage
 * @see OrderHistoryPage
 */
public class CashierDashboard extends JFrame {

    /**
     * Constructs the CashierDashboard GUI.
     * Sets up the frame properties, background colors, and action buttons.
     */
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

        /**
         * Opens the Order Page when "New Order" is clicked.
         *
         * @see OrderPage
         */
        btnNewOrder.addActionListener(e -> new OrderPage(rootPaneCheckingEnabled).setVisible(true));

        /**
         * Opens the Order History Page when "Order History" is clicked.
         *
         * @see OrderHistoryPage
         */
        btnOrderHistory.addActionListener(e -> new OrderHistoryPage().setVisible(true));

        // Button panel with the same light orange background
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(255, 235, 179)); // Light orange
        btnPanel.add(btnNewOrder);
        btnPanel.add(btnOrderHistory);

        panel.add(btnPanel, BorderLayout.CENTER);
        add(panel);
    }

    /**
     * The main method to launch the Cashier Dashboard.
     * This creates an instance of the CashierDashboard and makes it visible.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CashierDashboard().setVisible(true));
    }
}
