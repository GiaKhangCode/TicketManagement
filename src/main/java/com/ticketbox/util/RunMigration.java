package com.ticketbox.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RunMigration {

    public static void main(String[] args) {
        String filePath = (args.length > 0) ? args[0] : "migration_v2_add_schedules.sql";
        System.out.println("Running migration from: " + filePath);

        try (Connection conn = DatabaseConnection.getConnection();
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            if (conn == null) {
                System.out.println("Cannot connect to database.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                // Remove comments
                if (line.trim().startsWith("--")) continue;
                
                sb.append(line).append(" ");
                
                // End of statement
                if (line.trim().endsWith(";")) {
                    String sql = sb.toString().replace(";", "").trim();
                    if (!sql.isEmpty()) {
                        executeSQL(conn, sql);
                    }
                    sb.setLength(0); // Reset buffer
                }
            }
            
            System.out.println("Migration completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeSQL(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("Executing: " + sql);
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("Error executing SQL: " + sql);
            System.err.println("Message: " + e.getMessage());
            // Don't throw, try next (some might fail if re-run)
        }
    }
}
