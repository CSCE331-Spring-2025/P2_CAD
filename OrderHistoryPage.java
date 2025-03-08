import java.sql.*;
import javax.swing.*;

/**
 * A window that displays the order history from the database.
 * <p>
 * This class provides functionality to view past orders and 
 * manages adding new orders to the database. It displays orders
 * in ascending chronological order with their IDs, prices, and timestamps.
 * <p>
 * It also provides static utility methods for adding orders to the
 * database and managing the relationship between orders and menu items.
 *
 * @author Rayan Ali, Sareem MominKhoja, Chloe Lee, Chase Varghese
 * @see JFrame
 * @see Connection
 */
public class OrderHistoryPage extends JFrame {
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    
    /**
     * Constructs a new OrderHistoryPage window displaying all past orders.
     * <p>
     * This constructor initializes the JFrame window and queries the 
     * database for all customer orders. Each order is displayed with its
     * ID, total price, and timestamp in a scrollable text area.
     * <p>
     * Any database errors are caught and displayed in the text area.
     * 
     * @see JTextArea
     * @see JScrollPane
     */
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
    
    /**
     * Adds a new order to the database and returns the generated order ID.
     * <p>
     * This method inserts a new row into the customer_order table with the
     * provided total price and the current timestamp. It retrieves and returns
     * the auto-generated order ID for further use.
     *
     * @param totalPrice the total price of the order
     * @return the generated order ID, or -1 if an error occurs
     * @see PreparedStatement
     * @see Statement#RETURN_GENERATED_KEYS
     */
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

    /**
     * Associates a menu item with an order in the database.
     * <p>
     * This method checks if the specified menu item already exists in the order.
     * If it does, the quantity is incremented by the specified amount.
     * If not, a new association is created in the junction table.
     * <p>
     * This implements a many-to-many relationship between orders and menu items.
     *
     * @param orderId the ID of the order
     * @param menuId the ID of the menu item
     * @param quantity the quantity of the menu item to add
     * @see PreparedStatement
     */
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
    
    /**
     * Retrieves the menu ID for a given menu item name.
     * <p>
     * This method queries the database to find the menu ID that corresponds
     * to the provided menu item name. This is useful for converting between
     * user-friendly item names and database IDs.
     *
     * @param itemName the name of the menu item to look up
     * @return the menu ID of the item, or -1 if not found or an error occurs
     * @see PreparedStatement
     */
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