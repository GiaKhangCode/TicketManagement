package com.ticketbox.model;

import java.util.ArrayList;
import java.util.List;

public class SeatZone {
    private String id; // Unique ID for the zone within the map
    private int x;
    private int y;
    private int width;
    private int height;
    private String label; // e.g. "VIP A", "Stand B"
    private String colorHex; // e.g. "#FF0000"
    private int ticketTypeId; // Linked TicketType ID (0 if none)
    
    // New fields for advanced shapes
    private String shapeType = "RECTANGLE"; // RECTANGLE, OVAL, POLYGON, SECTOR
    private List<Point> polygonPoints = new ArrayList<>();
    
    // Sector specific (Arc/Donut Slice)
    private double startAngle = 45; // Default
    private double arcAngle = 90;   // Default
    private double innerRadiusRatio = 0.5; // 0.0 to 1.0 (0 = Pie, 0.5 = Donut)
    private double rotation = 0; // Degrees (0-360)

    public static class Point {
        public int x, y;
        public Point() {}
        public Point(int x, int y) { this.x = x; this.y = y; }
    }

    public SeatZone() {
    }

    public SeatZone(String id, int x, int y, int width, int height, String label, String colorHex) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public int getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(int ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }

    public List<Point> getPolygonPoints() {
        return polygonPoints;
    }

    public void setPolygonPoints(List<Point> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getArcAngle() {
        return arcAngle;
    }

    public void setArcAngle(double arcAngle) {
        this.arcAngle = arcAngle;
    }

    public double getInnerRadiusRatio() {
        return innerRadiusRatio;
    }

    public void setInnerRadiusRatio(double innerRadiusRatio) {
        this.innerRadiusRatio = innerRadiusRatio;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public SeatZone copy() {
        SeatZone newZone = new SeatZone();
        newZone.setId(java.util.UUID.randomUUID().toString()); // New ID
        newZone.setX(this.x);
        newZone.setY(this.y);
        newZone.setWidth(this.width);
        newZone.setHeight(this.height);
        newZone.setLabel(this.label); // Or append "Copy"
        newZone.setColorHex(this.colorHex);
        newZone.setTicketTypeId(this.ticketTypeId);
        newZone.setShapeType(this.shapeType);
        newZone.setStartAngle(this.startAngle);
        newZone.setArcAngle(this.arcAngle);
        newZone.setInnerRadiusRatio(this.innerRadiusRatio);
        newZone.setRotation(this.rotation);
        
        List<Point> newPoints = new ArrayList<>();
        for (Point p : this.polygonPoints) {
            newPoints.add(new Point(p.x, p.y));
        }
        newZone.setPolygonPoints(newPoints);
        
        return newZone;
    }
}
