package com.ticketbox.model;

public class TicketType {
    private int id;
    private int eventId;
    private String name;
    private double price;
    private int quantity;
    private int sold;

    public TicketType() {
    }

    public TicketType(int eventId, String name, double price, int quantity) {
        this.eventId = eventId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.sold = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }
    
    public int getAvailableQuantity() {
        return quantity - sold;
    }
}
