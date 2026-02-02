package com.ticketbox.dao;

// Recompile trigger
import com.ticketbox.model.Event;
import com.ticketbox.model.EventSchedule;
import com.ticketbox.model.TicketType;
import com.ticketbox.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private EventScheduleDAO scheduleDAO = new EventScheduleDAO();
    private TicketTypeDAO ticketTypeDAO = new TicketTypeDAO();

    public boolean addEvent(Event event) {
        String sql = "INSERT INTO EVENTS (NAME, DESCRIPTION, LOCATION, START_TIME, END_TIME, ORGANIZER_ID, STATUS, IMAGE_URL, CATEGORY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ID"})) { // Modified to get generated keys
            
            pstmt.setString(1, event.getName());
            if (event.getDescription() != null) {
                pstmt.setString(2, event.getDescription());
            } else {
                pstmt.setNull(2, java.sql.Types.CLOB);
            }

            pstmt.setString(3, event.getLocation());
            pstmt.setTimestamp(4, event.getStartTime());
            pstmt.setTimestamp(5, event.getEndTime());
            pstmt.setInt(6, event.getOrganizerId());
            pstmt.setString(7, event.getStatus());
            pstmt.setString(8, event.getImageUrl());
            pstmt.setString(9, event.getCategory());
            
            int affectedRows = pstmt.executeUpdate(); // Changed return type
            if (affectedRows > 0) {
                 try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setId(generatedKeys.getInt(1));
                    }
                }
                 
                // Save schedules
                if (event.getSchedules() != null) { // Added null check for schedules
                    for (EventSchedule s : event.getSchedules()) {
                        s.setEventId(event.getId());
                        scheduleDAO.create(s);
                    }
                }
                
                // Save ticket types
                if (event.getTicketTypes() != null) {
                    for (TicketType t : event.getTicketTypes()) {
                        t.setEventId(event.getId());
                        ticketTypeDAO.addTicketType(t);
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false; // Added return for when no rows are affected
    }

    public List<Event> getEventsByOrganizer(int organizerId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS WHERE ORGANIZER_ID = ? ORDER BY START_TIME DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, organizerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs)); // Used helper method
            }
        } catch (SQLException e) {
            System.err.println("Load event error: " + e.getMessage());
            // Fail gracefully?
        }
        return events;
    }
    
    public List<Event> getAllApprovedEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS WHERE STATUS = 'APPROVED' ORDER BY START_TIME";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs)); // Used helper method
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
    public boolean updateEvent(Event event) {
        String sql = "UPDATE EVENTS SET NAME=?, DESCRIPTION=?, LOCATION=?, START_TIME=?, END_TIME=?, IMAGE_URL=?, STATUS=?, CATEGORY=? WHERE ID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, event.getName());
            if (event.getDescription() != null) {
                pstmt.setString(2, event.getDescription());
            } else {
                pstmt.setNull(2, java.sql.Types.CLOB);
            }
            pstmt.setString(3, event.getLocation());
            pstmt.setTimestamp(4, event.getStartTime());
            pstmt.setTimestamp(5, event.getEndTime());
            pstmt.setString(6, event.getImageUrl());
            // Reset status to PENDING on update or keep? Usually re-approval is needed.
            // Let's keep it simple: if updated, maybe status changes? 
            // For now, let's assume we update the status passed in, or maybe force PENDING?
            // The user didn't specify, but typically editing resets approval.
            // However, to be safe, I will just update what's passed. 
            // Check plan: "update existing event details".
            pstmt.setString(7, event.getStatus());
            pstmt.setString(8, event.getCategory());
            pstmt.setInt(9, event.getId());
            
            int affected = pstmt.executeUpdate(); // Changed return type
            if (affected > 0) {
                // Update schedules: Delete all old, insert new
                scheduleDAO.deleteByEventId(event.getId());
                if (event.getSchedules() != null) { // Added null check for schedules
                    for (EventSchedule s : event.getSchedules()) {
                        s.setEventId(event.getId());
                        scheduleDAO.create(s);
                    }
                }
                return true;
            }
            return false; // Added return for when no rows are affected
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM EVENTS WHERE ID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Delete schedules first (optional if cascade, but good practice)
            scheduleDAO.deleteByEventId(eventId);
            
            pstmt.setInt(1, eventId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Event> getPendingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS WHERE STATUS = 'PENDING' ORDER BY START_TIME";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs)); // Used helper method
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
    public List<Event> searchApprovedEvents(String keyword) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS WHERE STATUS = 'APPROVED' AND (LOWER(NAME) LIKE ? OR LOWER(LOCATION) LIKE ?) ORDER BY START_TIME";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs)); // Used helper method
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public List<Event> getEventsByCategory(String category) {
        List<Event> events = new ArrayList<>();
        // Handle "Khác" or "Thể loại khác" mapping if needed, but UI sends exact string.
        String sql = "SELECT * FROM EVENTS WHERE STATUS = 'APPROVED' AND CATEGORY = ? ORDER BY START_TIME";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
    
    // Helper method to map ResultSet to Event object and populate schedules
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("ID"));
        e.setName(rs.getString("NAME"));
        e.setDescription(rs.getString("DESCRIPTION")); // Clob handling might be needed if long
        e.setLocation(rs.getString("LOCATION"));
        e.setStartTime(rs.getTimestamp("START_TIME"));
        e.setEndTime(rs.getTimestamp("END_TIME"));
        e.setOrganizerId(rs.getInt("ORGANIZER_ID"));
        // The duplicate line below was removed as it's redundant
        // e.setOrganizerId(rs.getInt("ORGANIZER_ID")); 
        e.setStatus(rs.getString("STATUS")); 
        e.setImageUrl(rs.getString("IMAGE_URL"));
        e.setCategory(rs.getString("CATEGORY"));
        
        // Populate schedules
        e.setSchedules(scheduleDAO.getByEventId(e.getId()));
        
        return e;
    }
}
