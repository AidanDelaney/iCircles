package icircles.concreteDiagram;

import icircles.util.DEB;

import java.awt.Point;
import java.awt.geom.Ellipse2D;

public class ConcreteSpiderFoot implements Labellable {

    public static final double FOOT_RADIUS = 4;
    private double x;
    private double y;
    private ConcreteSpider spider;
    private final int LABEL_BUFFX = 5;
    private int labelBuffY = 18;//not a constant
    private final int LABEL_NUDGE = 2;
    private boolean flipped = false;
    private Point labelPoint;
    

    public ConcreteSpiderFoot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Ellipse2D.Double getBlob() {
        double rad = FOOT_RADIUS;
        return new Ellipse2D.Double(getX() - rad, getY() - rad, 2 * rad, 2 * rad);
    }

    /**
     * Puts the coordinates and dimensions of this foot into the given ellipse.
     * @param outBlob this ellipse will contain the coordinates and dimensions
     * of this foot.
     */
    public void getBlob(Ellipse2D.Double outBlob) {
        outBlob.x = getX() - FOOT_RADIUS;
        outBlob.y = getY() - FOOT_RADIUS;
        outBlob.width = 2 * FOOT_RADIUS;
        outBlob.height = 2 * FOOT_RADIUS;
    }

    // TODO: Maybe you should use 'hashCode' instead of 'checksum'?
    public double checksum() {
        if (DEB.level >= 2) {
            System.out.println("build checksum for foot from coords (" + getX() 
                           + ", " + getY() + ")\n");
        }
        return getX() + 1.02 * getY();
    }

    /**
     * Returns the x coordinate of the centre of this foot.
     * @return the x coordinate of the centre of this foot.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x coordinate of the centre of this foot.
     * @param x the new x coordinate of the centre of this foot.
     */
    void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y coordinate of the centre of this foot.
     * @return the y coordinate of the centre of this foot.
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y coordinate of the centre of this foot.
     * @param y the new y coordinate of the centre of this foot.
     */
    void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the spider to which this foot belongs.
     * @return the spider to which this foot belongs.
     */
    public ConcreteSpider getSpider() {
        return spider;
    }

    void setSpider(ConcreteSpider spider) {
        this.spider = spider;
    }

	@Override
	public Point getLabelPoint() {
		if(labelPoint == null) {
			labelPoint = new Point((int) (getX() - LABEL_BUFFX), (int) (getY() + labelBuffY));
		}
		return labelPoint;
	}

	@Override
	public Point nudgeLabelPoint() {
		//alternate between flipping the label from above to below the foot
		//and nudging it
		if(flipped) {
			labelBuffY += LABEL_NUDGE;
			labelPoint = new Point((int) (getX() - LABEL_BUFFX), (int) (getY() + labelBuffY));
		} else {
			labelPoint = new Point((int) (getX() - LABEL_BUFFX), (int) (getY() - labelBuffY));
		}
		flipped = !flipped;
		return labelPoint;
	}
	
	@Override
	public String getLabel() {
		return this.getSpider().as.getName();
	}
}
