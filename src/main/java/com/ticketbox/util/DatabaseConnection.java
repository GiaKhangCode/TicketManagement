package com.ticketbox.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Thông tin kết nối Oracle Database Localhost
    // Lưu ý: Người dùng cần thay đổi USER và PASS cho phù hợp với máy cá nhân
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl"; // Hoặc ORCL tùy cài đặt
    private static final String USER = "TicketClone"; // Mặc định thường là sys hoặc system
    private static final String PASS = "Admin123"; // Password mặc định ví dụ

    // Removed static connection field to avoid "Closed Connection" issues with try-with-resources
    // private static Connection connection = null;

    // Private Constructor
    private DatabaseConnection() {}

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Đăng ký Driver
            Class.forName("oracle.jdbc.OracleDriver");
            
            conn = DriverManager.getConnection(URL, USER, PASS);
            // System.out.println("Kết nối Oracle Database thành công!"); // Avoid spamming console
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
    
    // No longer needed as callers will close their own connections
    /*
    public static void closeConnection() {
        ...
    }
    */
    
    // Hàm main để test kết nối nhanh
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            System.out.println("Test thành công!");
        } else {
            System.out.println("Test thất bại!");
        }
    }
}
