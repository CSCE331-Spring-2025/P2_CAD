import java.awt.*;
import java.sql.*;
import javax.swing.*;

/**
 * LoginPage provides a login interface for employees using a PIN-based system.
 * It connects to the PostgreSQL database and redirects the user to the correct dashboard
 * based on their role (manager or cashier).
 * 
 * @author 
 *     Sareem Mominkhoja,  
 *     Rayan Ali,  
 *     Chloe Lee,  
 *     Chase Varghese
 */
public class LoginPage extends JFrame {
    private JTextField pinField;
    private JButton loginButton;

    /**
     * Constructs the login page GUI with orange background,
     * a text field for PIN input, and a login button.
     */
    public LoginPage() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);

        // Set background color to soft orange
        getContentPane().setBackground(new Color(0xFFC364));

        setLayout(new GridLayout(2, 1)); // Input + Button layout

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(0xFFC364)); // Match parent background

        inputPanel.add(new JLabel("Enter Employee PIN:"));
        pinField = new JTextField(20);
        inputPanel.add(pinField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> authenticate());

        add(inputPanel);
        add(loginButton);

        setLocationRelativeTo(null); // Center the window
    }

    /**
     * Authenticates the user by checking the entered PIN against the database.
     * If the PIN is found and matches a manager, the manager dashboard is opened;
     * otherwise, the cashier dashboard is opened.
     * If the PIN is invalid or not found, a warning is displayed.
     */
    private void authenticate() {
        String pin = pinField.getText().trim();
        System.out.println("PIN entered: '" + pin + "'");

        int employeeId;
        try {
            employeeId = Integer.parseInt(pin);
        } catch (NumberFormatException ex) {
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
                String pos = rs.getString("position").trim();

                System.out.println("Found employee: " + foundId + " with position [" + pos + "]");

                if (pos.equalsIgnoreCase("manager")) {
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

    /**
     * Entry point to run the login page independently.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
