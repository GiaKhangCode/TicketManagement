package com.ticketbox.view;

import com.ticketbox.dao.TicketTypeDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.TicketType;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageTicketTypesDialog extends JDialog {
    private Event event;
    private TicketTypeDAO ticketTypeDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JTextField txtPrice;
    private JSpinner spinnerQty;

    public ManageTicketTypesDialog(Window owner, Event event) {
        super(owner, "Quản lý loại vé: " + event.getName(), ModalityType.APPLICATION_MODAL);
        this.event = event;
        this.ticketTypeDAO = new TicketTypeDAO();
        initComponents();
        loadData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thêm loại vé mới"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Tên vé (vd: VIP):"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(txtName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("Giá vé (VNĐ):"), gbc);
        txtPrice = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(txtPrice, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("Số lượng:"), gbc);
        spinnerQty = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
        gbc.gridx = 1; gbc.gridy = 2; inputPanel.add(spinnerQty, gbc);
        
        JButton btnAdd = new JButton("Thêm");
        btnAdd.addActionListener(e -> addTicketType());
        gbc.gridx = 1; gbc.gridy = 3; inputPanel.add(btnAdd, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Table Panel
        String[] columns = {"ID", "Tên loại vé", "Giá", "Tổng số lượng", "Đã bán"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelete = new JButton("Xóa loại vé");
        JButton btnClose = new JButton("Đóng");
        
        btnDelete.addActionListener(e -> deleteTicketType());
        btnClose.addActionListener(e -> dispose());
        
        btnPanel.add(btnDelete);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        List<TicketType> types = ticketTypeDAO.getTicketTypesByEvent(event.getId());
        for (TicketType t : types) {
            tableModel.addRow(new Object[]{t.getId(), t.getName(), t.getPrice(), t.getQuantity(), t.getSold()});
        }
    }
    
    private void addTicketType() {
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        int qty = (Integer) spinnerQty.getValue();
        
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            TicketType type = new TicketType(event.getId(), name, price, qty);
            
            if (ticketTypeDAO.addTicketType(type)) {
                JOptionPane.showMessageDialog(this, "Thêm loại vé thành công!");
                txtName.setText("");
                txtPrice.setText("");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm loại vé.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá vé phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTicketType() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại vé cần xóa!");
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (ticketTypeDAO.deleteTicketType(id)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
            } else {
                 JOptionPane.showMessageDialog(this, "Không thể xóa (có thể vé đã được bán).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
