package com.ticketbox.model;

// Force recompile logic

import java.sql.Timestamp;

public class Event {
    private int id;
    private String name;
    private String description;
    private String location;
    private Timestamp startTime;
    private Timestamp endTime;
    private int organizerId;
    private String status; // PENDING, APPROVED, REJECTED
    private String imageUrl;
    private String category; // Added Category field
    private String organizerName; // Transient, for display

    public Event() {}

    public Event(int id, String name, String description, String location, Timestamp startTime, Timestamp endTime, int organizerId, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerId = organizerId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    private java.util.List<EventSchedule> schedules = new java.util.ArrayList<>();

    public java.util.List<EventSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(java.util.List<EventSchedule> schedules) {
        this.schedules = schedules;
    }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    private java.util.List<TicketType> ticketTypes = new java.util.ArrayList<>();
    public java.util.List<TicketType> getTicketTypes() { return ticketTypes; }
    public void setTicketTypes(java.util.List<TicketType> ticketTypes) { this.ticketTypes = ticketTypes; }
}
