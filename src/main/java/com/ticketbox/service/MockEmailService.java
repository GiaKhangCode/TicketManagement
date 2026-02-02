package com.ticketbox.service;

import com.ticketbox.model.Booking;
import com.ticketbox.model.Ticket;
import com.ticketbox.model.User;
import java.text.SimpleDateFormat;
import java.util.List;

public class MockEmailService {
    
    public void sendTicketEmail(User user, Booking booking, List<Ticket> tickets, String eventName) {
        // Giả lập độ trễ gửi mail
        new Thread(() -> {
            try {
                System.out.println("\n[MAIL SERVER] Đang kết nối...");
                Thread.sleep(1000);
                System.out.println("[MAIL SERVER] Đang gửi email tới: " + user.getEmail());
                Thread.sleep(500);
                
                StringBuilder content = new StringBuilder();
                content.append("==================================================\n");
                content.append("           XÁC NHẬN ĐẶT VÉ THÀNH CÔNG             \n");
                content.append("==================================================\n");
                content.append("Xin chào ").append(user.getFullName()).append(",\n\n");
                content.append("Cảm ơn bạn đã đặt vé tại Ticketbox!\n");
                content.append("Dưới đây là thông tin vé của bạn:\n\n");
                
                content.append("Sự kiện: ").append(eventName).append("\n");
                content.append("Mã đơn hàng: #").append(booking.getId()).append("\n");
                content.append("Ngày đặt: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(booking.getBookingDate())).append("\n");
                content.append("Tổng tiền: ").append(String.format("%,.0f VNĐ", booking.getTotalAmount())).append("\n\n");
                
                content.append("--- CHI TIẾT VÉ ---\n");
                for (Ticket t : tickets) {
                    content.append("Vé ID: ").append(t.getId())
                           .append(" | QR Code: ").append(t.getQrCode()).append("\n");
                }
                content.append("\n==================================================\n");
                content.append("Vui lòng xuất trình mã QR này tại cổng soát vé.\n");
                content.append("Chúc bạn có những trải nghiệm tuyệt vời!\n");
                content.append("==================================================\n");
                
                System.out.println(content.toString());
                System.out.println("[MAIL SERVER] Email đã gửi thành công!\n");
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
