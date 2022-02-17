package edu.wm.cs.semeru.redraw.classifiers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import edu.semeru.android.core.entity.model.fusion.DynGuiComponent;
import edu.wm.cs.semeru.redraw.ViewNode;
import edu.wm.cs.semeru.redraw.ViewType;


public class KMeansClassifier implements Classifier {
	
	private EntityManager em;
	private String imagesRoot;
	private int K = 1;
	private HashMap<DynGuiComponent, Mat> images;
	private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);;
	FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
	DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
	
	private class ResultTuple {
		protected int score;
		protected String component;
		
		public ResultTuple(int s, String d) {
			score = s;
			component = d;
		}
	}
	
	
	public KMeansClassifier(String imagesRoot) {
		this.imagesRoot = imagesRoot;
	}

	@Override
	public void train(String database) {
		System.out.println("KMeansClassifier -- Extracting components from database");
        em = Persistence.createEntityManagerFactory(database).createEntityManager();
        getDatabaseImages();
        System.out.println("KMeansClassifier -- Done extracting images");
	}

	@Override
	public ViewType getComponentType(ViewNode node) {
		if (em == null) {
			/* Must train before calling getComponentType */
			return null;
		}
		
		if (!node.isType(ViewType.IMAGE)) {
			/* Cannot identify without image */
			return null;
		}
		
		String imageStr = imagesRoot + "/" + node.getData() + ".png";
		return getComponentType(imageStr);
	}
	
	
	public ViewType getComponentType(String imageStr) {
		if (!(new File(imageStr).exists())) {
			System.err.println("KMeansClassifier -- ERROR: Could not read image " + imageStr);
			return null;
		}
		Mat img = Highgui.imread(imageStr);
		MatOfKeyPoint keypoints_orig = new MatOfKeyPoint();
		Mat descriptors_orig = new Mat();
		detector.detect(img, keypoints_orig);
		extractor.compute(img, keypoints_orig, descriptors_orig);
		
		ArrayList<ResultTuple> results = new ArrayList();
		for (DynGuiComponent d : images.keySet()) {
			String name = d.getName();
			try {
				int score = compareImages(descriptors_orig, images.get(d));
				if (results.size() < K || score < results.get(results.size() - 1).score) {
					int i = results.size() - 1;
					while (i >= 0 && score < results.get(i).score) {
						--i;
					}
					results.add(i+1, new ResultTuple(score, name));
				}
				if (results.size() > K) {
					results.remove(results.size() - 1);
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		if (results.size() == 0) {
			System.err.println("KMeansClassifier -- Could not find any matching images");
			return null;
		}
		
		HashMap<String, Integer> counts = new HashMap();
		String mode = results.get(results.size() - 1).component;
		while (results.size() > 0) {
			ResultTuple result = results.remove(0);
			String name = result.component;
			if (counts.containsKey(name)) {
				counts.put(name, counts.get(name) + 1);
			}
			else {
				counts.put(name, 1);
			}
		}

		for (String c : counts.keySet()) {
			if (counts.get(c) < counts.get(mode)) {
				mode = c;
			}
		}
		return new ViewType(mode, "android:src=\"@drawable/");
	}
	
	
	private void getDatabaseImages() {
		images = new HashMap();
		Query q = em.createQuery("SELECT d FROM DynGuiComponent d");
		for (DynGuiComponent d : (List<DynGuiComponent>) q.getResultList()) {
			if (d.getGuiScreenshot() == null || !(new File(d.getGuiScreenshot()).exists())) {
				System.err.println("KMeansClassifier -- ERROR: Could not read image " + d.getGuiScreenshot());;
				continue;
			}
			if ("android.widget.CheckBox".equals(d.getName())) {
				continue;
			}
			try {
				Mat compImage = Highgui.imread(d.getGuiScreenshot());
				MatOfKeyPoint keypoints = new MatOfKeyPoint();
				Mat descriptors = new Mat();
				
				detector.detect(compImage, keypoints);
				extractor.compute(compImage, keypoints, descriptors);
				images.put(d, descriptors);
			} catch (Exception e) {
				continue;
			}
		}
		System.out.println(q.getResultList().size());
		System.out.println("Images: " + Integer.toString(images.size()));
	}
	
	
	private int compareImages(Mat descriptors_orig, Mat descriptors_comp) {
		if (descriptors_orig.cols() != descriptors_comp.cols()) {
			return 1000;
		}
		// Match points of two images
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptors_orig, descriptors_comp, matches);
	 
		// Check matches of key points
		DMatch[] match = matches.toArray();
		double sum_dists = 0;
	    for (DMatch d : match) {
	    	sum_dists += d.distance;
	    }
	    sum_dists = sum_dists / (match.length > 0 ? match.length : 1);
	    // Add bias for unmatched descriptors
	    int unmatched = (descriptors_orig.cols() + descriptors_comp.cols() / 2) - matches.cols();
	    sum_dists += 10 * (unmatched > 0 ? unmatched : 0);
	    sum_dists = sum_dists < 0 ? 0 : sum_dists;
	    if (unmatched == 0 && matches.cols() == 0) {
	    	return 1000;
	    }
		return (int) (sum_dists);
	}
	
	
	public void testOnTrainingData() {
		/* For ten folds of the training data, evaluate the ability of the classifier
		 * to identify the type of each component
		 */
		
		HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
		ArrayList<DynGuiComponent> components = new ArrayList<DynGuiComponent>(images.keySet());
		Collections.shuffle(components);
		HashMap<DynGuiComponent, Mat> test_data = new HashMap<DynGuiComponent, Mat>();
		int folds = 10;
		int fold_size = components.size() / folds;
		for (int fold = 0; fold < folds; fold++) {
			// For each fold, remove size / folds components from training data, use as test data
			for (int c = 0; c < fold_size && c < components.size(); c++) {
				DynGuiComponent comp = components.remove(0);
				test_data.put(comp, images.remove(comp));
			}
			
			// Predict component type for each component in test set
			for (DynGuiComponent d : test_data.keySet()) {
				if (d.getGuiScreenshot() == null || !(new File(d.getGuiScreenshot()).exists())) {
					System.err.println("KMeansClassifier -- ERROR: Could not read image " + d.getGuiScreenshot());;
					continue;
				}
				try {
					ViewType v = getComponentType(d.getGuiScreenshot());
					if (!results.containsKey(d.getName())) {
						results.put(d.getName(), new HashMap<String, Integer>());
					}
					HashMap<String, Integer> res = results.get(d.getName());
					if (!res.containsKey(v.viewName())) {
						res.put(v.viewName(), 1);
					}
					else {
						res.put(v.viewName(), res.get(v.viewName()) + 1);
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
					continue;
				}
			}
			
			// Put test components back into training set
			for (DynGuiComponent d : test_data.keySet()) {
				images.put(d, test_data.get(d));
			}
			test_data.clear();
		}
		
		/* Print resulting confusion matrix */
		ArrayList<String> cols = new ArrayList<String>(results.keySet());
		Collections.sort(cols);
		System.out.print("|");
		for (String s : cols) {
			System.out.print(s + "|");
		}
		System.out.println("");
		System.out.print("| ");
		for (String s : cols) {
			HashMap<String, Integer> res = results.get(s);
			for (String d : cols) {
				if (res.containsKey(d)) {
					System.out.print(Integer.toString(res.get(d)) + " | ");
				}
				else {
					System.out.print("0 | ");
				}
			}
			System.out.println("");
		}
		
			
	}
	
}
