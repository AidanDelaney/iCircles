package icircles.concreteDiagram;

import icircles.abstractDescription.AbstractDescription;
import icircles.gui.CirclesPanel;
import icircles.util.CannotDrawException;
import icircles.util.DEB;

import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;

public class ConcreteDiagram {

    Rectangle2D.Double box;
    ArrayList<CircleContour> circles;
    ArrayList<ConcreteZone> shadedZones;
    ArrayList<ConcreteZone> unshadedZones;
    ArrayList<ConcreteSpider> spiders;
    private Font font;

    public ConcreteDiagram(Rectangle2D.Double box,
            ArrayList<CircleContour> circles,
            ArrayList<ConcreteZone> shadedZones,
            ArrayList<ConcreteZone> unshadedZones,
            ArrayList<ConcreteSpider> spiders) {
        this.box = box;
        this.circles = circles;
        this.shadedZones = shadedZones;
        this.unshadedZones = unshadedZones;
        this.spiders = spiders;
    }

    public ArrayList<CircleContour> getCircles() {
        return circles;
    }

    public double checksum() {
        return circles_checksum() + shading_checksum() + spiders_checksum();
    }

    private double circles_checksum() {

        double result = 0.0;
        if (circles == null) {
            return result;
        }

        Iterator<CircleContour> cIt = circles.iterator();
        while (cIt.hasNext()) {
            CircleContour c = cIt.next();
            if (DEB.level >= 2) {
                System.out.println("build checksum for contour at coords (" + c.cx 
                		       + ", " + c.cy + ") radius "+ c.radius +"\n");
            }
            result += c.cx * 0.345 + c.cy * 0.456 + c.radius * 0.567 + c.ac.checksum() * 0.555;
            result *= 1.2;
        }
        return result;
    }

    private double shading_checksum() {

        double result = 0.0;
        Iterator<ConcreteZone> czIt = shadedZones.iterator();
        while (czIt.hasNext()) {
            ConcreteZone cz = czIt.next();
            if (DEB.level >= 2) {
                System.out.println("build checksum for shading\n");
            }
            result += cz.abr.checksum() * 1000.0;
        }
        return result;
    }

    private double spiders_checksum() {

        double result = 0.0;
        if (spiders == null) {
            return result;
        }

        Iterator<ConcreteSpider> sIt = spiders.iterator();
        while (sIt.hasNext()) {
            ConcreteSpider s = sIt.next();
            if (DEB.level >= 2) {
                System.out.println("build checksum for spider\n");
            }
            result += s.checksum();
            result *= 1.2;
        }
        return result;
    }

    public ArrayList<ConcreteZone> getShadedZones() {
        return shadedZones;
    }

    public ArrayList<ConcreteZone> getUnshadedZones() {
        return unshadedZones;
    }

    public Rectangle2D.Double getBox() {
        return box;
    }

    /**
     * This can be used to obtain a drawing of an abstract diagram.
     *
     * @param ad the description to be drawn
     * @param size the size of the drawing panel
     * @return
     * @throws CannotDrawException
     */
    public static ConcreteDiagram makeConcreteDiagram(AbstractDescription ad, int size) throws CannotDrawException {
        // TODO
        if (!ad.checks_ok()) {
            // not drawable
            throw new CannotDrawException("badly formed diagram spec");
        }
        DiagramCreator dc = new DiagramCreator(ad);
        ConcreteDiagram cd = dc.createDiagram(size);
        return cd;
    }

/*
 *     public static void main(String[] args) {
        //DEB.level = 3;
        AbstractDescription ad = AbstractDescription.makeForTesting("a ab b c",
                true); // randomised shading

        String failuremessage = "no failure";
        ConcreteDiagram cd = null;
        try {
            cd = ConcreteDiagram.makeConcreteDiagram(ad, 300);
        } catch (CannotDrawException ex) {
            failuremessage = ex.message;
        }

        CirclesPanel cp = new CirclesPanel("a sample CirclesPanel", failuremessage, cd,
                true); // do use colors
        cp.setAutoRescale(true);

        JFrame viewingFrame = new JFrame("frame to hold a CirclesPanel");
        viewingFrame.getContentPane().add(cp);
        viewingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        viewingFrame.pack();
        viewingFrame.setVisible(true);
    }
*/
    
    public ArrayList<ConcreteSpider> getSpiders() {
        return spiders;
    }

    public void setFont(Font f) {
        font = f;
    }

    public Font getFont() {
        return font;
    }

    public int getSize() {
        return (int) Math.ceil(box.height);
    }

    // <editor-fold defaultstate="collapsed" desc="Diagram Element Lookup by Coordinates">
    /**
     * Returns the {@link ConcreteSpiderFoot spider foot} located at the given
     * coordinates. <p>Returns {@code null} if no foot is located on the given
     * coordinates.</p>
     *
     * @param p the coordinates at which to look for a spider's foot. <p>These
     * are the coordinates in the diagram's own local coordinate system. Thus,
     * if you look up elements with a point in the coordinate system of {@link
     * CirclesPanel2 a panel} then you first have to convert the coordinates
     * of the point with {@link
     * CirclesPanel2#toDiagramCoordinates(java.awt.Point)} and then use the
     * resulting point as an argument to this method.</p>
     * @return the {@link ConcreteSpiderFoot spider foot} located at the given
     * coordinates. <p>Returns {@code null} if no foot is located on the given
     * coordinates.</p>
     */
    public ConcreteSpiderFoot getSpiderFootAtPoint(Point p) {
        if (getSpiders() != null) {
            for (ConcreteSpider s : getSpiders()) {
                for (ConcreteSpiderFoot f : s.feet) {
                    double dist = Math.sqrt((p.x - f.getX()) * (p.x - f.getX())
                            + (p.y - f.getY()) * (p.y - f.getY()));
                    if (dist < ConcreteSpiderFoot.FOOT_RADIUS + 2) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Does same as {@link ConcreteDiagram#getSpiderFootAtPoint(java.awt.Point)},
     * however, the hit-test is performed by scaling the distance with the
     * scaling factor first.
     *
     * @param p the coordinates at which to look for a spider's foot. <p>These
     * are the coordinates in the diagram's own local coordinate system. Thus,
     * if you look up elements with a point in the coordinate system of {@link
     * CirclesPanel2 a panel} then you first have to convert the coordinates
     * of the point with {@link
     * CirclesPanel2#toDiagramCoordinates(java.awt.Point)} and then use the
     * resulting point as an argument to this method.</p>
     * @param scaleFactor the scale factor with which to multiply the distance
     * between the given point and particular spiders.
     * @return the {@link ConcreteSpiderFoot spider foot} located at the given
     * coordinates. <p>Returns {@code null} if no foot is located on the given
     * coordinates.</p>
     */
    public ConcreteSpiderFoot getSpiderFootAtPoint(Point p, double scaleFactor) {
        final double threshold = (ConcreteSpiderFoot.FOOT_RADIUS + 2)/scaleFactor;
        if (getSpiders() != null) {
            for (ConcreteSpider s : getSpiders()) {
                for (ConcreteSpiderFoot f : s.feet) {
                    double dist = Math.sqrt((p.x - f.getX()) * (p.x - f.getX())
                            + (p.y - f.getY()) * (p.y - f.getY()));
                    if (dist < threshold) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link CircleContour circle contour} that is located in the
     * <span style="font-style:italic;">vicinity</span> of the given point.
     * <p>The vicinity is dependent upon the given {@code tolerance}.</p>
     *
     * @param p the coordinates at which to look for a circle contour. <p>These
     * are the coordinates in the diagram's own local coordinate system. Thus,
     * if you look up elements with a point in the coordinate system of {@link
     * CirclesPanel2 a panel} then you first have to convert the coordinates
     * of the point with {@link
     * CirclesPanel2#toDiagramCoordinates(java.awt.Point)} and then use the
     * resulting point as an argument to this method.</p>
     * @param tolerance the distance from the contour which is still considered
     * a hit.
     * @return the {@link CircleContour circle contour} that is located <span
     * style="font-style:italic;">near</span> the given point. <p>Returns {@code
     * null} if no circle contour is located near the given coordinates.</p>
     */
    public CircleContour getCircleContourAtPoint(Point p, double tolerance) {
        if (getCircles() != null) {
            for (CircleContour cc : getCircles()) {
                double dist = Math.sqrt((p.x - cc.get_cx()) * (p.x - cc.get_cx())
                        + (p.y - cc.get_cy()) * (p.y - cc.get_cy()));
                if (dist > cc.get_radius() - tolerance && dist < cc.get_radius() + tolerance) {
                    return cc;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link ConcreteZone zone} that contains the given point.
     * @param p the coordinates at which to look for the zone. <p>These
     * are the coordinates in the diagram's own local coordinate system. Thus,
     * if you look up elements with a point in the coordinate system of {@link
     * CirclesPanel2 a panel} then you first have to convert the coordinates
     * of the point with {@link
     * CirclesPanel2#toDiagramCoordinates(java.awt.Point)} and then use the
     * resulting point as an argument to this method.</p>
     * @return the {@link ConcreteZone zone} that contains the given point.
     * <p>Returns {@code null} if no zone is located at the given
     * coordinates.</p>
     */
    public ConcreteZone getZoneAtPoint(Point p) {
        for (ConcreteZone zone : this.unshadedZones) {
            if (zone.getShape(box).contains(p)) {
                return zone;
            }
        }
        for (ConcreteZone zone : this.shadedZones) {
            if (zone.getShape(box).contains(p)) {
                return zone;
            }
        }
        return null;
    }
    // </editor-fold>
}
