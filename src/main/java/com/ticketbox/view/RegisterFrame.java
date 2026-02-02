package com.ticketbox.view;

import com.ticketbox.controller.AuthController;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegisterFrame extends JFrame {
    private AuthController controller;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JComboBox<String> cbRole;
    private JButton btnRegister;
    private JButton btnBack;

    public RegisterFrame(AuthController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ticketbox - Đăng ký tài khoản");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        // 1. LEFT SIDE - Branding (Same as Login but maybe different text)
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                // Gradient Background
                GradientPaint gp = new GradientPaint(0, 0, ThemeColor.SECONDARY, w, h, ThemeColor.PRIMARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        
        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));
        
        JLabel lblLogo = new JLabel("Tham gia ngay");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel lblSlogan = new JLabel("Trải nghiệm mua vé chưa từng có");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSlogan.setForeground(new Color(255, 255, 255, 200));
        lblSlogan.setAlignmentX(CENTER_ALIGNMENT);
        
        brandContent.add(lblLogo);
        brandContent.add(Box.createVerticalStrut(10));
        brandContent.add(lblSlogan);
        
        leftPanel.add(brandContent);
        
        // 2. RIGHT SIDE - Register Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setPreferredSize(new Dimension(350, 550));
        
        // Header
        JLabel lblHeader = new JLabel("Tạo tài khoản mới");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(ThemeColor.TEXT_PRIMARY);
        lblHeader.setAlignmentX(LEFT_ALIGNMENT);
        
        formContainer.add(lblHeader);
        formContainer.add(Box.createVerticalStrut(20));
        
        // Fields
        txtFullName = addFormField(formContainer, "Họ và tên");
        txtEmail = addFormField(formContainer, "Email");
        txtUsername = addFormField(formContainer, "Tên đăng nhập");
        txtPassword = addPasswordField(formContainer, "Mật khẩu");
        txtConfirmPassword = addPasswordField(formContainer, "Nhập lại mật khẩu");
        
        // Role
        JLabel lblRole = new JLabel("Bạn là:");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setForeground(ThemeColor.TEXT_PRIMARY);
        lblRole.setAlignmentX(LEFT_ALIGNMENT);
        formContainer.add(lblRole);
        formContainer.add(Box.createVerticalStrut(5));
        
        String[] roles = {"CUSTOMER", "ORGANIZER"};
        cbRole = new JComboBox<>(roles);
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbRole.setAlignmentX(LEFT_ALIGNMENT);
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formContainer.add(cbRole);
        formContainer.add(Box.createVerticalStrut(20));

        // Buttons
        btnRegister = new JButton("Đăng ký");
        btnRegister.setBackground(ThemeColor.PRIMARY);
        btnRegister.setForeground(ThemeColor.TEXT_INVERSE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnRegister.setAlignmentX(LEFT_ALIGNMENT);
        
        formContainer.add(btnRegister);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Back Link
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setBackground(Color.WHITE);
        backPanel.setAlignmentX(LEFT_ALIGNMENT);
        backPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel lblHaveAcc = new JLabel("Đã có tài khoản? ");
        lblHaveAcc.setForeground(ThemeColor.TEXT_SECONDARY);
        
        btnBack = new JButton("Đăng nhập");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(ThemeColor.PRIMARY);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        backPanel.add(lblHaveAcc);
        backPanel.add(btnBack);
        
        formContainer.add(backPanel);
        
        rightPanel.add(formContainer);
        
        add(mainPanel);
        
        // Events
        btnRegister.addActionListener(e -> handleRegister());
        btnBack.addActionListener(e -> controller.showLoginView());
        
         // Hover effect for back button
        btnBack.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnBack.setForeground(ThemeColor.SECONDARY);
            }
            public void mouseExited(MouseEvent e) {
                btnBack.setForeground(ThemeColor.PRIMARY);
            }
        });
    }
    
    private JTextField addFormField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ThemeColor.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
        
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(100, 35));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
        return field;
    }
    
    private JPasswordField addPasswordField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ThemeColor.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
        
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(100, 35));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
        return field;
    }

    private void handleRegister() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());
        String email = txtEmail.getText();
        String fullName = txtFullName.getText();
        String role = (String) cbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); 
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRole(role);

        controller.handleRegister(newUser);
    }
}
