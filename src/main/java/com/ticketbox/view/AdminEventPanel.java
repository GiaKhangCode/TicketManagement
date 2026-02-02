package com.ticketbox.view;

import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.TableStyler;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminEventPanel extends JPanel {
    private EventDAO eventDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Event> allEvents;
    
    public AdminEventPanel() {
        this.eventDAO = new EventDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // 1. Header
        JPanel headerPanel = new JPanel(new GridBagLayout()); 
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 20);
        
        // Title
        JLabel lblTitle = new JLabel("Quản lý Sự kiện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        headerPanel.add(lblTitle, gbc);
        
        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton btnApprove = createActionButton("Duyệt", new Color(6, 78, 59), true);
        btnApprove.setForeground(new Color(52, 211, 153));
        
        JButton btnReject = createActionButton("Từ chối", new Color(127, 29, 29), true);
        btnReject.setForeground(new Color(248, 113, 113));
        
        JButton btnDelete = createActionButton("Xóa", ThemeColor.BG_CARD, false);
        JButton btnRefresh = createActionButton("Làm mới", ThemeColor.BG_CARD, false);
        
        btnApprove.addActionListener(e -> updateStatus("APPROVED"));
        btnReject.addActionListener(e -> updateStatus("REJECTED"));
        btnDelete.addActionListener(e -> deleteEvent());
        btnRefresh.addActionListener(e -> loadData());
        
        actionPanel.add(btnApprove);
        actionPanel.add(btnReject);
        actionPanel.add(btnDelete);
        actionPanel.add(btnRefresh);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        headerPanel.add(actionPanel, gbc);
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. Table
        String[] columnNames = {"ID", "Tên Sự Kiện", "ID BTC", "Địa điểm", "Bắt đầu", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        TableStyler.applyStyle(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableStyler.StatusColumnRenderer());
        
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(ThemeColor.BG_MAIN);
        tableWrapper.setBorder(new EmptyBorder(0, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70)));
        scrollPane.getViewport().setBackground(ThemeColor.BG_MAIN);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        add(tableWrapper, BorderLayout.CENTER);
    }
    
    private JButton createActionButton(String text, Color bg, boolean isPrimary) {
        com.ticketbox.view.component.RoundedButton btn = new com.ticketbox.view.component.RoundedButton(text, 10);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(isPrimary ? Color.WHITE : ThemeColor.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
    
    public void loadData() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
            allEvents = eventDAO.getAllEvents(); 
            for (Event e : allEvents) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getName(), e.getOrganizerId(), e.getLocation(), e.getStartTime(), e.getStatus()
                });
            }
        }
    }
    
    private void updateStatus(String status) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event event = allEvents.get(selectedRow);
        event.setStatus(status);
        
        if (eventDAO.updateEvent(event)) {
             JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
             loadData();
        } else {
             JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteEvent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event event = allEvents.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa sự kiện: " + event.getName() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (eventDAO.deleteEvent(event.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa sự kiện!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa sự kiện.");
            }
        }
    }
}
