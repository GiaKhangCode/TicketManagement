package com.ticketbox.view;

import com.ticketbox.model.Ticket;
import com.ticketbox.util.QRCodeUtil;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TicketView extends JPanel {
    private Ticket ticket;

    public TicketView(Ticket ticket) {
        this.ticket = ticket;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        setPreferredSize(new Dimension(600, 220));
        
        // Left Side: Event Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(20, 25, 20, 20));
        
        JLabel lblEventName = new JLabel("<html><body style='width: 300px'>" + ticket.getEventName() + "</body></html>");
        lblEventName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblEventName.setForeground(ThemeColor.TEXT_PRIMARY);
        
        JLabel lblTime = new JLabel("🕒 " + ticket.getEventDate());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTime.setForeground(ThemeColor.TEXT_SECONDARY);
        
        JLabel lblLocation = new JLabel("📍 " + ticket.getLocation());
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLocation.setForeground(ThemeColor.TEXT_SECONDARY);
        
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typePanel.setBackground(Color.WHITE);
        JLabel lblType = new JLabel(ticket.getTicketTypeName());
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblType.setForeground(ThemeColor.PRIMARY);
        lblType.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColor.PRIMARY, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        typePanel.add(lblType);

        infoPanel.add(lblEventName);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblTime);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblLocation);
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(typePanel);
        
        add(infoPanel, BorderLayout.CENTER);
        
        // Right Side: QR Code
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBackground(new Color(248, 250, 252));
        qrPanel.setPreferredSize(new Dimension(200, 220));
        qrPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(226, 232, 240))); // Dashed line simulation?
        
        JLabel lblQR = new JLabel();
        lblQR.setHorizontalAlignment(SwingConstants.CENTER);
        
        try {
            BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(ticket.getQrCode());
            lblQR.setIcon(new ImageIcon(qrImage));
        } catch (Exception e) {
            lblQR.setText("QR Error");
        }
        
        JLabel lblCode = new JLabel(ticket.getQrCode(), SwingConstants.CENTER);
        lblCode.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblCode.setForeground(ThemeColor.TEXT_SECONDARY);
        lblCode.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        qrPanel.add(lblQR, BorderLayout.CENTER);
        qrPanel.add(lblCode, BorderLayout.SOUTH);
        
        add(qrPanel, BorderLayout.EAST);
    }
}
