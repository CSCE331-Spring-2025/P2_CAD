import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class OrderPage extends JFrame {
    private JPanel orderListPanel;
    private JLabel totalLabel;
    private ArrayList<String> selectedItems;
    private ArrayList<Double> selectedPrices;
    private boolean isEmployee;

    public OrderPage(boolean isEmployee) {
        this.isEmployee = isEmployee;
        setTitle("Place Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        selectedItems = new ArrayList<>();
        selectedPrices = new ArrayList<>();

        // Left Side: Menu Items
        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        add(menuScroll, BorderLayout.CENTER);

        // Right Side: Order Summary
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setPreferredSize(new Dimension(300, 500));

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));

        JScrollPane orderScroll = new JScrollPane(orderListPanel);
        orderScroll.setPreferredSize(new Dimension(300, 350));
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        // Total Price Label
        totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        orderPanel.add(totalLabel, BorderLayout.NORTH);

        // Checkout Button
        JButton btnCheckout = new JButton("Checkout to Payment");
        btnCheckout.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCheckout.addActionListener(e -> checkoutOrder());
        orderPanel.add(btnCheckout, BorderLayout.SOUTH);

        add(orderPanel, BorderLayout.EAST);

        // Fetch Menu Items & Display as Buttons
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, Price FROM Menu_Item");

            while (rs.next()) {
                String itemName = rs.getString("name");
                double itemPrice = rs.getDouble("Price");

                JButton itemButton = new JButton(itemName + " - $" + itemPrice);
                itemButton.setHorizontalTextPosition(SwingConstants.CENTER);

                itemButton.addActionListener(e -> addItemToOrder(itemName, itemPrice));
                menuPanel.add(itemButton);
            }
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching menu items.");
            e.printStackTrace();
        }
    }

    private void addItemToOrder(String itemName, double price) {
        selectedItems.add(itemName);
        selectedPrices.add(price);

        JPanel itemPanel = new JPanel(new BorderLayout());
        JLabel itemLabel = new JLabel(itemName + " - $" + price);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Only show the "X" button if an employee is logged in
        if (isEmployee) {
            JButton removeButton = new JButton("X");
            removeButton.setPreferredSize(new Dimension(30, 30));
            removeButton.setFont(new Font("Arial", Font.BOLD, 12));
            removeButton.addActionListener(e -> removeItem(itemPanel, itemName, price));
            itemPanel.add(removeButton, BorderLayout.EAST);
        }

        itemPanel.add(itemLabel, BorderLayout.WEST);
        orderListPanel.add(itemPanel);
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotal();
    }

    private void removeItem(JPanel itemPanel, String itemName, double price) {
        selectedItems.remove(itemName);
        selectedPrices.remove(price);
        orderListPanel.remove(itemPanel);
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotal();
    }

    private void updateTotal() {
        double total = selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void checkoutOrder() {
        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items selected!");
            return;
        }
        
        // Save the order to the database and update history
        double totalPrice = selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
        
        // Add the order with timestamp to the OrderHistoryPage
        OrderHistoryPage.addOrderToHistory(totalPrice);
    
        // Open the CheckoutPage with the current order summary
        CheckoutPage checkoutPage = new CheckoutPage(selectedItems, selectedPrices);
        checkoutPage.setVisible(true);
        this.dispose();
    }    
}
