import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class POSApplication extends JFrame implements ActionListener {
    // CardLayout to manage different "pages"
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // Database credentials using dbSetup
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    private static final String DB_USER = dbSetup.user;
    private static final String DB_PASSWORD = dbSetup.pswd;

    // Panel names
    private final String MANAGER_PAGE = "Manager";
    private final String MENU_PAGE = "Menu";
    private final String INVENTORY_PAGE = "Inventory";
    private final String EMPLOYEE_PAGE = "Employees";

    public POSApplication() {
        setTitle("POS System - Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center window

        // Create pages
        JPanel managerPanel = createManagerPanel();
        JPanel menuPanel = new MenuPanel(); // New MenuPanel for viewing/adding/updating menu items
        JPanel inventoryPanel = new InventoryPage();
        JPanel employeePanel = new EmployeeManagementPanel();

        // Add pages to cardPanel
        cardPanel.add(managerPanel, MANAGER_PAGE);
        cardPanel.add(menuPanel, MENU_PAGE);
        cardPanel.add(inventoryPanel, INVENTORY_PAGE);
        cardPanel.add(employeePanel, EMPLOYEE_PAGE);

        add(cardPanel, BorderLayout.CENTER);

        // Navigation panel at the bottom
        JPanel navPanel = new JPanel();
        JButton btnManager = new JButton("Manager");
        JButton btnMenu = new JButton("Menu");
        JButton btnInventory = new JButton("Inventory");
        JButton btnEmployee = new JButton("Employees");

        btnManager.setActionCommand(MANAGER_PAGE);
        btnMenu.setActionCommand(MENU_PAGE);
        btnInventory.setActionCommand(INVENTORY_PAGE);
        btnEmployee.setActionCommand(EMPLOYEE_PAGE);

        btnManager.addActionListener(this);
        btnMenu.addActionListener(this);
        btnInventory.addActionListener(this);
        btnEmployee.addActionListener(this);

        navPanel.add(btnManager);
        navPanel.add(btnMenu);
        navPanel.add(btnInventory);
        navPanel.add(btnEmployee);

        add(navPanel, BorderLayout.SOUTH);
    }

    // Manager panel: displays sales trends
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

    // Employee management panel: displays employee information
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Employee Management", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label, BorderLayout.NORTH);
        
        JTextArea empArea = new JTextArea();
        empArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Employee_ID, Position FROM Employee");
            while(rs.next()){
                empArea.append("ID: " + rs.getInt("Employee_ID") + " - Position: " + rs.getString("Position") + "\n");
            }
            conn.close();
        } catch(Exception e) {
            empArea.setText("Error fetching employee data.");
            e.printStackTrace();
        }
        
        panel.add(new JScrollPane(empArea), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cardLayout.show(cardPanel, e.getActionCommand());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new POSApplication().setVisible(true));
    }
}
