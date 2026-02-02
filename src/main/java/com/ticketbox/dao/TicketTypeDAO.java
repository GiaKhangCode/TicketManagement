package com.ticketbox.dao;

import com.ticketbox.model.TicketType;
import com.ticketbox.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketTypeDAO {
    
    public boolean addTicketType(TicketType type) {
        String sql = "INSERT INTO TICKET_TYPES (EVENT_ID, NAME, PRICE, QUANTITY, SOLD) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, type.getEventId());
            pstmt.setString(2, type.getName());
            pstmt.setDouble(3, type.getPrice());
            pstmt.setInt(4, type.getQuantity());
            pstmt.setInt(5, 0); // Sold init to 0
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TicketType> getTicketTypesByEvent(int eventId) {
        List<TicketType> types = new ArrayList<>();
        String sql = "SELECT * FROM TICKET_TYPES WHERE EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TicketType t = new TicketType();
                t.setId(rs.getInt("ID"));
                t.setEventId(rs.getInt("EVENT_ID"));
                t.setName(rs.getString("NAME"));
                t.setPrice(rs.getDouble("PRICE"));
                t.setQuantity(rs.getInt("QUANTITY"));
                t.setSold(rs.getInt("SOLD"));
                types.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    public boolean deleteTicketType(int id) {
        String sql = "DELETE FROM TICKET_TYPES WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getSoldQuantityForSchedule(int ticketTypeId, int scheduleId) {
        String sql = "SELECT COUNT(*) FROM TICKETS t " +
                     "JOIN BOOKINGS b ON t.BOOKING_ID = b.ID " +
                     "WHERE t.TICKET_TYPE_ID = ? AND b.SCHEDULE_ID = ? AND b.STATUS = 'SUCCESS'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ticketTypeId);
            pstmt.setInt(2, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public List<TicketType> getTicketTypesByEventAndSchedule(int eventId, int scheduleId) {
        List<TicketType> types = new ArrayList<>();
        // Get all types for event
        String sql = "SELECT * FROM TICKET_TYPES WHERE EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TicketType t = new TicketType();
                t.setId(rs.getInt("ID"));
                t.setEventId(rs.getInt("EVENT_ID"));
                t.setName(rs.getString("NAME"));
                t.setPrice(rs.getDouble("PRICE"));
                t.setQuantity(rs.getInt("QUANTITY"));
                
                // Override 'sold' with specific schedule count
                t.setSold(getSoldQuantityForSchedule(t.getId(), scheduleId));
                
                types.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }
}
