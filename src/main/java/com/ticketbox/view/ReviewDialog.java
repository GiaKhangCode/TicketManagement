package com.ticketbox.view;

import com.ticketbox.dao.ReviewDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.Review;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ReviewDialog extends JDialog {
    private User user;
    private Event event;
    private ReviewDAO reviewDAO;
    private int selectedRating = 0;
    private JTextArea txtComment;
    private JLabel[] starLabels;

    public ReviewDialog(Window owner, User user, Event event) {
        super(owner, "Đánh giá sự kiện", ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.event = event;
        this.reviewDAO = new ReviewDAO();
        
        setSize(400, 450);
        setLocationRelativeTo(owner);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ThemeColor.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title
        JLabel lblTitle = new JLabel("Đánh giá của bạn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Event Name
        JLabel lblEvent = new JLabel(event.getName());
        lblEvent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEvent.setForeground(ThemeColor.PRIMARY);
        lblEvent.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblEvent);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Star Rating
        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        starPanel.setBackground(ThemeColor.BG_MAIN);
        starPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        starLabels = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            final int ratingValue = i + 1;
            JLabel star = new JLabel("★"); // Or use an icon
            star.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32)); // Use a font that supports stars
            star.setForeground(Color.GRAY);
            star.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            star.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    highlightStars(ratingValue);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    highlightStars(selectedRating);
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedRating = ratingValue;
                    highlightStars(selectedRating);
                }
            });
            
            starLabels[i] = star;
            starPanel.add(star);
        }
        mainPanel.add(starPanel);
        
        mainPanel.add(Box.createVerticalStrut(5));
        JLabel lblHint = new JLabel("Chọn số sao để đánh giá");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(ThemeColor.TEXT_SECONDARY);
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblHint);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Comment Area
        JLabel lblComment = new JLabel("Nhận xét (Tùy chọn):");
        lblComment.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblComment.setForeground(ThemeColor.TEXT_PRIMARY);
        lblComment.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel lblWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblWrapper.setBackground(ThemeColor.BG_MAIN);
        lblWrapper.add(lblComment);
        lblWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); 
        mainPanel.add(lblWrapper);
        
        mainPanel.add(Box.createVerticalStrut(5));
        
        txtComment = new JTextArea(5, 20);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtComment.setBackground(ThemeColor.BG_CARD);
        txtComment.setForeground(ThemeColor.TEXT_PRIMARY);
        txtComment.setCaretColor(ThemeColor.TEXT_PRIMARY);
        txtComment.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeColor.SECONDARY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane scrollComment = new JScrollPane(txtComment);
        scrollComment.setBorder(null);
        scrollComment.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(scrollComment);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Submit Button
        JButton btnSubmit = new com.ticketbox.view.component.RoundedButton("Gửi đánh giá", 15);
        btnSubmit.setBackground(ThemeColor.PRIMARY);
        btnSubmit.setForeground(ThemeColor.TEXT_INVERSE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setPreferredSize(new Dimension(200, 40));
        btnSubmit.setMaximumSize(new Dimension(200, 40));
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnSubmit.addActionListener(e -> submitReview());
        
        mainPanel.add(btnSubmit);
        
        add(mainPanel);
    }
    
    private void highlightStars(int count) {
        for (int i = 0; i < 5; i++) {
            if (i < count) {
                starLabels[i].setForeground(new Color(255, 215, 0)); // Gold
            } else {
                starLabels[i].setForeground(Color.GRAY);
            }
        }
    }
    
    private void submitReview() {
        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn số sao đánh giá!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Review review = new Review();
        review.setUserId(user.getId());
        review.setEventId(event.getId());
        review.setRating(selectedRating);
        review.setComment(txtComment.getText().trim());
        
        if (reviewDAO.addReview(review)) {
            JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã đánh giá!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi gửi đánh giá. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
