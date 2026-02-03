package com.ticketbox.dao;

import com.ticketbox.model.Ticket;

import com.ticketbox.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    public boolean updateResaleStatus(int ticketId, boolean isResale, double resalePrice) {
        String sql = "UPDATE TICKETS SET IS_RESALE = ?, RESALE_PRICE = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, isResale ? 1 : 0);
            pstmt.setDouble(2, resalePrice);
            pstmt.setInt(3, ticketId);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Ticket> getResaleTickets(String searchQuery) {
        List<Ticket> tickets = new ArrayList<>();
        // Query to join Tickets -> TicketType -> Event -> Schedule -> Venue (if applicable)
        // Simplified based on current Event/Schedule structure
        String sql = "SELECT t.ID, t.QR_CODE, t.RESALE_PRICE, t.TICKET_TYPE_ID, " +
                     "tt.NAME as TYPE_NAME, tt.PRICE as ORIGINAL_PRICE, " +
                     "e.NAME as EVENT_NAME, es.START_TIME as EVENT_DATE, e.LOCATION " +
                     "FROM TICKETS t " +
                     "JOIN TICKET_TYPES tt ON t.TICKET_TYPE_ID = tt.ID " +
                     "JOIN EVENTS e ON tt.EVENT_ID = e.ID " +
                     "JOIN BOOKINGS b ON t.BOOKING_ID = b.ID " +
                     "JOIN EVENT_SCHEDULES es ON b.SCHEDULE_ID = es.ID " +
                     "WHERE t.IS_RESALE = 1 AND t.STATUS = 'VALID'";
        
        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql += " AND LOWER(e.NAME) LIKE ?";
        }
        
        sql += " ORDER BY es.START_TIME ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (searchQuery != null && !searchQuery.isEmpty()) {
                pstmt.setString(1, "%" + searchQuery.toLowerCase() + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Ticket t = new Ticket();
                t.setId(rs.getInt("ID"));
                t.setQrCode(rs.getString("QR_CODE"));
                t.setResalePrice(rs.getDouble("RESALE_PRICE"));
                t.setIsResale(true);
                t.setTicketTypeId(rs.getInt("TICKET_TYPE_ID"));
                t.setStatus("VALID");
                
                t.setTicketTypeName(rs.getString("TYPE_NAME"));
                t.setPrice(rs.getDouble("ORIGINAL_PRICE"));
                t.setEventName(rs.getString("EVENT_NAME"));
                t.setLocation(rs.getString("LOCATION"));
                
                Timestamp ts = rs.getTimestamp("EVENT_DATE");
                if (ts != null) {
                    t.setEventDate(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts));
                }
                
                tickets.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public Ticket getTicketById(int ticketId) {
        String sql = "SELECT * FROM TICKETS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Ticket t = new Ticket();
                t.setId(rs.getInt("ID"));
                t.setBookingId(rs.getInt("BOOKING_ID"));
                t.setTicketTypeId(rs.getInt("TICKET_TYPE_ID"));
                t.setQrCode(rs.getString("QR_CODE"));
                t.setStatus(rs.getString("STATUS"));
                t.setIsResale(rs.getInt("IS_RESALE") == 1);
                t.setResalePrice(rs.getDouble("RESALE_PRICE"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
