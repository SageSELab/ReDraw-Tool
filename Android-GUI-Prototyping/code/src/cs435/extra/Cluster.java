package cs435.extra;

import java.util.ArrayList;
import java.util.List;

/**
 * http://www.dataonfocus.com/k-means-clustering-java-code/
 */
public class Cluster {
	
	public List<Point> points;
	public Point centroid;
	public int id;
	
	//Creates a new Cluster
	public Cluster(int id) {
		this.id = id;
		this.points = new ArrayList<>();
		this.centroid = null;
	}

	public List<Point> getPoints() {
		return points;
	}
	
	public int getPointCount() {
		return points.size();
	}
	
	public void addPoint(Point point) {
		points.add(point);
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public Point getCentroid() {
		return centroid;
	}

	public void setCentroid(Point centroid) {
		this.centroid = centroid;
	}

	public int getId() {
		return id;
	}
	
	public void clear() {
		points.clear();
	}
	
	public void plotCluster() {
		System.out.println("[Cluster: " + id+"]");
		System.out.println("[Centroid: " + centroid + "]");
		//System.out.println("[Points: \n");
		//for(Point p : points) {
	//		System.out.println(p);
//		}
	//	System.out.println("]");
	}

}