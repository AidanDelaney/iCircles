package icircles.concreteDiagram;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import icircles.abstractDescription.AbstractBasicRegion;
import icircles.abstractDescription.AbstractCurve;
import icircles.recomposition.RecompData;
import icircles.util.CannotDrawException;
import icircles.util.DEB;

public class PiercingDrawer {

    /**
     * Find two points where two circles meet (null if they don't, equal points
     * if they just touch)
     * 
     * @param c1x
     * @param c1y
     * @param rad1
     * @param c2x
     * @param c2y
     * @param rad2
     * @return
     */
    private static double[][] intersectCircles(double c1x, double c1y, double rad1,
            double c2x, double c2y, double rad2) {

        double ret[][] = new double[2][2];
        double dx = c1x - c2x;
        double dy = c1y - c2y;
        double d2 = dx * dx + dy * dy;
        double d = Math.sqrt(d2);

        if (d > rad1 + rad2 || d < Math.abs(rad1 - rad2)) {
            return null; // no solution
        }

        double a = (rad1 * rad1 - rad2 * rad2 + d2) / (2 * d);
        double h = Math.sqrt(rad1 * rad1 - a * a);
        double x2 = c1x + a * (c2x - c1x) / d;
        double y2 = c1y + a * (c2y - c1y) / d;

        double paX = x2 + h * (c2y - c1y) / d;
        double paY = y2 - h * (c2x - c1x) / d;
        double pbX = x2 - h * (c2y - c1y) / d;
        double pbY = y2 + h * (c2x - c1x) / d;

        ret[0][0] = paX;
        ret[0][1] = paY;
        ret[1][0] = pbX;
        ret[1][1] = pbY;

        return ret;
    }

    /**
     * Given drawnCircles and an AbstractBasicRegion (zone), create a
     * ConcreteZone (which has a shape as well as knowing which contours it
     * belongs to / doesn't belong to). Call this only after createCircles has
     * successfully done its job.
     * 
     * @param z
     * @return
     */
    private static ConcreteZone makeConcreteZone(AbstractBasicRegion z, Map<AbstractCurve, CircleContour> abstractToConcreteContourMap, ArrayList<CircleContour> drawnCircles) {
        ArrayList<CircleContour> includingCircles = new ArrayList<CircleContour>();
        ArrayList<CircleContour> excludingCircles = new ArrayList<CircleContour>(
                drawnCircles);
        Iterator<AbstractCurve> acIt = z.getContourIterator();
        while (acIt.hasNext()) {
            AbstractCurve ac = acIt.next();
            CircleContour containingCC = abstractToConcreteContourMap.get(ac);
            excludingCircles.remove(containingCC);
            includingCircles.add(containingCC);
        }
        ConcreteZone cz = new ConcreteZone(z, includingCircles,
                excludingCircles);
        return cz;
    }

    /**
     * Is this circle in this area, including some slop for a gap. Slop is
     * smallestRadius.
     * 
     * @param c
     * @param a
     * @return
     */
    private static boolean circleInArea(CircleContour c, Area a, double smallestRadius) {
        Area test = new Area(c.getFatInterior(smallestRadius));
        test.subtract(a);
        return test.isEmpty();
    }

    /**
     * Determine a largish radius for a circle centered at given cx, cy which
     * fits inside area a. Return a CircleContour with this centre, radius and
     * labelled according to the AbstractCurve.
     * 
     * @param a
     * @param ac
     * @param centreX
     * @param centreY
     * @param suggestedRadius
     * @param startRadius
     * @param smallestRadius
     * @return
     */
    private static CircleContour growCircleContour(Area a, AbstractCurve ac,
            double centreX, double centreY, double suggestedRadius,
            double startRadius, double smallestRadius) {
        CircleContour attempt = new CircleContour(centreX, centreY,
                suggestedRadius, ac);
        if (circleInArea(attempt, a, smallestRadius)) {
            return new CircleContour(centreX, centreY, suggestedRadius, ac);
        }

        boolean ok = true;
        double good_rad = -1.0;
        double rad = startRadius;
        while (ok) {
            attempt = new CircleContour(centreX, centreY, rad, ac);
            if (circleInArea(attempt, a, smallestRadius)) {
                good_rad = rad;
                rad *= 1.5;
            } else {
                break;
            }
        }// loop for increasing radii
        if (good_rad < 0.0) {
            return null;
        }
        CircleContour sol = new CircleContour(centreX, centreY, good_rad, ac);
        return sol;
    }

    /**
     * Once we have chosen a CircleContour to put in the diagram, call this
     * function to perform the necessary steps. (generate debug, give it a
     * colour, store it as a drawnCircle,...)
     * 
     * @param c
     */
    static void addCircle(ArrayList<CircleContour> drawnCircles, CircleContour c) {
        if (DEB.level > 2) {
            System.out.println("adding " + c.debug());
        }
// TODO: make colour assignments work        assignCircleColour(c);
        drawnCircles.add(c);
    }

    public static boolean doSinglePiercing(RecompData rd, Map<AbstractCurve, CircleContour> abstractToConcreteContourMap, ArrayList<CircleContour> drawnCircles, double smallestRadius, double suggested_rad, GuideSizeStrategy guideSizes) throws CannotDrawException {
        // add a single
        // piercing---------------------------------------------------
        AbstractCurve ac = rd.added_curve;
        Rectangle2D.Double outerBox = CircleContour
                .makeBigOuterBox(drawnCircles);

        if (DEB.level > 3) {
            System.out.println("make a single-piercing contour");
        }
        AbstractBasicRegion abr0 = rd.split_zones.get(0);
        AbstractBasicRegion abr1 = rd.split_zones.get(1);
        AbstractCurve c = abr0.getStraddledContour(abr1);
        CircleContour cc = abstractToConcreteContourMap.get(c);
        ConcreteZone cz0 = makeConcreteZone(abr0, abstractToConcreteContourMap, drawnCircles);
        ConcreteZone cz1 = makeConcreteZone(abr1, abstractToConcreteContourMap, drawnCircles);
        Area a = new Area(cz0.getShape(outerBox));

        /*DEB.show(4, a, "for single piercing first half "
                + debugImageNumber);
        DEB.show(4, new Area(cz1.getShape(outerBox)),
                "for single piercing second half "
                        + debugImageNumber);*/
        a.add(cz1.getShape(outerBox));

        //DEB.show(4, a, "for single piercing " + debugImageNumber);

        // We have made a piercing which is centred on the
        // circumference of circle c.
        // but if the contents of rd.addedCurve are not equally
        // balanced between
        // things inside c and things outside, we may end up
        // squashing lots
        // into half of rd.addedCurve, leaving the other half
        // looking empty.
        // See if we can nudge c outwards or inwards to accommodate
        // its contents.

        // iterate through zoneScores, looking for zones inside c,
        // then ask whether they are inside or outside cc. If we
        // get a big score outside, then try to move c outwards.

        // HashMap<AbstractBasicRegion, Double> zoneScores;
        double score_in_c = 0.0;
        double score_out_of_c = 0.0;

        double center_of_circle_lies_on_rad = cc.radius;
        double smallest_allowed_rad = smallestRadius;

        Set<AbstractBasicRegion> allZones = guideSizes
                .getScoredZones();
        for (AbstractBasicRegion abr : allZones) {
            DEB.out(1,
                    "compare " + abr.debug() + " against "
                            + c.debug());
            if (!abr.is_in(rd.added_curve)) {
                continue;
            }
            DEB.out(1, "OK " + abr.debug() + " is in " + c.debug()
                    + ", so compare against " + cc.debug());
            if (abr.is_in(c)) {
                score_in_c += guideSizes.getGuideSize(abr);
            } else {
                score_out_of_c += guideSizes.getGuideSize(abr);
            }
        }
        DEB.out(3, "scores for " + c + " are inside=" + score_in_c
                + " and outside=" + score_out_of_c);

        if (score_out_of_c > score_in_c) {
            double nudge = suggested_rad * 0.3;
            smallest_allowed_rad += nudge;
            center_of_circle_lies_on_rad += nudge;
        } else if (score_out_of_c < score_in_c) {
            double nudge = Math.min(suggested_rad * 0.3,
                    (cc.radius * 2 - suggested_rad) * 0.5);
            smallest_allowed_rad += nudge;
            center_of_circle_lies_on_rad -= nudge;
        }

        // now place circles around cc, checking whether they fit
        // into a
        CircleContour solution = null;
        for (AngleIterator ai = new AngleIterator(); ai.hasNext();) {
            double angle = ai.nextAngle();
            double x = cc.cx + Math.cos(angle)
                    * center_of_circle_lies_on_rad;
            double y = cc.cy + Math.sin(angle)
                    * center_of_circle_lies_on_rad;
            if (a.contains(x, y)) {
                // how big a circle can we make?
                double start_rad;
                if (solution != null) {
                    start_rad = solution.radius + smallestRadius;
                } else {
                    start_rad = smallestRadius;
                }
                CircleContour attempt = growCircleContour(a,
                        rd.added_curve, x, y, suggested_rad,
                        start_rad, smallest_allowed_rad);
                if (attempt != null) {
                    solution = attempt;
                    if (solution.radius == guideSizes
                            .getGuideSize(ac)) {
                        break; // no need to try any more
                    }
                }

            }// check that the centre is ok
        }// loop for different centre placement
        if (solution == null) // no single piercing found which was
                                // OK
        {
            throw new CannotDrawException("1-peircing no fit");
        } else {
            DEB.out(2, "added a single piercing labelled "
                    + solution.ac.getLabel());
            abstractToConcreteContourMap.put(rd.added_curve,
                    solution);
            addCircle(drawnCircles, solution);
        }
        return true;
    }

    public static boolean doDoublePiercing(RecompData rd, Map<AbstractCurve, CircleContour> abstractToConcreteContourMap, ArrayList<CircleContour> drawnCircles, double smallestRadius, double suggested_rad) throws CannotDrawException {
        // double piercing
        AbstractBasicRegion abr0 = rd.split_zones.get(0);
        AbstractBasicRegion abr1 = rd.split_zones.get(1);
        AbstractBasicRegion abr2 = rd.split_zones.get(2);
        AbstractBasicRegion abr3 = rd.split_zones.get(3);
        AbstractCurve c1 = abr0.getStraddledContour(abr1);
        AbstractCurve c2 = abr0.getStraddledContour(abr2);
        CircleContour cc1 = abstractToConcreteContourMap.get(c1);
        CircleContour cc2 = abstractToConcreteContourMap.get(c2);
        
        Rectangle2D.Double outerBox = CircleContour
                .makeBigOuterBox(drawnCircles);

        double[][] intn_coords = intersectCircles(cc1.cx, cc1.cy,
                cc1.radius, cc2.cx, cc2.cy, cc2.radius);
        if (intn_coords == null) {
            System.out
                    .println("double piercing on non-intersecting circles");
            return false;
        }

        ConcreteZone cz0 = makeConcreteZone(abr0, abstractToConcreteContourMap, drawnCircles);
        ConcreteZone cz1 = makeConcreteZone(abr1, abstractToConcreteContourMap, drawnCircles);
        ConcreteZone cz2 = makeConcreteZone(abr2, abstractToConcreteContourMap, drawnCircles);
        ConcreteZone cz3 = makeConcreteZone(abr3, abstractToConcreteContourMap, drawnCircles);
        Area a = new Area(cz0.getShape(outerBox));
        a.add(cz1.getShape(outerBox));
        a.add(cz2.getShape(outerBox));
        a.add(cz3.getShape(outerBox));

//        DEB.show(4, a, "for double piercing " + debugImageNumber);

        double cx, cy;
        if (a.contains(intn_coords[0][0], intn_coords[0][1])) {
            if (DEB.level > 2) {
                System.out.println("intn at (" + intn_coords[0][0]
                        + "," + intn_coords[0][1] + ")");
            }
            cx = intn_coords[0][0];
            cy = intn_coords[0][1];
        } else if (a.contains(intn_coords[1][0], intn_coords[1][1])) {
            if (DEB.level > 2) {
                System.out.println("intn at (" + intn_coords[1][0]
                        + "," + intn_coords[1][1] + ")");
            }
            cx = intn_coords[1][0];
            cy = intn_coords[1][1];
        } else {
            if (DEB.level > 2) {
                System.out
                        .println("no suitable intn for double piercing");
            }
            throw new CannotDrawException("2peircing + disjoint");
        }

        CircleContour solution = growCircleContour(a,
                rd.added_curve, cx, cy, suggested_rad,
                smallestRadius, smallestRadius);
        if (solution == null) // no double piercing found which was
                                // OK
        {
            throw new CannotDrawException("2peircing no fit");
        } else {
            DEB.out(2, "added a double piercing labelled "
                    + solution.ac.getLabel());
            abstractToConcreteContourMap.put(rd.added_curve,
                    solution);
            addCircle(drawnCircles, solution);
        }

        return true;
    }
}
