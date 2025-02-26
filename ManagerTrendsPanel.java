import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ManagerTrendsPanel extends JPanel {
    // Database credentials from dbSetup
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_cad_db";
    private static final String DB_USER = dbSetup.user;
    private static final String DB_PASSWORD = dbSetup.pswd;

    public ManagerTrendsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Total revenue label at the top (for the past 39 weeks)
        double totalRevenue = getTotalRevenue();
        JLabel revenueLabel = new JLabel("Total Revenue (Past 39 Weeks): $" + String.format("%.2f", totalRevenue));
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        revenueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(revenueLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Best Days of the Week Panel
        JPanel dowPanel = new JPanel(new BorderLayout());
        dowPanel.setBorder(new TitledBorder("Best Days of the Week (Past 39 Weeks)"));
        dowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTable dowTable = new JTable();
        DefaultTableModel dowModel = new DefaultTableModel(new String[]{"Day", "Revenue"}, 0);
        Object[][] dowData = getBestDaysOfWeek();
        for (Object[] row : dowData) {
            dowModel.addRow(row);
        }
        dowTable.setModel(dowModel);
        dowPanel.add(new JScrollPane(dowTable), BorderLayout.CENTER);
        add(dowPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Top 10 Best Selling Menu Items Panel
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(new TitledBorder("Top 10 Best Selling Menu Items"));
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTable menuTable = new JTable();
        DefaultTableModel menuModel = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        Object[][] menuData = getTopMenuItems();
        for (Object[] row : menuData) {
            menuModel.addRow(row);
        }
        menuTable.setModel(menuModel);
        menuPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        add(menuPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Top 25 Best Days Overall Panel
        JPanel bestDaysPanel = new JPanel(new BorderLayout());
        bestDaysPanel.setBorder(new TitledBorder("Top 25 Best Days (Overall)"));
        bestDaysPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTable bestDaysTable = new JTable();
        DefaultTableModel bestDaysModel = new DefaultTableModel(new String[]{"Date", "Revenue"}, 0);
        Object[][] bestDaysData = getBestDaysOfYear();
        for (Object[] row : bestDaysData) {
            bestDaysModel.addRow(row);
        }
        bestDaysTable.setModel(bestDaysModel);
        bestDaysPanel.add(new JScrollPane(bestDaysTable), BorderLayout.CENTER);
        add(bestDaysPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // NEW: Daily Report Button Panel
        JPanel dailyReportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dailyReportButton = new JButton("Generate Daily Report");
        dailyReportButton.addActionListener(e -> generateDailyReport());
        dailyReportPanel.add(dailyReportButton);
        dailyReportPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(dailyReportPanel);
    }

    // Query total revenue for past 39 weeks
    private double getTotalRevenue() {
        double revenue = 0.0;
        String query = "SELECT SUM(Total_Price) AS total_revenue FROM customer_order " +
                       "WHERE Time >= CURRENT_DATE - INTERVAL '39 weeks'";
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                revenue = rs.getDouble("total_revenue");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenue;
    }

    // Query best days of the week for past 39 weeks (chronologically ordered)
    private Object[][] getBestDaysOfWeek() {
        Object[][] data = new Object[7][2];
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        // Initialize with zero revenue
        for (int i = 0; i < 7; i++) {
            data[i][0] = dayNames[i];
            data[i][1] = 0.0;
        }
        String query = "SELECT EXTRACT(DOW FROM Time) AS day, SUM(Total_Price) AS revenue " +
                       "FROM customer_order " +
                       "WHERE Time >= CURRENT_DATE - INTERVAL '39 weeks' " +
                       "GROUP BY day " +
                       "ORDER BY day";
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int day = rs.getInt("day");
                double rev = rs.getDouble("revenue");
                if (day >= 0 && day < 7) {
                    data[day][0] = dayNames[day];
                    data[day][1] = rev;
                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Query top 10 best selling menu items
    private Object[][] getTopMenuItems() {
        String query = "SELECT m.name, COUNT(*) AS sold_count " +
                       "FROM C_M_Junction cmj " +
                       "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
                       "GROUP BY m.name " +
                       "ORDER BY sold_count DESC " +
                       "LIMIT 10";
        Object[][] data = new Object[10][2];
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int index = 0;
            while (rs.next() && index < 10) {
                data[index][0] = rs.getString("name");
                data[index][1] = rs.getInt("sold_count");
                index++;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Query top 25 best days overall by revenue
    private Object[][] getBestDaysOfYear() {
        String query = "SELECT DATE(Time) AS order_date, SUM(Total_Price) AS revenue " +
                       "FROM customer_order " +
                       "GROUP BY order_date " +
                       "ORDER BY revenue DESC " +
                       "LIMIT 25";
        Object[][] data = new Object[25][2];
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int index = 0;
            while (rs.next() && index < 25) {
                data[index][0] = rs.getDate("order_date").toString();
                data[index][1] = rs.getDouble("revenue");
                index++;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // NEW: Generate a daily report for a specific date
    private void generateDailyReport() {
        String dateStr = JOptionPane.showInputDialog(this, "Enter date (YYYY-MM-DD):");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return;
        }
        java.sql.Date sqlDate;
        try {
            sqlDate = java.sql.Date.valueOf(dateStr.trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        // Query for total orders and revenue for that day
        int totalOrders = 0;
        double totalRevenue = 0.0;
        String query1 = "SELECT COUNT(*) AS orders, SUM(Total_Price) AS revenue FROM customer_order WHERE DATE(Time) = ?";
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(query1);
            pstmt.setDate(1, sqlDate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalOrders = rs.getInt("orders");
                totalRevenue = rs.getDouble("revenue");
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching daily report data.");
            return;
        }

        // Query for top 3 best-selling menu items on that day
        DefaultTableModel topItemsModel = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        String query2 = "SELECT m.name, COUNT(*) AS sold_count " +
                        "FROM customer_order co " +
                        "JOIN C_M_Junction cmj ON co.Order_ID = cmj.Order_ID " +
                        "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
                        "WHERE DATE(co.Time) = ? " +
                        "GROUP BY m.name " +
                        "ORDER BY sold_count DESC " +
                        "LIMIT 3";
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(query2);
            pstmt.setDate(1, sqlDate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                topItemsModel.addRow(new Object[]{rs.getString("name"), rs.getInt("sold_count")});
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching top items data.");
            return;
        }

        // Create a panel to display the report
        JPanel reportPanel = new JPanel(new BorderLayout());
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setText("Daily Report for " + dateStr + "\n\n");
        reportArea.append("Total Orders: " + totalOrders + "\n");
        reportArea.append("Total Revenue: $" + String.format("%.2f", totalRevenue) + "\n\n");

        JTable topItemsTable = new JTable(topItemsModel);
        JScrollPane tableScroll = new JScrollPane(topItemsTable);
        tableScroll.setPreferredSize(new Dimension(400, 100));

        reportPanel.add(reportArea, BorderLayout.NORTH);
        reportPanel.add(tableScroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, reportPanel, "Daily Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // For testing independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Manager Trends");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.add(new ManagerTrendsPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
