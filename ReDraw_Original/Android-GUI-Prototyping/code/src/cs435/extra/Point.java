package cs435.extra;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Point {

    private double x = 0;
    private double y = 0;
    private double z = 0;
    public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	private int cluster_number = 0;

    public Point(double x, double y, double z)
    {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getX()  {
        return this.x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getY() {
        return this.y;
    }
    
    public void setCluster(int n) {
        this.cluster_number = n;
    }
    
    public int getCluster() {
        return this.cluster_number;
    }
    
    //Calculates the distance between two points.
    protected static double distance(Point p, Point centroid) {
        return Math.sqrt(distance2(p, centroid));
    }

    protected static double distance2(Point p, Point centroid) {
    	double xd, yd, zd;
    	xd = centroid.getX() - p.getX();
    	yd = centroid.getY() - p.getY();
    	zd = centroid.getZ() - p.getZ();
        return xd * xd + yd * yd + zd * zd;
    }
    
    //Creates random point
    protected static Point createRandomPoint(int min, int max) {
    	Random r = new Random();
    	double x = min + (max - min) * r.nextDouble();
    	double y = min + (max - min) * r.nextDouble();
    	double z = min + (max - min) * r.nextDouble();
    	return new Point(x,y,z);
    }
    
    protected static List<Point> createRandomPoints(int min, int max, int number) {
    	List<Point> points = new ArrayList<Point>(number);
    	for(int i = 0; i < number; i++) {
    		points.add(createRandomPoint(min,max));
    	}
    	return points;
    }
    
    public String toString() {
    	return "(" + x + "," + y + "," + z +")";
    }
}