import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EmployeeManagementPage extends JFrame {
    public EmployeeManagementPage() {
        setTitle("Employee Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JTextArea empArea = new JTextArea();
        empArea.setEditable(false);
        
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db", dbSetup.user, dbSetup.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Employee_ID, Position FROM Employee");
            while(rs.next()){
                empArea.append("ID: " + rs.getInt("Employee_ID") + " - Position: " + rs.getString("Position") + "\n");
            }
            conn.close();
        } catch(Exception e) {
            empArea.setText("Error fetching employee data.");
            e.printStackTrace();
        }
        
        add(new JScrollPane(empArea));
    }
}
