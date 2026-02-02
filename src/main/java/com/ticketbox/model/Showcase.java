package com.ticketbox.model;

import java.util.ArrayList;
import java.util.List;

public class Showcase {
    private int id;
    private String name;
    private int displayOrder;
    private boolean active;
    private List<Event> events;

    public Showcase() {
        this.events = new ArrayList<>();
    }

    public Showcase(int id, String name, int displayOrder, boolean active) {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
        this.active = active;
        this.events = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
    
    public void addEvent(Event event) {
        this.events.add(event);
    }

    @Override
    public String toString() {
        return name;
    }
}
