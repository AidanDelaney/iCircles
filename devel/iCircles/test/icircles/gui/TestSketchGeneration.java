package icircles.gui;

import icircles.gui.CirclesSVGGenerator.SketchCirclesSVGDrawer;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.junit.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.parser.*;

import static org.junit.Assert.*;

/**
 * Unfortunately much of the sketch output must be visually inspected, so there
 * is little to automatically test.
 * 
 * @author Aidan Delaney <aidan@phoric.eu>
 *
 */
public class TestSketchGeneration {
    static String[] svgPaths = {
        "M 1.0101457,54.331459 C 26.263966,176.55993 46.467016,199.79344 117.1777,212.92542 187.88838,226.0574 251.52799,170.49901 272.74119,156.35688 293.9544,142.21474 291.93409,26.047199 259.60921,17.965979 227.28433,9.8847586 193.94929,-2.2370714 163.64472,5.8441486 133.34014,13.925369 42.426416,15.945679 28.284276,33.118269"
        , "M 92.934027,25.037035 C 35.251895,11.072404 6.5637881,30.716551 13.888377,56.867128 26.203625,100.83558 18.290011,160.24424 62.629463,168.47871 c 33.183641,6.16267 80.972457,17.40066 116.478907,11.75863 32.74144,-5.20267 63.18181,-30.51719 74.43992,-38.0226 10.1935,-6.79566 34.21515,-23.00737 32.8946,-53.606268 -1.42759,-33.079056 -10.04174,-66.444508 -26.83368,-70.642493 -32.32488,-8.08122 -6.06092,42.42641 -36.36549,50.507627 -13.79979,3.679944 -42.18789,0.04217 -68.15136,3.211596 -31.05277,3.790687 -37.28349,-60.041475 -44.98573,-50.688764"
        , "M 34.34519,77.564981 C 5.05076,131.10307 -4.0406095,200.8036 36.36549,214.94573 c 40.4061,14.14214 108.08632,34.34519 142.43151,24.24366 34.34519,-10.10152 73.74114,-17.17259 98.99495,-39.39595 25.25381,-22.22335 48.48732,-70.71067 50.50763,-94.95435 2.0203,-24.243659 13.13198,-45.45686 -14.14214,-64.649759 -27.27412,-19.1929 -53.53808,-39.39595037 -78.7919,-20.20305 -25.25381,19.1929 -53.53808,23.23351 -54.54823,44.446709 -1.01016,21.213211 42.4264,40.4061 14.14213,70.71069 -28.28427,30.30458 -60.60915,32.32488 -75.76144,7.07107 -15.15229,-25.25381 4.04061,-53.538099 -30.30458,-69.700539 -34.34518,-16.162441 -46.46701,-2.0203 -49.49747,0"
        , "m 74.690964,237.76193 c 0,20.62534 -16.720142,37.34548 -37.345482,37.34548 C 16.720142,275.10741 0,258.38727 0,237.76193 c 0,-20.62534 16.720142,-37.34548 37.345482,-37.34548 20.62534,0 37.345482,16.72014 37.345482,37.34548"
    };
    static List<List<CubicCurve2D.Float>> splines   = new ArrayList<List<CubicCurve2D.Float>>();
    static float                          waggle    = 10.0f;
    static int                            subdivide = 1;

    private static List<CubicCurve2D.Float> subdivide(List<CubicCurve2D.Float> spline) {
        if(null == spline || spline.isEmpty()) {
            return null;
        }

        List<CubicCurve2D.Float> ret   = new ArrayList<CubicCurve2D.Float>();
        for(CubicCurve2D.Float curve: spline) {
            CubicCurve2D.Float left = new CubicCurve2D.Float()
                               , right = new CubicCurve2D.Float();
            curve.subdivide(left, right);
            ret.add(left);
            ret.add(right);
        }
        return ret;
    }

    private static List<CubicCurve2D.Float> waggle(List<CubicCurve2D.Float> spline) {
        if(null == spline || spline.isEmpty()) {
            return null;
        }

        List<CubicCurve2D.Float> ret = new ArrayList<CubicCurve2D.Float>();

        Point2D.Float p1 = (Point2D.Float) spline.get(0).getP1();
        for(CubicCurve2D.Float curve : spline) {
            float dx = (float) (Math.random() * waggle) - (waggle/2.0f);
            float dy = (float) (Math.random() * waggle) - (waggle/2.0f);

            // Set the start of this curve to being the end of the previous
            // curve.  Offset the end of this curve and set it as the start of
            // the next curve.
            ret.add(new CubicCurve2D.Float(p1.x, p1.y
                                          , curve.ctrlx1, curve.ctrly1
                                          , curve.ctrlx2, curve.ctrly2
                                          , curve.x2 + dx, curve.y2 + dy));
            p1.setLocation(curve.x2 + dx, curve.y2 + dy);
        }
        return ret;
    }

    private static Point2D.Float svgPointToPoint2D(String point) {
        // No error checking

        String[] tokens = point.split(",");
        return new Point2D.Float((new Float(tokens[0])).floatValue(),
                (new Float(tokens[1])).floatValue());
    }

    private static List<CubicCurve2D.Float> svgPathToSpline(String svgPath) {
        ListCubicCurve2DPathHandler lcc2dph = new ListCubicCurve2DPathHandler();
        PathParser pp = new PathParser();
        pp.setPathHandler(lcc2dph);
        pp.parse(svgPath);

        List<CubicCurve2D.Float> spline = lcc2dph.spline;
        return spline;
    }

    @BeforeClass
    public static void setUp() {
        for(String svgPath: svgPaths) {
            splines.add(svgPathToSpline(svgPath));
        }
    }

    public Element splineToSVGPath(Document document, List<CubicCurve2D.Float> spline) {
     // <g> is an SVG group
        // TODO: add a random(ish) rotation to the group
        Element path = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path"); 

        String d = "M " + spline.get(0).x1 + "," + spline.get(0).y1 +" C ";
        for (CubicCurve2D.Float curve : spline) {
            d += " " + curve.ctrlx1 + ", " + curve.ctrly1 
                    + " " + curve.ctrlx2 + ", "  + curve.ctrly2
                    + " " + curve.x2 + ", " + curve.y2;
        }
        path.setAttribute("fill", "none");
        path.setAttribute("stroke", "green");
        path.setAttribute("d", d);
        return path;
   
    }

    @Test
    public void testSketchContour() throws TransformerFactoryConfigurationError, TransformerException {
        CirclesSVGGenerator.SketchCirclesSVGDrawer scsd = new SketchCirclesSVGDrawer();

        // Get a DOMImplementation.
        DOMImplementation domImpl = SVGDOMImplementation
                .getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument document = (SVGDocument) domImpl.createDocument(svgNS,
                "svg", null);

        // Get the root element (the 'svg' element).
        Element svgRoot = document.getDocumentElement();

        for(List<CubicCurve2D.Float> spline: splines) {

            // subdivide the spline
            for(int i = 0; i< subdivide; i++) {
                spline = subdivide(spline);
            }
            // introduce waggle
            spline = waggle(spline);

            Node path = splineToSVGPath(document, spline);
            svgRoot.appendChild(path);

            // Output to file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(spline.hashCode() + "-" + waggle + "-" + subdivide + ".svg"));
            Source input = new DOMSource(document);

            transformer.transform(input, output);

            // remove the node
            svgRoot.removeChild(path);
        }
    }

    @Test
    public void testSketches ()  throws TransformerFactoryConfigurationError, TransformerException {
        // setting globals ain't cool.
        for(float i = 0.0f; i <= 100.0f; i+=10.0f) {
            waggle = i;
            for(int j = 0; j <= 5; j++) {
                subdivide = j;
                testSketchContour();
            }
        }
    }
}
