package icircles.concreteDiagram;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import icircles.abstractDescription.AbstractBasicRegion;
import icircles.abstractDescription.AbstractCurve;
import icircles.abstractDescription.AbstractDescription;
import icircles.concreteDiagram.BuildStep.Piercing;
import icircles.recomposition.RecompData;
import icircles.recomposition.RecompositionStep;
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

    /**
     * Checks to see if any of the subsequent build steps will cause a piercing
     * of the passed in AbstractCurve.
     * @param bs
     * @return
     */
    private static boolean willPierce(BuildStep bs, AbstractCurve ac) {
        // look ahead - are we going to add a piercing to this?
        // if so, push it to one side to make space
        BuildStep future_bs = bs.next;
        while (future_bs != null) {
            if (future_bs.getType() == Piercing.ONE_PIERCING) {
                AbstractBasicRegion abr0 = future_bs.recomp_data
                        .get(0).split_zones.get(0);
                AbstractBasicRegion abr1 = future_bs.recomp_data
                        .get(0).split_zones.get(1);
                AbstractCurve ac_future = abr0
                        .getStraddledContour(abr1);
                if (ac_future == ac) {
                    return true;
                }
            }
            future_bs = future_bs.next;
        }
        return false;
    }

    private static ArrayList<CircleContour> placeContours(Rectangle2D.Double outerBox,
            int smallestRadius, double guideRadius, AbstractBasicRegion zone,
            AbstractDescription lastDiagram,
            ArrayList<AbstractCurve> abstractCurves,
            HashMap<AbstractCurve, CircleContour> abstractToConcreteContourMap,
            ArrayList<CircleContour> drawnCircles)
            throws CannotDrawException {
        ArrayList<CircleContour> result = new ArrayList<CircleContour>();

        // special case : handle the drawing if it's the first contour(s)
        boolean is_first_contour = !abstractToConcreteContourMap.keySet()
                .iterator().hasNext();
        if (is_first_contour) {
            int label_index = 0;
            for (AbstractCurve ac : abstractCurves) {
                result.add(new CircleContour(outerBox.getCenterX() - 0.5
                        * (guideRadius * 3 * abstractCurves.size()) + 1.5
                        * guideRadius + guideRadius * 3 * label_index, outerBox
                        .getCenterY(), guideRadius, ac));
                label_index++;
            }
            DEB.out(2, "added first contours into diagram, labelled "
                    + abstractCurves.get(0).getLabel());
            return result;
        }

        // general case : it's (they're) not our first contour
        if (zone.getNumContours() == 0) {
            // adding contour(s) outside everything else
            double minx = Double.MAX_VALUE;
            double maxx = Double.MIN_VALUE;
            double miny = Double.MAX_VALUE;
            double maxy = Double.MIN_VALUE;

            for (CircleContour c : drawnCircles) {
                if (c.getMinX() < minx) {
                    minx = c.getMinX();
                }
                if (c.getMaxX() > maxx) {
                    maxx = c.getMaxX();
                }
                if (c.getMinY() < miny) {
                    miny = c.getMinY();
                }
                if (c.getMaxY() > maxy) {
                    maxy = c.getMaxY();
                }
            }
            if (abstractCurves.size() == 1) {
                if (maxx - minx < maxy - miny) {// R
                    result.add(new CircleContour(maxx + guideRadius * 1.5,
                            (miny + maxy) * 0.5, guideRadius, abstractCurves
                                    .get(0)));
                } else {// B
                    result.add(new CircleContour((minx + maxx) * 0.5, maxy
                            + guideRadius * 1.5, guideRadius, abstractCurves
                            .get(0)));
                }
            } else if (abstractCurves.size() == 2) {
                if (maxx - minx < maxy - miny) {// R
                    result.add(new CircleContour(maxx + guideRadius * 1.5,
                            (miny + maxy) * 0.5, guideRadius, abstractCurves
                                    .get(0)));
                    result.add(new CircleContour(minx - guideRadius * 1.5,
                            (miny + maxy) * 0.5, guideRadius, abstractCurves
                                    .get(1)));
                } else {// T
                    result.add(new CircleContour((minx + maxx) * 0.5, maxy
                            + guideRadius * 1.5, guideRadius, abstractCurves
                            .get(0)));
                    result.add(new CircleContour((minx + maxx) * 0.5, miny
                            - guideRadius * 1.5, guideRadius, abstractCurves
                            .get(1)));
                }
            } else {
                if (maxx - minx < maxy - miny) {// R
                    double lowy = (miny + maxy) * 0.5 - 0.5
                            * abstractCurves.size() * guideRadius * 3
                            + guideRadius * 1.5;
                    for (int i = 0; i < abstractCurves.size(); i++) {
                        result.add(new CircleContour(maxx + guideRadius * 1.5,
                                lowy + i * 3 * guideRadius, guideRadius,
                                abstractCurves.get(i)));
                    }
                } else {
                    double lowx = (minx + maxx) * 0.5 - 0.5
                            * abstractCurves.size() * guideRadius * 3
                            + guideRadius * 1.5;
                    for (int i = 0; i < abstractCurves.size(); i++) {
                        result.add(new CircleContour(
                                lowx + i * 3 * guideRadius, maxy + guideRadius
                                        * 1.5, guideRadius, abstractCurves
                                        .get(i)));
                    }
                }
            }
        }
        return result;
    }

    /**
     * A wrapper function around placeContours which has an interface for
     * placing just one contour.
     * 
     * @param outerBox
     * @param smallest_rad
     * @param guide_rad
     * @param zone
     * @param last_diag
     * @param ac
     * @param debug_index
     * @return
     * @throws CannotDrawException
     */
    private static CircleContour findCircleContour(Rectangle2D.Double outerBox,
            int smallest_rad, double guide_rad, AbstractBasicRegion zone,
            AbstractDescription last_diag, AbstractCurve ac,
            HashMap<AbstractCurve, CircleContour> abstractToConcreteContourMap,
            ArrayList<CircleContour> drawnCircles)
            throws CannotDrawException {
        ArrayList<AbstractCurve> acs = new ArrayList<AbstractCurve>();
        acs.add(ac);
        ArrayList<CircleContour> result = placeContours(outerBox, smallest_rad,
                guide_rad, zone, last_diag, acs, abstractToConcreteContourMap, drawnCircles);
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Is this circle in this area, including some slop for a gap. Slop is
     * smallestRadius.
     * 
     * @param c
     * @param a
     * @return
     */
    private static boolean circleInArea(CircleContour c, Area a, int smallestRadius) {
        Area test = new Area(c.getFatInterior(smallestRadius));
        test.subtract(a);
        return test.isEmpty();
    }

    public static boolean doNestedPiercing(RecompData rd, HashMap<AbstractCurve, CircleContour> abstractToConcreteContourMap, ArrayList<CircleContour> drawnCircles, int smallestRadius, double suggested_rad, BuildStep buildStepsHead, ArrayList<RecompositionStep> recompSteps) throws CannotDrawException {
        AbstractCurve ac = rd.added_curve;
        Rectangle2D.Double outerBox = CircleContour
                .makeBigOuterBox(drawnCircles);


        if (DEB.level > 3) {
            System.out.println("make a nested contour");
        }
        // make a circle inside containingCircles, outside
        // excludingCirles.

        AbstractBasicRegion zone = rd.split_zones.get(0);

        RecompositionStep last_step = recompSteps.get(recompSteps
                .size() - 1);
        AbstractDescription last_diag = last_step.to();

        // put contour into a zone
        CircleContour c = findCircleContour(outerBox,
                smallestRadius, suggested_rad, zone, last_diag, ac, abstractToConcreteContourMap, drawnCircles);

        if (c == null) {
            throw new CannotDrawException(
                    "cannot place nested contour");
        }

        if (willPierce(buildStepsHead, ac)
                && rd.split_zones.get(0).getNumContours() > 0) {
            // nudge to the left
            c.cx -= c.radius * 0.5;

            ConcreteZone cz = makeConcreteZone(rd.split_zones
                    .get(0), abstractToConcreteContourMap, drawnCircles);
            Area a = new Area(cz.getShape(outerBox));
            if (!circleInArea(c, a, smallestRadius)) {
                c.cx += c.radius * 0.25;
                c.radius *= 0.75;
            }
        }
        abstractToConcreteContourMap.put(ac, c);
        addCircle(drawnCircles, c);

        return true;
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
