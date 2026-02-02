package com.ticketbox.dao;

import com.ticketbox.util.DatabaseConnection;
import com.ticketbox.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    
    public boolean addReview(Review review) {
        String sql = "INSERT INTO REVIEWS (USER_ID, EVENT_ID, RATING, COMMENT_TEXT) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getEventId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasUserReviewed(int userId, int eventId) {
        String sql = "SELECT COUNT(*) FROM REVIEWS WHERE USER_ID = ? AND EVENT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Review> getReviewsByEvent(int eventId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM REVIEWS WHERE EVENT_ID = ? ORDER BY CREATED_AT DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("ID"));
                    r.setUserId(rs.getInt("USER_ID"));
                    r.setEventId(rs.getInt("EVENT_ID"));
                    r.setRating(rs.getInt("RATING"));
                    r.setComment(rs.getString("COMMENT_TEXT"));
                    r.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }
}
