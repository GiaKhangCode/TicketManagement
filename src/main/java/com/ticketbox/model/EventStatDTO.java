package com.ticketbox.model;

public class EventStatDTO {
    private String eventName;
    private int totalTickets;
    private int soldTickets;
    private double revenue;

    public EventStatDTO(String eventName, int totalTickets, int soldTickets, double revenue) {
        this.eventName = eventName;
        this.totalTickets = totalTickets;
        this.soldTickets = soldTickets;
        this.revenue = revenue;
    }

    public String getEventName() { return eventName; }
    public int getTotalTickets() { return totalTickets; }
    public int getSoldTickets() { return soldTickets; }
    public double getRevenue() { return revenue; }
}
