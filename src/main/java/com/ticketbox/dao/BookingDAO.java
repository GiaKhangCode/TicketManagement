package com.ticketbox.dao;

import com.ticketbox.model.Booking;
import com.ticketbox.model.Ticket;
import com.ticketbox.util.DatabaseConnection;
import java.sql.*;
import java.util.List;

public class BookingDAO {

    public boolean createBooking(Booking booking, List<Ticket> tickets) {
        Connection conn = null;
        PreparedStatement pstmtBooking = null;
        PreparedStatement pstmtTicket = null;
        PreparedStatement pstmtUpdateQty = null;
        ResultSet rs = null;

        String insertBookingSQL = "INSERT INTO BOOKINGS (USER_ID, TOTAL_AMOUNT, STATUS, SCHEDULE_ID) VALUES (?, ?, ?, ?)";
        // Oracle 12c+ supports IDENTITY, use RETURNING INTO for ID if needed, 
        // or standard JDBC getGeneratedKeys
        
        String insertTicketSQL = "INSERT INTO TICKETS (BOOKING_ID, TICKET_TYPE_ID, QR_CODE, STATUS) VALUES (?, ?, ?, ?)";
        // We still increment SOLD on TicketType for global stats, but availability check is per-schedule (handled elsewhere)
        String updateQtySQL = "UPDATE TICKET_TYPES SET SOLD = SOLD + 1 WHERE ID = ?";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Booking and get generated ID
            pstmtBooking = conn.prepareStatement(insertBookingSQL, new String[]{"ID"});
            pstmtBooking.setInt(1, booking.getUserId());
            pstmtBooking.setDouble(2, booking.getTotalAmount());
            pstmtBooking.setString(3, booking.getStatus());
            pstmtBooking.setInt(4, booking.getScheduleId());
            
            int affectedRows = pstmtBooking.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating booking failed, no rows affected.");
            }

            int bookingId = 0;
            try (ResultSet generatedKeys = pstmtBooking.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bookingId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating booking failed, no ID obtained.");
                }
            }

            // 2. Insert Tickets and Update Quantity
            pstmtTicket = conn.prepareStatement(insertTicketSQL);
            pstmtUpdateQty = conn.prepareStatement(updateQtySQL);

            for (Ticket ticket : tickets) {
                // Insert Ticket
                pstmtTicket.setInt(1, bookingId);
                pstmtTicket.setInt(2, ticket.getTicketTypeId());
                pstmtTicket.setString(3, ticket.getQrCode());
                pstmtTicket.setString(4, ticket.getStatus());
                pstmtTicket.addBatch();

                // Update Ticket Type Sold Count
                pstmtUpdateQty.setInt(1, ticket.getTicketTypeId());
                pstmtUpdateQty.addBatch();
            }

            pstmtTicket.executeBatch();
            int[] updateCounts = pstmtUpdateQty.executeBatch();
            
            for (int count : updateCounts) {
                if (count == 0) {
                     System.err.println("WARNING: Ticket Type Quantity/Sold update failed (0 rows affected)."); 
                }
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtBooking != null) pstmtBooking.close();
                if (pstmtTicket != null) pstmtTicket.close();
                if (pstmtUpdateQty != null) pstmtUpdateQty.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "SELECT * FROM BOOKINGS WHERE USER_ID = ? ORDER BY BOOKING_DATE DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking b = new Booking();
                b.setId(rs.getInt("ID"));
                b.setUserId(rs.getInt("USER_ID"));
                b.setBookingDate(rs.getTimestamp("BOOKING_DATE"));
                b.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
                b.setStatus(rs.getString("STATUS"));
                b.setScheduleId(rs.getInt("SCHEDULE_ID"));
                bookings.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
    
    public List<Ticket> getTicketsByBooking(int bookingId) {
        List<Ticket> tickets = new java.util.ArrayList<>();
        String sql = "SELECT * FROM TICKETS WHERE BOOKING_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Ticket t = new Ticket();
                t.setId(rs.getInt("ID"));
                t.setBookingId(rs.getInt("BOOKING_ID"));
                t.setTicketTypeId(rs.getInt("TICKET_TYPE_ID"));
                t.setQrCode(rs.getString("QR_CODE"));
                t.setStatus(rs.getString("STATUS"));
                tickets.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }
}
