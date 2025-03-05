import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class POSApplication extends JFrame implements ActionListener {
    // CardLayout to manage different pages
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // Database credentials from dbSetup
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    private static final String DB_USER = dbSetup.user;
    private static final String DB_PASSWORD = dbSetup.pswd;

    // Panel names (used for CardLayout)
    private final String TREND_PAGE = "Trends";
    private final String MENU_PAGE = "Menu";
    private final String INVENTORY_PAGE = "Inventory";
    private final String EMPLOYEE_PAGE = "Employees";

    public POSApplication() {
        setTitle("POS System - Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Center the window
        setBackground(new Color(255, 153, 51)); // Set background to #ffc364

        // Create pages
        JPanel trendsPanel = new ManagerTrendsPanel();       // Displays overall trends
        JPanel menuPanel = new MenuPanel();                     // Menu management panel
        JPanel inventoryPanel = new InventoryPage();            // Inventory management panel
        JPanel employeePanel = new EmployeeManagementPanel();   // Employee management panel

        // Add pages to the card panel
        cardPanel.add(trendsPanel, TREND_PAGE);
        cardPanel.add(menuPanel, MENU_PAGE);
        cardPanel.add(inventoryPanel, INVENTORY_PAGE);
        cardPanel.add(employeePanel, EMPLOYEE_PAGE);

        add(cardPanel, BorderLayout.CENTER);

        // Navigation panel at the bottom
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navPanel.setBackground(new Color(0xFFF9F4)); // soft white background for the nav panel

        JButton btnTrends = new JButton("Trends");
        JButton btnMenu = new JButton("Menu");
        JButton btnInventory = new JButton("Inventory");
        JButton btnEmployees = new JButton("Employees");

        // Set action commands
        btnTrends.setActionCommand(TREND_PAGE);
        btnMenu.setActionCommand(MENU_PAGE);
        btnInventory.setActionCommand(INVENTORY_PAGE);
        btnEmployees.setActionCommand(EMPLOYEE_PAGE);

        // Use a darker button background for contrast and set text color to black
        Color buttonBg = new Color(0x8d3b00); // this is the same orange as before, or adjust as needed
        btnTrends.setBackground(buttonBg);
        btnTrends.setForeground(Color.BLACK);
        btnMenu.setBackground(buttonBg);
        btnMenu.setForeground(Color.BLACK);
        btnInventory.setBackground(buttonBg);
        btnInventory.setForeground(Color.BLACK);
        btnEmployees.setBackground(buttonBg);
        btnEmployees.setForeground(Color.BLACK);

        // Optional: Remove the focus painted border for a cleaner look
        btnTrends.setFocusPainted(false);
        btnMenu.setFocusPainted(false);
        btnInventory.setFocusPainted(false);
        btnEmployees.setFocusPainted(false);

        // Add action listeners
        btnTrends.addActionListener(this);
        btnMenu.addActionListener(this);
        btnInventory.addActionListener(this);
        btnEmployees.addActionListener(this);

        // Add buttons to the navPanel
        navPanel.add(btnTrends);
        navPanel.add(btnMenu);
        navPanel.add(btnInventory);
        navPanel.add(btnEmployees);

        // Add navPanel to the frame
        add(navPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cardLayout.show(cardPanel, e.getActionCommand());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new POSApplication().setVisible(true));
    }
}