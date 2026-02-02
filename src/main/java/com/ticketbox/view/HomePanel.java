package com.ticketbox.view;

import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.EventCard;
import com.ticketbox.view.component.RoundedButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HomePanel extends JPanel {
    private User user;
    private EventDAO eventDAO;
    private JPanel mainContainer; // Replaces gridPanel
    private JTextField txtSearch;
    private String selectedCategory = "Tất cả";
    private Map<String, JLabel> categoryButtons;
    
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
        heroPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 
        
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
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.SECONDARY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton btnSearch = new RoundedButton("Tìm kiếm", 10);
        btnSearch.setBackground(ThemeColor.BG_CARD); 
        btnSearch.setForeground(ThemeColor.TEXT_PRIMARY); 
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
            lblCat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            lblCat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectCategory(cat);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!cat.equals(selectedCategory)) {
                        lblCat.setForeground(ThemeColor.PRIMARY);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!cat.equals(selectedCategory)) {
                        lblCat.setForeground(ThemeColor.TEXT_SECONDARY);
                    }
                }
            });
            
            categoryButtons.put(cat, lblCat);
            categoryBar.add(lblCat);
        }
        
        searchContainer.add(categoryBar);
        heroPanel.add(searchContainer);
        add(heroPanel, BorderLayout.NORTH);
        
        // --- 3. Main Content Area (Scrollable) ---
        mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(ThemeColor.BG_MAIN);
        mainContainer.setBorder(new EmptyBorder(20, 40, 40, 40));
        
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize Default
        selectCategory("Tất cả");
    }
    
    private void selectCategory(String category) {
        this.selectedCategory = category;
        
        // Update UI styles
        for (Map.Entry<String, JLabel> entry : categoryButtons.entrySet()) {
            if (entry.getKey().equals(category)) {
                entry.getValue().setForeground(ThemeColor.PRIMARY);
            } else {
                entry.getValue().setForeground(ThemeColor.TEXT_SECONDARY);
            }
        }
        
        performSearch();
    }
    
    public void loadEvents() {
        // Explicit reload
        performSearch(); 
    }
    
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String category = selectedCategory;
        
        new Thread(() -> {
            // Fetch Showcases first (only if "Tất cả" and no keyword)
            List<com.ticketbox.model.Showcase> showcases = new java.util.ArrayList<>();
            java.util.Set<Integer> showcasedEventIds = new java.util.HashSet<>();
            
            if ("Tất cả".equals(category) && keyword.isEmpty()) {
                showcases = new com.ticketbox.dao.ShowcaseDAO().getActiveShowcasesWithEvents();
                for (com.ticketbox.model.Showcase s : showcases) {
                    for (Event e : s.getEvents()) {
                        showcasedEventIds.add(e.getId());
                    }
                }
            }
            
            List<Event> events;
            // Always fetch based on category selected, but "Tất cả" fetches Approved.
            if ("Tất cả".equals(category)) {
                events = eventDAO.getAllApprovedEvents();
            } else {
                 events = eventDAO.getEventsByCategory(category);
            }
            
            // Filter by keyword if exists
            if (!keyword.isEmpty()) {
                 final String k = keyword.toLowerCase();
                 events = events.stream()
                         .filter(e -> e.getName().toLowerCase().contains(k) 
                                   || (e.getLocation() != null && e.getLocation().toLowerCase().contains(k)))
                         .collect(Collectors.toList());
            }
            
            // Exclude showcased events from main list if we are showing showcases
            if (!showcasedEventIds.isEmpty()) {
                events.removeIf(e -> showcasedEventIds.contains(e.getId()));
            }
            
            updateContent(showcases, events, category);
        }).start();
    }
    
    private void updateContent(List<com.ticketbox.model.Showcase> showcases, List<Event> events, String filterContext) {
        SwingUtilities.invokeLater(() -> {
            mainContainer.removeAll();
            
            boolean hasContent = false;
            
            // 1. Render Showcases (if any)
            if (showcases != null && !showcases.isEmpty()) {
                for (com.ticketbox.model.Showcase s : showcases) {
                    if (!s.getEvents().isEmpty()) {
                         addCategorySection(s.getName(), s.getEvents()); // Reuse same section style
                         hasContent = true;
                    }
                }
            }
            
            // 2. Render Remaining Events
            if (events.isEmpty() && !hasContent) {
                JLabel lblEmpty = new JLabel("Không tìm thấy sự kiện nào.", SwingConstants.CENTER);
                lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
                mainContainer.add(Box.createVerticalStrut(50));
                mainContainer.add(lblEmpty);
            } else if (!events.isEmpty()) {
                if ("Tất cả".equals(filterContext)) {
                    // Group by category
                    Map<String, List<Event>> grouped = events.stream()
                            .collect(Collectors.groupingBy(e -> e.getCategory() != null ? e.getCategory() : "Khác"));
                    
                    String[] orderedCats = { 
                        "Nhạc sống", "Sân khấu & Nghệ thuật", "Thể Thao", 
                        "Hội thảo & Workshop", "Tham quan & Trải nghiệm", "Khác"
                    };
                    
                    for (String cat : orderedCats) {
                        if (grouped.containsKey(cat) && !grouped.get(cat).isEmpty()) {
                             addCategorySection(cat, grouped.get(cat));
                        }
                    }
                    
                    // Handle others
                    for (String key : grouped.keySet()) {
                        boolean isOrdered = false;
                        for (String o : orderedCats) if (o.equals(key)) isOrdered = true;
                        if (!isOrdered && !grouped.get(key).isEmpty()) {
                            addCategorySection(key, grouped.get(key));
                        }
                    }
                    
                } else {
                    addCategorySection(filterContext, events);
                }
            }
            
            mainContainer.revalidate();
            mainContainer.repaint();
        });
    }
    
    private void addCategorySection(String title, List<Event> events) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(ThemeColor.BG_MAIN);
        section.setBorder(new EmptyBorder(0, 0, 10, 0)); // Bottom spacing reduced (was 30)

        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420)); // Cap height - Reduced since cards are smaller

        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeColor.BG_MAIN);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JLabel lblMore = new JLabel("Xem thêm >");
        lblMore.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMore.setForeground(ThemeColor.ACCENT);
        lblMore.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblMore, BorderLayout.EAST);
        section.add(header, BorderLayout.NORTH);
        
        // Horizontal List
        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10)); // Gap 20
        cardContainer.setBackground(ThemeColor.BG_MAIN);
        
        for (Event e : events) {
            EventCard card = new EventCard(e, user);
            cardContainer.add(card);
        }
        
        JScrollPane scrollPane = new JScrollPane(cardContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setPreferredSize(new Dimension(800, 390)); // Adjusted for card size (350 + scrollbar)

        
        section.add(scrollPane, BorderLayout.CENTER);
        
        mainContainer.add(section);
    }
}
