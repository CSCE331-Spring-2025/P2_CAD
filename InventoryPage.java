import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * InventoryPage handles the display and management of inventory items.
 * It allows adding, deleting, and updating items from the PostgreSQL database.
 * The UI is styled with an orange theme.
 *
 * @author Sareem Mominkhoja,
 *         Rayan Ali,
 *         Chloe Lee,
 *         Chase Varghese
 */
public class InventoryPage extends JPanel {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, updateButton;

    /**
     * Constructs the inventory panel with table, buttons, and layout.
     */
    public InventoryPage() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xFFC364)); // Light orange

        String[] columnNames = { "ID", "Name", "Category", "Current Stock", "Suggestion" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.getTableHeader().setBackground(new Color(0xFF8C00));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);

        loadInventoryData();

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.getViewport().setBackground(new Color(0xFFC364));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0xFFC364));

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

    /**
     * Loads inventory data from the database into the table.
     */
    private void loadInventoryData() {
        tableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT inventory_id, name, category, current_number, inventory_suggestion FROM inventory ORDER BY inventory_id ASC")) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("inventory_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("current_number"),
                    rs.getInt("inventory_suggestion")
                };
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching inventory data.");
        }
    }

    /**
     * Opens a dialog to add a new inventory item.
     */
    private void addNewItem() {
        JTextField itemNameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField stockField = new JTextField();

        Object[] message = {
            "Item Name:", itemNameField,
            "Category (optional):", categoryField,
            "Stock Quantity:", stockField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add New Inventory Item", JOptionPane.OK_CANCEL_OPTION);
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

            int nextId = getNextInventoryId();
            if (nextId == -1) {
                JOptionPane.showMessageDialog(this, "Error determining next inventory ID.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user, dbSetup.pswd)) {
                String query = "INSERT INTO inventory (inventory_id, name, category, current_number) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, nextId);
                pstmt.setString(2, itemName);
                pstmt.setString(3, category.isEmpty() ? null : category);
                pstmt.setInt(4, stockQuantity);
                pstmt.executeUpdate();

                loadInventoryData();
                JOptionPane.showMessageDialog(this, "Item added successfully with ID: " + nextId);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding item to inventory.");
            }
        }
    }

    /**
     * Gets the next available inventory ID from the database.
     * @return the next ID, or -1 if there is an error.
     */
    private int getNextInventoryId() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(inventory_id) AS max_id FROM inventory")) {
            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes the selected inventory item from the database.
     */
    private void deleteSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
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
                    dbSetup.user, dbSetup.pswd)) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM inventory WHERE inventory_id = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadInventoryData();
                JOptionPane.showMessageDialog(this, "Item deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting item from inventory.");
            }
        }
    }

    /**
     * Opens a dialog to update the selected item's stock quantity.
     */
    private void updateSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int currentStock = (int) tableModel.getValueAt(selectedRow, 3);

        String newStockStr = JOptionPane.showInputDialog(this, "Enter new current stock for item ID " + id + ":", currentStock);
        if (newStockStr == null) return;

        try {
            int newStock = Integer.parseInt(newStockStr.trim());
            if (newStock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user, dbSetup.pswd)) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE inventory SET current_number = ? WHERE inventory_id = ?");
                pstmt.setInt(1, newStock);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                loadInventoryData();
                JOptionPane.showMessageDialog(this, "Item updated successfully.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for stock.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating inventory item.");
        }
    }

    /**
     * Main method to run InventoryPage independently.
     * @param args not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.getContentPane().setBackground(new Color(0xFFC364));
            frame.add(new InventoryPage());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
