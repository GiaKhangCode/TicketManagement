package com.ticketbox.controller;

import com.ticketbox.dao.UserDAO;
import com.ticketbox.model.User;
import com.ticketbox.view.LoginFrame;
import com.ticketbox.view.MainFrame;
import com.ticketbox.view.RegisterFrame;
import javax.swing.JOptionPane;

public class AuthController {
    private LoginFrame loginFrame;
    private RegisterFrame registerFrame;
    private UserDAO userDAO;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public void showLoginView() {
        if (loginFrame == null) {
            loginFrame = new LoginFrame(this);
        }
        loginFrame.setVisible(true);
        if (registerFrame != null) registerFrame.setVisible(false);
    }

    public void showRegisterView() {
        if (registerFrame == null) {
            registerFrame = new RegisterFrame(this);
        }
        registerFrame.setVisible(true);
        if (loginFrame != null) loginFrame.setVisible(false);
    }

    public void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.login(username, password);
        if (user != null) {
            // Đăng nhập thành công
            // SKIP SUCCESS DIALOG: Direct navigation
            loginFrame.dispose(); // Đóng login
            
            // Mở màn hình chính (MainFrame)
            
            // Mở màn hình chính (MainFrame)
            MainFrame mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(loginFrame, "Sai tên đăng nhập hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleRegister(User user) {
        if (userDAO.checkUsernameExists(user.getUsername())) {
            JOptionPane.showMessageDialog(registerFrame, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDAO.register(user)) {
            JOptionPane.showMessageDialog(registerFrame, "Đăng ký thành công! Vui lòng đăng nhập.");
            showLoginView();
        } else {
            JOptionPane.showMessageDialog(registerFrame, "Đăng ký thất bại. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
