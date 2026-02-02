package com.ticketbox.view;

import com.ticketbox.dao.UserDAO;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.RoundedButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfileDialog extends JDialog {
    private User user;
    private UserDAO userDAO;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private Component parentFrame;

    public ProfileDialog(Frame owner, User user) {
        super(owner, "Thông tin cá nhân", true);
        this.user = user;
        this.parentFrame = owner;
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);

        // --- Header ---
        JPanel headerPanel = new JPanel(new GridLayout(1, 1));
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(30, 0, 20, 0));

        // Initial Avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColor.PRIMARY);
                g2.fillOval((getWidth() - 80) / 2, 0, 80, 80);
                
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                String initials = getInitials(user.getFullName());
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(initials);
                int textH = fm.getAscent();
                g2.drawString(initials, (getWidth() - textW) / 2, (80 + textH) / 2 - 5);
            }
        };
        avatarPanel.setPreferredSize(new Dimension(100, 90));
        avatarPanel.setOpaque(false);
        headerPanel.add(avatarPanel);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeColor.BG_MAIN);
        formPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0; gbc.weightx = 1.0;

        // Username
        addLabel(formPanel, gbc, "Tên đăng nhập", 0);
        JTextField txtUsername = createTextField(user.getUsername(), false);
        addField(formPanel, gbc, txtUsername, 1);

        // Role
        addLabel(formPanel, gbc, "Vai trò", 2);
        JLabel lblRole = new JLabel(user.getRole(), SwingConstants.CENTER);
        lblRole.setOpaque(true);
        lblRole.setBackground(ThemeColor.BG_CARD);
        lblRole.setForeground(ThemeColor.ACCENT);
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Padding
        // Make it look like a badge, simple way:
        JPanel roleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        roleWrapper.setOpaque(false);
        roleWrapper.add(lblRole);
        addField(formPanel, gbc, roleWrapper, 3);

        // Full Name
        addLabel(formPanel, gbc, "Họ và tên", 4);
        txtFullName = createTextField(user.getFullName(), true);
        addField(formPanel, gbc, txtFullName, 5);

        // Email
        addLabel(formPanel, gbc, "Email", 6);
        txtEmail = createTextField(user.getEmail(), true);
        addField(formPanel, gbc, txtEmail, 7);

        add(formPanel, BorderLayout.CENTER);

        // --- Footer ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footerPanel.setBackground(ThemeColor.BG_MAIN);

        RoundedButton btnSave = new RoundedButton("Lưu thay đổi", 10);
        btnSave.setBackground(ThemeColor.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.addActionListener(e -> saveProfile());
        
        RoundedButton btnChangePass = new RoundedButton("Đổi mật khẩu", 10);
        btnChangePass.setBackground(ThemeColor.BG_CARD);
        btnChangePass.setForeground(ThemeColor.ACCENT);
        btnChangePass.setPreferredSize(new Dimension(130, 40));
        btnChangePass.addActionListener(e -> new ChangePasswordDialog(this, user).setVisible(true));

        RoundedButton btnClose = new RoundedButton("Đóng", 10);
        btnClose.setBackground(ThemeColor.BG_CARD);
        btnClose.setForeground(ThemeColor.TEXT_PRIMARY);
        btnClose.setPreferredSize(new Dimension(80, 40));
        btnClose.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnChangePass);
        footerPanel.add(btnClose);

        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void addLabel(JPanel panel, GridBagConstraints gbc, String text, int gridy) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(ThemeColor.TEXT_SECONDARY);
        gbc.gridy = gridy;
        panel.add(label, gbc);
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, Component comp, int gridy) {
        gbc.gridy = gridy;
        gbc.insets = new Insets(0, 0, 15, 0); // Bottom margin
        panel.add(comp, gbc);
        gbc.insets = new Insets(10, 0, 5, 0); // Reset
    }

    private JTextField createTextField(String text, boolean editable) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(ThemeColor.TEXT_PRIMARY);
        field.setBackground(editable ? ThemeColor.BG_CARD : new Color(40, 40, 45));
        field.setCaretColor(ThemeColor.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.SECONDARY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setEditable(editable);
        return field;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void saveProfile() {
        String newName = txtFullName.getText().trim();
        String newEmail = txtEmail.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng không để trống thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setFullName(newName);
        user.setEmail(newEmail);

        if (userDAO.updateProfile(user)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            
            // Try to repaint parent title if it's MainFrame
            if (parentFrame instanceof MainFrame) {
                ((MainFrame) parentFrame).setTitle("Ve'ryGood - Hệ thống vé sự kiện | " + newName);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
