import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class POSApplication extends JFrame implements ActionListener {
    // Use CardLayout to manage different "pages"
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Database credentials
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    private static final String DB_USER = "team_cad";
    private static final String DB_PASSWORD = "cad";

    // Panel names
    private static final String MANAGER_PAGE = "Manager";
    private static final String CASHIER_PAGE = "Cashier";
    private static final String INVENTORY_PAGE = "Inventory";

    // Database connection
    private Connection conn;

    public POSApplication() {
        setTitle("POS System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Establish database connection
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            showError("Database Connection Failed. Check credentials.", e);
            return;
        }

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

        // Create navigation buttons
        JPanel navPanel = new JPanel();
        String[] pages = {MANAGER_PAGE, CASHIER_PAGE, INVENTORY_PAGE};
        for (String page : pages) {
            JButton button = new JButton(page);
            button.setActionCommand(page);
            button.addActionListener(this);
            navPanel.add(button);
        }

        // Add navigation panel to the bottom
        add(navPanel, BorderLayout.SOUTH);
    }

    /** Creates the Manager Panel */
    private JPanel createManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Manager Page: Sales Trends", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea trendsArea = new JTextArea();
        trendsArea.setEditable(false);
        panel.add(new JScrollPane(trendsArea), BorderLayout.CENTER);

        // Load data asynchronously
        loadDataAsync(() -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(Total_Price) AS TotalSales FROM customer_order")) {
                try {
                    if (rs.next()) {
                        trendsArea.setText("Total Sales: $" + rs.getDouble("TotalSales"));
                    } else {
                        trendsArea.setText("No data available.");
                    }
                } catch (SQLException e) {
                }
            }
        }, trendsArea);

        return panel;
    }

    /** Creates the Cashier Panel */
    private JPanel createCashierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Cashier Page: Menu", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea menuArea = new JTextArea();
        menuArea.setEditable(false);
        panel.add(new JScrollPane(menuArea), BorderLayout.CENTER);

        // Load data asynchronously
        loadDataAsync(() -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name, Price FROM Menu_Item")) {
                StringBuilder menu = new StringBuilder();
                try {
                    while (rs.next()) {
                        menu.append(rs.getString("name")).append(" - $").append(rs.getDouble("Price")).append("\n");
                    }
                } catch (SQLException e) {
                }
                menuArea.setText(menu.toString());
            }
        }, menuArea);

        return panel;
    }

    /** Creates the Inventory Panel */
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Inventory Page", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);

        JTextArea inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        panel.add(new JScrollPane(inventoryArea), BorderLayout.CENTER);

        // Load data asynchronously
        loadDataAsync(() -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT Name, Current_Number FROM Inventory")) {
                StringBuilder inventory = new StringBuilder();
                try {
                    while (rs.next()) {
                        inventory.append(rs.getString("Name")).append(" - ")
                                .append(rs.getInt("Current_Number")).append(" in stock\n");
                    }
                } catch (SQLException e) {
                }
                inventoryArea.setText(inventory.toString());
            }
        }, inventoryArea);

        return panel;
    }

    /** Handles navigation between pages */
    @Override
    public void actionPerformed(ActionEvent e) {
        cardLayout.show(cardPanel, e.getActionCommand());
    }

    /** Loads data asynchronously */
    private void loadDataAsync(Runnable queryTask, JTextArea outputArea) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    queryTask.run();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> outputArea.setText("Error fetching data."));
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    /** Displays an error message */
    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /** Closes the database connection */
    private void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /** Main method */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            POSApplication app = new POSApplication();
            app.setVisible(true);
        });
    }
}
