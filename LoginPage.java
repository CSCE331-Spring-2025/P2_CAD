import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginPage extends JFrame {
    private JTextField pinField;
    private JButton loginButton;

    public LoginPage() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        
        // Set the frame background to orange (#ffc364)
        getContentPane().setBackground(new Color(0xFFC364));
        
        // Use a 2x1 grid layout: one row for the input panel, one for the button
        setLayout(new GridLayout(2, 1));
        
        // Input panel (FlowLayout) for the label + text field
        JPanel inputPanel = new JPanel(new FlowLayout());
        // Also set this panel’s background to orange
        inputPanel.setBackground(new Color(0xFFC364));
        
        inputPanel.add(new JLabel("Enter Employee PIN:"));
        pinField = new JTextField(20);
        inputPanel.add(pinField);
        
        loginButton = new JButton("Login");
        // Keep the button’s default look (logic unchanged)
        loginButton.addActionListener(e -> authenticate());
        
        // Add panels/components to the frame
        add(inputPanel);
        add(loginButton);
        
        setLocationRelativeTo(null);
    }
    
    private void authenticate() {
        // Get the PIN input and trim any extra whitespace.
        String pin = pinField.getText().trim();
        System.out.println("PIN entered: '" + pin + "'");
        
        int employeeId;
        try {
            employeeId = Integer.parseInt(pin);
        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Invalid PIN format.");
            return;
        }
        
        try {
            String dbUrl = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
            Connection conn = DriverManager.getConnection(dbUrl, dbSetup.user, dbSetup.pswd);
            String query = "SELECT * FROM employee WHERE pin = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
             
            if (rs.next()) {
                int foundId = rs.getInt("pin");
                String pos = rs.getString("position");
                // Debug print: Show the retrieved position enclosed in brackets.
                System.out.println("Found employee: " + foundId + " with position [" + pos + "]");
                 
                // Use trim() on the position from the database.
                if (pos.trim().equalsIgnoreCase("manager")) {
                    JOptionPane.showMessageDialog(this, "Login successful as Manager");
                    new POSApplication().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Login successful as Cashier");
                    new CashierDashboard().setVisible(true);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN.");
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database.");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
