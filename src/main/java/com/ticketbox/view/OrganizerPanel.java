package com.ticketbox.view;

import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.TableStyler;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class OrganizerPanel extends JPanel {
    private User organizer;
    private EventDAO eventDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Event> currentEvents;

    public OrganizerPanel(User user) {
        this.organizer = user;
        this.eventDAO = new EventDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // 1. Header / Action Bar
        // PERFOMANCE FIX: Use GridBagLayout to prevent overlap
        JPanel headerPanel = new JPanel(new GridBagLayout()); 
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 20); // Gap between title and buttons
        
        // Title (Left, Grows)
        JLabel lblTitle = new JLabel("Quản lý Sự kiện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        
        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Take available space
        headerPanel.add(lblTitle, gbc);
        
        // Actions (Right, Fixed)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton btnAdd = createActionButton("Tạo sự kiện", ThemeColor.PRIMARY, true);
        JButton btnEdit = createActionButton("Cập nhật", ThemeColor.BG_CARD, false);
        JButton btnDelete = createActionButton("Xóa", ThemeColor.BG_CARD, false);
        JButton btnTicketTypes = createActionButton("Quản lý vé", ThemeColor.BG_CARD, false);
        JButton btnStageMap = createActionButton("Sơ đồ", ThemeColor.BG_CARD, false); // New Button
        JButton btnRefresh = createActionButton("Làm mới", ThemeColor.BG_CARD, false);
        
        btnAdd.addActionListener(e -> showAddEventDialog());
        btnEdit.addActionListener(e -> showEditEventDialog());
        btnDelete.addActionListener(e -> deleteEvent());
        btnTicketTypes.addActionListener(e -> showManageTicketTypesDialog());
        btnStageMap.addActionListener(e -> showStageBuilderDialog()); // Action
        btnRefresh.addActionListener(e -> loadData());
        
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnTicketTypes);
        actionPanel.add(btnStageMap); // Add to UI
        actionPanel.add(btnRefresh);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        headerPanel.add(actionPanel, gbc);
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. Table
        String[] columnNames = {"ID", "Tên Sự Kiện", "Địa điểm", "Bắt đầu", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
             public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        
        // Apply Modern Style
        TableStyler.applyStyle(table);
        // Custom Renderer for Status (Column 4)
        table.getColumnModel().getColumn(4).setCellRenderer(new TableStyler.StatusColumnRenderer());
        
        // Wrapper for padding
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(ThemeColor.BG_MAIN);
        tableWrapper.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70)));
        scrollPane.getViewport().setBackground(ThemeColor.BG_MAIN);
        
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        add(tableWrapper, BorderLayout.CENTER);
    }
    
    // Helper to create styled buttons
    private JButton createActionButton(String text, Color bg, boolean isPrimary) {
        com.ticketbox.view.component.RoundedButton btn = new com.ticketbox.view.component.RoundedButton(text, 10);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(isPrimary ? Color.WHITE : ThemeColor.TEXT_PRIMARY); 
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
    
    public void loadData() {
        tableModel.setRowCount(0);
        currentEvents = eventDAO.getEventsByOrganizer(organizer.getId());
        for (Event e : currentEvents) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(), e.getLocation(), e.getStartTime(), e.getStatus()
            });
        }
    }
    
    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(SwingUtilities.getWindowAncestor(this), organizer);
        dialog.setVisible(true);
        
        if (dialog.isEventAdded()) {
            loadData();
        }
    }

    private void showEditEventDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event eventToEdit = currentEvents.get(selectedRow);
        AddEventDialog dialog = new AddEventDialog(SwingUtilities.getWindowAncestor(this), organizer, eventToEdit);
        dialog.setVisible(true);
        
        if (dialog.isEventAdded()) { 
            loadData();
        }
    }

    private void deleteEvent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sự kiện này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Event eventToDelete = currentEvents.get(selectedRow);
            if (eventDAO.deleteEvent(eventToDelete.getId())) {
                JOptionPane.showMessageDialog(this, "Xóa sự kiện thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xóa sự kiện.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showManageTicketTypesDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện để quản lý vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event event = currentEvents.get(selectedRow);
        ManageTicketTypesDialog dialog = new ManageTicketTypesDialog(SwingUtilities.getWindowAncestor(this), event);
        dialog.setVisible(true);
    }

    private void showStageBuilderDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện để thiết kế sơ đồ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Event event = currentEvents.get(selectedRow);
        StageBuilderDialog dialog = new StageBuilderDialog(SwingUtilities.getWindowAncestor(this), event);
        dialog.setVisible(true);
    }
}
