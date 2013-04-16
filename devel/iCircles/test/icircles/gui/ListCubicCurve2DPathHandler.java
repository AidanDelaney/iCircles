package icircles.gui;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathHandler;

public class ListCubicCurve2DPathHandler implements PathHandler {
    Point2D.Float p1 = new Point2D.Float(0.0f, 0.0f);
    List<CubicCurve2D.Float> spline;

    public ListCubicCurve2DPathHandler() {
    }

    @Override
    public void startPath() throws ParseException {
        spline = new ArrayList<CubicCurve2D.Float>();
    }

    @Override
    public void endPath() throws ParseException {
    }

    @Override
    public void movetoRel(float x, float y) throws ParseException {
        p1 = new Point2D.Float(p1.x + x, p1.y + y);
    }

    @Override
    public void movetoAbs(float x, float y) throws ParseException {
        p1 = new Point2D.Float(x, y);
    }

    @Override
    public void closePath() throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoRel(float x, float y) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoAbs(float x, float y) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoHorizontalRel(float x) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoHorizontalAbs(float x) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoVerticalRel(float y) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void linetoVerticalAbs(float y) throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoCubicRel(float x1, float y1, float x2, float y2,
            float x, float y) throws ParseException {
        Point2D.Float cp1 = new Point2D.Float(p1.x + x1, p1.y + y1);
        Point2D.Float cp2 = new Point2D.Float(p1.x + x2, p1.y + y2);
        Point2D.Float p2  = new Point2D.Float(p1.x + x, p1.y + y);
        spline.add(new CubicCurve2D.Float(p1.x, p1.y, cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y));
        p1 = p2; // the new start point is the end of the segment that we've
                 // just curved to
    }

    @Override
    public void curvetoCubicAbs(float x1, float y1, float x2, float y2,
            float x, float y) throws ParseException {
        Point2D.Float cp1 = new Point2D.Float(x1, y1);
        Point2D.Float cp2 = new Point2D.Float(x2, y2);
        Point2D.Float p2  = new Point2D.Float(x , y);
        spline.add(new CubicCurve2D.Float(p1.x, p1.y, cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y));
        p1 = p2; // the new start point is the end of the segment that we've
                 // just curved to
        
    }

    @Override
    public void curvetoCubicSmoothRel(float x2, float y2, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoQuadraticRel(float x1, float y1, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoQuadraticAbs(float x1, float y1, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoQuadraticSmoothRel(float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void arcRel(float rx, float ry, float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void arcAbs(float rx, float ry, float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag, float x, float y)
            throws ParseException {
        // TODO Auto-generated method stub
        
    }
    
}