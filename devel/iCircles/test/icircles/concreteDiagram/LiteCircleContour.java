package icircles.concreteDiagram;
/**
 * This class exists to model the centre point and radius of @see CircleContour.
 * We don't use a CircleContour directly as the LiteCircelContour.equals() has
 * vastly different semantics to CircleContour.equals().
 * 
 * @author Aidan Delaney <aidan@ontologyengineering.org>
 *
 */
public class LiteCircleContour {
    public double cx, cy, r;
    public LiteCircleContour(double cx, double cy, double r) {
        this.cx = cx;
        this.cy = cy;
        this.r = r;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        LiteCircleContour lcc = (LiteCircleContour) obj;
        return (cx == lcc.cx) && (cy == lcc.cy) && (r == lcc.r);
    }
}