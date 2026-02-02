package com.ticketbox.view;

import com.ticketbox.dao.UserDAO;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ForgotPasswordDialog extends JDialog {
    private UserDAO userDAO;
    private JTextField txtUsername;
    private JTextField txtEmail;
    
    // Step 2 components
    private JPanel step2Panel;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;
    private String verifiedUsername;

    public ForgotPasswordDialog(Frame owner) {
        super(owner, "Quên mật khẩu", true);
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setSize(400, 380);
        setLocationRelativeTo(getParent());
        setLayout(new CardLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);
        
        // --- STEP 1: Verify Identity ---
        JPanel step1 = new JPanel(new GridBagLayout());
        step1.setBackground(ThemeColor.BG_MAIN);
        step1.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        
        // Header
        JLabel lblHeader = new JLabel("Khôi phục tài khoản");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(ThemeColor.TEXT_PRIMARY);
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        step1.add(lblHeader, gbc);
        
        // Inputs
        gbc.insets = new Insets(5, 0, 5, 0); // Reset insets
        
        addLabel(step1, "Tên đăng nhập:", 1, gbc);
        txtUsername = createTextField();
        addComponent(step1, txtUsername, 2, gbc);
        
        addLabel(step1, "Email đăng ký:", 3, gbc);
        txtEmail = createTextField();
        addComponent(step1, txtEmail, 4, gbc);
        
        // Button
        JButton btnVerify = new JButton("Tiếp tục");
        btnVerify.setBackground(ThemeColor.PRIMARY);
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVerify.setFocusPainted(false);
        btnVerify.setPreferredSize(new Dimension(100, 40));
        btnVerify.addActionListener(e -> verifyIdentity());
        
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        step1.add(btnVerify, gbc);
        
        add(step1, "STEP1");
        
        // --- STEP 2: Reset Password ---
        step2Panel = new JPanel(new GridBagLayout());
        step2Panel.setBackground(ThemeColor.BG_MAIN);
        step2Panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc2 = (GridBagConstraints) gbc.clone();
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.anchor = GridBagConstraints.CENTER;
        
        JLabel lblHeader2 = new JLabel("Đặt mật khẩu mới");
        lblHeader2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader2.setForeground(ThemeColor.TEXT_PRIMARY);
        lblHeader2.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc2.gridy = 0;
        gbc2.insets = new Insets(0, 0, 20, 0);
        step2Panel.add(lblHeader2, gbc2);
        
        gbc2.insets = new Insets(5, 0, 5, 0);
        
        addLabel(step2Panel, "Mật khẩu mới:", 1, gbc2);
        txtNewPass = createPasswordField();
        addComponent(step2Panel, txtNewPass, 2, gbc2);
        
        addLabel(step2Panel, "Xác nhận mật khẩu:", 3, gbc2);
        txtConfirmPass = createPasswordField();
        addComponent(step2Panel, txtConfirmPass, 4, gbc2);
        
        JButton btnReset = new JButton("Hoàn tất");
        btnReset.setBackground(ThemeColor.PRIMARY);
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setFocusPainted(false);
        btnReset.setPreferredSize(new Dimension(130, 40)); // Increased width
        btnReset.addActionListener(e -> resetPassword());
        
        gbc2.gridy = 5;
        gbc2.insets = new Insets(20, 0, 0, 0);
        gbc2.fill = GridBagConstraints.NONE;
        gbc2.anchor = GridBagConstraints.CENTER; // Explicitly center
        step2Panel.add(btnReset, gbc2);
        
        add(step2Panel, "STEP2");
    }
    
    private void addLabel(JPanel panel, String text, int gridy, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ThemeColor.TEXT_SECONDARY);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lbl, gbc);
    }
    
    private void addComponent(JPanel panel, Component comp, int gridy, GridBagConstraints gbc) {
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(comp, gbc);
    }
    
    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setPreferredSize(new Dimension(300, 40));
        tf.setBackground(ThemeColor.BG_CARD);
        tf.setForeground(ThemeColor.TEXT_PRIMARY);
        tf.setCaretColor(ThemeColor.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.SECONDARY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return tf;
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
    
    private void verifyIdentity() {
        String user = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (user.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        
        if (userDAO.verifyUserIdentity(user, email)) {
            verifiedUsername = user;
            CardLayout cl = (CardLayout) getContentPane().getLayout();
            cl.show(getContentPane(), "STEP2");
        } else {
            JOptionPane.showMessageDialog(this, "Thông tin không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetPassword() {
        String newVal = new String(txtNewPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());
        
        if (newVal.isEmpty() || confirm.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu mới!");
             return;
        }
        
        if (!newVal.equals(confirm)) {
             JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        if (userDAO.resetPassword(verifiedUsername, newVal)) {
            JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
