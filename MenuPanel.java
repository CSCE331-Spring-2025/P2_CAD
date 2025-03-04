import java.awt.*;
import java.sql.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MenuPanel extends JPanel {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, toggleSeasonalButton;

    private Map<String, SeasonalItem> seasonalItems;

    public MenuPanel() {
        setLayout(new BorderLayout());

        // Define table columns: ID, Name, Price
        String[] columnNames = { "ID", "Name", "Price", "Seasonal" };

        // Create table model (non-editable cells by default)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(tableModel);

        loadMenuData();

        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Menu Item");
        updateButton = new JButton("Update Selected Item");
        deleteButton = new JButton("Delete Selected Item");
        toggleSeasonalButton = new JButton("Add Seasonal Items");

        addButton.addActionListener(e -> addMenuItem());
        updateButton.addActionListener(e -> updateSelectedItem());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        toggleSeasonalButton.addActionListener(e -> toggleSeasonalItems());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleSeasonalButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Inner class to represent Seasonal Items
    private class SeasonalItem {
        int id;
        String name;
        double price;
        String seasonal;
    
        SeasonalItem(int id, String name, double price, String season) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.seasonal = season;
        }
    }

    private void toggleSeasonalItems() {
        // Define seasonal items
        SeasonalItem[] seasonalMenu = {
            new SeasonalItem(getNextMenuId(), "Spring Blossom Salad", 12.99, "Spring"),
            new SeasonalItem(getNextMenuId(), "Summer Sunset Smoothie", 5.99, "Summer"),
            new SeasonalItem(getNextMenuId(), "Autumn Harvest Soup", 8.99, "Autumn"),
            new SeasonalItem(getNextMenuId(), "Winter Comfort Stew", 14.99, "Winter")
        };

        // Toggle seasonal items
        for (SeasonalItem item : seasonalMenu) {
            if (!seasonalItems.containsKey(item.name)) {
                // Add seasonal item to database and menu
                addSeasonalItemToDatabase(item);
                seasonalItems.put(item.name, item);
                toggleSeasonalButton.setText("Remove Seasonal Items");
            } else {
                // Remove seasonal item from database and menu
                removeSeasonalItemFromDatabase(item);
                seasonalItems.remove(item.name);
                toggleSeasonalButton.setText("Add Seasonal Items");
            }
        }

        // Reload menu data to reflect changes
        loadMenuData();
    }

    private void addSeasonalItemToDatabase(SeasonalItem item) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            String query = "INSERT INTO Menu_Item (menu_id, name, price, seasonal) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, item.id);
            pstmt.setString(2, item.name);
            pstmt.setDouble(3, item.price);
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding seasonal menu item.");
        }
    }

    private void removeSeasonalItemFromDatabase(SeasonalItem item) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            String query = "DELETE FROM Menu_Item WHERE menu_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, item.id);
            pstmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing seasonal menu item.");
        }
    }

    private void loadMenuData() {
        tableModel.setRowCount(0); // Clear existing data

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            Statement stmt = conn.createStatement();
            // Updated query to include seasonal flag
            ResultSet rs = stmt.executeQuery(
                "SELECT menu_id, name, price, COALESCE(seasonal, false) AS seasonal " +
                "FROM Menu_Item ORDER BY menu_id"
            );

            while (rs.next()) {
                int id = rs.getInt("menu_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                boolean isSeasonal = rs.getBoolean("seasonal");
                Object[] rowData = { id, name, price, isSeasonal };
                tableModel.addRow(rowData);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching menu data.");
        }
    }

    private void addMenuItem() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] message = {
            "Menu Item Name:", nameField,
            "Price:", priceField
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
            // Auto-assign the next available menu_id
            int nextId = getNextMenuId();
            if (nextId == -1) {
                JOptionPane.showMessageDialog(this, "Error determining next menu ID.");
                return;
            }

            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user,
                    dbSetup.pswd
                );
                String query = "INSERT INTO Menu_Item (menu_id, name, price) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, nextId);
                pstmt.setString(2, name);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate();
                conn.close();

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
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                dbSetup.user,
                dbSetup.pswd
            );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(menu_id) AS max_id FROM Menu_Item");
            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            } else {
                nextId = 1;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    private void updateSelectedItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        double currentPrice = (double) tableModel.getValueAt(selectedRow, 2);

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));

        Object[] message = {
            "New Menu Item Name:", nameField,
            "New Price:", priceField
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
            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user,
                    dbSetup.pswd
                );
                String query = "UPDATE Menu_Item SET name = ?, price = ? WHERE menu_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newName);
                pstmt.setDouble(2, newPrice);
                pstmt.setInt(3, id);
                pstmt.executeUpdate();
                conn.close();

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
            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                    dbSetup.user,
                    dbSetup.pswd
                );
                String query = "DELETE FROM Menu_Item WHERE menu_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                conn.close();

                loadMenuData();
                JOptionPane.showMessageDialog(this, "Menu item deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting menu item.");
            }
        }
    }
    
    // Main method for testing independently (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Menu Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.add(new MenuPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}