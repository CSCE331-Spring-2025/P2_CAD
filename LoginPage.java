
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginPage extends JFrame {
    /**
     * Database Setup class for configuring database connection parameters.
     * This class provides constants for database user credentials.
     *
     * @author Sareem, Rayan, Chloe, Chase
     * @param  name  the name of the database
     * @return the login page
     * @throws MalformedURLException  if the user login is not correct
     * @see    login pin page
     */
    private JTextField pinField;
    private JButton loginButton;

    public LoginPage() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLayout(new GridLayout(2, 1));
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter Employee PIN:"));
        pinField = new JTextField(20);
        inputPanel.add(pinField);
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> authenticate());
        
        add(inputPanel);
        add(loginButton);
        
        setLocationRelativeTo(null);
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
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
             Connection conn = DriverManager.getConnection(dbUrl, "team_cad", "cad");
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

    private static class POSApplication {

        public POSApplication() {
        }

        private void setVisible(boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class CashierDashboard {

        public CashierDashboard() {
        }

        private void setVisible(boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
