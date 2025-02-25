import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InventoryPage extends JPanel {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, updateButton;

    public InventoryPage() {
        setLayout(new BorderLayout());

        // Table column names: ID, Name, Category, Current Stock
        String[] columnNames = { "ID", "Name", "Category", "Current Stock" };

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No cell is editable in this view
            }
        };

        inventoryTable = new JTable(tableModel);
        loadInventoryData(); // Populate data from the database

        // Add table inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create a panel for action buttons
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Inventory Item");
        deleteButton = new JButton("Delete Selected Item");
        updateButton = new JButton("Update Selected Item");

        addButton.addActionListener(e -> addNewItem());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        updateButton.addActionListener(e -> updateSelectedItem());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadInventoryData() {
        tableModel.setRowCount(0); // Clear existing data

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            Statement stmt = conn.createStatement();
            // Query includes the category column
            ResultSet rs = stmt.executeQuery("SELECT inventory_id, name, category, current_number FROM inventory");

            while (rs.next()) {
                int id = rs.getInt("inventory_id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                int currentStock = rs.getInt("current_number");

                Object[] rowData = { id, name, category, currentStock };
                tableModel.addRow(rowData);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching inventory data.");
        }
    }

    private void addNewItem() {
        // Create input fields for item name, category, and stock quantity
        JTextField itemNameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField stockField = new JTextField();

        Object[] message = {
            "Item Name:", itemNameField,
            "Category (optional):", categoryField,
            "Stock Quantity:", stockField
        };

        int option = JOptionPane.showConfirmDialog(
            null, message, "Add New Inventory Item", JOptionPane.OK_CANCEL_OPTION
        );
        if (option == JOptionPane.OK_OPTION) {
            String itemName = itemNameField.getText().trim();
            String category = categoryField.getText().trim();
            int stockQuantity;

            if (itemName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item name cannot be empty.");
                return;
            }
            try {
                stockQuantity = Integer.parseInt(stockField.getText().trim());
                if (stockQuantity < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid stock quantity.");
                return;
            }

            // Auto assign the next available inventory_id
            int nextId = getNextInventoryId();

            if (nextId == -1) {
                JOptionPane.showMessageDialog(this, "Error determining next inventory ID.");
                return;
            }

            // Insert new item into the database with auto-assigned id
            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user,
                    dbSetup.pswd
                );
                String query = "INSERT INTO inventory (inventory_id, name, category, current_number) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, nextId);
                pstmt.setString(2, itemName);
                if (category.isEmpty()) {
                    pstmt.setNull(3, Types.VARCHAR);
                } else {
                    pstmt.setString(3, category);
                }
                pstmt.setInt(4, stockQuantity);
                pstmt.executeUpdate();
                conn.close();

                // Refresh the inventory table
                loadInventoryData();

                JOptionPane.showMessageDialog(this, "Item added successfully with ID: " + nextId);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding item to inventory.");
            }
        }
    }

    // Method to query the next available inventory_id
    private int getNextInventoryId() {
        int nextId = -1;
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(inventory_id) AS max_id FROM inventory");
            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            } else {
                nextId = 1; // Start at 1 if no records exist
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    private void deleteSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0); // Get inventory_id from first column

        int confirm = JOptionPane.showConfirmDialog(
            this, "Are you sure you want to delete this item?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user,
                    dbSetup.pswd
                );
                String query = "DELETE FROM inventory WHERE inventory_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                conn.close();

                // Refresh the table after deletion
                loadInventoryData();

                JOptionPane.showMessageDialog(this, "Item deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting item from inventory.");
            }
        }
    }

    // New method: Update selected item's current stock
    private void updateSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }
        
        // Get the current stock and id of the selected item
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int currentStock = (int) tableModel.getValueAt(selectedRow, 3);

        // Ask user for new current stock value
        String newStockStr = JOptionPane.showInputDialog(this, "Enter new current stock for item ID " + id + ":", currentStock);
        if (newStockStr == null) {
            // User cancelled
            return;
        }
        
        int newStock;
        try {
            newStock = Integer.parseInt(newStockStr.trim());
            if (newStock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for stock.");
            return;
        }
        
        // Update the database with the new stock value
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            String query = "UPDATE inventory SET current_number = ? WHERE inventory_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            conn.close();
            
            // Refresh the table after update
            loadInventoryData();
            JOptionPane.showMessageDialog(this, "Item updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating inventory item.");
        }
    }

    // Main method for testing InventoryPage independently (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.add(new InventoryPage());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
