package com.ticketbox.dao;

import com.google.gson.Gson;
import com.ticketbox.model.SeatMap;
import com.ticketbox.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SeatMapDAO {
    
    private Gson gson;

    public SeatMapDAO() {
        this.gson = new Gson();
    }

    public boolean saveSeatMap(SeatMap map) {
        if (map == null) return false;
        
        String json = gson.toJson(map);
        String sqlMerge = "MERGE INTO EVENT_SEAT_MAPS m " +
                          "USING (SELECT ? AS eid, ? AS data FROM dual) d " +
                          "ON (m.EVENT_ID = d.eid) " +
                          "WHEN MATCHED THEN UPDATE SET m.MAP_DATA = d.data, m.CREATED_AT = CURRENT_TIMESTAMP " +
                          "WHEN NOT MATCHED THEN INSERT (EVENT_ID, MAP_DATA) VALUES (d.eid, d.data)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlMerge)) {
             
            pstmt.setInt(1, map.getEventId());
            // Oracle JDBC should handle String > 4000 as CLOB automatically in newer drivers,
            // or we might need setClob object. For simple JSON usually setString works if <32k in recent Oracle.
            // If very large, might need StringReader.
            pstmt.setString(2, json);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public SeatMap getSeatMap(int eventId) {
        String sql = "SELECT MAP_DATA FROM EVENT_SEAT_MAPS WHERE EVENT_ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("MAP_DATA");
                    if (json != null && !json.isEmpty()) {
                        return gson.fromJson(json, SeatMap.class);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean deleteSeatMap(int eventId) {
        String sql = "DELETE FROM EVENT_SEAT_MAPS WHERE EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
