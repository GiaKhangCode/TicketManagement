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
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    private User loggedInUser;
    private JPanel cards;
    private CardLayout cardLayout;
    private Map<String, JButton> menuButtons;
    private String currentCard = "HOME";

    public MainFrame(User user) {
        this.loggedInUser = user;
        this.menuButtons = new HashMap<>();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ticketbox - Hệ thống vé sự kiện | " + loggedInUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        
        // Layout chính
        setLayout(new BorderLayout());
        
        // Sidebar Menu
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeColor.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 600));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        // Sidebar Title / Logo Area
        JLabel lblBrand = new JLabel("TICKETBOX", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBrand.setForeground(ThemeColor.ACCENT); // Use new Accent Color
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBrand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        sidebar.add(lblBrand);
        sidebar.add(javax.swing.Box.createVerticalStrut(30));
        
        // Menu Items
        addMenuButton(sidebar, "Trang chủ", "HOME");
        
        if ("CUSTOMER".equals(loggedInUser.getRole())) {
             addMenuButton(sidebar, "Vé của tôi", "MY_TICKETS");
        }
        
        if ("ORGANIZER".equals(loggedInUser.getRole())) {
            addMenuButton(sidebar, "Quản lý Sự kiện", "ORG_EVENTS");
            addMenuButton(sidebar, "Báo cáo Doanh thu", "ORG_STATS");
        }
        
        if ("ADMIN".equals(loggedInUser.getRole())) {
             addMenuButton(sidebar, "Duyệt Sự kiện", "ADMIN_EVENTS");
        }

        sidebar.add(javax.swing.Box.createVerticalGlue());

        // Bottom Menu
        addMenuButton(sidebar, "Tài khoản", "PROFILE");
        addMenuButton(sidebar, "Đăng xuất", "LOGOUT");
        
        add(sidebar, BorderLayout.WEST);
        
        // Main Content Area (CardLayout)
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(ThemeColor.BG_MAIN);
        
        // Thêm các Panel con
        cards.add(new HomePanel(loggedInUser), "HOME");
        
        if ("CUSTOMER".equals(loggedInUser.getRole())) {
             cards.add(new MyTicketsPanel(loggedInUser), "MY_TICKETS");
        }
        
        if ("ORGANIZER".equals(loggedInUser.getRole())) {
            cards.add(new OrganizerPanel(loggedInUser), "ORG_EVENTS");
            cards.add(new StatisticsPanel(loggedInUser), "ORG_STATS");
        }
        
        if ("ADMIN".equals(loggedInUser.getRole())) {
            cards.add(new AdminPanel(), "ADMIN_EVENTS");
        }
        
        add(cards, BorderLayout.CENTER);
        
        // Highlight logic init
        updateActiveButton("HOME");
    }
    
    private void addMenuButton(JPanel sidebar, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(ThemeColor.SIDEBAR);
        btn.setForeground(ThemeColor.TEXT_SECONDARY); // Slate 400
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Mouse Hover & Click Events
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    btn.setBackground(ThemeColor.SECONDARY); // Slate 700
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    btn.setBackground(ThemeColor.SIDEBAR);
                    btn.setForeground(ThemeColor.TEXT_SECONDARY);
                }
            }
        });
        
        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                this.dispose();
                new com.ticketbox.controller.AuthController().showLoginView();
            } else if ("PROFILE".equals(cardName)) {
                new ProfileDialog(this, loggedInUser).setVisible(true);
            } else {
                cardLayout.show(cards, cardName);
                currentCard = cardName;
                updateActiveButton(cardName);
                
                // Refresh data if needed when switching tab
                Component comp = null;
                for (Component c : cards.getComponents()) {
                    if (c.isVisible()) {
                        comp = c;
                        break;
                    }
                }
                
                if (comp instanceof HomePanel) {
                     ((HomePanel) comp).loadEvents();
                } else if (comp instanceof MyTicketsPanel) {
                     ((MyTicketsPanel) comp).loadData();
                } else if (comp instanceof OrganizerPanel) {
                     ((OrganizerPanel) comp).loadData();
                } else if (comp instanceof StatisticsPanel) {
                     ((StatisticsPanel) comp).loadData();
                } else if (comp instanceof AdminPanel) {
                     ((AdminPanel) comp).loadData();
                }
            }
        });
        
        menuButtons.put(cardName, btn);
        sidebar.add(btn);
        sidebar.add(javax.swing.Box.createVerticalStrut(5));
    }
    
    private void updateActiveButton(String activeCard) {
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            String key = entry.getKey();
            JButton btn = entry.getValue();
            
            if (key.equals(activeCard)) {
                btn.setBackground(ThemeColor.PRIMARY);
                btn.setForeground(Color.WHITE);
            } else if (!key.equals("LOGOUT") && !key.equals("PROFILE")) { // Keep Logout/Profile separate style? Or reset all
                btn.setBackground(ThemeColor.SIDEBAR);
                btn.setForeground(ThemeColor.TEXT_SECONDARY);
            } else {
                 btn.setBackground(ThemeColor.SIDEBAR);
                 btn.setForeground(ThemeColor.TEXT_SECONDARY);
            }
        }
    }
}
