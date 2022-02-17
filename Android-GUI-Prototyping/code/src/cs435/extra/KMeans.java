/* 
 * KMeans.java ; Cluster.java ; Point.java
 *
 * Solution implemented by DataOnFocus
 * www.dataonfocus.com
 * 2015
 *
*/
package cs435.extra;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import cs435.extra.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import cs435.guiproto.ImagesHelper;

public class KMeans {

	//Number of Clusters. This metric should be related to the number of points
	private int numClusters;
//	private static float ERROR_BOUND = (float) 0.000000001;
	private List<Point> points;
	private List<Cluster> clusters;
	
	public KMeans(List<Point> points, int clusters) {
		this.points = points;
		this.clusters = new ArrayList<>();
		this.numClusters = clusters;
	}
	
	public static List<Color> getColorsThroughKMeans(int[] pixels, int clusters) {
		List<Point> points;
		List<Point> centroids;
		List<Color> colors;
		
		// Convert the image into a point list
		points = new ArrayList<>();
		for (int pixel : pixels) {
			Color c = ImagesHelper.intToRgb(pixel);
			points.add(new Point(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue()  / 255.0));
		}
		
		// Run k-means clustering to get the major colors
		KMeans kmeans = new KMeans(points, clusters);
		kmeans.init();
		centroids = kmeans.runAndGetCentroids();
		
		// Convert the centroids back into colors and display them
		colors = new ArrayList<>();
		for (int i=0; i<centroids.size(); i++) {
			Point p = centroids.get(i);
			colors.add(
				new Color(
					(int) (p.getX() * 255.0),
					(int) (p.getY() * 255.0),
					(int) (p.getZ() * 255.0)
				)
			);
		}
		
		return colors;
	}
	
	//Initializes the process
	public void init() {
		Random rand = new Random();
		
		//Create Clusters
		// Get centroids from kmeans++
		/*
		 *  Choose a center at random from the data points.
		 *  For each point:
		 *	- Get the distance between this and the center
		 *	- Square it
		 *	- This is the probability that this will be chosen as the next center
		 *  Repeat the last step until you run out of centers
		 */
		
		Cluster cluster;
		Point centroid;
		// First center is random
		centroid = points.get(rand.nextInt(points.size()));	
		cluster  = new Cluster(0);
		cluster.setCentroid(centroid);
		clusters.add(cluster);
		
		// Next center is randomly chosen using the distance squared as the probability
		double[] distances2 = new double[points.size()];
		double d2sum;
		for (int i=1; i<numClusters; i++) {
			// Find everyone's square distances
			d2sum = 0.0;
			for (int j=0; j<points.size(); j++) {
				double d2 = Point.distance2(points.get(j), clusters.get(i-1).getCentroid());
				distances2[j] = d2;
				d2sum += d2;
			}
			// Choose a new cluster at random, weighted by their square distance
			double r = rand.nextDouble() * d2sum;
			for (int j=0; j<points.size(); j++) {
				// TODO Convert this into an "almost equal"?
				if (r <= distances2[j]) {
					cluster = new Cluster(i);
					cluster.setCentroid(points.get(j));
					clusters.add(cluster);
					break;
				}
				r -= distances2[j];
			}
		}
		
		//Print Initial state
		plotClusters();
	}

	private void plotClusters() {
		for (int i = 0; i < numClusters; i++) {
			Cluster c = clusters.get(i);
			c.plotCluster();
		}
	}
	
	/**
	 * Calculate the major colors of the image, sorted by Frequency.
	 * @return
	 */
	public List<Point> runAndGetCentroids() {
		boolean finish = false;
		int iteration = 0;
		int sameCounter = 0;
		double prevDist = 0;
		
		List<Point> currentCentroids = null;
		
		// Add in new data, one at a time, recalculating centroids with each new one. 
		while(!finish) {
			//Clear cluster state
			clearClusters();
			
			List<Point> lastCentroids = getCentroids();
			
			//Assign points to the closer cluster
			assignCluster();
			
			//Calculate new centroids.
			calculateCentroids();
			
			iteration++;
			
			currentCentroids = getCentroids();
			
			//Calculates total distance between new and old Centroids
			double distance = 0;
			for(int i = 0; i < lastCentroids.size(); i++) {
				distance += Point.distance(lastCentroids.get(i),currentCentroids.get(i));
			}

			if (prevDist == distance){
				sameCounter++;
			}
			else{
				sameCounter = 0;
			}
			prevDist = distance;
//			System.out.println("#################");
//			System.out.println("Iteration: " + iteration);
//			System.out.println("Centroid distances: " + distance);
			plotClusters();
			
			if(distance == 0 || sameCounter>=500) {
				finish = true;
			}
		}
		
		// Get centroids by frequency
		// Sort clusters by decreasing number of points
		clusters.sort((a, b) -> (a.points.size() > b.points.size() ? -1 : 1));
		List<Point> centroids = new ArrayList<>();
		for (Cluster c : clusters) {
//			System.out.println("#Clusters: " + c.getPointCount());
			centroids.add(c.centroid);
		}
		
		return centroids;
	}
	
	private void clearClusters() {
		for(Cluster cluster : clusters) {
			cluster.clear();
		}
	}
	
	private List<Point> getCentroids() {
		List<Point> centroids = new ArrayList<>(numClusters);
		for(Cluster cluster : clusters) {
			Point aux = cluster.getCentroid();
			Point point = new Point(aux.getX(),aux.getY(), aux.getZ());
			centroids.add(point);
		}
		return centroids;
	}
	
	private void assignCluster() {
		double max = Double.MAX_VALUE;
		double min = max; 
		int cluster = 0;
		double distance = 0.0; 
		
		for(Point point : points) {
			min = max;
			for(int i = 0; i < numClusters; i++) {
				Cluster c = clusters.get(i);
				distance = Point.distance(point, c.getCentroid());
				if(distance < min){
					min = distance;
					cluster = i;
				}
			}
			point.setCluster(cluster);
			clusters.get(cluster).addPoint(point);
		}
	}
	
	private void calculateCentroids() {
		for(Cluster cluster : clusters) {
			double sumX = 0;
			double sumY = 0;
			List<Point> list = cluster.getPoints();
			int n_points = list.size();
			
			for(Point point : list) {
				sumX += point.getX();
				sumY += point.getY();
			}
			
			Point centroid = cluster.getCentroid();
			if(n_points > 0) {
				double newX = sumX / n_points;
				double newY = sumY / n_points;
				centroid.setX(newX);
				centroid.setY(newY);
			}
		}
	}
}
