package com.ticketbox.view;

import com.ticketbox.dao.TicketDAO;
import com.ticketbox.model.Ticket;
import com.ticketbox.util.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResellDialog extends JDialog {
    private Ticket ticket;
    private TicketDAO ticketDAO;
    private boolean isUpdated = false;
    private JTextField txtPrice;

    public ResellDialog(Window owner, Ticket ticket) {
        super(owner, "Bán lại vé", ModalityType.APPLICATION_MODAL);
        this.ticket = ticket;
        this.ticketDAO = new TicketDAO();
        initComponents();
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    private void initComponents() {
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeColor.BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Đăng bán vé");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeColor.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblInfo = new JLabel("Mã vé: " + ticket.getQrCode());
        lblInfo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lblInfo.setForeground(ThemeColor.TEXT_SECONDARY);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblInfo);
        panel.add(Box.createVerticalStrut(30));

        // Price Input
        JLabel lblPrice = new JLabel("Giá bán lại (VNĐ):");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(ThemeColor.TEXT_PRIMARY);
        lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtPrice = new JTextField();
        txtPrice.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        if (ticket.isIsResale()) {
            txtPrice.setText(String.format("%.0f", ticket.getResalePrice()));
            lblTitle.setText("Cập nhật giá bán");
        }

        panel.add(lblPrice);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtPrice);
        panel.add(Box.createVerticalStrut(30));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(ThemeColor.BG_MAIN);
        
        JButton btnSave = new JButton("Đăng bán");
        btnSave.setBackground(ThemeColor.PRIMARY);
        btnSave.setForeground(ThemeColor.TEXT_INVERSE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton btnCancelResale = new JButton("Hủy bán");
        btnCancelResale.setBackground(new Color(220, 38, 38)); // Red
        btnCancelResale.setForeground(Color.WHITE);
        btnCancelResale.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelResale.setVisible(ticket.isIsResale());

        btnSave.addActionListener(e -> saveResale());
        
        btnCancelResale.addActionListener(e -> {
            if (ticketDAO.updateResaleStatus(ticket.getId(), false, 0)) {
                JOptionPane.showMessageDialog(this, "Đã hủy bán lại vé.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                isUpdated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnPanel.add(btnSave);
        if (ticket.isIsResale()) {
            btnSave.setText("Cập nhật");
            btnPanel.add(btnCancelResale);
        }

        panel.add(btnPanel);
        
        add(panel, BorderLayout.CENTER);
    }
    
    private void saveResale() {
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                 JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            
            if (ticketDAO.updateResaleStatus(ticket.getId(), true, price)) {
                JOptionPane.showMessageDialog(this, "Đăng bán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                isUpdated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }
}
