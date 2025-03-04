import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EmployeeManagementPanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;

    public EmployeeManagementPanel() {
        setLayout(new BorderLayout());
        
        // Define table columns: Employee ID, First Name, Last Name, Position, PIN
        String[] columnNames = { "Employee ID", "First Name", "Last Name", "Position", "PIN" };
        
        // Create table model; all cells are non-editable (use dialogs for editing)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeeTable = new JTable(tableModel);
        loadEmployeeData();
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel for add, update, delete actions
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Employee");
        updateButton = new JButton("Update Selected Employee");
        deleteButton = new JButton("Delete Selected Employee");
        
        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateSelectedEmployee());
        deleteButton.addActionListener(e -> deleteSelectedEmployee());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // Load employee data from the database and populate the table
    private void loadEmployeeData() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(
                 "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                 dbSetup.user,
                 dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT Employee_ID, First_name, Last_name, Position, pin FROM Employee ORDER BY Employee_ID"
             )) 
        {
            while (rs.next()) {
                int id = rs.getInt("Employee_ID");
                String firstName = rs.getString("First_name");
                String lastName = rs.getString("Last_name");
                String position = rs.getString("Position");
                int pin = rs.getInt("pin");
                Object[] rowData = { id, firstName, lastName, position, pin };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching employee data.");
        }
    }
    
    // Get next available Employee_ID (auto-assignment)
    private int getNextEmployeeId() {
        int nextId = -1;
        try (Connection conn = DriverManager.getConnection(
                 "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                 dbSetup.user,
                 dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(Employee_ID) AS max_id FROM Employee")) 
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
    
    // Add a new employee using a dialog
    private void addEmployee() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField pinField = new JTextField();  // NEW: PIN field
        
        Object[] message = {
            "First Name:", firstNameField,
            "Last Name:", lastNameField,
            "Position:", positionField,
            "PIN:", pinField
        };
        
        int option = JOptionPane.showConfirmDialog(null, message, "Add New Employee", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String position = positionField.getText().trim();
            String pinStr = pinField.getText().trim();
            
            if (firstName.isEmpty() || lastName.isEmpty() || position.isEmpty() || pinStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled.");
                return;
            }
            
            int pinVal;
            try {
                pinVal = Integer.parseInt(pinStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "PIN must be a number.");
                return;
            }
            
            int nextId = getNextEmployeeId();
            if (nextId == -1) {
                JOptionPane.showMessageDialog(this, "Error determining next employee ID.");
                return;
            }
            
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd)) 
            {
                String query = "INSERT INTO Employee (Employee_ID, First_name, Last_name, Position, pin) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, nextId);
                pstmt.setString(2, firstName);
                pstmt.setString(3, lastName);
                pstmt.setString(4, position);
                pstmt.setInt(5, pinVal);
                pstmt.executeUpdate();
                
                loadEmployeeData();
                JOptionPane.showMessageDialog(this, "Employee added successfully with ID: " + nextId);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding employee.");
            }
        }
    }
    
    // Update the selected employee's data
    private void updateSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.");
            return;
        }
        
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentFirstName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentLastName = (String) tableModel.getValueAt(selectedRow, 2);
        String currentPosition = (String) tableModel.getValueAt(selectedRow, 3);
        int currentPin = (int) tableModel.getValueAt(selectedRow, 4);
        
        JTextField firstNameField = new JTextField(currentFirstName);
        JTextField lastNameField = new JTextField(currentLastName);
        JTextField positionField = new JTextField(currentPosition);
        JTextField pinField = new JTextField(String.valueOf(currentPin)); // NEW: PIN field with current value
        
        Object[] message = {
            "First Name:", firstNameField,
            "Last Name:", lastNameField,
            "Position:", positionField,
            "PIN:", pinField
        };
        
        int option = JOptionPane.showConfirmDialog(null, message, "Update Employee", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newFirstName = firstNameField.getText().trim();
            String newLastName = lastNameField.getText().trim();
            String newPosition = positionField.getText().trim();
            String pinStr = pinField.getText().trim();
            
            if (newFirstName.isEmpty() || newLastName.isEmpty() || newPosition.isEmpty() || pinStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled.");
                return;
            }
            
            int pinVal;
            try {
                pinVal = Integer.parseInt(pinStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "PIN must be a number.");
                return;
            }
            
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd)) 
            {
                String query = "UPDATE Employee SET First_name = ?, Last_name = ?, Position = ?, pin = ? WHERE Employee_ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newFirstName);
                pstmt.setString(2, newLastName);
                pstmt.setString(3, newPosition);
                pstmt.setInt(4, pinVal);
                pstmt.setInt(5, id);
                pstmt.executeUpdate();
                
                loadEmployeeData();
                JOptionPane.showMessageDialog(this, "Employee updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating employee.");
            }
        }
    }
    
    // Delete the selected employee
    private void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                     dbSetup.user,
                     dbSetup.pswd)) 
            {
                String query = "DELETE FROM Employee WHERE Employee_ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadEmployeeData();
                JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting employee.");
            }
        }
    }
    
    // Main method for testing this panel independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Employee Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.add(new EmployeeManagementPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
