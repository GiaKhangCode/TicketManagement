package com.ticketbox.view;

import com.ticketbox.dao.BookingDAO;
import com.ticketbox.model.Booking;
import com.ticketbox.model.Ticket;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyTicketsPanel extends JPanel {
    private User user;
    private BookingDAO bookingDAO;
    private com.ticketbox.dao.ReviewDAO reviewDAO; // Added ReviewDAO
    private com.ticketbox.dao.EventDAO eventDAO; // For fetching event details
    private JPanel listPanel;

    public MyTicketsPanel(User user) {
        this.user = user;
        this.bookingDAO = new BookingDAO();
        this.reviewDAO = new com.ticketbox.dao.ReviewDAO();
        this.eventDAO = new com.ticketbox.dao.EventDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_MAIN);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        
        JLabel lblHeader = new JLabel("Vé của tôi");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(ThemeColor.TEXT_PRIMARY);
        headerPanel.add(lblHeader, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content List (Scrollable)
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ThemeColor.BG_MAIN);
        listPanel.setBorder(new EmptyBorder(10, 30, 20, 30));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(ThemeColor.BG_MAIN);
        scrollPane.getViewport().setBackground(ThemeColor.BG_MAIN);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void loadData() {
        listPanel.removeAll();
        List<Booking> bookings = bookingDAO.getBookingsByUser(user.getId());
        
        if (bookings.isEmpty()) {
            JLabel lblEmpty = new JLabel("Bạn chưa đặt vé nào.", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(50));
            listPanel.add(lblEmpty);
        } else {
            for (Booking b : bookings) {
                listPanel.add(createBookingCard(b));
                listPanel.add(Box.createVerticalStrut(15));
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private JPanel createBookingCard(Booking booking) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColor.BG_CARD); // Dark Card Background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Border
                g2.setColor(new Color(63, 63, 70)); // Dark Border
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setPreferredSize(new Dimension(600, 120));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                 // Optional: Highlight border logic
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                showBookingDetails(booking);
            }
        });

        // 1. Left - Icon & Date
        JPanel leftInfo = new JPanel(new GridLayout(2, 1));
        leftInfo.setOpaque(false);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        
        JLabel lblDate = new JLabel(sdf.format(booking.getBookingDate()));
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDate.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JLabel lblTime = new JLabel(timeSdf.format(booking.getBookingDate()));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTime.setForeground(ThemeColor.TEXT_SECONDARY);
        
        leftInfo.add(lblDate);
        leftInfo.add(lblTime);
        
        // 2. Center - Booking Info
        JPanel centerInfo = new JPanel(new GridLayout(2, 1));
        centerInfo.setOpaque(false);
        centerInfo.setBorder(new EmptyBorder(0, 30, 0, 0));
        
        JLabel lblId = new JLabel("Mã đơn: #" + booking.getId());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblId.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JLabel lblPrice = new JLabel(String.format("%,.0f VNĐ", booking.getTotalAmount()));
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblPrice.setForeground(ThemeColor.PRIMARY);
        
        centerInfo.add(lblId);
        centerInfo.add(lblPrice);
        
        // 3. Right - Status Badge
        JPanel rightInfo = new JPanel(new GridBagLayout());
        rightInfo.setOpaque(false);
        
        JLabel lblStatus = new JLabel(booking.getStatus());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setOpaque(true);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setPreferredSize(new Dimension(100, 30));
        
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "SUCCESS".equalsIgnoreCase(booking.getStatus())) {
            lblStatus.setBackground(new Color(6, 78, 59)); // Emerald 900
            lblStatus.setForeground(new Color(52, 211, 153));   // Emerald 400
        } else {
             lblStatus.setBackground(new Color(127, 29, 29)); // Red 900
             lblStatus.setForeground(new Color(248, 113, 113));   // Red 400
        }
        
        rightInfo.add(lblStatus);
        
        // Review Button (Only if Success/Confirmed)
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "SUCCESS".equalsIgnoreCase(booking.getStatus())) {
            // Check if already reviewed?
            // Need event ID. Booking -> Schedule -> Event.
            // But we don't have eventId handy here easily without fetching.
            // Let's postpone check to click or do async load? 
            // For UI responsiveness, let's just add button and check state inside action or lightweight check.
            // Ideally, we should fetch Event with Booking or use EventDAO.
            
            // We can fetch event now since we have booking.scheduleId
            com.ticketbox.model.Event evt = eventDAO.getEventByScheduleId(booking.getScheduleId());
            if (evt != null) {
                boolean hasReviewed = reviewDAO.hasUserReviewed(user.getId(), evt.getId());
                
                JButton btnReview = new JButton(hasReviewed ? "Đã đánh giá" : "Đánh giá");
                btnReview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnReview.setBackground(hasReviewed ? ThemeColor.BG_MAIN : ThemeColor.ACCENT);
                btnReview.setForeground(hasReviewed ? ThemeColor.TEXT_SECONDARY : Color.WHITE);
                btnReview.setEnabled(!hasReviewed);
                
                if (!hasReviewed) {
                    btnReview.addActionListener(e -> {
                        // Stop propagation to card click
                        // Use ReviewDialog
                        Window win = SwingUtilities.getWindowAncestor(this);
                        ReviewDialog dialog = new ReviewDialog(win, user, evt);
                        dialog.setVisible(true);
                        // Reload data to update button state
                        loadData();
                    });
                     // Prevent card click when clicking button? 
                     // Since button captures click, it should be fine.
                }
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = 1;
                gbc.insets = new Insets(5, 0, 0, 0);
                rightInfo.add(btnReview, gbc);
            }
        }
        
        card.add(leftInfo, BorderLayout.WEST);
        card.add(centerInfo, BorderLayout.CENTER);
        card.add(rightInfo, BorderLayout.EAST);
        
        return card;
    }
    
    private void showBookingDetails(Booking booking) {
        List<Ticket> tickets = bookingDAO.getTicketsByBooking(booking.getId());
        
        JDialog detailDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết đơn hàng #" + booking.getId(), Dialog.ModalityType.APPLICATION_MODAL);
        detailDialog.setSize(500, 600);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.getContentPane().setBackground(ThemeColor.BG_MAIN);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeColor.BG_MAIN);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("Danh sách vé");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(20));
        
        for (Ticket t : tickets) {
            JPanel tPanel = new JPanel(new BorderLayout());
            tPanel.setBackground(ThemeColor.BG_CARD);
            tPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                new EmptyBorder(10, 10, 10, 10)
            ));
            tPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            
            JLabel lblQr = new JLabel("🎫 Code: " + t.getQrCode());
            lblQr.setFont(new Font("Monospaced", Font.BOLD, 16));
            lblQr.setForeground(ThemeColor.ACCENT);
            
            tPanel.add(lblQr, BorderLayout.WEST);
            
            // Status/Resell Button
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actionPanel.setOpaque(false);
            
            if ("USED".equals(t.getStatus())) {
                JLabel lblUsed = new JLabel("Đã sử dụng");
                lblUsed.setForeground(ThemeColor.TEXT_SECONDARY);
                actionPanel.add(lblUsed);
            } else {
                JButton btnResell = new JButton(t.isIsResale() ? "Đang bán: " + String.format("%,.0f", t.getResalePrice()) : "Bán lại");
                btnResell.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnResell.setBackground(t.isIsResale() ? ThemeColor.ACCENT : ThemeColor.SECONDARY);
                btnResell.setForeground(Color.WHITE);
                
                btnResell.addActionListener(e -> {
                    ResellDialog dlg = new ResellDialog(SwingUtilities.getWindowAncestor(this), t);
                    dlg.setVisible(true);
                    if (dlg.isUpdated()) {
                        detailDialog.dispose();
                        loadData(); // Refresh main list
                    }
                });
                actionPanel.add(btnResell);
            }
            
            tPanel.add(actionPanel, BorderLayout.EAST);
            
            content.add(tPanel);
            content.add(Box.createVerticalStrut(10));
        }
        
        detailDialog.add(new JScrollPane(content), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> detailDialog.dispose());
        btnClose.setBackground(ThemeColor.BG_CARD);
        btnClose.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JPanel footer = new JPanel();
        footer.setBackground(ThemeColor.BG_MAIN);
        footer.add(btnClose);
        detailDialog.add(footer, BorderLayout.SOUTH);
        
        detailDialog.setVisible(true);
    }
}
