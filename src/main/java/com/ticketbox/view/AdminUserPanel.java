package com.ticketbox.view;

import com.ticketbox.dao.UserDAO;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.TableStyler;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminUserPanel extends JPanel {
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private UserDAO userDAO;
    private List<User> allUsers;
    
    public AdminUserPanel() {
        this.userDAO = new UserDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // 1. Header with Title and Actions
        JPanel headerPanel = new JPanel(new GridBagLayout()); 
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel lblTitle = new JLabel("Quản lý Người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        headerPanel.add(lblTitle, gbc);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // 2. Action Buttons (Below title or integrated? Let's keep existing style but separate row if many buttons)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(ThemeColor.BG_MAIN);
        actionPanel.setBorder(new EmptyBorder(0, 10, 10, 20)); // Adjust padding
        
        JButton btnLock = createActionButton("Khóa TK", new Color(185, 28, 28), true); 
        JButton btnUnlock = createActionButton("Mở khóa", new Color(21, 128, 61), true); 
        JButton btnPromote = createActionButton("Thăng cấp Admin", ThemeColor.PRIMARY, true);
        JButton btnDelete = createActionButton("Xóa TK", ThemeColor.BG_CARD, false);
        JButton btnRefresh = createActionButton("Làm mới", ThemeColor.BG_CARD, false);
        
        btnLock.setPreferredSize(new Dimension(100, 35));
        btnUnlock.setPreferredSize(new Dimension(100, 35));
        btnPromote.setPreferredSize(new Dimension(150, 35));
        btnDelete.setPreferredSize(new Dimension(100, 35));
        
        actionPanel.add(btnLock);
        actionPanel.add(btnUnlock);
        actionPanel.add(btnPromote);
        actionPanel.add(btnDelete);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(btnRefresh);
        
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(actionPanel, BorderLayout.NORTH);
        
        // 3. Table
        String[] cols = {"ID", "Username", "Họ Tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo"};
        userTableModel = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        TableStyler.applyStyle(userTable);
        
        // Custom renderer for Status
        userTable.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 if ("Đã khóa".equals(value)) {
                     setForeground(new Color(239, 68, 68)); // Red
                     setFont(getFont().deriveFont(Font.BOLD));
                 } else {
                     setForeground(new Color(34, 197, 94)); // Green
                 }
                 return c;
             }
        });
        
        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scroll.getViewport().setBackground(ThemeColor.BG_MAIN);
        centerWrapper.add(scroll, BorderLayout.CENTER);
        
        add(centerWrapper, BorderLayout.CENTER);
        
        // --- Listeners ---
        btnRefresh.addActionListener(e -> loadData());
        
        btnLock.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            if (userDAO.updateUserLockStatus(userId, true)) {
                JOptionPane.showMessageDialog(this, "Đã khóa tài khoản!");
                loadData();
            }
        });
        
        btnUnlock.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            if (userDAO.updateUserLockStatus(userId, false)) {
                JOptionPane.showMessageDialog(this, "Đã mở khóa tài khoản!");
                loadData();
            }
        });
        
        btnDelete.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa người dùng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (userDAO.deleteUser(userId)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa!");
                    loadData();
                }
            }
        });
        
        btnPromote.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            String currentRole = (String) userTableModel.getValueAt(selectedRow, 4);
            
            if ("ADMIN".equals(currentRole)) {
                 JOptionPane.showMessageDialog(this, "Tài khoản này đã là Admin!");
                 return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, "Thăng cấp người dùng này lên ADMIN?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (userDAO.updateUserRole(userId, "ADMIN")) {
                    JOptionPane.showMessageDialog(this, "Đã thăng cấp thành công!");
                    loadData();
                }
            }
        });
    }
    
    public void loadData() {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        
        allUsers = userDAO.getAllUsers();
        for (User u : allUsers) {
            userTableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.isLocked() ? "Đã khóa" : "Hoạt động",
                u.getCreatedAt()
            });
        }
    }
    
    private JButton createActionButton(String text, Color bg, boolean isPrimary) {
        com.ticketbox.view.component.RoundedButton btn = new com.ticketbox.view.component.RoundedButton(text, 10);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(isPrimary ? Color.WHITE : ThemeColor.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
}
