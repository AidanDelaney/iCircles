package icircles.gui;

import icircles.concreteDiagram.*;

import java.awt.Color;
import java.util.List;
import java.awt.geom.*;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

public class CirclesSVGGenerator {

    public static abstract class CircleSVGDrawer {
        /**
         * Converts an awt Area to a String representing an SVG path.
         * 
         * @param a
         *            The Area to convert to an SVG path.
         * @returns An SVG specification of the passed in Area.
         */
        protected static String toSVGPath(Area a) {
            StringBuilder sb = new StringBuilder();

            PathIterator it = a.getPathIterator(null);
            if (null == it) {
                return new String();
            }

            // PathIterator is not a normal Java Iterator
            while (!it.isDone()) {
                double[] c = new double[6];

                switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO:
                    sb.append(String.format("M%.2f,%.2f ", c[0], c[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    sb.append(String.format("L%.2f,%.2f ", c[0], c[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    sb.append(String.format("Q%.2f,%.2f,%.2f,%.2f ", c[0],
                            c[1], c[2], c[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    sb.append(String.format("C%.2f,%.2f,%.2f,%.2f,%.2f,%.2f ",
                            c[0], c[1], c[2], c[3], c[4], c[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    sb.append("Z");
                    break;
                }

                // update
                it.next();
            }
            return sb.toString();
        }

        /**
         * Converts a Java Color into a HTML color code.
         * 
         * @param c
         *            The Java Color to convert.
         * @returns A HTML color code as a string prefixed with a '#' symbol.
         */
        protected static String toHexString(Color c) {
            StringBuilder sb = new StringBuilder('#');

            if (c.getRed() < 16)
                sb.append('0');
            sb.append(Integer.toHexString(c.getRed()));

            if (c.getGreen() < 16)
                sb.append('0');
            sb.append(Integer.toHexString(c.getGreen()));

            if (c.getBlue() < 16)
                sb.append('0');
            sb.append(Integer.toHexString(c.getBlue()));

            return sb.toString();
        }

        public abstract SVGDocument toSVG(ConcreteDiagram cd);
    }

    public static class PlainCircleSVGDrawer extends CircleSVGDrawer {
        /**
         * Draws a concreteDiagram as an SVG.
         * 
         * This approach is wholly declarative. It currently knows nothing about
         * the on screen rendering of the diagram. To make decisions based on
         * the on screen rendering (such as better label placement) we will, in
         * future, have to build a GVT (from the Batik library) of the
         * SVGDocument.
         * 
         * @returns An SVGDocument DOM structure representing the SVG.
         */
        @Override
        public SVGDocument toSVG(ConcreteDiagram cd) {
            // Get a DOMImplementation.
            DOMImplementation domImpl = SVGDOMImplementation
                    .getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
            SVGDocument document = (SVGDocument) domImpl.createDocument(svgNS,
                    "svg", null);

            // Get the root element (the 'svg' element).
            Element svgRoot = document.getDocumentElement();

            // Set the width and height attributes on the root 'svg' element.
            svgRoot.setAttributeNS(null, "width",
                    Integer.toString(cd.getSize()));
            svgRoot.setAttributeNS(null, "height",
                    Integer.toString(cd.getSize()));

            // Draw the shaded zones
            for (ConcreteZone z : cd.getShadedZones()) {
                Element path = document.createElementNS(svgNS, "path");
                path.setAttributeNS(null, "d",
                        toSVGPath(z.getShape(cd.getBox())));
                path.setAttributeNS(null, "fill", "#cccccc"); // grey
                path.setAttributeNS(null, "z-index",
                        Integer.toString(zOrder.SHADING.ordinal()));

                svgRoot.appendChild(path);
            }

            // TODO: Concrete* should return themselves as DocumentFragments
            for (CircleContour c : cd.getCircles()) {
                // Draw the circle
                Element circle = document.createElementNS(svgNS, "circle");
                circle.setAttributeNS(null, "cx", Double.toString(c.get_cx()));
                circle.setAttributeNS(null, "cy", Double.toString(c.get_cy()));
                circle.setAttributeNS(null, "r",
                        Double.toString(c.get_radius()));
                circle.setAttributeNS(null, "z-index",
                        Integer.toString(zOrder.CONTOUR.ordinal()));
                // Not pretty, but it works.
                Color strokeColor = c.color();
                circle.setAttributeNS(
                        null,
                        "stroke",
                        (null == strokeColor) ? "black" : "#"
                                + toHexString(c.color()));
                circle.setAttributeNS(null, "stroke-width", "2");
                circle.setAttributeNS(null, "fill", "none");
                svgRoot.appendChild(circle);

                // TODO: Put this text in a path around the circle
                // alternatively come up with some better label placement
                // algorithm
                Element text = document.createElementNS(svgNS, "text");
                text.setAttributeNS(null, "x", Double.toString(c.get_cx()));
                text.setAttributeNS(null, "y",
                        Double.toString(c.get_cy() + c.get_radius()));
                text.setAttributeNS(null, "text-anchor", "middle");
                text.setAttributeNS(
                        null,
                        "fill",
                        (null == strokeColor) ? "black" : "#"
                                + toHexString(c.color()));
                text.setAttributeNS(null, "z-index",
                        Integer.toString(zOrder.LABEL.ordinal()));

                Text textNode = document.createTextNode(c.ac.getLabel()
                        .getLabel());
                text.appendChild(textNode);
                svgRoot.appendChild(text);
            }

            for (ConcreteSpider cs : cd.getSpiders()) {
                for (ConcreteSpiderFoot f : cs.feet) {
                    // Draw the foot
                    Element circle = document.createElementNS(svgNS, "circle");
                    circle.setAttributeNS(null, "cx", Double.toString(f.getX()));
                    circle.setAttributeNS(null, "cy", Double.toString(f.getY()));
                    circle.setAttributeNS(null, "r",
                            Double.toString(ConcreteSpiderFoot.FOOT_RADIUS));
                    circle.setAttributeNS(null, "z-index",
                            Integer.toString(zOrder.SPIDER.ordinal()));

                    circle.setAttributeNS(null, "stroke", "black");
                    circle.setAttributeNS(null, "stroke-width", "2");
                    circle.setAttributeNS(null, "fill", "black");
                    svgRoot.appendChild(circle);
                }

                for (ConcreteSpiderLeg l : cs.legs) {
                    Element line = document.createElementNS(svgNS, "line");
                    line.setAttributeNS(null, "x1",
                            Double.toString(l.from.getX()));
                    line.setAttributeNS(null, "y1",
                            Double.toString(l.from.getY()));
                    line.setAttributeNS(null, "x2",
                            Double.toString(l.to.getX()));
                    line.setAttributeNS(null, "y2",
                            Double.toString(l.to.getY()));
                    line.setAttributeNS(null, "z-index",
                            Integer.toString(zOrder.SPIDER.ordinal()));

                    line.setAttributeNS(null, "stroke", "black");
                    line.setAttributeNS(null, "stroke-width", "2");
                    line.setAttributeNS(null, "fill", "black");
                    svgRoot.appendChild(line);
                }
            }
            return document;
        }
    }

    public static class SketchCirclesSVGDrawer extends CircleSVGDrawer {
        /**
         * The DOM API states that NodeLists are dynamic, this means that when
         * we replace a circle in the NodeList with a group of circles, then the
         * original NodeList updates itself to contain the newly added circles.
         * As we want to replace all circles with a group that possibly contains
         * circles, then we need to turn the NodeList into a non-dynamic
         * structure.
         * @param nl
         * @return Either null if the input parameter is null, or a List
         * representation of the NodeList.
         */
        private List<Node> nodeListToList(NodeList nl) {
            if(null == nl) {
                return null;
            }

            List <Node> list = new ArrayList<Node>();
            for(int i = 1; i < nl.getLength(); i++) {
                list.add(nl.item(i));
            }
            return list;
        }

        /**
         * Given a circle defined by an (x, y) centre point and a radius, we
         * return a List of Cubic Bezier curves that <emph>almost</emph> fit the
         * circle.  Rather than exactly fitting the circle, the returned curves
         * represent a path that is akin to a user sketch of the circle.  In
         * particular the first point of the first stroke and the last point of
         * the last stroke are offset such that they do not join and, ideally, 
         * even cross each other.
         * 
         * TODO: Add randomness to magicDelta so that the sketch is more random
         * looking.
         * 
         * @param circleX
         * @param circleY
         * @param circleR
         * @return
         */
        private List<CubicCurve2D.Float> circleToPath(float circleX, float circleY, float circleR) {
            List<CubicCurve2D.Float> path = new ArrayList<CubicCurve2D.Float>();

            // magic number pulled from thin air
            final float magicDelta = 10;
            path.add(new CubicCurve2D.Float(circleX - magicDelta,
                    (circleY - circleR) - magicDelta,           // x1, y1
                    circleX + (circleR / 2), circleY - circleR, // control point 1
                    circleX + circleR, circleY - (circleR / 2), // control point 2
                    circleX + circleR, circleY // to (x2, y2)
            ));

            path.add(new CubicCurve2D.Float(circleX + circleR, circleY, circleX
                    + circleR, circleY + (circleR / 2),
                    circleX + (circleR / 2), circleY + circleR, circleX,
                    circleY + circleR));

            path.add(new CubicCurve2D.Float(circleX, circleY + circleR, circleX
                    - (circleR / 2), circleY + circleR, circleX - circleR,
                    circleY + (circleR / 2), circleX - circleR, circleY));

            path.add(new CubicCurve2D.Float(circleX - circleR, circleY, circleX
                    - circleR, circleY - (circleR / 2), circleX, circleY
                    - circleR, circleX + magicDelta, (circleY - circleR)
                    - magicDelta));

            return path;
        }

        static private Point2D evalParametric(CubicCurve2D curve, double t) {
            if (null == curve) {
                return null;
            }

            // B(t) = (1-t)^3 P_0 + 3(1-t)^2t C_1 + 3(1 - t)t^2 C_2 + t^3 P_1
            // do nothing fancy, just calculate it.
            double rx = ((Math.pow((1 - t), 3) * curve.getX1()))
                    + (3 * Math.pow((1 - t), 2) * t * curve.getCtrlX1())
                    + (3 * (1 - t) * t * t * curve.getCtrlX2())
                    + (t * t * t * curve.getX2());
            double ry = ((Math.pow((1 - t), 3) * curve.getY1()))
                    + (3 * Math.pow((1 - t), 2) * t * curve.getCtrlY1())
                    + (3 * (1 - t) * t * t * curve.getCtrlY2())
                    + (t * t * t * curve.getY2());

            return new Point2D.Float((float) rx, (float) ry);
        }

        static private Point2D evalParametricTangent(CubicCurve2D curve, double t) {
            if(null == curve) {
                return null;
            }

            // B'(t) = 3(1-t)^2 P_0 + 3((1-t)^2 - 2t(1-t)) C_1
            //                      + 3(2t(1-t)-t^2) C_2
            //                      + 3t^2 P_1
            // Calculate the x and y values at t of the derivative of the cubic
            // Bezier.
            double rx = ((3 - (6 * t) + (3 * t * t)) * curve.getX1())
                    + ((9 - (24 * t) + (15 * t * t)) * curve.getCtrlX1())
                    + (((6 * t) - (9 * t * t)) * curve.getCtrlX2())
                    + ((3 * t * t) * curve.getX2());
            double ry = ((3 - (6 * t) + (3 * t * t)) * curve.getY1())
                    + ((9 - (24 * t) + (15 * t * t)) * curve.getCtrlY1())
                    + (((6 * t) - (9 * t * t)) * curve.getCtrlY2())
                    + ((3 * t * t) * curve.getY2());

            return new Point2D.Float((float) rx, (float) ry);
        }

        /**
         * Calculates the unit normal of a particular vector, where the vector
         * is represented by the direction and magnitude of the line segment
         * from (0,0) to (p.x, p.y).
         * @param p
         * @return
         */
        static private Point2D vectorUnitNormal(Point2D p) {
            // if null object passed or if the passed vector has zero length
            if(null == p || ((0 == p.getX()) && (0 == p.getY()))) {
                return null;
            }

            // normalise the input "vector"
            // 1. get length of vector (Pythagoras)
            // 2. divide both x and y by this length
            // note: c cannot be 0 as we've already considered zero length input
            // vectors above.
            double c   = Math.sqrt((p.getX() * p.getX()) + (p.getY() * p.getY()));
            double nvx = p.getX() / c;
            double nvy = p.getY() / c;

            // Now rotate (nvx, nvy) by 90 degrees to get the normal for the
            // input vector.
            // rx = nvx * cos (pi/2) - nvy * sin (pi/2)
            // ty = nvx * sin (pi/2) + nvy * cos (pi/2)
            // but cos (pi/2) = 0 and sin (pi/2) = 1, so this simplifies
            return new Point2D.Float((float) (-1 * nvy), (float) nvx);
        }

        /**
         * Turns a List<CubicCurve2D.Float> into a SVG Element representing a
         * sketch of that spline.
         * 
         */
        private Element splineToSketch(SVGDocument document, List<CubicCurve2D.Float> spline) {
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

            // <g> is an SVG group
            // TODO: add a random(ish) rotation to the group
            Element group = document.createElementNS(svgNS, "g"); 

            // For each curve in the path, draw along it using a "brush".  In
            // our case the brush is a simple circle, but this could be changed
            // to something more advanced.
            for (CubicCurve2D.Float curve : spline) {
             // TODO: magic number & step in loop guard
                for (double i = 0.0; i <= 1.0; i += 0.01) { 
                    Point2D result = evalParametric(curve, i);

                    // Add random jitter at some random positive or negative 
                    // distance along the unit normal to the tangent of the 
                    // curve
                    Point2D n = vectorUnitNormal(evalParametricTangent(curve, i));
                    float dx = (float) ((Math.random() - 0.5) * n.getX());
                    float dy = (float) ((Math.random() - 0.5) * n.getY());

                    Element brush = document.createElementNS(svgNS, "circle");
                    brush.setAttribute("cx",
                            Double.toString(result.getX() + dx));
                    brush.setAttribute("cy",
                            Double.toString(result.getY() + dy));
                    // TODO: magic number for circle radius
                    brush.setAttribute("r", Double.toString(1.0));
                    brush.setAttribute("fill", "green");
                    brush.setAttributeNS(null, "z-index",
                            Integer.toString(zOrder.CONTOUR.ordinal()));
                    group.appendChild(brush);
                }
            }

            return group;
        }
        /**
         * Creates a new group in the given document where the new group is a
         * collection of "brush strokes" that represent a "sketch" of the passed
         * circle.  In our case the brush strokes are simple circles.
         * @param document
         * @param circle
         * @return
         */
        private Element circleToSketch(SVGDocument document, SVGCircleElement circle) {
            // Turn the circle into a path

            // We start the path at the maximal x and y point
            float circleX = circle.getCx().getAnimVal().getValue();
            float circleY = circle.getCy().getAnimVal().getValue();
            float circleR = circle.getR().getAnimVal().getValue();

            List<CubicCurve2D.Float> spline = circleToPath(circleX, circleY,
                    circleR);

            return splineToSketch(document, spline);
        }

        @Override
        public SVGDocument toSVG(ConcreteDiagram cd) {
            /*
             * Use a Plain drawer to generate an SVG Document, then
             * "post-process" any SVG circles in the document to convert them
             * into "sketches".
             */
            SVGDocument document = (new PlainCircleSVGDrawer()).toSVG(cd);
            // return document;

            // find each circle in the document and turn it into a sketch. We
            // need to keep track of the circles and their eventual replacements
            // as each circle is replaced by 10's of smaller circles, thus the
            // DOM updates and we get the 10's of circles in our NodeList circles.
            NodeList   circles     = document.getElementsByTagName("circle");
            List<Node> replaceable = nodeListToList(circles);
            for(Node n : replaceable) {
                Node circleAsSketch = circleToSketch(document, (SVGCircleElement) n);
                n.getParentNode().replaceChild(circleAsSketch, n);
            }

            return document;
        }
    }

    static enum zOrder {
        SHADING, CONTOUR, LABEL, SPIDER
    };

    private ConcreteDiagram diagram;
    private CircleSVGDrawer drawer;

    /**
     * 
     * @throws IllegalArgumentException
     */
    public CirclesSVGGenerator(ConcreteDiagram d) {
        if (null == d) {
            throw new IllegalArgumentException("ConcreteDiagram is null");
        }
        diagram = d;
        drawer = new PlainCircleSVGDrawer();
    }

    public CirclesSVGGenerator(ConcreteDiagram d, CircleSVGDrawer drawer) {
        this(d);
        this.drawer = drawer;
    }

    @Override
    public String toString() {
        Document document = drawer.toSVG(diagram);

        // Use the old transformer method as we cannot be guaranteed that
        // the underlying JDK supports DOM level 3.
        try {
            Source source = new DOMSource(document.getDocumentElement());
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SVGDocument toSVG() {
        return drawer.toSVG(diagram);
    }
}