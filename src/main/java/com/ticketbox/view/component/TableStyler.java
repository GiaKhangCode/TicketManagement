package com.ticketbox.view.component;

import com.ticketbox.util.ThemeColor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class TableStyler {

    public static void applyStyle(JTable table) {
        table.setRowHeight(50); // Increase row height for badges
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(63, 63, 70)); // Zinc 700 (Dark Grid)
        table.setBackground(ThemeColor.BG_CARD); // Dark Row Background
        table.setForeground(ThemeColor.TEXT_PRIMARY); // Light Text
        table.setSelectionBackground(ThemeColor.PRIMARY); // Selection Color
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Header Style
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 50));
        header.setBackground(ThemeColor.BG_MAIN); // Dark Header BG
    }
    
    // Custom Header Renderer
    static class HeaderRenderer extends DefaultTableCellRenderer {
        public HeaderRenderer() {
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(ThemeColor.BG_MAIN);
            setForeground(ThemeColor.TEXT_SECONDARY);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(63, 63, 70)),
                    BorderFactory.createEmptyBorder(0, 15, 0, 0)
            ));
            return this;
        }
    }
    
    // Custom Badge Renderer for Status
    public static class StatusColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // We return a custom component that paints the badge
            BadgePanel badge = new BadgePanel();
            String status = (String) value;
            badge.setText(status);
            
            // Background of the cell itself (behind badge) matches table
            badge.setCellBackground(isSelected ? table.getSelectionBackground() : ThemeColor.BG_CARD);

            if (status != null) {
                switch (status) {
                    case "APPROVED":
                    case "CONFIRMED":
                    case "SUCCESS":
                         badge.setBadgeColor(new Color(6, 78, 59), new Color(52, 211, 153)); // Emerald 900 bg, 400 text
                         break;
                    case "PENDING":
                         badge.setBadgeColor(new Color(120, 53, 15), new Color(252, 211, 77)); // Amber 900 bg, 300 text
                         break;
                    case "REJECTED":
                    case "CANCELLED":
                         badge.setBadgeColor(new Color(127, 29, 29), new Color(248, 113, 113)); // Red 900 bg, 400 text
                         break;
                    default:
                         badge.setBadgeColor(new Color(63, 63, 70), Color.LIGHT_GRAY);
                }
            }
            return badge;
        }
    }
    
    static class BadgePanel extends javax.swing.JPanel {
        private String text;
        private Color badgeBg;
        private Color badgeFg;
        private Color cellBg;

        public BadgePanel() {
            setOpaque(true); // Panel is opaque to draw cell background
        }

        public void setText(String text) { this.text = text; }
        public void setBadgeColor(Color bg, Color fg) { this.badgeBg = bg; this.badgeFg = fg; }
        public void setCellBackground(Color bg) { this.cellBg = bg; setBackground(bg); }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw Cell Background (handled by super if setBackground is correct, but let's ensure)
            g2.setColor(cellBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 2. Draw Badge
            if (text != null && !text.isEmpty()) {
                int badgeW = 100;
                int badgeH = 26;
                int x = (getWidth() - badgeW) / 2; // Center
                int y = (getHeight() - badgeH) / 2;

                g2.setColor(badgeBg);
                g2.fillRoundRect(x, y, badgeW, badgeH, 12, 12); // Rounded rect

                g2.setColor(badgeFg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                
                // Center text
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int textX = x + (badgeW - fm.stringWidth(text)) / 2;
                int textY = y + (badgeH - fm.getHeight()) / 2 + fm.getAscent();
                
                g2.drawString(text, textX, textY);
            }
        }
    }
}
