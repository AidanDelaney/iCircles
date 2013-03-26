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

        private List<CubicCurve2D.Float> circleToPath(float circleX,
                float circleY, float circleR) {
            List<CubicCurve2D.Float> path = new ArrayList<CubicCurve2D.Float>();

            // magic number pulled from thin air
            final float magicDelta = 10;
            path.add(new CubicCurve2D.Float(circleX - magicDelta,
                    (circleY - circleR) - magicDelta, // x1, y1
                    circleX + (circleR / 2), circleY - circleR, // control point
                                                                // 1
                    circleX + circleR, circleY - (circleR / 2), // control point
                                                                // 2
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

        private Element circleToSVGPath(SVGDocument document,
                SVGCircleElement circle) {
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

            // Turn the circle into a path

            // We start the path at the maximal x and y point
            float circleX = circle.getCx().getAnimVal().getValue();
            float circleY = circle.getCy().getAnimVal().getValue();
            float circleR = circle.getR().getAnimVal().getValue();

            List<CubicCurve2D.Float> path = circleToPath(circleX, circleY,
                    circleR);

            Element group = document.createElementNS(svgNS, "g"); // <g> is an
                                                                  // SVG group
            for (CubicCurve2D.Float curve : path) {
                StringBuilder strpath = new StringBuilder();
                strpath.append("M " + curve.getX1() + ", " + curve.getY1()
                        + " ");

                strpath.append("C" + curve.getCtrlX1() + ", "
                        + curve.getCtrlY1() + " " + curve.getCtrlX2() + ", "
                        + curve.getCtrlY2() + " " + curve.getX2() + ", "
                        + curve.getY2());
                Element svgpath = document.createElementNS(svgNS, "path");
                svgpath.setAttribute("stroke", "green");
                svgpath.setAttribute("stroke-width", "2");
                svgpath.setAttribute("fill", "none");
                svgpath.setAttribute("d", strpath.toString());
                group.appendChild(svgpath);

                for (double i = 0.0; i <= 1.0; i += 0.01) { // TODO: magic
                                                            // number in loop
                                                            // guard
                    Point2D result = evalParametric(curve, i);

                    // Add random jitter between -1.0 and + 1.0
                    float dx = (float) ((Math.random() * 2.0) - 1.0);
                    float dy = (float) ((Math.random() * 2.0) - 1.0);

                    Element brush = document.createElementNS(svgNS, "circle");
                    brush.setAttribute("cx",
                            Double.toString(result.getX() + dx));
                    brush.setAttribute("cy",
                            Double.toString(result.getY() + dy));
                    brush.setAttribute("r", Double.toString(2.0)); // TODO:
                                                                   // magic
                                                                   // number
                    brush.setAttribute("fill", "green");
                    group.appendChild(brush);
                }
            }

            return group;
        }

        private Node pathToSketchPath(Element group) {
            return group;
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

            // find each circle in the document and turn it into a sketch
            NodeList circles = document.getElementsByTagName("circle");
            // for(int i = 0; i < circles.getLength(); i++) {
            SVGCircleElement circle = (SVGCircleElement) circles.item(0);// i);
            Node circleAsSketch = pathToSketchPath(circleToSVGPath(document,
                    circle));
            circle.getParentNode().replaceChild(circleAsSketch, circle);
            // }

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

    static public Point2D evalParametric(CubicCurve2D curve, double t) {
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
}