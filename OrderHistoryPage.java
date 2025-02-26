import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class OrderHistoryPage extends JFrame {
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    
    public OrderHistoryPage() {
        setTitle("Order History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Order_ID, Total_Price, Time FROM customer_order ORDER BY Time ASC");
            
            while (rs.next()) {
                historyArea.append("Order " + rs.getInt("Order_ID") + ": $" + rs.getDouble("Total_Price") + " on " + rs.getTimestamp("Time") + "\n");
            }
            
            conn.close();
        } catch (Exception e) {
            historyArea.setText("Error fetching order history.");
            e.printStackTrace();
        }
        
        add(new JScrollPane(historyArea));
    }

    public static void addOrderToHistory(double totalPrice) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO customer_order (Total_Price, Time) VALUES (?, NOW())"
            );
            stmt.setDouble(1, totalPrice);
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error adding order to history.");
            e.printStackTrace();
        }
    }
}
