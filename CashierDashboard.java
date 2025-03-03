import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CashierDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel orderPanel;
    private OrderPage currentOrderPage;
    private JLabel totalLabel;
    
    public CashierDashboard() {
        setTitle("Cashier Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Create tab panel at the top
        JPanel tabPanel = new JPanel();
        tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tabPanel.setBackground(new Color(240, 240, 240));
        
        // Create tabs with different background colors
        JButton cashiersTab = createTab("Cashiers", new Color(247, 185, 185));
        JButton orderHistoryTab = createTab("Order History", new Color(247, 185, 185));
        JButton managersTab = createTab("Managers", new Color(247, 185, 185));
        
        tabPanel.add(cashiersTab);
        tabPanel.add(orderHistoryTab);
        tabPanel.add(managersTab);
        
        add(tabPanel, BorderLayout.NORTH);
        
        // Card panel for different views
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Create cashier panel (default view)
        JPanel cashierPanel = createCashierPanel();
        
        // Create order history panel - use the OrderHistoryPage as a JPanel
        JPanel historyPanel = createOrderHistoryPanel();
        
        // Create manager login panel
        JPanel managerLoginPanel = createManagerLoginPanel();
        
        // Add panels to card layout
        cardPanel.add(cashierPanel, "cashier");
        cardPanel.add(historyPanel, "history");
        cardPanel.add(managerLoginPanel, "manager");
        
        add(cardPanel, BorderLayout.CENTER);
        
        // Add action listeners to tabs
        cashiersTab.addActionListener(e -> {
            cardLayout.show(cardPanel, "cashier");
            refreshCashierPanel();
        });
        
        orderHistoryTab.addActionListener(e -> {
            // Refresh the order history panel to show the latest orders
            cardPanel.remove(1);
            cardPanel.add(createOrderHistoryPanel(), "history");
            cardLayout.show(cardPanel, "history");
        });
        
        managersTab.addActionListener(e -> {
            cardLayout.show(cardPanel, "manager");
        });
    }
    
    private JButton createTab(String text, Color bgColor) {
        JButton tab = new JButton(text);
        tab.setBackground(bgColor);
        tab.setFocusPainted(false);
        tab.setBorderPainted(false);
        tab.setPreferredSize(new Dimension(100, 30));
        return tab;
    }
    
    private JPanel createCashierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Left side: Navigation menu
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation Menu"));
        navigationPanel.setPreferredSize(new Dimension(200, 0));
        
        JButton newOrderBtn = new JButton("New Order");
        newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newOrderBtn.setMaximumSize(new Dimension(180, 40));
        newOrderBtn.setPreferredSize(new Dimension(180, 40));
        
        navigationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        navigationPanel.add(newOrderBtn);
        navigationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Right side: Order panel with two sections
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Top section: Order Trend
        JPanel trendPanel = new JPanel();
        trendPanel.setBorder(BorderFactory.createTitledBorder("Order Trend"));
        trendPanel.setPreferredSize(new Dimension(0, 200));
        rightPanel.add(trendPanel, BorderLayout.NORTH);
        
        // Bottom section: Order List & Total
        orderPanel = new JPanel(new BorderLayout());
        
        JPanel orderListPanel = new JPanel();
        orderListPanel.setBorder(BorderFactory.createTitledBorder("Order List"));
        orderListPanel.setLayout(new BorderLayout());
        
        // Create a dummy order list
        JTextArea orderList = new JTextArea();
        orderList.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(orderList);
        orderListPanel.add(scrollPane, BorderLayout.CENTER);
        
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setBorder(BorderFactory.createTitledBorder("Total"));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        orderPanel.add(orderListPanel, BorderLayout.CENTER);
        orderPanel.add(totalLabel, BorderLayout.SOUTH);
        
        rightPanel.add(orderPanel, BorderLayout.CENTER);
        
        panel.add(navigationPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        // Add action listener to new order button
        newOrderBtn.addActionListener(e -> {
            currentOrderPage = new OrderPage(true); // true for employee access
            currentOrderPage.setVisible(true);
        });
        
        return panel;
    }
    
    private JPanel createOrderHistoryPanel() {
        // Create a panel to contain the OrderHistoryPage content
        JPanel historyContainerPanel = new JPanel(new BorderLayout());
        
        try {
            // Create a text area for displaying order history
            JTextArea historyArea = new JTextArea();
            historyArea.setEditable(false);
            historyArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            // Add a title and styling to the panel
            JLabel titleLabel = new JLabel("Order History", SwingConstants.CENTER);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            historyContainerPanel.add(titleLabel, BorderLayout.NORTH);
            
            // Fetch data from database
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", 
                dbSetup.user, 
                dbSetup.pswd
            );
            
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(
                "SELECT Order_ID, Total_Price, Time FROM customer_order ORDER BY Time DESC"
            );
            
            // Format the data
            while (rs.next()) {
                historyArea.append("Order #" + rs.getInt("Order_ID") + 
                                  " | Amount: $" + String.format("%.2f", rs.getDouble("Total_Price")) + 
                                  " | Date: " + rs.getTimestamp("Time") + "\n");
            }
            
            conn.close();
            
            // Add the text area to a scroll pane for better viewing
            JScrollPane scrollPane = new JScrollPane(historyArea);
            historyContainerPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Add a refresh button
            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> {
                cardPanel.remove(1);
                cardPanel.add(createOrderHistoryPanel(), "history");
                cardLayout.show(cardPanel, "history");
            });
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(refreshButton);
            historyContainerPanel.add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading order history: " + e.getMessage());
            historyContainerPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }
        
        return historyContainerPanel;
    }
    
    @SuppressWarnings("null")
    private JPanel createManagerLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Manager Login"));
        
        JLabel pinLabel = new JLabel("Enter Manager PIN:");
        pinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField pinField = new JPasswordField(15);
        pinField.setMaximumSize(new Dimension(150, 30));
        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(pinLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        loginPanel.add(pinField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(loginButton);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(loginPanel);
        
        // Add action listener for login
        loginButton.addActionListener(e -> {
            String pin = new String(pinField.getPassword()).trim();
            
            if (pin.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter your PIN");
                return;
            }
            
            try {
                int employeeId = Integer.parseInt(pin);
                authenticateManager(employeeId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid PIN format");
            }
        });
        
        return panel;
    }
    
    private void authenticateManager(int pin) {
        // Temporary hardcoded manager PIN for testing
        if (pin == 1111) {
            JOptionPane.showMessageDialog(this, "Login successful as Manager");
            // Open the manager view
            new POSApplication().setVisible(true);
            this.dispose();
            return;
        }
        
        try {
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", 
                dbSetup.user, 
                dbSetup.pswd
            );
            
            String query = "SELECT * FROM employee WHERE pin = ? AND position = 'manager'";
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, pin);
            java.sql.ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful as Manager");
                new POSApplication().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN or not a manager");
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database");
        }
    }
    
    private void refreshCashierPanel() {
        // This method can be used to refresh the order list if needed
        // For now it's a placeholder
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CashierDashboard().setVisible(true));
    }
}
















/*import javax.swing.*;
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
}*/
