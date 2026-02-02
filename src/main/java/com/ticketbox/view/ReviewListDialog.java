package com.ticketbox.view;

import com.ticketbox.dao.ReviewDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.Review;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ReviewListDialog extends JDialog {
    private Event event;
    private ReviewDAO reviewDAO;

    public ReviewListDialog(Window owner, Event event) {
        super(owner, "Đánh giá về: " + event.getName(), ModalityType.APPLICATION_MODAL);
        this.event = event;
        this.reviewDAO = new ReviewDAO();
        
        setSize(500, 600);
        setLocationRelativeTo(owner);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeColor.BG_MAIN);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeColor.BG_MAIN);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("Đánh giá từ khách hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // List of Reviews
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ThemeColor.BG_MAIN);
        listPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        List<Review> reviews = reviewDAO.getReviewsByEvent(event.getId());
        
        if (reviews.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có đánh giá nào cho sự kiện này.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(ThemeColor.TEXT_SECONDARY);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(50));
            listPanel.add(lblEmpty);
        } else {
            for (Review r : reviews) {
                listPanel.add(createReviewItem(r));
                listPanel.add(Box.createVerticalStrut(15));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(ThemeColor.BG_MAIN);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close Button
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(ThemeColor.BG_MAIN);
        footerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setBackground(ThemeColor.BG_CARD);
        btnClose.setForeground(ThemeColor.TEXT_PRIMARY);
        btnClose.addActionListener(e -> dispose());
        
        footerPanel.add(btnClose);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createReviewItem(Review review) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColor.BG_CARD);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); // Cap height
        
        // Top: Rating + Date
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel lblStars = new JLabel(getStarString(review.getRating()));
        lblStars.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        lblStars.setForeground(new Color(255, 215, 0)); // Gold
        
        String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(review.getCreatedAt());
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(ThemeColor.TEXT_SECONDARY);
        
        topPanel.add(lblStars, BorderLayout.WEST);
        topPanel.add(lblDate, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center: Comment
        JTextArea txtComment = new JTextArea(review.getComment());
        txtComment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtComment.setForeground(ThemeColor.TEXT_PRIMARY);
        txtComment.setBackground(ThemeColor.BG_CARD);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setEditable(false);
        txtComment.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        panel.add(txtComment, BorderLayout.CENTER);
        
        // Bottom: User (Masked)
        JLabel lblUser = new JLabel("Khách hàng #" + review.getUserId());
        lblUser.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblUser.setForeground(ThemeColor.TEXT_SECONDARY);
        lblUser.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        panel.add(lblUser, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private String getStarString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) sb.append("★");
            else sb.append("☆");
        }
        return sb.toString();
    }
}
