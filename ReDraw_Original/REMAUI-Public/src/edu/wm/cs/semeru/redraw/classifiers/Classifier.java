package edu.wm.cs.semeru.redraw.classifiers;

import edu.wm.cs.semeru.redraw.ViewNode;
import edu.wm.cs.semeru.redraw.ViewType;

public interface Classifier {
	/**
	 * Identifies the component type for the node
	 * -- Returns the corresponding ViewType
	 * @param node
	 */
	public ViewType getComponentType(ViewNode node);
	
	/**
	 * Trains the classifier with a given database
	 */
	public void train(String database);
	
	/**
	 * Tests the classifier on the training data
	 * -- Should perform a 10 fold cross evaluation
	 */
	public void testOnTrainingData();
}
