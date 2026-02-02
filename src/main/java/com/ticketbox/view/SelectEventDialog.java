package com.ticketbox.view;

import com.ticketbox.dao.EventDAO;
import com.ticketbox.model.Event;
import com.ticketbox.util.ThemeColor;
import com.ticketbox.view.component.TableStyler;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class SelectEventDialog extends JDialog {
    private EventDAO eventDAO;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private Event selectedEvent;
    private boolean confirmed = false;

    public SelectEventDialog(Window owner) {
        super(owner, "Chọn Sự kiện", ModalityType.APPLICATION_MODAL);
        this.eventDAO = new EventDAO();
        initComponents();
        loadData(""); // Load all initially
    }

    private void initComponents() {
        setSize(800, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColor.BG_MAIN);

        // Header: Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(ThemeColor.BG_MAIN);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtSearch = new JTextField(30);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc địa điểm...");
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(ThemeColor.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));

        topPanel.add(new JLabel("Tìm kiếm: "));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        
        add(topPanel, BorderLayout.NORTH);

        // Center: Table
        String[] cols = {"ID", "Tên Sự kiện", "Địa điểm", "Thời gian"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        TableStyler.applyStyle(table);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ThemeColor.BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        // Bottom: Action
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(ThemeColor.BG_MAIN);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnCancel = new JButton("Hủy");
        JButton btnSelect = new JButton("Chọn");
        btnSelect.setBackground(ThemeColor.PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setPreferredSize(new Dimension(100, 35));

        btnCancel.addActionListener(e -> dispose());
        btnSelect.addActionListener(e -> selectAndClose());
        
        // Double click to select
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectAndClose();
                }
            }
        });

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSelect);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData(String keyword) {
        tableModel.setRowCount(0);
        List<Event> events;
        if (keyword.isEmpty()) {
            events = eventDAO.getAllApprovedEvents();
        } else {
            events = eventDAO.searchApprovedEvents(keyword);
        }

        for (Event e : events) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(), e.getLocation(), e.getStartTime()
            });
        }
    }

    private void selectAndClose() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sự kiện!");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        // We could fetch the full object, but ID and Name are enough for now
        selectedEvent = new Event();
        selectedEvent.setId(id);
        selectedEvent.setName((String) tableModel.getValueAt(row, 1));
        
        confirmed = true;
        dispose();
    }

    public Event getSelectedEvent() {
        return confirmed ? selectedEvent : null;
    }
}
