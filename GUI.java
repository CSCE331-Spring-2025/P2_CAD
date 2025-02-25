import java.sql.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Database GUI Application
 * - Connects to a PostgreSQL database.
 * - Retrieves last names from the "employee" table.
 * - Displays data in a JTextArea within a JFrame.
 */
public class GUI extends JFrame implements ActionListener {
    static JFrame f;

    public static void main(String[] args) {
        new GUI().initialize();
    }

    public void initialize() {
        String databaseName = "team_cad_db";
        String databaseUser = "team_cad";
        String databasePassword = "cad";
        String databaseUrl = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", databaseName);

        StringBuilder names = new StringBuilder();

        // Establish database connection
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {
            JOptionPane.showMessageDialog(null, "Database Connection Successful!");

            // Query execution
            String sqlStatement = "SELECT last_name FROM employee";
            try (Statement stmt = conn.createStatement();
                 ResultSet result = stmt.executeQuery(sqlStatement)) {

                while (result.next()) {
                    names.append(result.getString("last_name")).append("\n");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            return;
        }

        // Build GUI with retrieved data
        createGUI(names.toString());
    }

    public void createGUI(String textContent) {
        f = new JFrame("DB GUI");

        // Create a panel
        JPanel panel = new JPanel();

        // JTextArea to display database results
        JTextArea textArea = new JTextArea(textContent, 10, 30);
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea));

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        panel.add(closeButton);

        // Add panel to frame
        f.add(panel);

        // Frame settings
        f.setSize(400, 400);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Close")) {
            f.dispose();
        }
    }
}
