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

        // 1. Total revenue label at the top (for the past 39 weeks)
        double totalRevenue = getTotalRevenue();
        JLabel revenueLabel = new JLabel("Total Revenue (Past 39 Weeks): $" + String.format("%.2f", totalRevenue));
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        revenueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(revenueLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Best Days of the Week Panel (X-Report)
        JPanel dowPanel = new JPanel(new BorderLayout());
        dowPanel.setBorder(new TitledBorder("Best Days of the Week (Past 39 Weeks) [X-Report]"));
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

        // 3. Top 10 Best Selling Menu Items Panel
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

        // 4. Top 25 Best Days Overall Panel (Z-Report)
        JPanel bestDaysPanel = new JPanel(new BorderLayout());
        bestDaysPanel.setBorder(new TitledBorder("Top 25 Best Days (Overall) [Z-Report]"));
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

        // 5. Daily Report, X-Report, Z-Report Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dailyReportButton = new JButton("Generate Daily Report");
        dailyReportButton.addActionListener(e -> generateDailyReport());
        buttonPanel.add(dailyReportButton);

        JButton xReportButton = new JButton("X-Report (Hourly, Current Day)");
        xReportButton.addActionListener(e -> generateXReport());
        buttonPanel.add(xReportButton);

        JButton zReportButton = new JButton("Z-Report (End of Day)");
        zReportButton.addActionListener(e -> generateZReport());
        buttonPanel.add(zReportButton);

        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(buttonPanel);
    }

    // ------------------ Helper Methods ------------------

    private double getTotalRevenue() {
        double revenue = 0.0;
        String query = "SELECT SUM(Total_Price) AS total_revenue FROM customer_order " +
                       "WHERE Time >= CURRENT_DATE - INTERVAL '39 weeks'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                revenue = rs.getDouble("total_revenue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenue;
    }

    private Object[][] getBestDaysOfWeek() {
        Object[][] data = new Object[7][2];
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int i = 0; i < 7; i++) {
            data[i][0] = dayNames[i];
            data[i][1] = 0.0;
        }
        String query = "SELECT EXTRACT(DOW FROM Time) AS day, SUM(Total_Price) AS revenue " +
                       "FROM customer_order " +
                       "WHERE Time >= CURRENT_DATE - INTERVAL '39 weeks' " +
                       "GROUP BY day " +
                       "ORDER BY day";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int day = rs.getInt("day");
                double rev = rs.getDouble("revenue");
                if (day >= 0 && day < 7) {
                    data[day][1] = rev;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private JPanel createBestDaysPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Best Days of the Week (Past 39 Weeks) [X-Report]"));
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[]{"Day", "Revenue"}, 0);
        Object[][] data = getBestDaysOfWeek();
        for (Object[] row : data) {
            model.addRow(row);
        }
        table.setModel(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Object[][] getTopMenuItems() {
        String query = "SELECT m.name, COUNT(*) AS sold_count " +
                       "FROM C_M_Junction cmj " +
                       "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
                       "GROUP BY m.name " +
                       "ORDER BY sold_count DESC " +
                       "LIMIT 10";
        Object[][] data = new Object[10][2];
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int index = 0;
            while (rs.next() && index < 10) {
                data[index][0] = rs.getString("name");
                data[index][1] = rs.getInt("sold_count");
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private JPanel createBestSellingMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Top 10 Best Selling Menu Items"));
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        Object[][] data = getTopMenuItems();
        for (Object[] row : data) {
            model.addRow(row);
        }
        table.setModel(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Object[][] getBestDaysOfYear() {
        String query = "SELECT DATE(Time) AS order_date, SUM(Total_Price) AS revenue " +
                       "FROM customer_order " +
                       "GROUP BY order_date " +
                       "ORDER BY revenue DESC " +
                       "LIMIT 25";
        Object[][] data = new Object[25][2];
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int index = 0;
            while (rs.next() && index < 25) {
                data[index][0] = rs.getDate("order_date").toString();
                data[index][1] = rs.getDouble("revenue");
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private JPanel createBestDaysOverallPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Top 25 Best Days (Overall) [Z-Report]"));
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Revenue"}, 0);
        Object[][] data = getBestDaysOfYear();
        for (Object[] row : data) {
            model.addRow(row);
        }
        table.setModel(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDailyReportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Generate Daily Report");
        button.addActionListener(e -> generateDailyReport());
        panel.add(button);
        return panel;
    }

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

        int totalOrders = 0;
        double totalRevenue = 0.0;
        String query1 = "SELECT COUNT(*) AS orders, SUM(Total_Price) AS revenue FROM customer_order WHERE DATE(Time) = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query1)) {
            pstmt.setDate(1, sqlDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalOrders = rs.getInt("orders");
                    totalRevenue = rs.getDouble("revenue");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching daily report data.");
            return;
        }

        DefaultTableModel topItemsModel = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        String query2 = "SELECT m.name, COUNT(*) AS sold_count " +
                        "FROM customer_order co " +
                        "JOIN C_M_Junction cmj ON co.Order_ID = cmj.Order_ID " +
                        "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
                        "WHERE DATE(co.Time) = ? " +
                        "GROUP BY m.name " +
                        "ORDER BY sold_count DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query2)) {
            pstmt.setDate(1, sqlDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topItemsModel.addRow(new Object[]{rs.getString("name"), rs.getInt("sold_count")});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching top items data.");
            return;
        }

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

    private void generateXReport() {
        // 1) Sales per hour
        DefaultTableModel hourSalesModel = new DefaultTableModel(new String[]{"Hour", "Total Sales"}, 0);
        String querySalesPerHour =
            "SELECT EXTRACT(HOUR FROM Time) AS hr, SUM(Total_Price) AS sales " +
            "FROM customer_order " +
            "WHERE DATE(Time) = CURRENT_DATE " +
            "GROUP BY hr " +
            "ORDER BY hr";
        runQueryToTable(querySalesPerHour, hourSalesModel);

        // 2) Number of menu items ordered per hour
        DefaultTableModel hourItemsModel = new DefaultTableModel(new String[]{"Hour", "Items Ordered"}, 0);
        String queryItemsPerHour =
            "SELECT EXTRACT(HOUR FROM co.Time) AS hr, COUNT(*) AS items_ordered " +
            "FROM C_M_Junction cmj " +
            "JOIN customer_order co ON co.Order_ID = cmj.Order_ID " +
            "WHERE DATE(co.Time) = CURRENT_DATE " +
            "GROUP BY hr " +
            "ORDER BY hr";
        runQueryToTable(queryItemsPerHour, hourItemsModel);

        // 3) All menu items sold today (from greatest to least)
        DefaultTableModel topMenuItemModel = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        String queryTopMenuToday =
            "SELECT m.name, COUNT(*) AS cnt " +
            "FROM C_M_Junction cmj " +
            "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
            "JOIN customer_order co ON co.Order_ID = cmj.Order_ID " +
            "WHERE DATE(co.Time) = CURRENT_DATE " +
            "GROUP BY m.name " +
            "ORDER BY cnt DESC";
        runQueryToTable(queryTopMenuToday, topMenuItemModel);

        // 4) All inventory items used (today)
        DefaultTableModel inventoryUsedModel = new DefaultTableModel(new String[]{"Inventory Item", "Used Count"}, 0);
        String queryInventoryUsedToday =
            "SELECT i.name, COUNT(*) AS used_count " +
            "FROM C_M_Junction cmj " +
            "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
            "JOIN m_i_junction mi ON m.Menu_ID = mi.Menu_ID " +
            "JOIN Inventory i ON i.inventory_id = mi.inventory_id " +
            "JOIN customer_order co ON co.Order_ID = cmj.Order_ID " +
            "WHERE DATE(co.Time) = CURRENT_DATE " +
            "GROUP BY i.name " +
            "ORDER BY used_count DESC";
        runQueryToTable(queryInventoryUsedToday, inventoryUsedModel);

        JPanel xPanel = new JPanel();
        xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.Y_AXIS));

        xPanel.add(new JLabel("X-Report: Hourly (Current Day)"));
        xPanel.add(new JLabel("Sales per Hour:"));
        JTable hourSalesTable = new JTable(hourSalesModel);
        xPanel.add(new JScrollPane(hourSalesTable));

        xPanel.add(new JLabel("Menu Items Ordered per Hour:"));
        JTable hourItemsTable = new JTable(hourItemsModel);
        xPanel.add(new JScrollPane(hourItemsTable));

        xPanel.add(new JLabel("All Menu Items Sold Today (Greatest to Least):"));
        JTable topMenuTodayTable = new JTable(topMenuItemModel);
        xPanel.add(new JScrollPane(topMenuTodayTable));

        xPanel.add(new JLabel("All Inventory Items Used (Today):"));
        JTable inventoryUsedTable = new JTable(inventoryUsedModel);
        xPanel.add(new JScrollPane(inventoryUsedTable));

        JScrollPane scrollPane = new JScrollPane(xPanel);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "X-Report (Current Day)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateZReport() {
        // 1) Total Sales for the Day
        double totalSales = 0.0;
        String queryTotalSales =
            "SELECT SUM(Total_Price) AS sales " +
            "FROM customer_order " +
            "WHERE DATE(Time) = CURRENT_DATE";

        // 2) All menu items sold today (from greatest to least)
        DefaultTableModel topMenuItemModel = new DefaultTableModel(new String[]{"Menu Item", "Sold Count"}, 0);
        String queryTopMenuToday =
            "SELECT m.name, COUNT(*) AS cnt " +
            "FROM C_M_Junction cmj " +
            "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
            "JOIN customer_order co ON co.Order_ID = cmj.Order_ID " +
            "WHERE DATE(co.Time) = CURRENT_DATE " +
            "GROUP BY m.name " +
            "ORDER BY cnt DESC";
        runQueryToTable(queryTopMenuToday, topMenuItemModel);

        // 3) All inventory items used (today)
        DefaultTableModel inventoryUsedModel = new DefaultTableModel(new String[]{"Inventory Item", "Used Count"}, 0);
        String queryInventoryUsedToday =
            "SELECT i.name, COUNT(*) AS used_count " +
            "FROM C_M_Junction cmj " +
            "JOIN Menu_Item m ON cmj.Menu_ID = m.Menu_ID " +
            "JOIN m_i_junction mi ON m.Menu_ID = mi.Menu_ID " +
            "JOIN Inventory i ON i.inventory_id = mi.inventory_id " +
            "JOIN customer_order co ON co.Order_ID = cmj.Order_ID " +
            "WHERE DATE(co.Time) = CURRENT_DATE " +
            "GROUP BY i.name " +
            "ORDER BY used_count DESC";
        runQueryToTable(queryInventoryUsedToday, inventoryUsedModel);

        totalSales = runQueryForTotalSales(queryTotalSales);

        JPanel zPanel = new JPanel();
        zPanel.setLayout(new BoxLayout(zPanel, BoxLayout.Y_AXIS));

        zPanel.add(new JLabel("Z-Report (End of Day)"));
        zPanel.add(new JLabel("Total Sales (Today): $" + String.format("%.2f", totalSales)));

        zPanel.add(new JLabel("All Menu Items Sold Today (Greatest to Least):"));
        JTable topMenuTable = new JTable(topMenuItemModel);
        zPanel.add(new JScrollPane(topMenuTable));

        zPanel.add(new JLabel("All Inventory Items Used (Today):"));
        JTable invUsedTable = new JTable(inventoryUsedModel);
        zPanel.add(new JScrollPane(invUsedTable));

        JScrollPane scrollPane = new JScrollPane(zPanel);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Z-Report (End of Day)", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------ Helper Methods ------------------
    private void runQueryToTable(String query, DefaultTableModel model) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int colCount = model.getColumnCount();
                Object[] rowData = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    rowData[i] = rs.getObject(i + 1);
                }
                model.addRow(rowData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double runQueryForTotalSales(String query) {
        double result = 0.0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                result = rs.getDouble(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // ------------------ Main Method ------------------
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
