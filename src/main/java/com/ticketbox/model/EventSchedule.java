package com.ticketbox.model;

import java.sql.Timestamp;

public class EventSchedule {
    private int id;
    private int eventId;
    private Timestamp startTime;
    private Timestamp endTime;

    public EventSchedule() {
    }

    public EventSchedule(int id, int eventId, Timestamp startTime, Timestamp endTime) {
        this.id = id;
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public EventSchedule(int eventId, Timestamp startTime, Timestamp endTime) {
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
