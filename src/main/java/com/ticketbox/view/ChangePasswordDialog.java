package com.ticketbox.view;

import com.ticketbox.dao.UserDAO;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChangePasswordDialog extends JDialog {
    private User user;
    private UserDAO userDAO;
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public ChangePasswordDialog(JDialog owner, User user) {
        super(owner, "Đổi mật khẩu", true);
        this.user = user;
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setSize(400, 380);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(ThemeColor.BG_MAIN);
        content.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        addLabel(content, "Mật khẩu hiện tại:", 0, gbc);
        txtCurrentPass = createPasswordField();
        addComponent(content, txtCurrentPass, 1, gbc);
        
        addLabel(content, "Mật khẩu mới:", 2, gbc);
        txtNewPass = createPasswordField();
        addComponent(content, txtNewPass, 3, gbc);
        
        addLabel(content, "Nhập lại mật khẩu mới:", 4, gbc);
        txtConfirmPass = createPasswordField();
        addComponent(content, txtConfirmPass, 5, gbc);
        
        add(content, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(ThemeColor.BG_MAIN);
        footer.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JButton btnSave = new JButton("Đổi mật khẩu");
        btnSave.setBackground(ThemeColor.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setFocusPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> changePassword());
        
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }
    
    private void addLabel(JPanel panel, String text, int gridy, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ThemeColor.TEXT_SECONDARY);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = gridy;
        panel.add(lbl, gbc);
    }
    
    private void addComponent(JPanel panel, Component comp, int gridy, GridBagConstraints gbc) {
        gbc.gridy = gridy;
        panel.add(comp, gbc);
    }
    
    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pf.setPreferredSize(new Dimension(300, 40));
        pf.setBackground(ThemeColor.BG_CARD);
        pf.setForeground(ThemeColor.TEXT_PRIMARY);
        pf.setCaretColor(ThemeColor.TEXT_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.SECONDARY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return pf;
    }
    
    private void changePassword() {
        String current = new String(txtCurrentPass.getPassword());
        String newVal = new String(txtNewPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());
        
        if (current.isEmpty() || newVal.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        if (!userDAO.checkPassword(user.getId(), current)) {
             JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        if (!newVal.equals(confirm)) {
             JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        if (userDAO.changePassword(user.getId(), newVal)) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
