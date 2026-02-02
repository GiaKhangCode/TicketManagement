package com.ticketbox.dao;

import com.ticketbox.model.Event;
import com.ticketbox.model.Showcase;
import com.ticketbox.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShowcaseDAO {

    public List<Showcase> getAllShowcases() {
        List<Showcase> showcases = new ArrayList<>();
        String sql = "SELECT * FROM SHOWCASES ORDER BY DISPLAY_ORDER";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Showcase s = new Showcase();
                s.setId(rs.getInt("ID"));
                s.setName(rs.getString("NAME"));
                s.setDisplayOrder(rs.getInt("DISPLAY_ORDER"));
                s.setActive(rs.getInt("IS_ACTIVE") == 1);
                showcases.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showcases;
    }

    public List<Showcase> getActiveShowcasesWithEvents() {
        List<Showcase> showcases = new ArrayList<>();
        String sql = "SELECT * FROM SHOWCASES WHERE IS_ACTIVE = 1 ORDER BY DISPLAY_ORDER";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Get Showcases
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Showcase s = new Showcase();
                        s.setId(rs.getInt("ID"));
                        s.setName(rs.getString("NAME"));
                        s.setDisplayOrder(rs.getInt("DISPLAY_ORDER"));
                        s.setActive(true);
                        showcases.add(s);
                    }
                }
            }
            
            // 2. Populate Events for each Showcase
            // We join with EVENTS to get event details
            // Order by SE.DISPLAY_ORDER desc (newest first in list usually?) or custom order
            String eventSql = "SELECT E.* FROM EVENTS E " +
                              "JOIN SHOWCASE_EVENTS SE ON E.ID = SE.EVENT_ID " +
                              "WHERE SE.SHOWCASE_ID = ? AND E.STATUS = 'APPROVED' " +
                              "ORDER BY SE.DISPLAY_ORDER ASC";
                              
            // Reuse EventDAO mapping logic ideally, but duplication for speed is fine
            for (Showcase s : showcases) {
                try (PreparedStatement ps = conn.prepareStatement(eventSql)) {
                    ps.setInt(1, s.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Event e = new Event();
                            e.setId(rs.getInt("ID"));
                            e.setName(rs.getString("NAME"));
                            e.setDescription(rs.getString("DESCRIPTION"));
                            e.setLocation(rs.getString("LOCATION"));
                            e.setStartTime(rs.getTimestamp("START_TIME"));
                            e.setEndTime(rs.getTimestamp("END_TIME"));
                            e.setOrganizerId(rs.getInt("ORGANIZER_ID"));
                            e.setStatus(rs.getString("STATUS"));
                            e.setImageUrl(rs.getString("IMAGE_URL"));
                            
                            // Essential: Load Schedules
                            e.setSchedules(new EventScheduleDAO().getByEventId(e.getId()));
                            // Essential: Load Ticket Types (for price)
                            e.setTicketTypes(new TicketTypeDAO().getTicketTypesByEvent(e.getId()));
                            
                            s.addEvent(e);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showcases;
    }

    public List<Event> getEventsInShowcase(int showcaseId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT E.* FROM EVENTS E " +
                     "JOIN SHOWCASE_EVENTS SE ON E.ID = SE.EVENT_ID " +
                     "WHERE SE.SHOWCASE_ID = ? " +
                     "ORDER BY SE.DISPLAY_ORDER ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, showcaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getInt("ID"));
                    e.setName(rs.getString("NAME"));
                    e.setDescription(rs.getString("DESCRIPTION"));
                    e.setLocation(rs.getString("LOCATION"));
                    e.setStartTime(rs.getTimestamp("START_TIME"));
                    e.setEndTime(rs.getTimestamp("END_TIME"));
                    e.setOrganizerId(rs.getInt("ORGANIZER_ID"));
                    e.setStatus(rs.getString("STATUS"));
                    e.setImageUrl(rs.getString("IMAGE_URL"));
                    e.setCategory(rs.getString("CATEGORY"));
                    
                    // No need to load full schedules for just listing
                    events.add(e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public boolean addEventToShowcase(int showcaseId, int eventId) {
        String sql = "INSERT INTO SHOWCASE_EVENTS (SHOWCASE_ID, EVENT_ID, DISPLAY_ORDER) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showcaseId);
            ps.setInt(2, eventId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log specific error for debugging if needed, but return false to indicate failure
            System.err.println("Error adding event to showcase: " + e.getMessage());
            return false;
        }
    }

    public void removeEventFromShowcase(int showcaseId, int eventId) {
        String sql = "DELETE FROM SHOWCASE_EVENTS WHERE SHOWCASE_ID = ? AND EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showcaseId);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Check if event is already in showcase
    public boolean isEventInShowcase(int showcaseId, int eventId) {
        String sql = "SELECT 1 FROM SHOWCASE_EVENTS WHERE SHOWCASE_ID = ? AND EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showcaseId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean addShowcase(Showcase showcase) {
        String sql = "INSERT INTO SHOWCASES (NAME, DISPLAY_ORDER, IS_ACTIVE) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, showcase.getName());
            ps.setInt(2, showcase.getDisplayOrder());
            ps.setInt(3, showcase.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateShowcase(Showcase showcase) {
        String sql = "UPDATE SHOWCASES SET NAME = ?, DISPLAY_ORDER = ?, IS_ACTIVE = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, showcase.getName());
            ps.setInt(2, showcase.getDisplayOrder());
            ps.setInt(3, showcase.isActive() ? 1 : 0);
            ps.setInt(4, showcase.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShowcase(int id) {
        String sql = "DELETE FROM SHOWCASES WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // First delete mappings
            try (PreparedStatement psMap = conn.prepareStatement("DELETE FROM SHOWCASE_EVENTS WHERE SHOWCASE_ID = ?")) {
                psMap.setInt(1, id);
                psMap.executeUpdate();
            }
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
