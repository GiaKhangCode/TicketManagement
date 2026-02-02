package com.ticketbox.view;

// Recompile trigger 2
import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.model.User;
import com.ticketbox.model.TicketType;

import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.*;

public class AddEventDialog extends JDialog {
    private User organizer;
    private EventDAO eventDAO;
    private Event eventToEdit;
    private boolean eventAdded = false;

    private JTextField txtName;
    private JTextArea txtDescription;
    private JTextField txtLocation;
    private JComboBox<String> cboCategory; // Added Category ComboBox
    private JTextField txtImageUrl;
    
    // Schedules
    private DefaultListModel<String> listModelSchedules;
    private JList<String> listSchedules;
    private java.util.List<com.ticketbox.model.EventSchedule> tempSchedules;

    // Ticket Types
    private DefaultListModel<String> listModelTicketTypes;
    private JList<String> listTicketTypes;
    private java.util.List<TicketType> tempTicketTypes;

    public AddEventDialog(Window owner, User organizer, Event eventToEdit) {
        super(owner, eventToEdit == null ? "Thêm Sự Kiện Mới" : "Cập nhật Sự Kiện", ModalityType.APPLICATION_MODAL);
        this.organizer = organizer;
        this.eventToEdit = eventToEdit;
        this.eventDAO = new EventDAO();
        this.tempSchedules = new java.util.ArrayList<>();
        this.listModelSchedules = new DefaultListModel<>();
        this.tempTicketTypes = new java.util.ArrayList<>();
        this.listModelTicketTypes = new DefaultListModel<>();
        
        initComponents();
        if (eventToEdit != null) {
            loadEventData();
        }
        // pack(); Removed to respect setSize
        setLocationRelativeTo(owner);
    }

    public AddEventDialog(Window owner, User organizer) {
        this(owner, organizer, null);
    }
    
    private void loadEventData() {
        txtName.setText(eventToEdit.getName());
        txtDescription.setText(eventToEdit.getDescription());
        txtLocation.setText(eventToEdit.getLocation());
        
        // Robust Category Selection
        String currentCat = eventToEdit.getCategory();
        if (currentCat != null) {
            currentCat = currentCat.trim();
            for (int i = 0; i < cboCategory.getItemCount(); i++) {
                if (cboCategory.getItemAt(i).equalsIgnoreCase(currentCat)) {
                    cboCategory.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        txtImageUrl.setText(eventToEdit.getImageUrl());
        
        if (eventToEdit.getSchedules() != null) {
            for (com.ticketbox.model.EventSchedule s : eventToEdit.getSchedules()) {
                tempSchedules.add(s);
                listModelSchedules.addElement(formatSchedule(s));
            }
        }
    }
    
    private String formatSchedule(com.ticketbox.model.EventSchedule s) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(s.getStartTime()) + " - " + sdf.format(s.getEndTime());
    }

    public boolean isEventAdded() {
        return eventAdded;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Increased padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelForm.add(new JLabel("Tên sự kiện:"), gbc);
        
        txtName = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelForm.add(txtName, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelForm.add(new JLabel("Mô tả:"), gbc);
        
        txtDescription = new JTextArea(4, 20); // Increased rows
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.2; // Give some vertical space
        gbc.fill = GridBagConstraints.BOTH; // Fill for text area
        panelForm.add(scrollDesc, gbc);
        
        // Reset weighty/fill for subsequent single-line fields
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Location
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelForm.add(new JLabel("Địa điểm:"), gbc);
        
        txtLocation = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelForm.add(txtLocation, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelForm.add(new JLabel("Thể loại:"), gbc);
        
        String[] categories = {"Nhạc sống", "Sân khấu & Nghệ thuật", "Hội thảo & Workshop", "Tham quan & Trải nghiệm", "Thể loại khác"};
        cboCategory = new JComboBox<>(categories);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelForm.add(cboCategory, gbc);
        
        // Image URL
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelForm.add(new JLabel("URL Hình ảnh:"), gbc);
        
        txtImageUrl = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        panelForm.add(txtImageUrl, gbc);
        
        // Schedules Section
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Lịch diễn:"), gbc);
        
        JPanel panelSchedules = new JPanel(new BorderLayout());
        listSchedules = new JList<>(listModelSchedules);
        panelSchedules.add(new JScrollPane(listSchedules), BorderLayout.CENTER);
        panelSchedules.setPreferredSize(new Dimension(300, 120)); // Fixed height
        
        JPanel panelSchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddSch = new JButton("Thêm lịch");
        JButton btnDelSch = new JButton("Xóa");
        
        btnAddSch.addActionListener(e -> addSchedule());
        btnDelSch.addActionListener(e -> removeSchedule());
        
        panelSchButtons.add(btnAddSch);
        panelSchButtons.add(btnDelSch);
        panelSchedules.add(panelSchButtons, BorderLayout.SOUTH);
        
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 0.3; // Give vertical space to list
        gbc.fill = GridBagConstraints.BOTH;
        panelForm.add(panelSchedules, gbc);

        // Ticket Types Section
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Loại vé:"), gbc);
        
        JPanel panelTicketTypes = new JPanel(new BorderLayout());
        listTicketTypes = new JList<>(listModelTicketTypes);
        panelTicketTypes.add(new JScrollPane(listTicketTypes), BorderLayout.CENTER);
        panelTicketTypes.setPreferredSize(new Dimension(300, 100));
        
        JPanel panelTTButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddTT = new JButton("Thêm loại vé");
        JButton btnDelTT = new JButton("Xóa");
        
        btnAddTT.addActionListener(e -> addTicketType());
        btnDelTT.addActionListener(e -> removeTicketType());
        
        panelTTButtons.add(btnAddTT);
        panelTTButtons.add(btnDelTT);
        panelTicketTypes.add(panelTTButtons, BorderLayout.SOUTH);
        
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0; gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        panelForm.add(panelTicketTypes, gbc);

        // Wrap form in ScrollPane
        JScrollPane scrollPane = new JScrollPane(panelForm);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnCancel.setPreferredSize(new Dimension(100, 35));
        
        btnSave.addActionListener(e -> saveEvent());
        btnCancel.addActionListener(e -> dispose());
        
        panelButtons.add(btnSave);
        panelButtons.add(btnCancel);
        add(panelButtons, BorderLayout.SOUTH);
        
        // Set Default Size (Compact)
        setSize(new Dimension(550, 500));
        setLocationRelativeTo(getOwner());
    }
    
    private void addSchedule() {
        JDialog dlg = new JDialog(this, "Chọn thời gian", ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new FlowLayout());
        
        JSpinner spinStart = new JSpinner(new SpinnerDateModel());
        spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy HH:mm"));
        spinStart.setValue(new Date());
        
        JSpinner spinEnd = new JSpinner(new SpinnerDateModel());
        spinEnd.setEditor(new JSpinner.DateEditor(spinEnd, "dd/MM/yyyy HH:mm"));
        spinEnd.setValue(new Date(System.currentTimeMillis() + 3600000));
        
        dlg.add(new JLabel("Từ:"));
        dlg.add(spinStart);
        dlg.add(new JLabel("Đến:"));
        dlg.add(spinEnd);
        
        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            Date start = (Date) spinStart.getValue();
            Date end = (Date) spinEnd.getValue();
            if (!end.after(start)) {
                JOptionPane.showMessageDialog(dlg, "Thời gian kết thúc phải sau bắt đầu!");
                return;
            }
            com.ticketbox.model.EventSchedule s = new com.ticketbox.model.EventSchedule();
            s.setStartTime(new Timestamp(start.getTime()));
            s.setEndTime(new Timestamp(end.getTime()));
            tempSchedules.add(s);
            listModelSchedules.addElement(formatSchedule(s));
            dlg.dispose();
        });
        
        dlg.add(btnOk);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    
    private void removeSchedule() {
        int idx = listSchedules.getSelectedIndex();
        if (idx != -1) {
            tempSchedules.remove(idx);
            listModelSchedules.remove(idx);
        }
    }

    private void addTicketType() {
        JDialog dlg = new JDialog(this, "Thêm loại vé", ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtTTName = new JTextField(15);
        JTextField txtTTPrice = new JTextField(10);
        JSpinner spinTTQty = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
        
        gbc.gridx = 0; gbc.gridy = 0; dlg.add(new JLabel("Tên vé (VIP, Thường...):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; dlg.add(txtTTName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; dlg.add(new JLabel("Giá vé (VNĐ):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; dlg.add(txtTTPrice, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; dlg.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; dlg.add(spinTTQty, gbc);
        
        JButton btnOk = new JButton("Thêm");
        btnOk.addActionListener(e -> {
            String name = txtTTName.getText().trim();
            String priceStr = txtTTPrice.getText().trim();
            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đủ thông tin!");
                return;
            }
            try {
                double price = Double.parseDouble(priceStr);
                int qty = (Integer) spinTTQty.getValue();
                
                TicketType tt = new TicketType(0, name, price, qty); // Event ID set later
                tempTicketTypes.add(tt);
                listModelTicketTypes.addElement(name + " - " + java.text.NumberFormat.getInstance().format(price) + " VNĐ - SL: " + qty);
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá phải là số!");
            }
        });
        
        gbc.gridx = 1; gbc.gridy = 3; dlg.add(btnOk, gbc);
        
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void removeTicketType() {
        int idx = listTicketTypes.getSelectedIndex();
        if (idx != -1) {
            tempTicketTypes.remove(idx);
            listModelTicketTypes.remove(idx);
        }
    }

    private void saveEvent() {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        String location = txtLocation.getText().trim();
        String imageUrl = txtImageUrl.getText().trim();
        String category = (String) cboCategory.getSelectedItem();

        // Validation
        if (name.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên và địa điểm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (tempSchedules.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất 1 lịch diễn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate min start and max end
        tempSchedules.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
        Timestamp minStart = tempSchedules.get(0).getStartTime();
        Timestamp maxEnd = tempSchedules.get(tempSchedules.size() - 1).getEndTime();

        // Logic for Create or Update
        if (eventToEdit == null) {
            Event event = new Event();
            event.setName(name);
            event.setDescription(description);
            event.setLocation(location);
            event.setCategory(category); // Set Category
            event.setStartTime(minStart);
            event.setEndTime(maxEnd);
            event.setOrganizerId(organizer.getId());
            event.setStatus("PENDING"); 
            event.setImageUrl(imageUrl);
            event.setSchedules(tempSchedules);
            event.setTicketTypes(tempTicketTypes);
            
             if (eventDAO.addEvent(event)) {
                JOptionPane.showMessageDialog(this, "Thêm sự kiện thành công! Chờ Admin duyệt.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                eventAdded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu sự kiện.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Update logic
            eventToEdit.setName(name);
            eventToEdit.setDescription(description);
            eventToEdit.setLocation(location);
            eventToEdit.setCategory(category); // Update Category
            eventToEdit.setStartTime(minStart);
            eventToEdit.setEndTime(maxEnd);
            eventToEdit.setImageUrl(imageUrl);
            eventToEdit.setSchedules(tempSchedules);
            
            if (eventDAO.updateEvent(eventToEdit)) {
                 JOptionPane.showMessageDialog(this, "Cập nhật sự kiện thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                eventAdded = true;
                dispose();
            } else {
                 JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật sự kiện.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
