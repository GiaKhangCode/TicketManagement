package com.ticketbox.view;

import com.ticketbox.dao.TicketDAO;
import com.ticketbox.model.Ticket;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;

import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResaleMarketplacePanel extends JPanel {
    // private User user; // Unused for now in demo logic
    private TicketDAO ticketDAO;
    private JPanel listPanel;
    private JTextField txtSearch;

    public ResaleMarketplacePanel(User user) {
        // this.user = user;
        this.ticketDAO = new TicketDAO();
        initComponents();
        loadData("");
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);

        // Header with Search
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("Chợ vé Resale");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBox.setOpaque(false);
        
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm sự kiện...");
        txtSearch.setPreferredSize(new Dimension(250, 35));
        
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setBackground(ThemeColor.PRIMARY);
        btnSearch.setForeground(ThemeColor.TEXT_INVERSE);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));
        
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(searchBox, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Content
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); // Vertical list
        listPanel.setBackground(ThemeColor.BG_MAIN);
        
        // Wrap in another panel to align top if few items
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeColor.BG_MAIN);
        wrapper.add(listPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadData(String query) {
        listPanel.removeAll();
        List<Ticket> tickets = ticketDAO.getResaleTickets(query);

        if (tickets.isEmpty()) {
            JLabel lblEmpty = new JLabel("Hiện chưa có vé nào được bán lại.");
            lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
            lblEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblEmpty.setBorder(new EmptyBorder(20, 30, 0, 0));
            listPanel.add(lblEmpty);
        } else {
            // Grid Layout for cards? BoxLayout is row by row. 
            // Let's us flow layout for cards wrapping or just vertical list of cards.
            // Vertical list better for details.
            for (Ticket t : tickets) {
                 // Don't show own tickets?
                 // For demo, maybe ok, but ideally filter out.
                 // We don't have owner ID in `getResaleTickets` result yet without joining Booking.
                 // Assume validation happens on Buy.
                 
                 listPanel.add(createTicketCard(t));
                 listPanel.add(Box.createVerticalStrut(15));
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createTicketCard(Ticket t) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColor.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(800, 140)); // Wider card
        card.setPreferredSize(new Dimension(800, 140));
        
        // Left: Event Info
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblEvent = new JLabel(t.getEventName());
        lblEvent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEvent.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JLabel lblTimeAddr = new JLabel("Thời gian: " + t.getEventDate() + "  Địa điểm: " + t.getLocation());
        lblTimeAddr.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTimeAddr.setForeground(ThemeColor.TEXT_SECONDARY);
        
        JLabel lblType = new JLabel("Loại vé: " + t.getTicketTypeName() + " | Gốc: " + String.format("%,.0f", t.getPrice()));
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblType.setForeground(ThemeColor.TEXT_SECONDARY);
        
        infoPanel.add(lblEvent);
        infoPanel.add(lblTimeAddr);
        infoPanel.add(lblType);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Right: Price & Buy
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // Price top, Button bottom
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(200, 0));
        
        JLabel lblResalePrice = new JLabel(String.format("%,.0f VNĐ", t.getResalePrice()), SwingConstants.RIGHT);
        lblResalePrice.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblResalePrice.setForeground(ThemeColor.ACCENT);
        
        JButton btnBuy = new JButton("Mua ngay");
        btnBuy.setBackground(ThemeColor.PRIMARY);
        btnBuy.setForeground(ThemeColor.TEXT_INVERSE);
        btnBuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBuy.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn mua vé này với giá " + String.format("%,.0f VNĐ", t.getResalePrice()) + "?",
                "Xác nhận mua", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // Mock Buy
                boolean success = ticketDAO.updateResaleStatus(t.getId(), false, 0); // Remove from market
                // In real app: create new booking/ticket for buyer and invalidate old one.
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Mua vé thành công!");
                    loadData(txtSearch.getText());
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi mua vé.");
                }
            }
        });
        
        actionPanel.add(lblResalePrice);
        actionPanel.add(btnBuy);
        
        card.add(actionPanel, BorderLayout.EAST);
        
        return card;
    }
}
