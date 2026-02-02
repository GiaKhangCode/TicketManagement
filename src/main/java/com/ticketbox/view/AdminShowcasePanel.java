package com.ticketbox.view;

import com.ticketbox.dao.ShowcaseDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.Showcase;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.TableStyler;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminShowcasePanel extends JPanel {
    private JComboBox<Showcase> cboShowcases;
    private JTable showcaseInfoTable;
    private DefaultTableModel showcaseInfoModel;
    private JTextField txtAddEventId;
    private ShowcaseDAO showcaseDAO;
    
    public AdminShowcasePanel() {
        this.showcaseDAO = new ShowcaseDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // 1. Header with Title
        JPanel headerPanel = new JPanel(new GridBagLayout()); 
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Quản lý Showcase");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        headerPanel.add(lblTitle, gbc);
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
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
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        
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
        contentPanel.add(scroll, BorderLayout.CENTER);
        
        // Bottom: Add/Remove Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        txtAddEventId = new JTextField(15);
        txtAddEventId.setEditable(false);
        
        JButton btnSelect = createActionButton("Chọn...", ThemeColor.BG_CARD, false);
        btnSelect.setPreferredSize(new Dimension(80, 35));
        btnSelect.addActionListener(e -> openSelectDialog());
        
        JButton btnAdd = createActionButton("Thêm", ThemeColor.PRIMARY, true);
        btnAdd.setPreferredSize(new Dimension(100, 35));
        btnAdd.addActionListener(e -> addEventToShowcase());
        
        JButton btnRemove = createActionButton("Xóa khỏi List", new Color(127, 29, 29), true);
        btnRemove.setForeground(new Color(248, 113, 113));
        btnRemove.setPreferredSize(new Dimension(120, 35));
        btnRemove.addActionListener(e -> removeEventFromShowcase());
        
        bottomPanel.add(new JLabel("Sự kiện: "));
        bottomPanel.add(txtAddEventId);
        bottomPanel.add(btnSelect);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnAdd);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnRemove);
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
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
        if (cboShowcases != null) {
             cboShowcases.removeAllItems();
             List<Showcase> list = showcaseDAO.getAllShowcases();
             for (Showcase s : list) {
                 cboShowcases.addItem(s);
             }
        }
    }
    
    private void loadShowcaseData() {
        if (showcaseInfoModel == null) return;
        showcaseInfoModel.setRowCount(0);
        
        Showcase selected = (Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        List<Event> events = showcaseDAO.getEventsInShowcase(selected.getId());
        
        for (Event e : events) {
            showcaseInfoModel.addRow(new Object[]{e.getId(), e.getName(), "Xóa"});
        }
    }
    
    private void openSelectDialog() {
        SelectEventDialog dialog = new SelectEventDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        Event selected = dialog.getSelectedEvent();
        if (selected != null) {
            txtAddEventId.setText(selected.getId() + " - " + selected.getName());
        }
    }

    private void addEventToShowcase() {
        Showcase selected = (Showcase) cboShowcases.getSelectedItem();
        if (selected == null) return;
        
        String text = txtAddEventId.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự kiện!");
            return;
        }
        
        try {
            // Parse ID from "ID - Name"
            String idPart = text.split(" - ")[0];
            int eventId = Integer.parseInt(idPart);
            
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
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ID!");
        }
    }
    
    private void removeEventFromShowcase() {
        int row = showcaseInfoTable.getSelectedRow();
        if (row == -1) return;
        
        int eventId = (int) showcaseInfoModel.getValueAt(row, 0);
        Showcase selected = (Showcase) cboShowcases.getSelectedItem();
        
        showcaseDAO.removeEventFromShowcase(selected.getId(), eventId);
        loadShowcaseData();
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
        
        Showcase s = new Showcase(0, name, order, true);
        if (showcaseDAO.addShowcase(s)) {
            JOptionPane.showMessageDialog(this, "Tạo thành công!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo showcase.");
        }
    }
    
    private void editShowcase() {
        Showcase selected = (Showcase) cboShowcases.getSelectedItem();
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
        Showcase selected = (Showcase) cboShowcases.getSelectedItem();
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
