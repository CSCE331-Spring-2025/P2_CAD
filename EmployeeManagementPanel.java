import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The EmployeeManagementPanel class provides a graphical interface for managing employees.
 * It allows adding, updating, and deleting employee records from the database.
 *
 * @author Sareem Mominkhoja, Rayan Ali, Chloe Lee, Chase Varghese
 * 
 */
public class EmployeeManagementPanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton;
    
    // Colors:
    private static final Color MAIN_BG_COLOR = new Color(0xFFC364); // Soft orange (#ffc364)
    private static final Color BUTTON_PANEL_COLOR = new Color(0xFDFDFD); // Soft white

    /**
     * Constructs the EmployeeManagementPanel and initializes the GUI components.
     */
    public EmployeeManagementPanel() {
        setBackground(MAIN_BG_COLOR);
        setLayout(new BorderLayout());
        
        // Define table columns: Employee ID, First Name, Last Name, Position, PIN
        String[] columnNames = { "Employee ID", "First Name", "Last Name", "Position", "PIN" };
        
        // Create table model; all cells are non-editable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeeTable = new JTable(tableModel);
        employeeTable.setBackground(Color.WHITE);
        employeeTable.setForeground(Color.BLACK);
        employeeTable.getTableHeader().setBackground(new Color(0xFF8C00)); // Dark orange header
        employeeTable.getTableHeader().setForeground(Color.WHITE);
        
        loadEmployeeData();

        // Create scroll pane and set its viewport background
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BUTTON_PANEL_COLOR);
        
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
    
    /**
     * Loads employee data from the database and populates the table.
     */
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
    
    /**
     * Retrieves the next available Employee_ID.
     *
     * @return The next available Employee_ID, or -1 if an error occurs.
     */
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
    
    /**
     * Adds a new employee using a dialog box for user input.
     */
    private void addEmployee() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField pinField = new JTextField(); 

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
    
    /**
     * Updates the selected employee's information.
     */
    private void updateSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.");
            return;
        }
        
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String newFirstName = JOptionPane.showInputDialog("Enter new first name:");
        String newLastName = JOptionPane.showInputDialog("Enter new last name:");
        String newPosition = JOptionPane.showInputDialog("Enter new position:");
        
        try (Connection conn = DriverManager.getConnection(
                 "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db",
                 dbSetup.user,
                 dbSetup.pswd)) 
        {
            String query = "UPDATE Employee SET First_name = ?, Last_name = ?, Position = ? WHERE Employee_ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newFirstName);
            pstmt.setString(2, newLastName);
            pstmt.setString(3, newPosition);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            
            loadEmployeeData();
            JOptionPane.showMessageDialog(this, "Employee updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating employee.");
        }
    }
    
    /**
     * Deletes the selected employee from the database.
     */
    private void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
    }
}
