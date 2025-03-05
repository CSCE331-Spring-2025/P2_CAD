import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

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
        
        // Set frame content background to soft orange (#FFC364)
        getContentPane().setBackground(new Color(0xFFC364));

        selectedItems = new ArrayList<>();
        selectedPrices = new ArrayList<>();

        // Left Side: Menu Items Panel
        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        // Set menu panel background to soft orange
        menuPanel.setBackground(new Color(0xFFC364));
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.getViewport().setBackground(new Color(0xFFC364));
        add(menuScroll, BorderLayout.CENTER);

        // Right Side: Order Summary Panel
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setPreferredSize(new Dimension(300, 500));
        // Set order panel background to soft orange
        orderPanel.setBackground(new Color(0xFFC364));

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        // Keep order list panel white for clarity
        orderListPanel.setBackground(Color.WHITE);

        JScrollPane orderScroll = new JScrollPane(orderListPanel);
        orderScroll.setPreferredSize(new Dimension(300, 350));
        // Keep the viewport white so table cells remain clear
        orderScroll.getViewport().setBackground(Color.WHITE);
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        // Total Price Label
        totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        orderPanel.add(totalLabel, BorderLayout.NORTH);

        // Checkout Button with light orange background
        JButton btnCheckout = new JButton("Checkout to Payment");
        btnCheckout.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCheckout.setBackground(new Color(0xFFCC80)); // Lighter orange
        btnCheckout.setForeground(Color.BLACK);
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
                // Set menu item button background to a light orange and text to black
                itemButton.setBackground(new Color(0xFFCC80));
                itemButton.setForeground(Color.BLACK);
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
        // Optional: Set background for the item panel if desired
        itemPanel.setBackground(Color.WHITE);
        JLabel itemLabel = new JLabel(itemName + " - $" + price);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Only show the "X" button if an employee is logged in.
        // DO NOT change styling for the remove button.
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
        
        // Add the order with timestamp to the OrderHistoryPage and get the order id
        int orderId = OrderHistoryPage.addOrderToHistory(totalPrice);

        // Count quantity of each item
        Map<String, Integer> itemCount = new HashMap<>();
        for (String item : selectedItems) {
            itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            
            int menuId = OrderHistoryPage.getMenuIdFromName(itemName);
            if (menuId != -1) {
                OrderHistoryPage.addMenuItemToOrder(orderId, menuId, quantity);
            } else {
                System.out.println("Error: Menu ID not found for item " + itemName);
            }
        }
    
        // Open the CheckoutPage with the current order summary
        CheckoutPage checkoutPage = new CheckoutPage(selectedItems, selectedPrices);
        checkoutPage.setVisible(true);
        this.dispose();
    }
}
