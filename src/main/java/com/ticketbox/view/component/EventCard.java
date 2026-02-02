package com.ticketbox.view.component;

import com.ticketbox.model.Event;
import com.ticketbox.model.EventSchedule;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.BookingDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class EventCard extends JPanel {

    private Event event;
    private User user;
    private JLabel lblImage;

    public EventCard(Event event, User user) {
        this.event = event;
        this.user = user;
        initComponents();
        loadEventImage();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            BorderFactory.createEmptyBorder(0, 0, 15, 0) // Padding bottom
        ));
        
        // Fixed size for card logic often handled by parent layout, but we can set preferred size
        setPreferredSize(new Dimension(280, 350));

        // --- 1. Image Section ---
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.BLACK);
        imagePanel.setPreferredSize(new Dimension(280, 150));
        
        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setForeground(Color.GRAY);
        // Default text while loading or if missing
        lblImage.setText(event.getName());
        
        imagePanel.add(lblImage, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.NORTH);

        // --- 2. Details Section ---
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(ThemeColor.BG_CARD);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Title
        JLabel lblTitle = new JLabel(event.getName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblTitle);
        detailsPanel.add(Box.createVerticalStrut(5));

        // Category
        if (event.getCategory() != null) {
            JLabel lblCategory = new JLabel(event.getCategory().toUpperCase());
            lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCategory.setForeground(ThemeColor.PRIMARY);
            lblCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsPanel.add(lblCategory);
            detailsPanel.add(Box.createVerticalStrut(5));
        }

        // Location
        JLabel lblLocation = new JLabel("Địa điểm: " + (event.getLocation() != null ? event.getLocation() : "TBA"));
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocation.setForeground(ThemeColor.TEXT_SECONDARY);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblLocation);
        
        detailsPanel.add(Box.createVerticalStrut(5));
        
        // Date (Start Time)
        String dateStr = event.getStartTime() != null ? 
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(event.getStartTime()) : "TBA";
        JLabel lblDate = new JLabel("Thời gian: " + dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(ThemeColor.TEXT_SECONDARY);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblDate);

        add(detailsPanel, BorderLayout.CENTER);

        // --- 3. Action Section ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setBackground(ThemeColor.BG_CARD);

        JButton btnBuy = new RoundedButton("Mua vé ngay", 15);
        btnBuy.setBackground(ThemeColor.PRIMARY);
        btnBuy.setForeground(ThemeColor.TEXT_INVERSE);
        btnBuy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuy.setPreferredSize(new Dimension(150, 35));
        btnBuy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBuyTicket();
            }
        });

        actionPanel.add(btnBuy);
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadEventImage() {
        String urlString = event.getImageUrl();
        if (urlString == null || urlString.isEmpty()) {
            lblImage.setText("No Image");
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(urlString);
                BufferedImage image = ImageIO.read(url);
                if (image != null) {
                    // Resize to fit 280x150
                    Image scaled = image.getScaledInstance(280, 150, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblImage.setText("");
                        lblImage.setIcon(icon);
                    } else {
                        lblImage.setText("Image Error");
                    }
                } catch (Exception e) {
                    lblImage.setText("Error");
                    // e.printStackTrace(); 
                }
            }
        };
        worker.execute();
    }

    private void handleBuyTicket() {
        List<EventSchedule> schedules = event.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sự kiện chưa có lịch diễn!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Simplified logic: Pick the first schedule
        EventSchedule selectedSchedule = schedules.get(0);

        Window window = SwingUtilities.getWindowAncestor(this);
        BookingDialog dialog = new BookingDialog(window, event, selectedSchedule, user);
        dialog.setVisible(true);
    }
}
