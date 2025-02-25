import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class InventoryPage extends JFrame {
    public InventoryPage() {
        setTitle("Inventory Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JTextArea invArea = new JTextArea();
        invArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Name, Current_Number FROM Inventory");
            while(rs.next()){
                invArea.append(rs.getString("Name") + " - " + rs.getInt("Current_Number") + " in stock\n");
            }
            conn.close();
        } catch(Exception e) {
            invArea.setText("Error fetching inventory.");
            e.printStackTrace();
        }
        
        add(new JScrollPane(invArea));
    }
}
