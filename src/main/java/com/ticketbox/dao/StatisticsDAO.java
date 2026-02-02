package com.ticketbox.dao;

import com.ticketbox.model.EventStatDTO;
import com.ticketbox.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDAO {

    public List<EventStatDTO> getEventStatistics(int organizerId) {
        List<EventStatDTO> stats = new ArrayList<>();
        // Query to aggregate statistics:
        // Summing quantity from TICKET_TYPES for Total Tickets
        // Summing sold from TICKET_TYPES for Sold Tickets
        // Calculating approximate revenue based on SOLD * PRICE
        // Note: For exact revenue we should query BOOKINGS/TICKETS, but TICKET_TYPES.SOLD * PRICE is a good approximation for this summary.
        // Actually, let's try to join properly if possible, but TICKET_TYPES has the aggregated SOLD count already.
        
        String sql = "SELECT e.NAME, " +
                     "SUM(tt.QUANTITY) as TOTAL_QTY, " +
                     "SUM(tt.SOLD) as TOTAL_SOLD, " +
                     "SUM(tt.SOLD * tt.PRICE) as TOTAL_REVENUE " +
                     "FROM EVENTS e " +
                     "JOIN TICKET_TYPES tt ON e.ID = tt.EVENT_ID " +
                     "WHERE e.ORGANIZER_ID = ? " +
                     "GROUP BY e.ID, e.NAME, e.START_TIME " +
                     "ORDER BY e.START_TIME DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, organizerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("NAME");
                int totalQty = rs.getInt("TOTAL_QTY");
                int totalSold = rs.getInt("TOTAL_SOLD");
                double revenue = rs.getDouble("TOTAL_REVENUE");
                
                stats.add(new EventStatDTO(name, totalQty, totalSold, revenue));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
