package com.ticketbox.model;

public class Ticket {
    private int id;
    private int bookingId;
    private int ticketTypeId;
    private String qrCode;
    private String status; // VALID, USED, CANCELLED

    // Joined fields for display
    private String eventName;
    private String eventDate;
    private String location;
    private String ticketTypeName;
    private double price;

    private boolean isResale;
    private double resalePrice;

    public Ticket() {}

    public Ticket(int ticketTypeId, String qrCode) {
        this.ticketTypeId = ticketTypeId;
        this.qrCode = qrCode;
        this.status = "VALID";
        this.isResale = false;
        this.resalePrice = 0.0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(int ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isIsResale() { return isResale; }
    public void setIsResale(boolean isResale) { this.isResale = isResale; }
    public double getResalePrice() { return resalePrice; }
    public void setResalePrice(double resalePrice) { this.resalePrice = resalePrice; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getTicketTypeName() { return ticketTypeName; }
    public void setTicketTypeName(String ticketTypeName) { this.ticketTypeName = ticketTypeName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
