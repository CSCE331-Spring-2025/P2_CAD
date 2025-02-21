import java.sql.*;
import java.awt.event.*;
import javax.swing.*;

/*
  TODO:
  1) Change credentials for your own team's database (Already Set)
  2) Change SQL command to a relevant query that retrieves a small amount of data
  3) Create a JTextArea object using the queried data
  4) Add the new object to the JPanel p
*/

public class GUI extends JFrame implements ActionListener {
    static JFrame f;

    public static void main(String[] args) {
        // Building the connection
        Connection conn = null;
        String database_name = "team_cad_db";
        String database_user = "team_cad";
        String database_password = "cad";
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);

        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Failed!");
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "Opened database successfully");

        StringBuilder names = new StringBuilder(); // Store query results
        try {
            // Create a statement object
            Statement stmt = conn.createStatement();
            // SQL Query: Retrieve last names from the employee table
            String sqlStatement = "SELECT last_name FROM employee";
            ResultSet result = stmt.executeQuery(sqlStatement);
            
            // Fetch results and append to StringBuilder
            while (result.next()) {
                names.append(result.getString("last_name")).append("\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
            e.printStackTrace();
        }

        // Create a new frame
        f = new JFrame("DB GUI");

        // Create an object
        GUI s = new GUI();

        // Create a panel
        JPanel p = new JPanel();

        // Create a JTextArea to display the database results
        JTextArea textArea = new JTextArea(names.toString(), 10, 30);
        textArea.setEditable(false);

        // Add JTextArea to the panel
        p.add(new JScrollPane(textArea));

        // Create a Close button
        JButton b = new JButton("Close");
        b.addActionListener(s);
        p.add(b);

        // Add panel to frame
        f.add(p);

        // Set the size of frame
        f.setSize(400, 400);
        f.setVisible(true);

        // Closing the connection
        try {
            conn.close();
            JOptionPane.showMessageDialog(null, "Connection Closed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }

    // If button is pressed
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Close")) {
            f.dispose();
        }
    }
}
