import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class OrderPage extends JFrame {
    private boolean isEmployee;
    private JLabel totalLabel;
    private JPanel orderListPanel;
    private Map<String, Integer> selectedItems = new HashMap<>();
    private List<Double> selectedPrices = new ArrayList<>();
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";

    public OrderPage(boolean isEmployee) {
        this.isEmployee = isEmployee;
        setTitle("Place Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // menu panel
        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        add(menuScroll, BorderLayout.CENTER);

        // order panel
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setPreferredSize(new Dimension(300, 500));

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        JScrollPane orderScroll = new JScrollPane(orderListPanel);
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        orderPanel.add(totalLabel, BorderLayout.NORTH);

        JButton btnCheckout = new JButton("Checkout to Payment");
        btnCheckout.addActionListener(this::checkoutOrder);
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
        selectedItems.put(itemName, selectedItems.getOrDefault(itemName, 0) + 1);
        selectedPrices.add(price);
        updateOrderList();
    }

    private void updateOrderList() {
        orderListPanel.removeAll();
        for (Map.Entry<String, Integer> entry : selectedItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            double totalPrice = quantity * selectedPrices.get(selectedItems.keySet().toArray().length - 1);
    
            JPanel itemPanel = new JPanel(new BorderLayout());
            JLabel itemLabel = new JLabel(itemName + " x" + quantity + " - $" + totalPrice);
    
            if (isEmployee) {
                JButton removeButton = new JButton("X");
                removeButton.addActionListener(e -> removeItem(itemName));
                itemPanel.add(removeButton, BorderLayout.EAST);
            }
    
            itemPanel.add(itemLabel, BorderLayout.WEST);
            orderListPanel.add(itemPanel);
        }
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotal();
    }
    
    private double getItemPrice(String itemName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             PreparedStatement stmt = conn.prepareStatement("SELECT Price FROM Menu_Item WHERE name = ?")) {
            stmt.setString(1, itemName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void removeItem(String itemName) {
    if (selectedItems.containsKey(itemName)) {
        if (selectedItems.get(itemName) > 1) {
            selectedItems.put(itemName, selectedItems.get(itemName) - 1);
        } else {
            selectedItems.remove(itemName);
        }

        double itemPrice = getItemPrice(itemName);
        selectedPrices.remove((Double) itemPrice);

        updateOrderList();
    }
}


    private void updateTotal() {
        double total = selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }
    private Integer currentOrderId = null;
    
    private void checkoutOrder(ActionEvent event) {
        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items in order.");
            return;
        }
    
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd)) {
            if (currentOrderId == null) {
                String orderQuery = "INSERT INTO customer_order (Time, Total_Price, status) VALUES (CURRENT_TIMESTAMP, 0, 'IN_PROGRESS') RETURNING Order_ID;";
                try (PreparedStatement stmt = conn.prepareStatement(orderQuery);
                     ResultSet rs = stmt.executeQuery()) {
    
                    if (rs.next()) {
                        currentOrderId = rs.getInt(1);
                    }
                }
            }
    
            if (currentOrderId != null) {
                insertOrderItems(conn, currentOrderId);
                updateOrderTotal(conn, currentOrderId);
                JOptionPane.showMessageDialog(this, "Order #" + currentOrderId + " completed!");
                
                // Chloe: fail to connect the ChekoutPage because of the parameter type
                // new CheckoutPage(currentOrderId).setVisible(true);
                resetOrder(); 
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Order processing failed.");
        }
    }
    
    private void resetOrder() {
        currentOrderId = null;
        selectedItems.clear();
        selectedPrices.clear();
        orderListPanel.removeAll();
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotal();
    }

    private void insertOrderItems(Connection conn, int orderId) throws SQLException {
        String query = """
            INSERT INTO C_M_Junction (Order_ID, Menu_ID, Quantity)
            VALUES (?, (SELECT Menu_ID FROM Menu_Item WHERE name = ?), ?)
            ON CONFLICT (Order_ID, Menu_ID) 
            DO UPDATE SET Quantity = C_M_Junction.Quantity + EXCLUDED.Quantity;
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Map.Entry<String, Integer> entry : selectedItems.entrySet()) {
                stmt.setInt(1, orderId);
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void updateOrderTotal(Connection conn, int orderId) throws SQLException {
        String query = "UPDATE customer_order SET Total_Price = (SELECT SUM(mi.Price) FROM C_M_Junction cmj JOIN Menu_Item mi ON cmj.Menu_ID = mi.Menu_ID WHERE cmj.Order_ID = ?) WHERE Order_ID = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderPage(true).setVisible(true));
    }
}
