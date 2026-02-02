package com.ticketbox.view;

import com.ticketbox.dao.StatisticsDAO;
import com.ticketbox.model.EventStatDTO;
import com.ticketbox.model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class StatisticsPanel extends JPanel {
    private User organizer;
    private StatisticsDAO statisticsDAO;
    private JTable table;
    private DefaultTableModel tableModel;

    public StatisticsPanel(User organizer) {
        this.organizer = organizer;
        this.statisticsDAO = new StatisticsDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(com.ticketbox.util.ThemeColor.BG_MAIN);
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(com.ticketbox.util.ThemeColor.BG_MAIN); 
        JLabel lblHeader = new JLabel("Báo cáo Doanh thu & Thống kê");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(com.ticketbox.util.ThemeColor.TEXT_PRIMARY);
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content (Split Pane: Table + Chart)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(com.ticketbox.util.ThemeColor.BG_MAIN);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        
        // 1. Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(com.ticketbox.util.ThemeColor.BG_CARD);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTableTitle = new JLabel("Chi tiết theo Sự kiện");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTableTitle.setForeground(com.ticketbox.util.ThemeColor.TEXT_PRIMARY);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        
        String[] columnNames = {"Tên sự kiện", "Tổng vé", "Đã bán", "Doanh thu (ước tính)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override // Disable editing
            public boolean isCellEditable(int row, int column) { return false; }
            @Override // Column types for sorting/rendering
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class;
                if (columnIndex > 0) return Integer.class;
                return String.class;
            }
        };
        table = new JTable(tableModel);
        com.ticketbox.view.component.TableStyler.applyStyle(table); // Apply Styling
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(com.ticketbox.util.ThemeColor.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        splitPane.setTopComponent(tablePanel);
        
        // 2. Chart Panel
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(com.ticketbox.util.ThemeColor.BG_CARD);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Placeholder for chart, populated in loadData
        chartContainer.setName("ChartPanel");
        
        splitPane.setBottomComponent(chartContainer);
        splitPane.setContinuousLayout(true);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Refresh Button
        JButton btnRefresh = new JButton("Làm mới dữ liệu");
        btnRefresh.setBackground(com.ticketbox.util.ThemeColor.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> loadData());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(com.ticketbox.util.ThemeColor.BG_MAIN);
        footer.add(btnRefresh);
        add(footer, BorderLayout.SOUTH);
    }
    
    public void loadData() {
        tableModel.setRowCount(0);
        List<EventStatDTO> stats = statisticsDAO.getEventStatistics(organizer.getId());
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        


        for (EventStatDTO s : stats) {
            tableModel.addRow(new Object[]{
                s.getEventName(),
                s.getTotalTickets(),
                s.getSoldTickets(),
                s.getRevenue() 
            });
            
            dataset.addValue(s.getRevenue(), "Doanh thu", s.getEventName());
        }
        
        // Create Chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Top Doanh thu theo Sự kiện",
                "Sự kiện",
                "Doanh thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);
                
        // Customize Chart Theme
        barChart.setBackgroundPaint(com.ticketbox.util.ThemeColor.BG_CARD);
        barChart.getTitle().setPaint(Color.WHITE);
        
        org.jfree.chart.plot.CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(com.ticketbox.util.ThemeColor.BG_CARD);
        plot.setDomainGridlinePaint(new Color(60, 60, 60));
        plot.setRangeGridlinePaint(new Color(60, 60, 60));
        plot.setOutlinePaint(null);
        
        // Axis colors
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        
        // Bar color
        org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, com.ticketbox.util.ThemeColor.PRIMARY);
        renderer.setDrawBarOutline(false);
        
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(com.ticketbox.util.ThemeColor.BG_CARD);
        
        // Find Chart Container in SplitPane Bottom
        JSplitPane splitPane = (JSplitPane) ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        JPanel chartContainer = (JPanel) splitPane.getBottomComponent();
        
        chartContainer.removeAll();
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }
}
