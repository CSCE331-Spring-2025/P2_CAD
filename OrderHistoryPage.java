import java.sql.*;
import javax.swing.*;

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
            ResultSet rs = stmt.executeQuery("SELECT Order_ID, Total_Price, Time FROM customer_order ORDER BY Time ASC");  // Changed to DESC for most recent first
            
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
    

    public static int addOrderToHistory(double totalPrice) {
        int orderId = -1;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO customer_order (Total_Price, Time) VALUES (?, NOW())", // 쉼표 추가!
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.setDouble(1, totalPrice);
            stmt.executeUpdate();
    
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
    
            conn.close();
        } catch (Exception e) {
            System.out.println("Error adding order to history.");
            e.printStackTrace();
        }
        return orderId;
    }

    public static void addMenuItemToOrder(int orderId, int menuId, int quantity) {
        String checkQuery = "SELECT Quantity FROM c_m_junction WHERE Order_ID = ? AND Menu_ID = ?";
        String updateQuery = "UPDATE c_m_junction SET Quantity = Quantity + ? WHERE Order_ID = ? AND Menu_ID = ?";
        String insertQuery = "INSERT INTO c_m_junction (Order_ID, Menu_ID, Quantity) VALUES (?, ?, ?)";
    
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
    
            // check if the item already exists in the junction table
            checkStmt.setInt(1, orderId);
            checkStmt.setInt(2, menuId);
            ResultSet rs = checkStmt.executeQuery();
    
            if (rs.next()) {
                // if exists, update the quantity
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, orderId);
                updateStmt.setInt(3, menuId);
                updateStmt.executeUpdate();
            } else {
                // if not, insert a new row
                insertStmt.setInt(1, orderId);
                insertStmt.setInt(2, menuId);
                insertStmt.setInt(3, quantity);
                insertStmt.executeUpdate();
            }
    
        } catch (Exception e) {
            System.out.println("Error adding/updating menu item in c_m_junction.");
            e.printStackTrace();
        }
    }
    
    

    public static int getMenuIdFromName(String itemName) {
        int menuId = -1;
        String query = "SELECT Menu_ID FROM Menu_Item WHERE name = ?";
    
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                menuId = rs.getInt("Menu_ID");
            }
    
        } catch (Exception e) {
            System.out.println("Error retrieving Menu_ID.");
            e.printStackTrace();
        }
        return menuId;
    }
    
}
