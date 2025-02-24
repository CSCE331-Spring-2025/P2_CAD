import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class POSApplication extends JFrame implements ActionListener {
    // Use CardLayout to manage different "pages"
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // Database credentials
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    private static final String DB_USER = "team_cad";
    private static final String DB_PASSWORD = "cad";

    // Panel names for the CardLayout
    private final String MANAGER_PAGE = "Manager";
    private final String CASHIER_PAGE = "Cashier";
    private final String INVENTORY_PAGE = "Inventory";

    public POSApplication() {
        setTitle("POS System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        // Create the three pages (panels)
        JPanel managerPanel = createManagerPanel();
        JPanel cashierPanel = createCashierPanel();
        JPanel inventoryPanel = createInventoryPanel();

        // Add panels to cardPanel with identifiers
        cardPanel.add(managerPanel, MANAGER_PAGE);
        cardPanel.add(cashierPanel, CASHIER_PAGE);
        cardPanel.add(inventoryPanel, INVENTORY_PAGE);

        // Add the card panel to the center of the frame
        add(cardPanel, BorderLayout.CENTER);

        // Create a navigation panel with buttons at the bottom
        JPanel navPanel = new JPanel();
        JButton btnManager = new JButton("Manager");
        JButton btnCashier = new JButton("Cashier");
        JButton btnInventory = new JButton("Inventory");

        // Set action commands to match our card names
        btnManager.setActionCommand(MANAGER_PAGE);
        btnCashier.setActionCommand(CASHIER_PAGE);
        btnInventory.setActionCommand(INVENTORY_PAGE);

        // Register this class as the ActionListener
        btnManager.addActionListener(this);
        btnCashier.addActionListener(this);
        btnInventory.addActionListener(this);

        // Add the buttons to the navigation panel
        navPanel.add(btnManager);
        navPanel.add(btnCashier);
        navPanel.add(btnInventory);

        // Add the navigation panel to the bottom of the frame
        add(navPanel, BorderLayout.SOUTH);
    }

    // Manager panel (Displays Sales Trends)
    private JPanel createManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Manager Page: Sales Trends", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea trendsArea = new JTextArea();
        trendsArea.setEditable(false);

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(Total_Price) AS TotalSales FROM customer_order");

            if (rs.next()) {
                trendsArea.setText("Total Sales: $" + rs.getDouble("TotalSales"));
            } else {
                trendsArea.setText("No data available.");
            }
            conn.close();
        } catch (Exception e) {
            trendsArea.setText("Error connecting to database.");
            e.printStackTrace();
        }

        panel.add(new JScrollPane(trendsArea), BorderLayout.CENTER);
        return panel;
    }

    // Cashier panel (Displays Menu)
    private JPanel createCashierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Cashier Page: Menu", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea menuArea = new JTextArea();
        menuArea.setEditable(false);

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, Price FROM Menu_Item");

            while (rs.next()) {
                menuArea.append(rs.getString("name") + " - $" + rs.getDouble("Price") + "\n");
            }
            conn.close();
        } catch (Exception e) {
            menuArea.setText("Error fetching menu.");
            e.printStackTrace();
        }

        panel.add(new JScrollPane(menuArea), BorderLayout.CENTER);
        return panel;
    }

    // Inventory panel (Displays Inventory Items)
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Inventory Page", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Name, Current_Number FROM Inventory");

            while (rs.next()) {
                inventoryArea.append(rs.getString("Name") + " - " + rs.getInt("Current_Number") + " in stock\n");
            }
            conn.close();
        } catch (Exception e) {
            inventoryArea.setText("Error fetching inventory.");
            e.printStackTrace();
        }

        panel.add(new JScrollPane(inventoryArea), BorderLayout.CENTER);
        return panel;
    }

    // Handle navigation button clicks
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        cardLayout.show(cardPanel, command);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new POSApplication().setVisible(true);
        });
    }
}
