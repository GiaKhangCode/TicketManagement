package com.ticketbox.view;

import com.ticketbox.dao.BookingDAO;
import com.ticketbox.dao.TicketTypeDAO;
import com.ticketbox.model.Booking;
import com.ticketbox.model.Event;
import com.ticketbox.model.Ticket;
import com.ticketbox.model.TicketType;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.*;

public class BookingDialog extends JDialog {
    private Event event;
    private com.ticketbox.model.EventSchedule schedule;
    private User user;
    private TicketTypeDAO ticketTypeDAO;
    private BookingDAO bookingDAO;
    private List<TicketType> availableTypes;
    private Map<Integer, JSpinner> quantitySpinners;
    private JLabel lblTotalAmount;

    public BookingDialog(Window owner, Event event, com.ticketbox.model.EventSchedule schedule, User user) {
        super(owner, "Đặt vé: " + event.getName(), ModalityType.APPLICATION_MODAL);
        this.event = event;
        this.schedule = schedule;
        this.user = user;
        this.ticketTypeDAO = new TicketTypeDAO();
        this.bookingDAO = new BookingDAO();
        this.quantitySpinners = new HashMap<>();
        
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);
        
        // --- Split Pane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4); // 40% for details, 60% for booking
        splitPane.setBorder(null);
        splitPane.setDividerSize(5); 
        
        // --- LEFT: Event Details ---
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(ThemeColor.BG_CARD);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Image Placeholder/Loader
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(300, 180));
        lblImage.setMaximumSize(new Dimension(500, 200));
        lblImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Load image if available (Simple approach, reusing basic logic or just text)
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
             try {
                // Determine if we should load here or just show name. 
                // For a dialog, better to load asynchronously but for simplicity:
                lblImage.setText("Loading Image...");
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL(event.getImageUrl());
                        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                        if (img != null) {
                            Image scaled = img.getScaledInstance(300, -1, Image.SCALE_SMOOTH); // Width 300, maintain aspect
                             SwingUtilities.invokeLater(() -> {
                                 lblImage.setText("");
                                 lblImage.setIcon(new ImageIcon(scaled));
                             });
                        } else {
                            SwingUtilities.invokeLater(() -> lblImage.setText("No Image"));
                        }
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> lblImage.setText("Image Error"));
                    }
                }).start();
             } catch (Exception e) {
                 lblImage.setText("Image Error");
             }
        } else {
            lblImage.setText("No Image Available");
            lblImage.setForeground(Color.GRAY);
            lblImage.setBorder(BorderFactory.createLineBorder(ThemeColor.SECONDARY));
        }
        detailsPanel.add(lblImage);
        detailsPanel.add(Box.createVerticalStrut(20));
        
        // Event Name
        JLabel lblName = new JLabel("<html>" + event.getName() + "</html>"); // Wrap text
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(ThemeColor.PRIMARY);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblName);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Date
        JLabel lblDate = new JLabel("Thời gian: " + formatSchedule(schedule));
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDate.setForeground(ThemeColor.TEXT_PRIMARY);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblDate);
        detailsPanel.add(Box.createVerticalStrut(5));
        
        // Location
        JLabel lblLocation = new JLabel("Địa điểm: " + event.getLocation());
        lblLocation.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLocation.setForeground(ThemeColor.TEXT_PRIMARY);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblLocation);
        detailsPanel.add(Box.createVerticalStrut(15));
        
        // Description (Scrollable)
        JLabel lblDescHeader = new JLabel("Giới thiệu sự kiện:");
        lblDescHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDescHeader.setForeground(ThemeColor.ACCENT);
        lblDescHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblDescHeader);
        detailsPanel.add(Box.createVerticalStrut(5));
        
        JTextArea txtDesc = new JTextArea(event.getDescription() != null ? event.getDescription() : "Chưa có mô tả.");
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc.setForeground(ThemeColor.TEXT_SECONDARY);
        txtDesc.setBackground(ThemeColor.BG_CARD);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setBorder(null);
        
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(null);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(scrollDesc);
        
        detailsPanel.add(Box.createVerticalStrut(15));
        
        // View Reviews Button
        JButton btnViewReviews = new JButton("Xem đánh giá từ khách hàng");
        btnViewReviews.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnViewReviews.setForeground(ThemeColor.ACCENT);
        btnViewReviews.setContentAreaFilled(false);
        btnViewReviews.setBorderPainted(false);
        btnViewReviews.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewReviews.setHorizontalAlignment(SwingConstants.LEFT);
        btnViewReviews.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnViewReviews.addActionListener(e -> {
            new ReviewListDialog(this, event).setVisible(true);
        });
        
        detailsPanel.add(btnViewReviews);
        
        splitPane.setLeftComponent(detailsPanel);
        
        // --- RIGHT: Booking Panel ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ThemeColor.BG_MAIN);
        
        // Right Header
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rightHeader.setBackground(ThemeColor.BG_MAIN);
        rightHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        JLabel lblBookingTitle = new JLabel("Chọn vé");
        lblBookingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBookingTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        rightHeader.add(lblBookingTitle);
        rightPanel.add(rightHeader, BorderLayout.NORTH);
        
        // Ticket Types List (Reuse existing logic)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(ThemeColor.BG_MAIN);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); // Adjust padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fetch types with availability for specific schedule
        availableTypes = ticketTypeDAO.getTicketTypesByEventAndSchedule(event.getId(), schedule.getId());
        
        if (availableTypes.isEmpty()) {
            JLabel lblEmpty = new JLabel("Sự kiện này chưa mở bán vé.");
            lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
            contentPanel.add(lblEmpty);
        } else {
            // Header Row
            gbc.gridx = 0; gbc.gridy = 0; 
            addHeaderLabel(contentPanel, gbc, "Loại vé");
            
            gbc.gridx = 1; gbc.gridy = 0; 
            addHeaderLabel(contentPanel, gbc, "Giá");
            
            gbc.gridx = 2; gbc.gridy = 0; 
            addHeaderLabel(contentPanel, gbc, "Còn lại");
            
            gbc.gridx = 3; gbc.gridy = 0; 
            addHeaderLabel(contentPanel, gbc, "Số lượng");
            
            int row = 1;
            for (TicketType type : availableTypes) {
                gbc.gridx = 0; gbc.gridy = row; 
                JLabel lblTypeName = new JLabel(type.getName());
                lblTypeName.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblTypeName.setForeground(ThemeColor.TEXT_PRIMARY);
                contentPanel.add(lblTypeName, gbc);
                
                gbc.gridx = 1; gbc.gridy = row;
                JLabel lblPrice = new JLabel(String.format("%,.0f VNĐ", type.getPrice()));
                lblPrice.setForeground(ThemeColor.PRIMARY);
                contentPanel.add(lblPrice, gbc);
                
                gbc.gridx = 2; gbc.gridy = row;
                JLabel lblQty = new JLabel(String.valueOf(type.getAvailableQuantity()));
                lblQty.setForeground(ThemeColor.TEXT_SECONDARY);
                contentPanel.add(lblQty, gbc);
                
                gbc.gridx = 3; gbc.gridy = row;
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
                spinner.addChangeListener(e -> updateTotal());
                quantitySpinners.put(type.getId(), spinner);
                contentPanel.add(spinner, gbc);
                
                row++;
            }
        }
        
        // Wrap contentPanel in a ScrollPane for the right side too if many ticket types
        JScrollPane scrollTickets = new JScrollPane(contentPanel);
        scrollTickets.setBorder(null);
        scrollTickets.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(scrollTickets, BorderLayout.CENTER);
        
        // Footer (Total & Button) reused
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(ThemeColor.BG_MAIN);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        lblTotalAmount = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalAmount.setForeground(ThemeColor.PRIMARY);
        
        JButton btnConfirm = new JButton("Thanh toán");
        btnConfirm.setBackground(ThemeColor.PRIMARY);
        btnConfirm.setForeground(ThemeColor.TEXT_INVERSE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> processBooking());
        
        footerPanel.add(lblTotalAmount, BorderLayout.CENTER);
        footerPanel.add(btnConfirm, BorderLayout.EAST);
        
        rightPanel.add(footerPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Set larger size
        setPreferredSize(new Dimension(1200, 700));
    }

    private void addHeaderLabel(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ThemeColor.TEXT_SECONDARY);
        panel.add(lbl, gbc);
    }
    
    private String formatSchedule(com.ticketbox.model.EventSchedule s) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(s.getStartTime());
    }
    
    private void updateTotal() {
        double total = 0;
        for (TicketType type : availableTypes) {
            int qty = (int) quantitySpinners.get(type.getId()).getValue();
            total += qty * type.getPrice();
        }
        lblTotalAmount.setText(String.format("Tổng tiền: %,.0f VNĐ", total));
    }
    
    private void processBooking() {
        if (user == null) {
             JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để mua vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
             dispose();
             return;
        }

        double total = 0;
        List<Ticket> tickets = new ArrayList<>();
        
        for (TicketType type : availableTypes) {
            int qty = (int) quantitySpinners.get(type.getId()).getValue();
            if (qty > 0) {
                if (qty > type.getAvailableQuantity()) {
                     JOptionPane.showMessageDialog(this, "Loại vé " + type.getName() + " không đủ số lượng cho lịch diễn này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                
                total += qty * type.getPrice();
                for (int i = 0; i < qty; i++) {
                    // Generate pseudo unique QR code
                    String qrCode = event.getId() + "-" + schedule.getId() + "-" + type.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
                    tickets.add(new Ticket(type.getId(), qrCode));
                }
            }
        }
        
        if (tickets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Booking booking = new Booking(user.getId(), schedule.getId(), total);
        
        if (bookingDAO.createBooking(booking, tickets)) {
            // Send Email (Mock)
            new com.ticketbox.service.MockEmailService().sendTicketEmail(user, booking, tickets, event.getName() + " (" + formatSchedule(schedule) + ")");
            
            JOptionPane.showMessageDialog(this, "Đặt vé thành công! Mã QR đã được gửi về email.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình xử lý.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
