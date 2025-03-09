import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * The OrderPage class represents a graphical user interface for placing orders.
 * It allows users to select menu items, view their order summary, and proceed to checkout.
 * The interface is designed with a soft orange theme and supports both customers and employees.
 * Employees have additional privileges, such as removing items from the order.
 * 
 * @author Rayan Ali, Sareem MominKhoja, Chloe Lee, Chase Varghese
 */
public class OrderPage extends JFrame {
    private JPanel orderListPanel; // Panel to display the list of selected items
    private JLabel totalLabel; // Label to display the total price of the order
    private ArrayList<String> selectedItems; // List of selected menu item names
    private ArrayList<Double> selectedPrices; // List of selected menu item prices
    private boolean isEmployee; // Flag to indicate if the user is an employee

    /**
     * Constructs an OrderPage with the specified employee status.
     * Initializes the GUI components, fetches menu items from the database, and sets up event listeners.
     *
     * @param isEmployee a boolean indicating whether the user is an employee
     */
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
        menuPanel.setBackground(new Color(0xFFC364));
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.getViewport().setBackground(new Color(0xFFC364));
        add(menuScroll, BorderLayout.CENTER);

        // Right Side: Order Summary Panel
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setPreferredSize(new Dimension(300, 500));
        orderPanel.setBackground(new Color(0xFFC364));

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setBackground(Color.WHITE);

        JScrollPane orderScroll = new JScrollPane(orderListPanel);
        orderScroll.setPreferredSize(new Dimension(300, 350));
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

    /**
     * Adds a selected menu item to the order list and updates the total price.
     *
     * @param itemName the name of the menu item to add
     * @param price    the price of the menu item to add
     */
    private void addItemToOrder(String itemName, double price) {
        selectedItems.add(itemName);
        selectedPrices.add(price);

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        JLabel itemLabel = new JLabel(itemName + " - $" + price);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Only show the "X" button if an employee is logged in.
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

    /**
     * Removes a selected menu item from the order list and updates the total price.
     *
     * @param itemPanel the panel representing the item to remove
     * @param itemName  the name of the menu item to remove
     * @param price     the price of the menu item to remove
     */
    private void removeItem(JPanel itemPanel, String itemName, double price) {
        selectedItems.remove(itemName);
        selectedPrices.remove(price);
        orderListPanel.remove(itemPanel);
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotal();
    }

    /**
     * Updates the total price label based on the current selected items.
     */
    private void updateTotal() {
        double total = selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    /**
     * Handles the checkout process. Saves the order to the database, updates order history,
     * and opens the CheckoutPage to finalize payment.
     */
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