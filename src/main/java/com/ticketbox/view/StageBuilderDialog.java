package com.ticketbox.view;

import com.ticketbox.dao.SeatMapDAO;
import com.ticketbox.dao.TicketTypeDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.SeatMap;
import com.ticketbox.model.SeatZone;
import com.ticketbox.model.TicketType;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.StageCanvas;
import com.ticketbox.view.component.RoundedButton; // Using custom button
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StageBuilderDialog extends JDialog {
    private Event event;
    private SeatMapDAO seatMapDAO;
    private TicketTypeDAO ticketTypeDAO;
    private StageCanvas canvas;
    private SeatMap currentMap;
    
    // Properties Panel Components
    private JTextField txtLabel;
    private JComboBox<TicketType> cboTicketType;
    private JPanel colorPreview;
    private String selectedColorHex = "#3B82F6";

    public StageBuilderDialog(Window owner, Event event) {
        super(owner, "Thiết kế Sơ đồ Sân khấu: " + event.getName(), ModalityType.APPLICATION_MODAL);
        this.event = event;
        this.seatMapDAO = new SeatMapDAO();
        this.ticketTypeDAO = new TicketTypeDAO();
        
        initComponents();
        loadMap();
    }
    
    private void initComponents() {
        setSize(1200, 800);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN); // Set background
        
        // --- Toolbar (Top) ---
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(ThemeColor.BG_CARD);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        ButtonGroup toolGroup = new ButtonGroup();
        
        JToggleButton btnSelect = createToolButton("Chọn", true);
        btnSelect.addActionListener(e -> canvas.setTool(StageCanvas.ToolType.SELECT));
        
        JToggleButton btnRect = createToolButton("Hình Vuông", false);
        btnRect.addActionListener(e -> canvas.setTool(StageCanvas.ToolType.DRAW_RECT));
        
        JToggleButton btnOval = createToolButton("Hình Tròn", false);
        btnOval.addActionListener(e -> canvas.setTool(StageCanvas.ToolType.DRAW_OVAL));
        
        JToggleButton btnSector = createToolButton("Hình Quạt", false);
        btnSector.addActionListener(e -> canvas.setTool(StageCanvas.ToolType.DRAW_SECTOR));
        
        JToggleButton btnPoly = createToolButton("Đa Giác", false);
        btnPoly.addActionListener(e -> {
            canvas.setTool(StageCanvas.ToolType.DRAW_POLY);
            JOptionPane.showMessageDialog(this, "Click chuột trái để chấm điểm, Click đúp để hoàn thành hình.", "Hướng dẫn vẽ Đa giác", JOptionPane.INFORMATION_MESSAGE);
        });
        
        toolGroup.add(btnSelect);
        toolGroup.add(btnRect);
        toolGroup.add(btnOval);
        toolGroup.add(btnSector); // Add
        toolGroup.add(btnPoly);
        
        toolbar.add(btnSelect);
        toolbar.add(btnRect);
        toolbar.add(btnOval);
        toolbar.add(btnSector); // Add
        toolbar.add(btnPoly);
        toolbar.addSeparator();
        
        RoundedButton btnDeleteZone = new RoundedButton("Xóa Khu vực", 10);
        btnDeleteZone.setBackground(new Color(185, 28, 28)); // Red
        btnDeleteZone.setForeground(Color.WHITE);
        btnDeleteZone.addActionListener(e -> canvas.deleteSelectedZones());
        
        toolbar.add(btnDeleteZone);
        toolbar.add(Box.createHorizontalGlue());
        
        RoundedButton btnSave = new RoundedButton("Lưu Sơ đồ", 10);
        btnSave.setBackground(new Color(21, 128, 61)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveMap());
        
        toolbar.add(btnSave);
        
        add(toolbar, BorderLayout.NORTH);
        
        // --- Canvas (Center) ---
        canvas = new StageCanvas();
        canvas.setOnSelectionChanged(this::updatePropertiesPanel);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        
        // --- Properties Panel (Right) ---
        JPanel propsPanel = new JPanel();
        propsPanel.setLayout(new GridBagLayout());
        propsPanel.setBackground(ThemeColor.BG_CARD);
        propsPanel.setPreferredSize(new Dimension(300, 0));
        propsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // Header
        JLabel lblProps = new JLabel("Thuộc tính Khu vực");
        lblProps.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblProps.setForeground(ThemeColor.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        propsPanel.add(lblProps, gbc);
        
        // Label Input
        gbc.gridy = 1; gbc.gridwidth = 1;
        propsPanel.add(createLabel("Tên hiển thị:"), gbc);
        
        txtLabel = new JTextField();
        txtLabel.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if (canvas.getSelectedZone() != null) {
                canvas.getSelectedZone().setLabel(txtLabel.getText());
                canvas.repaint();
            }
        }));
        gbc.gridy = 2;
        propsPanel.add(txtLabel, gbc);
        
        // Ticket Type
        gbc.gridy = 3;
        propsPanel.add(createLabel("Loại vé áp dụng:"), gbc);
        
        cboTicketType = new JComboBox<>();
        loadTicketTypes();
        cboTicketType.addActionListener(e -> {
            if (canvas.getSelectedZone() != null && cboTicketType.getSelectedItem() != null) {
                TicketType tt = (TicketType) cboTicketType.getSelectedItem();
                canvas.getSelectedZone().setTicketTypeId(tt.getId());
            }
        });
        gbc.gridy = 4;
        propsPanel.add(cboTicketType, gbc);
        
        // Color Picker
        gbc.gridy = 5;
        propsPanel.add(createLabel("Màu sắc:"), gbc);
        
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorRow.setOpaque(false);
        
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(30, 30));
        colorPreview.setBackground(Color.decode(selectedColorHex));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        RoundedButton btnPickColor = new RoundedButton("Chọn màu...", 5);
        btnPickColor.setPreferredSize(new Dimension(100, 30));
        btnPickColor.setBackground(ThemeColor.SECONDARY);
        btnPickColor.setForeground(Color.WHITE);
        btnPickColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Chọn màu khu vực", Color.decode(selectedColorHex));
            if (c != null) {
                selectedColorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                colorPreview.setBackground(c);
                if (canvas.getSelectedZone() != null) {
                    canvas.getSelectedZone().setColorHex(selectedColorHex);
                    canvas.repaint();
                }
            }
        });
        
        colorRow.add(colorPreview);
        colorRow.add(Box.createHorizontalStrut(10));
        colorRow.add(btnPickColor);
        
        gbc.gridy = 6;
        propsPanel.add(colorRow, gbc);
        
        // Info text
        gbc.gridy = 7; 
        gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.NORTH;
        JTextArea txtInfo = new JTextArea("Hướng dẫn:\n- Kéo thả để di chuyển\n- Kéo góc dưới phải để thay đổi kích thước\n- Nhập tên và chọn loại vé để lưu thông tin.");
        txtInfo.setWrapStyleWord(true);
        txtInfo.setLineWrap(true);
        txtInfo.setOpaque(false);
        txtInfo.setForeground(ThemeColor.TEXT_SECONDARY);
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        propsPanel.add(txtInfo, gbc);

        add(propsPanel, BorderLayout.EAST);
    }
    
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(ThemeColor.TEXT_SECONDARY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }
    
    // --- Logic ---
    
    private void loadTicketTypes() {
        cboTicketType.removeAllItems();
        // Add a dummy "None" type
        TicketType none = new TicketType();
        none.setId(0);
        none.setName("-- Chưa chọn --");
        cboTicketType.addItem(none);
        
        List<TicketType> types = ticketTypeDAO.getTicketTypesByEvent(event.getId());
        for (TicketType t : types) {
            cboTicketType.addItem(t);
        }
        
        // Custom Renderer to show Name + Price
        cboTicketType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TicketType) {
                    TicketType t = (TicketType) value;
                    if (t.getId() == 0) setText(t.getName());
                    else setText(t.getName() + " (" + String.format("%,.0f", t.getPrice()) + " VNĐ)");
                }
                return this;
            }
        });
    }
    
    private void loadMap() {
        currentMap = seatMapDAO.getSeatMap(event.getId());
        if (currentMap == null) {
            currentMap = new SeatMap(event.getId());
        }
        canvas.setZones(currentMap.getZones());
    }
    
    private void saveMap() {
        currentMap.setZones(canvas.getZones());
        currentMap.setEventId(event.getId());
        
        if (seatMapDAO.saveSeatMap(currentMap)) {
            JOptionPane.showMessageDialog(this, "Lưu sơ đồ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu sơ đồ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePropertiesPanel() {
        SeatZone z = canvas.getSelectedZone();
        if (z == null) {
            txtLabel.setEnabled(false);
            txtLabel.setText("");
            cboTicketType.setEnabled(false);
            cboTicketType.setSelectedIndex(0);
            return;
        }
        
        txtLabel.setEnabled(true);
        cboTicketType.setEnabled(true);
        
        txtLabel.setText(z.getLabel());
        selectedColorHex = z.getColorHex();
        colorPreview.setBackground(Color.decode(selectedColorHex));
        
        // Select correct ticket type
        for (int i = 0; i < cboTicketType.getItemCount(); i++) {
            TicketType t = cboTicketType.getItemAt(i);
            if (t.getId() == z.getTicketTypeId()) {
                cboTicketType.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private JToggleButton createToolButton(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text);
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setFocusPainted(false);
        btn.setSelected(selected);
        return btn;
    }
    
    // Callbacks Interface
    // Helper class to simplify DocumentListener
    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        public SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
