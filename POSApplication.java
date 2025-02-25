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
    private final String CASHIER_PAGE = "Cashier";
    private final String INVENTORY_PAGE = "Inventory";
    private final String EMPLOYEE_PAGE = "Employees";

    public POSApplication() {
        setTitle("POS System - Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center window

        // Create pages
        JPanel managerPanel = createManagerPanel();
        JPanel cashierPanel = createCashierPanel();
        JPanel inventoryPanel = createInventoryPanel();
        JPanel employeePanel = createEmployeePanel();

        // Add pages to cardPanel
        cardPanel.add(managerPanel, MANAGER_PAGE);
        cardPanel.add(cashierPanel, CASHIER_PAGE);
        cardPanel.add(inventoryPanel, INVENTORY_PAGE);
        cardPanel.add(employeePanel, EMPLOYEE_PAGE);

        add(cardPanel, BorderLayout.CENTER);

        // Navigation panel at the bottom
        JPanel navPanel = new JPanel();
        JButton btnManager = new JButton("Manager");
        JButton btnCashier = new JButton("Cashier");
        JButton btnInventory = new JButton("Inventory");
        JButton btnEmployee = new JButton("Employees");

        btnManager.setActionCommand(MANAGER_PAGE);
        btnCashier.setActionCommand(CASHIER_PAGE);
        btnInventory.setActionCommand(INVENTORY_PAGE);
        btnEmployee.setActionCommand(EMPLOYEE_PAGE);

        btnManager.addActionListener(this);
        btnCashier.addActionListener(this);
        btnInventory.addActionListener(this);
        btnEmployee.addActionListener(this);

        navPanel.add(btnManager);
        navPanel.add(btnCashier);
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

    // Cashier panel: displays menu items
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

    // Inventory panel: displays inventory items
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