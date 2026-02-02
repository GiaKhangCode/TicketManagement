package com.ticketbox.view;

import com.ticketbox.controller.AuthController;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private AuthController controller;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private Image loginImage;

    public LoginFrame(AuthController controller) {
        this.controller = controller;
        // Load Image Async
        new Thread(() -> {
            try {
                java.net.URL url = java.net.URI.create("https://github.com/GiaKhangCode/celestial_perspective/blob/main/login.jpg?raw=true").toURL();
                loginImage = javax.imageio.ImageIO.read(url);
                SwingUtilities.invokeLater(this::repaint);
            } catch (Exception e) {
                System.err.println("Could not load login image: " + e.getMessage());
            }
        }).start();
        
        initComponents();
    }

    private void initComponents() {
        setTitle("Ve'ryGood - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        // 1. LEFT SIDE - Banner / Branding
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (loginImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = loginImage.getWidth(this);
                    int imgHeight = loginImage.getHeight(this);
                    
                    if (imgWidth > 0 && imgHeight > 0) {
                        double panelAspect = (double) panelWidth / panelHeight;
                        double imgAspect = (double) imgWidth / imgHeight;
                        
                        int drawWidth, drawHeight;
                        int x, y;
                        
                        if (panelAspect > imgAspect) {
                            // Panel is wider than image -> fit width, crop height (or scale up)
                            drawWidth = panelWidth;
                            drawHeight = (int) (panelWidth / imgAspect);
                            x = 0;
                            y = (panelHeight - drawHeight) / 2;
                        } else {
                            // Panel is taller than image -> fit height, crop width (or scale up)
                            drawHeight = panelHeight;
                            drawWidth = (int) (panelHeight * imgAspect);
                            x = (panelWidth - drawWidth) / 2;
                            y = 0;
                        }
                        
                        g2d.drawImage(loginImage, x, y, drawWidth, drawHeight, this);
                    }
                    
                    // Add semi-transparent overlay to ensure text readability (if any text is added later)
                    // g2d.setColor(new Color(0, 0, 0, 50)); // Reduced overlay opacity as text is removed
                    // g2d.fillRect(0, 0, panelWidth, panelHeight);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    int w = getWidth();
                    int h = getHeight();
                    // Gradient Background: Dark Zinc -> Cyan Accent
                    GradientPaint gp = new GradientPaint(0, 0, new Color(24, 24, 27), w, h, new Color(8, 145, 178));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, w, h);
                }
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        
        // Branding Content
        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));
        
        //JLabel titleLabel = new JLabel("Ve'ryGood");
        //titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        //titleLabel.setForeground(Color.WHITE);
        //titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        
        //brandContent.add(titleLabel);
        // brandContent.add(Box.createVerticalStrut(10)); // Removed gap
        // brandContent.add(lblSlogan); // Removed slogan

        
        leftPanel.add(brandContent);
        
        // 2. RIGHT SIDE - Login Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(ThemeColor.BG_MAIN);
        
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(ThemeColor.BG_MAIN);
        formContainer.setPreferredSize(new Dimension(320, 400));
        
        // Welcome Text
        JLabel lblWelcome = new JLabel("Chào mừng trở lại!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(ThemeColor.TEXT_PRIMARY);
        lblWelcome.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel lblSub = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(ThemeColor.TEXT_SECONDARY);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        
        formContainer.add(lblWelcome);
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(lblSub);
        formContainer.add(Box.createVerticalStrut(40));
        
        // Inputs
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(ThemeColor.TEXT_PRIMARY);
        lblUser.setAlignmentX(LEFT_ALIGNMENT);
        
        txtUsername = new JTextField();
        styleTextField(txtUsername);
        
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(ThemeColor.TEXT_PRIMARY);
        lblPass.setAlignmentX(LEFT_ALIGNMENT);
        
        txtPassword = new JPasswordField();
        styleTextField(txtPassword);
        
        formContainer.add(lblUser);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(txtUsername);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(lblPass);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(txtPassword);
        formContainer.add(txtPassword);
        formContainer.add(Box.createVerticalStrut(5));
        
        // Forgot Password Link
        JLabel lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForgot.setForeground(ThemeColor.ACCENT);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.setAlignmentX(LEFT_ALIGNMENT);
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ForgotPasswordDialog(LoginFrame.this).setVisible(true);
            }
        });
        
        // Wrapper for right alignment or just place it
        JPanel forgotWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        forgotWrapper.setBackground(ThemeColor.BG_MAIN);
        forgotWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        forgotWrapper.add(lblForgot);
        
        formContainer.add(forgotWrapper);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Buttons - Using RoundedButton
        // Login Button
        com.ticketbox.view.component.RoundedButton btnLog = new com.ticketbox.view.component.RoundedButton("Đăng nhập", 10);
        btnLog.setBackground(ThemeColor.PRIMARY);
        btnLog.setForeground(ThemeColor.TEXT_INVERSE);
        btnLog.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLog.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin = btnLog; // Assign to field
        
        formContainer.add(btnLogin);
        formContainer.add(Box.createVerticalStrut(20));
        
        // Register Link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setBackground(ThemeColor.BG_MAIN);
        registerPanel.setAlignmentX(LEFT_ALIGNMENT);
        registerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel lblNoAcc = new JLabel("Chưa có tài khoản? ");
        lblNoAcc.setForeground(ThemeColor.TEXT_SECONDARY);
        
        // Register Button (Styled as Link/Ghost)
        btnRegister = new JButton("Đăng ký ngay");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(ThemeColor.PRIMARY);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        registerPanel.add(lblNoAcc);
        registerPanel.add(btnRegister);
        
        formContainer.add(registerPanel);
        
        rightPanel.add(formContainer);
        
        // Add to Main
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        
        add(mainPanel);

        // Events
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            controller.handleLogin(username, password);
        });
        
        txtPassword.addActionListener(e -> btnLogin.doClick()); // Enter key

        btnRegister.addActionListener(e -> controller.showRegisterView());
        
        // Hover effect for register button
        btnRegister.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnRegister.setForeground(ThemeColor.SECONDARY);
            }
            public void mouseExited(MouseEvent e) {
                btnRegister.setForeground(ThemeColor.PRIMARY);
            }
        });
    }
    
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(100, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setAlignmentX(LEFT_ALIGNMENT);
        // Padding is handled by FlatLaf default or can be added via border if needed
        // but FlatLaf usually does a good job.
    }
}
