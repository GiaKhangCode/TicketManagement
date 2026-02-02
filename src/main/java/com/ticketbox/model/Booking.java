package com.ticketbox.model;

import java.sql.Timestamp;

public class Booking {
    private int id;
    private int userId;
    private int scheduleId;
    private Timestamp bookingDate;
    private double totalAmount;
    private String status;

    public Booking() {
    }

    public Booking(int userId, double totalAmount) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = "SUCCESS"; // Default success for now
    }

    public Booking(int userId, int scheduleId, double totalAmount) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.totalAmount = totalAmount;
        this.status = "SUCCESS"; // Default success for now
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }
}
