package com.ticketbox.view.component;

import com.ticketbox.model.Event;
import com.ticketbox.model.EventSchedule;
import com.ticketbox.model.User;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.BookingDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class EventCard extends JPanel {

    private Event event;
    private User user;


    private int cornerRadius = 20;

    public EventCard(Event event, User user) {
        this.event = event;
        this.user = user;
        initComponents();
        loadEventImage();
        initInteractions();
    }

    private void initInteractions() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBuyTicket();
            }
        };
        addMouseListener(ma);
        
        // Add listener to all child panels (ImagePanel, DetailsPanel, ActionPanel)
        // so clicks on them trigger the buy action too.
        for (Component c : getComponents()) {
            c.addMouseListener(ma);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false); // Important for rounded corners
        setBackground(ThemeColor.BG_CARD);
        setBorder(new EmptyBorder(0, 0, 5, 0)); // Padding bottom reduced
        
        setPreferredSize(new Dimension(320, 365)); // Reduced height to remove excess space

        // --- 1. Image Section ---
        imagePanel = new ImagePanel();
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(320, 200)); 
        
        add(imagePanel, BorderLayout.NORTH);

        // --- 2. Details Section ---
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false); // Transparent to show rounded bg
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Reduced padding top/bottom

        // Title
        JLabel lblTitle = new JLabel(event.getName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblTitle);
        detailsPanel.add(Box.createVerticalStrut(2)); // Reduced gap

        // Category
        if (event.getCategory() != null) {
            JLabel lblCategory = new JLabel(event.getCategory().toUpperCase());
            lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCategory.setForeground(ThemeColor.PRIMARY);
            lblCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsPanel.add(lblCategory);
            detailsPanel.add(Box.createVerticalStrut(2)); // Reduced gap
        }

        // Location
        JLabel lblLocation = new JLabel("Địa điểm: " + (event.getLocation() != null ? event.getLocation() : "TBA"));
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocation.setForeground(ThemeColor.TEXT_SECONDARY);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblLocation);
        
        detailsPanel.add(Box.createVerticalStrut(2)); // Reduced gap
        
        // Date (Start Time)
        String dateStr = event.getStartTime() != null ? 
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(event.getStartTime()) : "TBA";
        JLabel lblDate = new JLabel("Thời gian: " + dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(ThemeColor.TEXT_SECONDARY);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblDate);

        detailsPanel.add(lblDate);

        detailsPanel.add(Box.createVerticalStrut(5)); // Reduced from 10
        
        // Price "From ..."
        double minPrice = 0;
        if (event.getTicketTypes() != null && !event.getTicketTypes().isEmpty()) {
            minPrice = event.getTicketTypes().stream()
                    .mapToDouble(com.ticketbox.model.TicketType::getPrice)
                    .min().orElse(0);
        }
        
        String priceText = minPrice > 0 ? 
                "Từ " + java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN")).format(minPrice) 
                : "Liên hệ";
        
        JLabel lblPrice = new JLabel(priceText);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(ThemeColor.PRIMARY); // Match app theme
        lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(lblPrice);

        detailsPanel.add(Box.createVerticalStrut(15)); // Add some spacing before button

        // --- 3. Action Section (Moved inside Details for better flow) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // Button itself centered in panel
        actionPanel.setOpaque(false);
        // CRITICAL: Set alignment to LEFT to match other siblings in BoxLayout, preventing them from shifting.
        // We stretch the panel width so FlowLayout.CENTER still works effectively.
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

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
        
        // Add mouse listener to button manually if needed, or rely on component recursion
        // Since we add to all components in initInteractions, it's fine.

        actionPanel.add(btnBuy);
        detailsPanel.add(actionPanel);

        add(detailsPanel, BorderLayout.CENTER);

        // --- 3. Action Section --- removed from South
        // JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // ... handled in details panel ...
        // add(actionPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.dispose();
        super.paintComponent(g);
    }
    
    private ImagePanel imagePanel;

    // Custom Panel for better image scaling
    private class ImagePanel extends JPanel {
        private Image img;

        public void setImage(Image img) {
            this.img = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Do NOT call super.paintComponent(g) if opaque is false, unless we handle it.
            // But we want to clip the image.
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clip to top rounded corners
            // We create a path that has rounded top corners and square bottom corners
            // Since ImagePanel is at the top, we just need to match the parent's radius
            
            int w = getWidth();
            int h = getHeight();
            int r = cornerRadius;
            
            // Define a shape that is rounded at top, square at bottom
            Path2D.Float path = new Path2D.Float();
            path.moveTo(0, h);
            path.lineTo(0, r);
            path.quadTo(0, 0, r, 0); // Top left
            path.lineTo(w - r, 0);
            path.quadTo(w, 0, w, r); // Top right
            path.lineTo(w, h);
            path.closePath();
            
            g2.setClip(path);
            
            if (img != null) {
                g2.drawImage(img, 0, 0, w, h, null);
            } else {
                // Placeholder
                g2.setColor(new Color(30, 30, 30));
                g2.fill(path); // Fill the clipped shape
                g2.setColor(Color.GRAY);
                String text = event != null ? event.getName() : "Loading...";
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(text)) / 2;
                int y = (h + fm.getAscent()) / 2;
                g2.drawString(text, x, y);
            }
            g2.dispose();
        }
    }
    
    private void loadEventImage() {
        String urlString = event.getImageUrl();
        if (urlString == null || urlString.isEmpty()) {
            return;
        }

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                URL url = new URL(urlString);
                return ImageIO.read(url);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    if (image != null) {
                        imagePanel.setImage(image);
                    }
                } catch (Exception e) {
                    // Fail silently or show placeholder
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
