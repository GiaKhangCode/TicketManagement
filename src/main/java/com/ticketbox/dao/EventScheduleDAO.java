package com.ticketbox.dao;

import com.ticketbox.model.EventSchedule;
import com.ticketbox.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventScheduleDAO {

    public boolean create(EventSchedule schedule) {
        String sql = "INSERT INTO EVENT_SCHEDULES (EVENT_ID, START_TIME, END_TIME) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            
            pstmt.setInt(1, schedule.getEventId());
            pstmt.setTimestamp(2, schedule.getStartTime());
            pstmt.setTimestamp(3, schedule.getEndTime());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        schedule.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<EventSchedule> getByEventId(int eventId) {
        List<EventSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM EVENT_SCHEDULES WHERE EVENT_ID = ? ORDER BY START_TIME ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EventSchedule s = new EventSchedule();
                    s.setId(rs.getInt("ID"));
                    s.setEventId(rs.getInt("EVENT_ID"));
                    s.setStartTime(rs.getTimestamp("START_TIME"));
                    s.setEndTime(rs.getTimestamp("END_TIME"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Delete all schedules for an event (used for update/cleanup)
    public boolean deleteByEventId(int eventId) {
        String sql = "DELETE FROM EVENT_SCHEDULES WHERE EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
