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

public class AdminPanel extends JPanel {
    private EventDAO eventDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Event> pendingEvents;
    
    // Showcase components
    private JComboBox<com.ticketbox.model.Showcase> cboShowcases;
    private JTable showcaseInfoTable;
    private DefaultTableModel showcaseInfoModel;
    private JTextField txtAddEventId;
    private com.ticketbox.dao.ShowcaseDAO showcaseDAO;

    public AdminPanel() {
        this.eventDAO = new EventDAO();
        this.showcaseDAO = new com.ticketbox.dao.ShowcaseDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Tab 1: Approval (Existing)
        tabbedPane.addTab("Duyệt Sự kiện", createApprovalPanel());
        
        // Tab 2: Showcase Management (New)
        tabbedPane.addTab("Quản lý Showcase", createShowcaseManagementPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColor.BG_MAIN);
        
        // 1. Header
        JPanel headerPanel = new JPanel(new GridBagLayout()); 
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 20);
        
        // Title
        JLabel lblTitle = new JLabel("Duyệt Sự kiện");
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
        
        JButton btnRefresh = createActionButton("Làm mới", ThemeColor.BG_CARD, false);
        
        btnApprove.addActionListener(e -> updateStatus("APPROVED"));
        btnReject.addActionListener(e -> updateStatus("REJECTED"));
        btnRefresh.addActionListener(e -> loadData());
        
        actionPanel.add(btnApprove);
        actionPanel.add(btnReject);
        actionPanel.add(btnRefresh);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        headerPanel.add(actionPanel, gbc);
        
        panel.add(headerPanel, BorderLayout.NORTH);

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
        
        panel.add(tableWrapper, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createShowcaseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColor.BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Top: Selection & Add
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel lblInfo = new JLabel("Chọn Danh mục:");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfo.setForeground(ThemeColor.TEXT_PRIMARY);
        topPanel.add(lblInfo);
        
        cboShowcases = new JComboBox<>();
        cboShowcases.setPreferredSize(new Dimension(200, 35));
        cboShowcases.addActionListener(e -> loadShowcaseData());
        topPanel.add(cboShowcases);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Buttons Panel for Showcase Actions
        JPanel shActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        shActionPanel.setOpaque(false);
        
        JButton btnCreate = createActionButton("Tạo mới", ThemeColor.PRIMARY, true);
        JButton btnEdit = createActionButton("Sửa", ThemeColor.BG_CARD, false);
        JButton btnDelete = createActionButton("Xóa", new Color(127, 29, 29), true);
        btnDelete.setForeground(new Color(248, 113, 113));
        
        btnCreate.setPreferredSize(new Dimension(100, 35));
        btnEdit.setPreferredSize(new Dimension(80, 35));
        btnDelete.setPreferredSize(new Dimension(80, 35));
        
        btnCreate.addActionListener(e -> createShowcase());
        btnEdit.addActionListener(e -> editShowcase());
        btnDelete.addActionListener(e -> deleteShowcase());
        
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnCreate);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        
        // Center: Table of Events in Showcase
        String[] cols = {"ID Event", "Tên Sự Kiện", "Thao tác"};
        showcaseInfoModel = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        showcaseInfoTable = new JTable(showcaseInfoModel);
        TableStyler.applyStyle(showcaseInfoTable);
        
        JScrollPane scroll = new JScrollPane(showcaseInfoTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70)));
        scroll.getViewport().setBackground(ThemeColor.BG_MAIN);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Bottom: Add/Remove Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        txtAddEventId = new JTextField(10);
        JButton btnAdd = createActionButton("Thêm ID", ThemeColor.PRIMARY, true);
        btnAdd.setPreferredSize(new Dimension(100, 35));
        
        JButton btnRemove = createActionButton("Xóa khỏi List", new Color(127, 29, 29), true);
        btnRemove.setForeground(new Color(248, 113, 113));
        btnRemove.setPreferredSize(new Dimension(120, 35));
        
        btnAdd.addActionListener(e -> addEventToShowcase());
        btnRemove.addActionListener(e -> removeEventFromShowcase());
        
        bottomPanel.add(new JLabel("ID Sự kiện: "));
        bottomPanel.add(txtAddEventId);
        bottomPanel.add(btnAdd);
        bottomPanel.add(Box.createHorizontalStrut(30));
        bottomPanel.add(btnRemove);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public void loadData() {
        // Load Approval Data
        if (tableModel != null) {
            tableModel.setRowCount(0);
            pendingEvents = eventDAO.getPendingEvents();
            for (Event e : pendingEvents) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getName(), e.getOrganizerId(), e.getLocation(), e.getStartTime(), e.getStatus()
                });
            }
        }
        
        // Load Showcases Combo
        if (cboShowcases != null) {
             cboShowcases.removeAllItems();
             List<com.ticketbox.model.Showcase> list = showcaseDAO.getAllShowcases();
             for (com.ticketbox.model.Showcase s : list) {
                 cboShowcases.addItem(s); // toString() needed or renderer
             }
        }
    }
    
    private void loadShowcaseData() {
        if (showcaseInfoModel == null) return;
        showcaseInfoModel.setRowCount(0);
        
        com.ticketbox.model.Showcase selected = (com.ticketbox.model.Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        // Use new method to get ALL events (Approved/Pending/etc)
        List<Event> events = showcaseDAO.getEventsInShowcase(selected.getId());
        
        for (Event e : events) {
            showcaseInfoModel.addRow(new Object[]{e.getId(), e.getName(), "Xóa"});
        }
    }
    
    private void addEventToShowcase() {
        com.ticketbox.model.Showcase selected = (com.ticketbox.model.Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        String idStr = txtAddEventId.getText().trim();
        if (idStr.isEmpty()) return;
        
        try {
            int eventId = Integer.parseInt(idStr);
            if (showcaseDAO.isEventInShowcase(selected.getId(), eventId)) {
                 JOptionPane.showMessageDialog(this, "Sự kiện đã có trong danh sách!");
                 return;
            }
            
            boolean success = showcaseDAO.addEventToShowcase(selected.getId(), eventId);
            
            if (success) {
                txtAddEventId.setText("");
                loadShowcaseData();
                JOptionPane.showMessageDialog(this, "Đã thêm thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!\nVui lòng kiểm tra lại ID Sự kiện có tồn tại không.", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID không hợp lệ! Vui lòng nhập số.");
        }
    }
    
    private void removeEventFromShowcase() {
        int row = showcaseInfoTable.getSelectedRow();
        if (row == -1) return;
        
        int eventId = (int) showcaseInfoModel.getValueAt(row, 0);
        com.ticketbox.model.Showcase selected = (com.ticketbox.model.Showcase) cboShowcases.getSelectedItem();
        
        showcaseDAO.removeEventFromShowcase(selected.getId(), eventId);
        loadShowcaseData();
    }

    private JButton createActionButton(String text, Color bg, boolean isPrimary) {
        com.ticketbox.view.component.RoundedButton btn = new com.ticketbox.view.component.RoundedButton(text, 10);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(isPrimary ? Color.WHITE : ThemeColor.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
    
    private void updateStatus(String status) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event event = pendingEvents.get(selectedRow);
        event.setStatus(status);
        
        if (eventDAO.updateEvent(event)) {
             JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
             loadData();
        } else {
             JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createShowcase() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên Showcase mới:");
        if (name == null || name.trim().isEmpty()) return;
        
        String orderStr = JOptionPane.showInputDialog(this, "Nhập thứ tự hiển thị (số):", "1");
        int order = 1;
        try {
            if (orderStr != null && !orderStr.trim().isEmpty()) {
                order = Integer.parseInt(orderStr.trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thứ tự phải là số!");
            return;
        }
        
        com.ticketbox.model.Showcase s = new com.ticketbox.model.Showcase(0, name, order, true);
        if (showcaseDAO.addShowcase(s)) {
            JOptionPane.showMessageDialog(this, "Tạo thành công!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo showcase.");
        }
    }
    
    private void editShowcase() {
        com.ticketbox.model.Showcase selected = (com.ticketbox.model.Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        String name = JOptionPane.showInputDialog(this, "Tên Showcase:", selected.getName());
        if (name == null || name.trim().isEmpty()) return;
        
        String orderStr = JOptionPane.showInputDialog(this, "Thứ tự hiển thị:", selected.getDisplayOrder());
        int order = selected.getDisplayOrder();
        try {
            if (orderStr != null && !orderStr.trim().isEmpty()) {
                order = Integer.parseInt(orderStr.trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thứ tự phải là số!");
            return;
        }
        
        selected.setName(name);
        selected.setDisplayOrder(order);
        
        if (showcaseDAO.updateShowcase(selected)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật.");
        }
    }
    
    private void deleteShowcase() {
        com.ticketbox.model.Showcase selected = (com.ticketbox.model.Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa showcase: " + selected.getName() + "?\nCác sự kiện trong showcase sẽ KHÔNG bị xóa khỏi hệ thống.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            if (showcaseDAO.deleteShowcase(selected.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa.");
            }
        }
    }
}
