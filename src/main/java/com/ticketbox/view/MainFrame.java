package com.ticketbox.view;

import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class MainFrame extends JFrame {
    private User loggedInUser;
    private JPanel cards;
    private CardLayout cardLayout;
    private String currentCard = "HOME";

    public MainFrame(User user) {
        this.loggedInUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ve'ryGood - Hệ thống vé sự kiện | " + (loggedInUser != null ? loggedInUser.getFullName() : "Guest"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800); // Increased width for better header layout
        setLocationRelativeTo(null);
        
        // Layout chính
        setLayout(new BorderLayout());
        
        // --- 1. HEADER (Top Navigation) ---
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(ThemeColor.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        
        // LEFT: Logo
        JLabel lblLogo = new JLabel("Ve'ryGood");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToHome();
            }
        });
        headerPanel.add(lblLogo, BorderLayout.WEST);
        
        // CENTER: Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setOpaque(false);
        
        JTextField txtSearch = new JTextField(30);
        txtSearch.putClientProperty("JTextField.placeholderText", "Bạn tìm gì hôm nay?");
        txtSearch.setPreferredSize(new Dimension(400, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Simple border
        // Making it look like pill shape requires custom painting or CompoundBorder with rounded logic, 
        // but simple white rect is fine for now or RoundedBorder.
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(Color.WHITE, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.setBackground(ThemeColor.BG_CARD); // Darker contrast
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(new EmptyBorder(5, 10, 5, 10)); // Flat style or RoundedButton if available from imports
        // Re-use RoundedButton if possible, but standard button ok for header.
        // Let's use logic to trigger search
        
        java.awt.event.ActionListener searchAction = e -> {
            String keyword = txtSearch.getText();
            switchToHome();
            // Find HomePanel
            for (Component c : cards.getComponents()) {
                if (c instanceof HomePanel) {
                    ((HomePanel) c).performSearch(keyword);
                    break;
                }
            }
        };
        btnSearch.addActionListener(searchAction);
        txtSearch.addActionListener(searchAction); // Enter key support
        
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        // Wrap center in a container to center it effectively in BorderLayout if needed, 
        // but BorderLayout.CENTER stretches. So we use GridBag or simple Flow in separate panel.
        // To make it truly centered, we might need a GridBag container wrapper.
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(searchPanel);
        headerPanel.add(centerWrapper, BorderLayout.CENTER);
        
        // RIGHT: User Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);
        
        if ("CUSTOMER".equals(loggedInUser.getRole())) {
             JButton btnMyTickets = createHeaderButton("Vé của tôi", "MY_TICKETS");
             rightPanel.add(btnMyTickets);
        }
        
        // Resale Market Button (Available for all or just customers?)
        JButton btnResale = createHeaderButton("Vé bán lại", "RESALE_MARKET");
        rightPanel.add(btnResale);

        if ("ORGANIZER".equals(loggedInUser.getRole())) {
             JButton btnCreateEvent = createHeaderButton("Tạo sự kiện", "ORG_EVENTS"); // Shortcut to Manager
             btnCreateEvent.setBackground(ThemeColor.ACCENT);
             btnCreateEvent.setForeground(Color.BLACK);
             // Make "Tạo sự kiện" distinct?
             btnCreateEvent.setText("+ Tạo sự kiện");
             rightPanel.add(btnCreateEvent);
        } else if ("ADMIN".equals(loggedInUser.getRole())) {
             JButton btnAdmin = new JButton("Quản trị");
             btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 14));
             btnAdmin.setForeground(Color.WHITE);
             btnAdmin.setContentAreaFilled(false);
             btnAdmin.setBorder(new javax.swing.border.LineBorder(Color.WHITE, 1, true));
             btnAdmin.setFocusPainted(false);
             btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
             btnAdmin.setPreferredSize(new Dimension(140, 35));
             
             JPopupMenu adminMenu = new JPopupMenu();
             
             JMenuItem itemEvents = new JMenuItem("Quản lý Sự kiện");
             itemEvents.addActionListener(e -> {
                 cardLayout.show(cards, "ADMIN_EVENTS");
                 currentCard = "ADMIN_EVENTS";
                 for (Component c : cards.getComponents()) { // Logic to find and refresh
                     if (c instanceof AdminEventPanel) ((AdminEventPanel) c).loadData();
                 }
             });
             
             JMenuItem itemUsers = new JMenuItem("Quản lý Người dùng");
             itemUsers.addActionListener(e -> {
                 cardLayout.show(cards, "ADMIN_USERS");
                 currentCard = "ADMIN_USERS";
                 for (Component c : cards.getComponents()) {
                     if (c instanceof AdminUserPanel) ((AdminUserPanel) c).loadData();
                 }
             });
             
             JMenuItem itemShowcase = new JMenuItem("Quản lý Showcase");
             itemShowcase.addActionListener(e -> {
                 cardLayout.show(cards, "ADMIN_SHOWCASE");
                 currentCard = "ADMIN_SHOWCASE";
                 for (Component c : cards.getComponents()) {
                     if (c instanceof AdminShowcasePanel) ((AdminShowcasePanel) c).loadData();
                 }
             });
             
             adminMenu.add(itemEvents);
             adminMenu.add(itemUsers);
             adminMenu.add(itemShowcase);
             
             btnAdmin.addActionListener(e -> adminMenu.show(btnAdmin, 0, btnAdmin.getHeight()));
             
             // Hover effect
             btnAdmin.addMouseListener(new MouseAdapter() {
                 public void mouseEntered(MouseEvent e) { btnAdmin.setContentAreaFilled(true); btnAdmin.setBackground(new Color(255,255,255,50)); }
                 public void mouseExited(MouseEvent e) { btnAdmin.setContentAreaFilled(false); }
             });
             
             rightPanel.add(btnAdmin);
        }
        
        // User Profile Dropdown
        JButton btnUser = new JButton();
        btnUser.setText(loggedInUser.getFullName());
        java.net.URL avatarUrl = getClass().getResource("/images/default_avatar.png");
        if (avatarUrl != null) {
            btnUser.setIcon(resizeIcon(new javax.swing.ImageIcon(avatarUrl), 30, 30)); 
        }
        // Allow text only if icon fails
        if (btnUser.getIcon() == null || btnUser.getIcon().getIconWidth() == -1) {
            btnUser.setText(" Tài khoản "); 
        } else {
             btnUser.setText(loggedInUser.getFullName() + " ▾"); 
        }
        
        btnUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUser.setForeground(Color.WHITE);
        btnUser.setContentAreaFilled(false);
        btnUser.setBorderPainted(false);
        btnUser.setFocusPainted(false);
        btnUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPopupMenu userMenu = new JPopupMenu();
        
        JMenuItem itemProfile = new JMenuItem("Hồ sơ cá nhân");
        itemProfile.addActionListener(e -> new ProfileDialog(this, loggedInUser).setVisible(true));
        
        JMenuItem itemLogout = new JMenuItem("Đăng xuất");
        itemLogout.addActionListener(e -> {
            this.dispose();
            new com.ticketbox.controller.AuthController().showLoginView();
        });
        
        userMenu.add(itemProfile);
        userMenu.addSeparator();
        userMenu.add(itemLogout);
        
        btnUser.addActionListener(e -> userMenu.show(btnUser, 0, btnUser.getHeight()));
        
        rightPanel.add(btnUser);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // --- CONTENT ---
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(ThemeColor.BG_MAIN);
        
        // Add Panels
        cards.add(new HomePanel(loggedInUser), "HOME");
        cards.add(new ResaleMarketplacePanel(loggedInUser), "RESALE_MARKET");
        
        if ("CUSTOMER".equals(loggedInUser.getRole())) {
             cards.add(new MyTicketsPanel(loggedInUser), "MY_TICKETS");
        }
        
        if ("ORGANIZER".equals(loggedInUser.getRole())) {
            cards.add(new OrganizerPanel(loggedInUser), "ORG_EVENTS");
            cards.add(new StatisticsPanel(loggedInUser), "ORG_STATS");
        }
        
        if ("ADMIN".equals(loggedInUser.getRole())) {
            cards.add(new AdminEventPanel(), "ADMIN_EVENTS");
            cards.add(new AdminUserPanel(), "ADMIN_USERS");
            cards.add(new AdminShowcasePanel(), "ADMIN_SHOWCASE");
        }
        
        add(cards, BorderLayout.CENTER);
    }
    
    private void switchToHome() {
        cardLayout.show(cards, "HOME");
        currentCard = "HOME";
    }
    
    private JButton createHeaderButton(String text, String targetCard) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorder(new javax.swing.border.LineBorder(Color.WHITE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setContentAreaFilled(true); btn.setBackground(new Color(255,255,255,50)); }
            public void mouseExited(MouseEvent e) { btn.setContentAreaFilled(false); }
        });
        
        btn.addActionListener(e -> {
            cardLayout.show(cards, targetCard);
            currentCard = targetCard;
            
            // Refresh data logic (copied from old)
            Component comp = null;
            for (Component c : cards.getComponents()) {
                 if (c.isVisible()) { comp = c; break; }
            }
             if (comp instanceof MyTicketsPanel) ((MyTicketsPanel) comp).loadData();
             else if (comp instanceof ResaleMarketplacePanel) ((ResaleMarketplacePanel) comp).loadData("");
             else if (comp instanceof OrganizerPanel) ((OrganizerPanel) comp).loadData();
             else if (comp instanceof StatisticsPanel) ((StatisticsPanel) comp).loadData();
             else if (comp instanceof AdminEventPanel) ((AdminEventPanel) comp).loadData();
             else if (comp instanceof AdminUserPanel) ((AdminUserPanel) comp).loadData();
             else if (comp instanceof AdminShowcasePanel) ((AdminShowcasePanel) comp).loadData();
        });
        return btn;
    }
    
    // Helper to resize icon safely
    private ImageIcon resizeIcon(ImageIcon icon, int w, int h) {
        if (icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE) return null;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}
