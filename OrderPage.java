import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class OrderPage extends JFrame {
    public OrderPage() {
        setTitle("Place Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        // Display menu items in a text area for now
        JTextArea orderArea = new JTextArea();
        orderArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, Price FROM Menu_Item");
            while(rs.next()){
                orderArea.append(rs.getString("name") + " - $" + rs.getDouble("Price") + "\n");
            }
            conn.close();
        } catch(Exception e) {
            orderArea.setText("Error fetching menu items.");
            e.printStackTrace();
        }
        
        JButton btnSubmitOrder = new JButton("Submit Order");
        btnSubmitOrder.addActionListener(e -> {
            // Here you would insert the order into the customer_order table.
            JOptionPane.showMessageDialog(this, "Order submitted!");
            this.dispose();
        });
        
        add(new JScrollPane(orderArea), BorderLayout.CENTER);
        add(btnSubmitOrder, BorderLayout.SOUTH);
    }
}
