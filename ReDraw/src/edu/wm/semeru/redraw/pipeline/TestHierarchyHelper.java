package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;

import edu.wm.cs.semeru.redraw.JSNode;

/**
 * Helper class so that we can test hierarchies of sketch files using matlab
 * @author Michael
 *
 */
public class TestHierarchyHelper {
	
	public static MatlabEngine eng;
	public static String command;
	public static String outputLocation;
	/**
	 * parameters:
	 * 1.) path to sketch file
	 * 2.) name of new/existing .mat file
	 * 3.) screenshot path
	 * 4.) name of matlab instance (if it exists)
	 * 5.) location to output cropped images
	 * @param args
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 * @throws CancellationException 
	 * @throws MatlabSyntaxException 
	 * @throws MatlabExecutionException 
	 */
	public static void main(String[] args) throws InterruptedException, MatlabExecutionException, MatlabSyntaxException, CancellationException, ExecutionException{
		String sketchFile = args[0];
		String dotMatFile = args[1];
		String screenshot = args[2];
		String matlabInstance = args[3];
		outputLocation = args[4];
		
		eng = MatlabEngine.connectMatlab(matlabInstance);
		
		getBoundingBoxes(sketchFile, screenshot);
		
		buildAndWriteTree(dotMatFile);
	}

	public static void getBoundingBoxes(String js, String screenshot) throws MatlabExecutionException, MatlabSyntaxException, CancellationException, EngineException, InterruptedException, ExecutionException{
		List<JSNode> containers = new ArrayList<JSNode>();
    	HashMap<String, JSNode> info = GeneratorWrapper.parseSketchFile(screenshot, js, containers, outputLocation);
    	int numNodes = info.size();
    	
    	int ii = 1;
    	for (Entry<String, JSNode> item: info.entrySet()){
    		JSNode node = item.getValue();
            command = "image = imread('" + item.getKey() + "');";
            eng.eval(command);
            command = "image = imresize(image, [128,128]);";
            eng.eval(command);
            command = "[pred, conf] = classify(classifier, image);";
            eng.eval(command);
            command = "label = code2name(str2double(cellstr(pred)), false);";
            eng.eval(command);
            command = "bboxes(" + ii + ",:) = [" + node.getX() + ", " + node.getY() + ", " + node.getWidth() + ", " + node.getHeight() + "];";
            eng.eval(command);
            command = "labels(" + ii + ",:) = [ label, \"\" ]";
            eng.eval(command);
            ii++;
    	}
	}
	
	public static void buildAndWriteTree(String dotMatFile) throws MatlabExecutionException, MatlabSyntaxException, CancellationException, EngineException, InterruptedException, ExecutionException{
		command = "input = formatDetectorOutput(bboxes, labels)";
		eng.eval(command);
		command = "outputTree = cnh.knn(input)";
		eng.eval(command);
		command = "save('" + dotMatFile + "', 'outputTree')";
		eng.eval(command);
	}
}
