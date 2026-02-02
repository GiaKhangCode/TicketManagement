package com.ticketbox.view;


import com.ticketbox.controller.AuthController;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Class chính để chạy ứng dụng
 */
public class App {
    public static void main(String[] args) {
        // Thiết lập giao diện FlatLaf (Modern UI)
        try {
            // Setup macOS style light theme
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
            
            // Global UI Settingshttps://github.com/GiaKhangCode/celestial_perspective/blob/main/login.jpg?raw=true
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            
            // Font
            java.awt.Font font = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
            UIManager.put("defaultFont", font);
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }



        java.awt.EventQueue.invokeLater(() -> {
            AuthController authController = new AuthController();
            authController.showLoginView();
        });
    }
}
