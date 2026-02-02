package com.ticketbox.view;

import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.EventCard;
import com.ticketbox.view.component.RoundedButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class HomePanel extends JPanel {
    private User user;
    private EventDAO eventDAO;
    private JPanel gridPanel;
    private JTextField txtSearch;
    private String selectedCategory = "Tất cả";
    private java.util.Map<String, JLabel> categoryButtons;
    
    public HomePanel(User user) {
        this.user = user;
        this.eventDAO = new EventDAO();
        initComponents();
        // loadEvents is called via selectCategory in init
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // --- 1. Hero Section / Banner ---
        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBackground(ThemeColor.SIDEBAR); // Darker top
        heroPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // Reduced padding
        
        // Removed text labels as requested
        
        // --- 2. Search & Filter Bar ---
        JPanel searchContainer = new JPanel();
        searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.Y_AXIS));
        searchContainer.setOpaque(false);
        searchContainer.setAlignmentX(CENTER_ALIGNMENT);
        
        // Search Row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchRow.setOpaque(false);
        
        txtSearch = new JTextField(30);
        txtSearch.putClientProperty("JTextField.placeholderText", "Bạn tìm gì hôm nay?");
        txtSearch.setPreferredSize(new Dimension(400, 45));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Add padding inside text field
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.SECONDARY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton btnSearch = new RoundedButton("Tìm kiếm", 10);
        btnSearch.setBackground(ThemeColor.BG_CARD); 
        btnSearch.setForeground(ThemeColor.TEXT_PRIMARY); // Clean look
        btnSearch.setPreferredSize(new Dimension(100, 45));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.addActionListener(e -> performSearch());
        
        searchRow.add(txtSearch);
        searchRow.add(btnSearch);
        
        searchContainer.add(searchRow);
        searchContainer.add(Box.createVerticalStrut(20));
        
        // Category Bar Row
        JPanel categoryBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        categoryBar.setOpaque(false);
        
        String[] categories = { 
            "Tất cả", "Nhạc sống", "Sân khấu & Nghệ thuật", "Thể Thao", 
            "Hội thảo & Workshop", "Tham quan & Trải nghiệm", "Khác", "Vé bán lại" 
        };
        
        categoryButtons = new java.util.HashMap<>();
        
        for (String cat : categories) {
            JLabel lblCat = new JLabel(cat);
            lblCat.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblCat.setForeground(ThemeColor.TEXT_SECONDARY);
            lblCat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            
            lblCat.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectCategory(cat);
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!cat.equals(selectedCategory)) {
                        lblCat.setForeground(ThemeColor.PRIMARY);
                    }
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!cat.equals(selectedCategory)) {
                        lblCat.setForeground(ThemeColor.TEXT_SECONDARY);
                    }
                }
            });
            
            categoryButtons.put(cat, lblCat);
            categoryBar.add(lblCat);
        }
        
        searchContainer.add(categoryBar);
        
        // heroPanel.add(Box.createVerticalStrut(10)); // Minimized space
        heroPanel.add(searchContainer);
        
        add(heroPanel, BorderLayout.NORTH);
        
        // --- 3. Event Grid (Scrollable) ---
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20)); // 3 columns
        gridPanel.setBackground(ThemeColor.BG_MAIN);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Wrap grid in container to align top-left if few items
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(ThemeColor.BG_MAIN);
        gridWrapper.add(gridPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize Default selection
        selectCategory("Tất cả");
    }
    
    private void selectCategory(String category) {
        this.selectedCategory = category;
        
        // Update UI styles
        for (java.util.Map.Entry<String, JLabel> entry : categoryButtons.entrySet()) {
            if (entry.getKey().equals(category)) {
                entry.getValue().setForeground(ThemeColor.PRIMARY); // Active
                 // Optional: Underline or bold stronger?
            } else {
                entry.getValue().setForeground(ThemeColor.TEXT_SECONDARY);
            }
        }
        
        performSearch();
    }
    
    public void loadEvents() {
        // Initial load handled by constructor calling selectCategory -> performSearch
        // But if needed explicitly:
        // performSearch(); 
    }
    
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String category = selectedCategory;
        
        new Thread(() -> {
            List<Event> events;
            if ("Tất cả".equals(category) && keyword.isEmpty()) {
                events = eventDAO.getAllApprovedEvents();
            } else if (category != null && !"Tất cả".equals(category)) {
                // Filter by category
                events = eventDAO.getEventsByCategory(category);
                 if (!keyword.isEmpty()) {
                     events.removeIf(e -> !e.getName().toLowerCase().contains(keyword.toLowerCase()));
                 }
            } else {
                 // Only keyword
                 events = eventDAO.searchApprovedEvents(keyword);
            }
            updateGrid(events);
        }).start();
    }
    
    private void updateGrid(List<Event> events) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.removeAll();
            
            if (events.isEmpty()) {
                JLabel lblEmpty = new JLabel("Không tìm thấy sự kiện nào phù hợp.", SwingConstants.CENTER);
                lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
                // Center in grid (trick: span 3 cols?) -> Grid doesn't span easily.
                // Just add to wrapper or resize grid.
                gridPanel.setLayout(new GridLayout(1, 1));
                gridPanel.add(lblEmpty);
            } else {
                gridPanel.setLayout(new GridLayout(0, 3, 20, 20)); // Reset to 3 cols
                for (Event e : events) {
                    gridPanel.add(new EventCard(e, user));
                }
            }
            
            gridPanel.revalidate();
            gridPanel.repaint();
        });
    }
}
