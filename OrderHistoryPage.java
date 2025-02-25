import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class OrderHistoryPage extends JFrame {
    public OrderHistoryPage() {
        setTitle("Order History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Order_ID, Total_Price, Time FROM customer_order ORDER BY Time DESC");
            while(rs.next()){
                historyArea.append("Order " + rs.getInt("Order_ID") + ": $" + rs.getDouble("Total_Price") + " on " + rs.getTimestamp("Time") + "\n");
            }
            conn.close();
        } catch(Exception e) {
            historyArea.setText("Error fetching order history.");
            e.printStackTrace();
        }
        
        add(new JScrollPane(historyArea));
    }
}
