package com.ticketbox.model;

import java.util.ArrayList;
import java.util.List;

public class SeatMap {
    private int eventId;
    private List<SeatZone> zones;
    private String backgroundImageUrl; // Optional

    public SeatMap() {
        this.zones = new ArrayList<>();
    }

    public SeatMap(int eventId) {
        this.eventId = eventId;
        this.zones = new ArrayList<>();
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public List<SeatZone> getZones() {
        return zones;
    }

    public void setZones(List<SeatZone> zones) {
        this.zones = zones;
    }
    
    public void addZone(SeatZone zone) {
        this.zones.add(zone);
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }
}
