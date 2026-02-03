
package com.ticketbox.view.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import com.ticketbox.model.SeatZone;

public class StageCanvas extends JPanel {
    private List<SeatZone> zones;
    private List<SeatZone> selectedZones = new ArrayList<>(); // Multi-selection support
    private SeatZone primarySelection; // The one showing properties (last selected)
    private int gridSize = 10;
    private boolean editable = true; // Default true for Builder
    
    // Drag/Resize state
    private boolean isDragging = false;
    private boolean isResizing = false;
    private boolean isRotating = false;
    private boolean isSelectingBox = false; // Rubber band selection
    private int dragOffsetX, dragOffsetY;
    private Point lastMousePos;
    
    // Tools
    public enum ToolType {
        SELECT, DRAW_RECT, DRAW_OVAL, DRAW_POLY, DRAW_SECTOR
    }
    private ToolType currentTool = ToolType.SELECT;
    
    // Drawing State
    private Point dragStartPoint; // For Rect/Oval
    private Point dragCurrentPoint; 
    private List<Point> currentPolyPoints = new ArrayList<>(); // For Polygon being drawn
    
    // Callbacks
    private Runnable onSelectionChanged;
    private ZoneClickListener onZoneClicked;

    public interface ZoneClickListener {
        void onZoneClick(SeatZone zone);
    }
    
    // Clipboards
    private List<SeatZone> clipboardZones = new ArrayList<>();

    private javax.swing.JPopupMenu popupMenu;

    public StageCanvas() {
        this.zones = new ArrayList<>();
        setBackground(new Color(30, 41, 59)); // Dark background
        setBorder(new LineBorder(new Color(100, 116, 139), 1));
        setFocusable(true); // Enable keyboard focus
        
        // Init Context Menu
        popupMenu = new javax.swing.JPopupMenu();
        
        javax.swing.JMenuItem rotateLeft = new javax.swing.JMenuItem("Xoay Trái 90°");
        rotateLeft.addActionListener(e -> rotateSelectedZones(-90));
        popupMenu.add(rotateLeft);
        
        javax.swing.JMenuItem rotateRight = new javax.swing.JMenuItem("Xoay Phải 90°");
        rotateRight.addActionListener(e -> rotateSelectedZones(90));
        popupMenu.add(rotateRight);
        
        javax.swing.JMenuItem rotate180 = new javax.swing.JMenuItem("Xoay 180°");
        rotate180.addActionListener(e -> rotateSelectedZones(180));
        popupMenu.add(rotate180);
        
        popupMenu.addSeparator();
        
        javax.swing.JMenuItem flipH = new javax.swing.JMenuItem("Lật Ngang");
        flipH.addActionListener(e -> flipSelectedZones(true));
        popupMenu.add(flipH);
        
        javax.swing.JMenuItem flipV = new javax.swing.JMenuItem("Lật Dọc");
        flipV.addActionListener(e -> flipSelectedZones(false));
        popupMenu.add(flipV);
        
        popupMenu.addSeparator();
        
        javax.swing.JMenuItem deleteItem = new javax.swing.JMenuItem("Xóa");
        deleteItem.addActionListener(e -> deleteSelectedZones());
        popupMenu.add(deleteItem);
        
        
        // Mouse Interactions
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(e)) {
                     // Right click logic
                     // If clicking on a selected zone, keep selection.
                     // If clicking on unselected zone, select it (exclusive).
                     SeatZone clicked = getZoneAt(e.getPoint());
                     if (clicked != null) {
                         if (!selectedZones.contains(clicked)) {
                             selectZoneExclusive(clicked);
                         }
                         popupMenu.show(e.getComponent(), e.getX(), e.getY());
                     }
                } else {
                    handleMousePressed(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }
        });
        
        // Keyboard Interactions
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!editable) return;
                
                int mod = e.getModifiersEx();
                boolean ctrl = (mod & InputEvent.CTRL_DOWN_MASK) != 0;
                
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedZones();
                } 
                else if (ctrl && e.getKeyCode() == KeyEvent.VK_C) {
                    copySelectedZones();
                } 
                else if (ctrl && e.getKeyCode() == KeyEvent.VK_V) {
                    pasteZones();
                } 
                else if (ctrl && e.getKeyCode() == KeyEvent.VK_D) {
                    duplicateSelectedZones();
                }
                else if (!selectedZones.isEmpty()) {
                    // Nudge with Arrows
                    int dx = 0, dy = 0;
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) dx = -1;
                    else if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = 1;
                    else if (e.getKeyCode() == KeyEvent.VK_UP) dy = -1;
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) dy = 1;
                    
                    if (dx != 0 || dy != 0) {
                        moveSelectedZones(dx, dy);
                    }
                }
            }
        });
    }

    public void setZones(List<SeatZone> zones) {
        this.zones = zones;
        repaint();
    }

    public List<SeatZone> getZones() {
        return zones;
    }

    // Legacy method for property panel - returns primary selection
    public SeatZone getSelectedZone() {
        return primarySelection;
    }
    
    public List<SeatZone> getSelectedZones() {
        return selectedZones;
    }

    public void setOnSelectionChanged(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }
    
    public void setOnZoneClicked(ZoneClickListener listener) {
        this.onZoneClicked = listener;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        repaint();
    }

    public void setTool(ToolType tool) {
        this.currentTool = tool;
        if (tool != ToolType.SELECT) {
            clearSelection();
        }
        repaint();
    }

    // Add single zone (for new creations)
    public void addZone() {
        SeatZone z = new SeatZone();
        z.setId(UUID.randomUUID().toString());
        z.setX(50);
        z.setY(50);
        z.setWidth(100);
        z.setHeight(80);
        z.setLabel("New Zone");
        z.setColorHex("#3B82F6"); // Blue 500
        z.setTicketTypeId(0);
        
        zones.add(z);
        selectZoneExclusive(z);
        repaint();
    }
    
    public void deleteSelectedZones() {
        if (!selectedZones.isEmpty()) {
            zones.removeAll(selectedZones);
            clearSelection();
            repaint();
        }
    }
    
    private void copySelectedZones() {
        clipboardZones.clear();
        for (SeatZone z : selectedZones) {
             clipboardZones.add(z.copy());
        }
    }
    
    private void pasteZones() {
        if (!clipboardZones.isEmpty()) {
            clearSelection();
            List<SeatZone> newPasted = new ArrayList<>();
            for (SeatZone clip : clipboardZones) {
                SeatZone newZone = clip.copy();
                // Offset
                newZone.setX(newZone.getX() + 20);
                newZone.setY(newZone.getY() + 20);
                
                if ("POLYGON".equals(newZone.getShapeType())) {
                    for (SeatZone.Point p : newZone.getPolygonPoints()) {
                        p.x += 20;
                        p.y += 20;
                    }
                }
                zones.add(newZone);
                newPasted.add(newZone);
            }
            // Select newly pasted
            selectedZones.addAll(newPasted);
            if (!newPasted.isEmpty()) primarySelection = newPasted.get(newPasted.size()-1);
            
            // Update clipboard for next paste (cascading effect)
            clipboardZones.clear();
            for(SeatZone z : newPasted) clipboardZones.add(z.copy());
            
            repaint();
            if (onSelectionChanged != null) onSelectionChanged.run();
        }
    }
    
    private void duplicateSelectedZones() {
        copySelectedZones();
        pasteZones();
    }
    
    public void rotateSelectedZones(double deltaDegrees) {
        for (SeatZone z : selectedZones) {
             double newRot = z.getRotation() + deltaDegrees;
             z.setRotation(newRot % 360);
        }
        repaint();
    }
    
    public void flipSelectedZones(boolean horizontal) {
        for (SeatZone z : selectedZones) {
            // Re-use existing flip logic per zone...
             flipZone(z, horizontal);
        }
        repaint();
    }
    
    private void flipZone(SeatZone selectedZone, boolean horizontal) {
         if ("POLYGON".equals(selectedZone.getShapeType())) {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (SeatZone.Point p : selectedZone.getPolygonPoints()) {
                if (p.x < minX) minX = p.x;
                if (p.x > maxX) maxX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.y > maxY) maxY = p.y;
            }
            int centerX = minX + (maxX - minX) / 2;
            int centerY = minY + (maxY - minY) / 2;
            
            for (SeatZone.Point p : selectedZone.getPolygonPoints()) {
                if (horizontal) {
                    p.x = centerX - (p.x - centerX);
                } else {
                    p.y = centerY - (p.y - centerY);
                }
            }
        } else if ("SECTOR".equals(selectedZone.getShapeType())) {
            double start = selectedZone.getStartAngle();
            double arc = selectedZone.getArcAngle();
            
            if (horizontal) {
                selectedZone.setStartAngle(180 - (start + arc));
            } else { // Vertical
                selectedZone.setStartAngle(360 - (start + arc));
            }
        }
    }

    private void moveSelectedZones(int dx, int dy) {
        for (SeatZone z : selectedZones) {
             z.setX(z.getX() + dx);
             z.setY(z.getY() + dy);
             
             if ("POLYGON".equals(z.getShapeType())) {
                for (SeatZone.Point p : z.getPolygonPoints()) {
                    p.x += dx;
                    p.y += dy;
                }
            }
        }
        repaint();
    }

    private void selectZoneExclusive(SeatZone z) {
        selectedZones.clear();
        if (z != null) {
            selectedZones.add(z);
            primarySelection = z;
        } else {
            primarySelection = null;
        }
        if (onSelectionChanged != null) onSelectionChanged.run();
        repaint();
    }
    
    private void addSelection(SeatZone z) {
        if (z != null && !selectedZones.contains(z)) {
            selectedZones.add(z);
            primarySelection = z;
            if (onSelectionChanged != null) onSelectionChanged.run();
            repaint();
        }
    }
    
    private void clearSelection() {
        selectedZones.clear();
        primarySelection = null;
        if (onSelectionChanged != null) onSelectionChanged.run();
        repaint();
    }
    
    private SeatZone getZoneAt(Point p) {
         for (int i = zones.size() - 1; i >= 0; i--) {
             SeatZone z = zones.get(i);
             if (createShape(z).contains(p)) return z;
         }
         return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw Grid (Optional)
        g2.setColor(new Color(51, 65, 85));
        for (int i = 0; i < getWidth(); i += gridSize) {
            g2.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += gridSize) {
            g2.drawLine(0, i, getWidth(), i);
        }
        
        // Draw Zones
        for (SeatZone z : zones) {
            drawZone(g2, z);
        }
        
        // Draw Preview (Current Drawing)
        if (editable) {
            drawPreview(g2);
            if (isSelectingBox && dragStartPoint != null && dragCurrentPoint != null) {
                // Draw Selection Box
                int x = Math.min(dragStartPoint.x, dragCurrentPoint.x);
                int y = Math.min(dragStartPoint.y, dragCurrentPoint.y);
                int w = Math.abs(dragCurrentPoint.x - dragStartPoint.x);
                int h = Math.abs(dragCurrentPoint.y - dragStartPoint.y);
                
                g2.setColor(new Color(59, 130, 246, 50)); // Semi-transparent blue
                g2.fillRect(x, y, w, h);
                g2.setColor(new Color(59, 130, 246));
                g2.drawRect(x, y, w, h);
            }
        }
    }
    
    private void drawPreview(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5}, 0));
        
        if (currentTool == ToolType.DRAW_RECT || currentTool == ToolType.DRAW_OVAL || currentTool == ToolType.DRAW_SECTOR) {
            if (dragStartPoint != null && dragCurrentPoint != null) {
                int x = Math.min(dragStartPoint.x, dragCurrentPoint.x);
                int y = Math.min(dragStartPoint.y, dragCurrentPoint.y);
                int w = Math.abs(dragCurrentPoint.x - dragStartPoint.x);
                int h = Math.abs(dragCurrentPoint.y - dragStartPoint.y);
                
                if (currentTool == ToolType.DRAW_OVAL) g2.drawOval(x, y, w, h);
                else if (currentTool == ToolType.DRAW_SECTOR) g2.drawArc(x, y, w, h, 45, 90); // Preview default arc
                else g2.drawRect(x, y, w, h);
            }
        } else if (currentTool == ToolType.DRAW_POLY && !currentPolyPoints.isEmpty()) {
            // Draw lines connecting points
            g2.setColor(Color.CYAN);
            g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < currentPolyPoints.size() - 1; i++) {
                Point p1 = currentPolyPoints.get(i);
                Point p2 = currentPolyPoints.get(i+1);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            // Line to cursor
            if (dragCurrentPoint != null && !currentPolyPoints.isEmpty()) {
                 Point last = currentPolyPoints.get(currentPolyPoints.size()-1);
                 g2.drawLine(last.x, last.y, dragCurrentPoint.x, dragCurrentPoint.y);
            }
        }
    }
    
    private void drawZone(Graphics2D g2, SeatZone z) {
        Color c = Color.decode(z.getColorHex());
        g2.setColor(c);
        
        Shape shape = createShape(z);
        g2.fill(shape);
        
        boolean isSelected = selectedZones.contains(z);
        
        // Border & Selection
        if (isSelected) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[]{5}, 0));
        } else {
            g2.setColor(c.darker());
            g2.setStroke(new BasicStroke(1));
        }
        g2.draw(shape);
        
        // Label (Draw centered, not rotated for readability, or rotated?)
        // Usually readability > rotation. We draw at center of bounds.
        g2.setColor(isBright(c) ? Color.BLACK : Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle bounds = shape.getBounds();
        int tx = bounds.x + (bounds.width - fm.stringWidth(z.getLabel())) / 2;
        int ty = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2;
        g2.drawString(z.getLabel(), tx, ty);
        
        // Resize Handle (if selected AND editable AND Rect/Oval AND No Rotation AND primary selection)
        // Multi-resize simple: Only show handles for primary selection to avoid clutter? 
        // Or show for all? Standard UX: Handles usually only on primary or bounding box.
        // Let's show only for primarySelection for safety.
        if (editable && isSelected && z == primarySelection && !"POLYGON".equals(z.getShapeType()) && z.getRotation() == 0) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            int handleSize = 8;
            g2.fillRect(bounds.x + bounds.width - handleSize, bounds.y + bounds.height - handleSize, handleSize, handleSize);
            g2.setColor(Color.BLACK);
            g2.drawRect(bounds.x + bounds.width - handleSize, bounds.y + bounds.height - handleSize, handleSize, handleSize);
        }
        
        // Rotation Handle (if selected AND editable AND primary)
        if (editable && isSelected && z == primarySelection) {
            drawRotationHandle(g2, z);
        }
    }
    
    private void drawRotationHandle(Graphics2D g2, SeatZone z) {
        // Calculate handle position (Top Center - 25px)
        double cx = z.getX() + z.getWidth() / 2.0;
        double cy = z.getY() + z.getHeight() / 2.0;
        double knobDist = 25;
        
        // Unrotated coords
        double kx = cx;
        double ky = z.getY() - knobDist;
        
        // Create Knob Shape (Circle)
        Ellipse2D knob = new Ellipse2D.Double(kx - 4, ky - 4, 8, 8);
        Line2D line = new Line2D.Double(cx, z.getY(), kx, ky); // Connect to top of box
        
        Shape transformedKnob = knob;
        Shape transformedLine = line;
        
        if (z.getRotation() != 0) {
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(z.getRotation()), cx, cy);
            transformedKnob = at.createTransformedShape(knob);
            transformedLine = at.createTransformedShape(line);
        }
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1));
        g2.draw(transformedLine);
        g2.fill(transformedKnob);
        g2.setColor(Color.BLACK);
        g2.draw(transformedKnob);
    }
    
    private Shape getRotationHandleShape(SeatZone z) {
        double cx = z.getX() + z.getWidth() / 2.0;
        double cy = z.getY() + z.getHeight() / 2.0;
        double knobDist = 25;
        double kx = cx;
        double ky = z.getY() - knobDist;
        
        Ellipse2D knob = new Ellipse2D.Double(kx - 4, ky - 4, 8, 8);
        
        if (z.getRotation() != 0) {
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(z.getRotation()), cx, cy);
            return at.createTransformedShape(knob);
        }
        return knob;
    }
    
    private Shape createShape(SeatZone z) {
        Shape baseShape;
        if ("POLYGON".equals(z.getShapeType())) {
            Polygon p = new Polygon();
            for (SeatZone.Point pt : z.getPolygonPoints()) {
                p.addPoint(pt.x, pt.y);
            }
            baseShape = p;
        } else if ("OVAL".equals(z.getShapeType())) {
            baseShape = new java.awt.geom.Ellipse2D.Float(z.getX(), z.getY(), z.getWidth(), z.getHeight());
        } else if ("SECTOR".equals(z.getShapeType())) {
            // Create Donut Slice
            double x = z.getX();
            double y = z.getY();
            double w = z.getWidth();
            double h = z.getHeight();
            double start = z.getStartAngle();
            double extent = z.getArcAngle();
            double ratio = z.getInnerRadiusRatio();
            
            Arc2D outer = new Arc2D.Double(x, y, w, h, start, extent, Arc2D.PIE);
            
            // Inner arc
            double iw = w * ratio;
            double ih = h * ratio;
            double ix = x + (w - iw) / 2;
            double iy = y + (h - ih) / 2;
            
            Arc2D inner = new Arc2D.Double(ix, iy, iw, ih, start, extent, Arc2D.PIE);
            
            Area area = new Area(outer);
            area.subtract(new Area(inner));
            baseShape = area;
        } else {
            baseShape = new Rectangle(z.getX(), z.getY(), z.getWidth(), z.getHeight());
        }
        
        if (z.getRotation() != 0) {
            Rectangle bounds = baseShape.getBounds();
            AffineTransform at = AffineTransform.getRotateInstance(
                Math.toRadians(z.getRotation()), 
                bounds.getCenterX(), 
                bounds.getCenterY()
            );
            return at.createTransformedShape(baseShape);
        }
        
        return baseShape;
    }
    
    private boolean isBright(Color c) {
        double brightness = (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000;
        return brightness > 128;
    }
    
    private void handleMousePressed(MouseEvent e) {
        if (!editable) {
            SeatZone z = getZoneAt(e.getPoint());
            if (z != null && onZoneClicked != null) onZoneClicked.onZoneClick(z);
            return;
        }

        if (currentTool == ToolType.SELECT) {
             // 1. Check Rotation Handle (Primary Only)
             if (primarySelection != null && selectedZones.contains(primarySelection)) {
                  Shape knob = getRotationHandleShape(primarySelection);
                  if (knob.contains(e.getPoint())) {
                      isRotating = true;
                      isDragging = false;
                      isResizing = false;
                      isSelectingBox = false;
                      return;
                  }
             }
             
             // 2. Check Resize Handle (Primary Only)
             if (primarySelection != null && selectedZones.contains(primarySelection) && primarySelection.getRotation() == 0 && !"POLYGON".equals(primarySelection.getShapeType())) {
                  Shape shape = createShape(primarySelection);
                  Rectangle bounds = shape.getBounds();
                  Rectangle handle = new Rectangle(bounds.x + bounds.width - 10, bounds.y + bounds.height - 10, 15, 15);
                  if (handle.contains(e.getPoint())) {
                      isResizing = true;
                      lastMousePos = e.getPoint();
                      isRotating = false;
                      isDragging = false;
                      isSelectingBox = false;
                      return;
                  }
             }

             // 3. Check Zone Click
             SeatZone clickedZone = getZoneAt(e.getPoint());
             
             if (clickedZone != null) {
                 // Clicked on a zone
                 if (!selectedZones.contains(clickedZone)) {
                     // New selection
                     if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                         // Add to selection
                         addSelection(clickedZone);
                     } else {
                         // Exclusive select
                         selectZoneExclusive(clickedZone);
                     }
                 }
                 // If already selected, do nothing (ready for drag)
                 // But update primary if Ctrl clicked?
                 if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                      primarySelection = clickedZone;
                 }
                 
                 isDragging = true;
                 // Set drag offset for all selected zones? 
                 // Complex for multiple. We calculate delta in Dragged.
                 lastMousePos = e.getPoint();
                 isResizing = false;
                 isRotating = false;
                 isSelectingBox = false;
                 
             } else {
                 // Clicked Empty Space -> Start Selection Box
                 isSelectingBox = true;
                 dragStartPoint = e.getPoint();
                 dragCurrentPoint = e.getPoint();
                 if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
                     clearSelection();
                 }
                 isDragging = false;
                 isResizing = false;
                 isRotating = false;
             }
             repaint();
            
        } else {
            // Drawing Tools
             dragStartPoint = e.getPoint();
             dragCurrentPoint = e.getPoint();
             
             if (currentTool == ToolType.DRAW_POLY) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        finishPolygon();
                    } else {
                        currentPolyPoints.add(e.getPoint());
                        dragCurrentPoint = e.getPoint(); 
                        repaint();
                    }
                }
             }
             repaint();
        }
    }
    
    private void finishPolygon() {
        if (currentPolyPoints.size() < 3) {
            currentPolyPoints.clear();
            repaint();
            return;
        }
        
        SeatZone z = new SeatZone();
        z.setId(UUID.randomUUID().toString());
        z.setShapeType("POLYGON");
        z.setColorHex("#3B82F6");
        z.setLabel("New Poly");
        
        // Calculate bounds for X,Y logic (even though we use points)
        Rectangle bounds = new Polygon(
            currentPolyPoints.stream().mapToInt(p -> p.x).toArray(),
            currentPolyPoints.stream().mapToInt(p -> p.y).toArray(),
            currentPolyPoints.size()
        ).getBounds();
        
        z.setX(bounds.x);
        z.setY(bounds.y);
        z.setWidth(bounds.width);
        z.setHeight(bounds.height);
        
        List<SeatZone.Point> modelPoints = new ArrayList<>();
        for (Point p : currentPolyPoints) {
            modelPoints.add(new SeatZone.Point(p.x, p.y));
        }
        z.setPolygonPoints(modelPoints);
        
        zones.add(z);
        selectZoneExclusive(z);
        
        currentPolyPoints.clear();
        setTool(ToolType.SELECT); // Auto switch back
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (isSelectingBox && dragStartPoint != null) {
            // Box Select Finalize
            int x = Math.min(dragStartPoint.x, e.getX());
            int y = Math.min(dragStartPoint.y, e.getY());
            int w = Math.abs(e.getX() - dragStartPoint.x);
            int h = Math.abs(e.getY() - dragStartPoint.y);
            Rectangle selectionRect = new Rectangle(x,y,w,h);
            
            for (SeatZone z : zones) {
                if (selectionRect.contains(createShape(z).getBounds())) {
                    addSelection(z);
                }
            }
            isSelectingBox = false;
            dragStartPoint = null;
            repaint();
            return;
        }

        if (currentTool == ToolType.DRAW_RECT || currentTool == ToolType.DRAW_OVAL || currentTool == ToolType.DRAW_SECTOR) {
            if (dragStartPoint != null) {
                int x = Math.min(dragStartPoint.x, e.getX());
                int y = Math.min(dragStartPoint.y, e.getY());
                int w = Math.abs(e.getX() - dragStartPoint.x);
                int h = Math.abs(e.getY() - dragStartPoint.y);
                
                if (w > 5 && h > 5) { // Min size
                    SeatZone z = new SeatZone();
                    z.setId(UUID.randomUUID().toString());
                    
                    if (currentTool == ToolType.DRAW_OVAL) z.setShapeType("OVAL");
                    else if (currentTool == ToolType.DRAW_SECTOR) z.setShapeType("SECTOR");
                    else z.setShapeType("RECTANGLE");
                    
                    z.setX(x);
                    z.setY(y);
                    z.setWidth(w);
                    z.setHeight(h);
                    z.setLabel("New Zone");
                    z.setColorHex("#3B82F6");
                    
                    zones.add(z);
                    selectZoneExclusive(z);
                }
            }
            dragStartPoint = null;
            setTool(ToolType.SELECT);
        }
        isDragging = false;
        isResizing = false;
        isRotating = false;
        
    }
    
    private void handleMouseDragged(MouseEvent e) {
         if (isSelectingBox) {
             dragCurrentPoint = e.getPoint();
             repaint();
             return;
         }
         
         if (currentTool == ToolType.SELECT && !selectedZones.isEmpty()) {
              if (isRotating && primarySelection != null) {
                  // Calculate angle from center to mouse
                  double cx = primarySelection.getX() + primarySelection.getWidth() / 2.0;
                  double cy = primarySelection.getY() + primarySelection.getHeight() / 2.0;
                  
                  double dx = e.getX() - cx;
                  double dy = e.getY() - cy;
                  
                  // atan2 returns angle in radians (-PI to PI), 0 is right (East)
                  double theta = Math.toDegrees(Math.atan2(dy, dx));
                  
                  // We want 0 at Top (North). atan2(0,-1) = -90 aka 270.
                  // Correction: theta += 90;
                  theta += 90;
                  
                  if (theta < 0) theta += 360;
                  
                  // Snap to 15 degrees for convenience?
                  if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                       theta = Math.round(theta / 15.0) * 15.0;
                  }
                  
                  primarySelection.setRotation(theta);
                  repaint();
              }
              else if (isResizing && primarySelection != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    int newW = Math.max(20, primarySelection.getWidth() + dx);
                    int newH = Math.max(20, primarySelection.getHeight() + dy);
                    primarySelection.setWidth(newW);
                    primarySelection.setHeight(newH);
                    // Note: Resizing polygons currently complex, only moving supported well.
                    lastMousePos = e.getPoint();
                    repaint();
              }
              else if (isDragging) {
                  // Bulk Move
                  int dx = e.getX() - lastMousePos.x;
                  int dy = e.getY() - lastMousePos.y;
                  
                  for (SeatZone z : selectedZones) {
                       z.setX(z.getX() + dx);
                       z.setY(z.getY() + dy);
                       
                       if ("POLYGON".equals(z.getShapeType())) {
                           for (SeatZone.Point p : z.getPolygonPoints()) {
                               p.x += dx;
                               p.y += dy;
                           }
                       }
                  }
                  lastMousePos = e.getPoint();
                  repaint();
              }
         } else if (currentTool != ToolType.SELECT) {
             dragCurrentPoint = e.getPoint();
             repaint();
         }
    }
    
    private void handleMouseMoved(MouseEvent e) {
        if (currentTool == ToolType.DRAW_POLY) {
            dragCurrentPoint = e.getPoint();
            repaint();
        }
    }
}
