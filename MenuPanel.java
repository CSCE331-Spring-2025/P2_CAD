import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MenuPanel extends JPanel {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;
    
    public MenuPanel() {
        setLayout(new BorderLayout());
        // Set the main panel background to orange (#ffc364)
        setBackground(new Color(0xFFC364));

        // Define table columns: ID, Name, Price, Seasonal
        String[] columnNames = { "ID", "Name", "Price", "Seasonal" };

        // Create table model (non-editable cells by default)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(tableModel);
        // Set table cell background and text colors for readability
        menuTable.setBackground(Color.WHITE);
        menuTable.setForeground(Color.BLACK);
        // Style the table header with dark orange background and white text
        menuTable.getTableHeader().setBackground(new Color(0xFF8C00));
        menuTable.getTableHeader().setForeground(Color.WHITE);
        
        loadMenuData();

        JScrollPane scrollPane = new JScrollPane(menuTable);
        // Set the viewport background to match the orange theme
        scrollPane.getViewport().setBackground(new Color(0xFFC364));
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        // Set button panel background to orange (#ffc364)
        buttonPanel.setBackground(new Color(0xFFC364));
        addButton = new JButton("Add Menu Item");
        updateButton = new JButton("Update Selected Item");
        deleteButton = new JButton("Delete Selected Item");

        addButton.addActionListener(e -> addMenuItem());
        updateButton.addActionListener(e -> updateSelectedItem());
        deleteButton.addActionListener(e -> deleteSelectedItem());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Load menu data from the database and populate the table
    private void loadMenuData() {
        tableModel.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection(
                 "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                 dbSetup.user,
                 dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT menu_id, name, price, COALESCE(seasonal, false) AS seasonal " +
                 "FROM Menu_Item ORDER BY menu_id"
             )) 
        {
            while (rs.next()) {
                int id = rs.getInt("menu_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                boolean isSeasonal = rs.getBoolean("seasonal");
                Object[] rowData = { id, name, price, isSeasonal };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching menu data.");
        }
    }

    // Add a new menu item using a dialog with a seasonal checkbox
    private void addMenuItem() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JCheckBox seasonalCheckBox = new JCheckBox("Seasonal", false);

        Object[] message = {
            "Menu Item Name:", nameField,
            "Price:", priceField,
            "Seasonal:", seasonalCheckBox
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add New Menu Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            double price;
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item name cannot be empty.");
                return;
            }
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price.");
                return;
            }
            boolean seasonal = seasonalCheckBox.isSelected();

            // Auto-assign the next available menu_id
            int nextId = getNextMenuId();
            if (nextId == -1) {
                JOptionPane.showMessageDialog(this, "Error determining next menu ID.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd))
            {
                String query = "INSERT INTO Menu_Item (menu_id, name, price, seasonal) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, nextId);
                pstmt.setString(2, name);
                pstmt.setDouble(3, price);
                pstmt.setBoolean(4, seasonal);
                pstmt.executeUpdate();
                loadMenuData();
                JOptionPane.showMessageDialog(this, "Menu item added successfully with ID: " + nextId);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding menu item.");
            }
        }
    }

    // Method to get next available menu_id
    private int getNextMenuId() {
        int nextId = -1;
        try (Connection conn = DriverManager.getConnection(
                 "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                 dbSetup.user,
                 dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(menu_id) AS max_id FROM Menu_Item"))
        {
            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            } else {
                nextId = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    // Update the selected menu item (include seasonal flag)
    private void updateSelectedItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        double currentPrice = (double) tableModel.getValueAt(selectedRow, 2);
        boolean currentSeasonal = (boolean) tableModel.getValueAt(selectedRow, 3);

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        JCheckBox seasonalCheckBox = new JCheckBox("Seasonal", currentSeasonal);

        Object[] message = {
            "New Menu Item Name:", nameField,
            "New Price:", priceField,
            "Seasonal:", seasonalCheckBox
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Update Menu Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            double newPrice;
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item name cannot be empty.");
                return;
            }
            try {
                newPrice = Double.parseDouble(priceField.getText().trim());
                if (newPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price.");
                return;
            }
            boolean seasonal = seasonalCheckBox.isSelected();
            
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd))
            {
                String query = "UPDATE Menu_Item SET name = ?, price = ?, seasonal = ? WHERE menu_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newName);
                pstmt.setDouble(2, newPrice);
                pstmt.setBoolean(3, seasonal);
                pstmt.setInt(4, id);
                pstmt.executeUpdate();
                loadMenuData();
                JOptionPane.showMessageDialog(this, "Menu item updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating menu item.");
            }
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd))
            {
                String query = "DELETE FROM Menu_Item WHERE menu_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadMenuData();
                JOptionPane.showMessageDialog(this, "Menu item deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting menu item.");
            }
        }
    }
    
    // Main method for testing this panel independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Menu Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            // Set frame background to the orange color
            frame.getContentPane().setBackground(new Color(0xFFC364));
            frame.add(new MenuPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
