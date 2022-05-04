package edu.wm.semeru.redraw.helpers;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;

import org.tensorflow.types.UInt8;

public class TensorHelper {

	private static Session sess;
	private static String filePath;
	
	public int openTensor()
	{
		
		return 0;
	}
	
	public static void loadPythonCheckpoint(String path)
	{

		filePath = path;
/*        try (SavedModelBundle bund = SavedModelBundle.load(path,"serve"))
        {
        	sess = bund.session();
        }
        catch(Exception ex)
        {
        	System.out.print(ex.getMessage());
        }*/
	}
	
	public static void callConvertCheckpointToSavedModel(String chckPath, String modelPath, String scriptPath)
	{
		
	}
	
	public static String classifyImage(String pathToImage)
	{
		

		try  (SavedModelBundle bund = SavedModelBundle.load(filePath,"serve")){
			
			sess = bund.session();
			byte[] bytes = Files.readAllBytes(Paths.get(pathToImage));

			final long[] shape = new long[] { 20, 10, 3 };
			Session.Runner runne= sess.runner();
		
			
			float[] probabilities = null;
			try (Tensor input = Tensor.create( bytes);//DataType.UINT8,shape,bytes);
					 Tensor<Float> output =
				                sess
				                    .runner()
				                    .feed("case/cond/cond_jpeg/decode_image/cond_jpeg/cond_png/DecodePng", input)
				                    .fetch("InceptionV3/Predictions/Softmax")
				                    .run()
				                    .get(0)
				.expect(Float.class))
			{
			
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return "";
	}
	
	public Tensor readTensorFile(String path)
	{
		
		
		
		Tensor output = null;//= Tensor.create();
		System.out.print(TensorFlow.version());

		return output;
	}
	
	public static Graph openGraphFromFile(String path) throws IOException
	{
		Path modelPath = Paths.get(path);
		byte[] graph = Files.readAllBytes(modelPath);

		Graph g = new Graph();
		
			g.importGraphDef(graph);
			
			return g;
		
			//can only do .pb files
			//using protobuf maybe we can convert .pbtxt files to binary format
		//this link shows how to do it in python maybe it can be moved to java
	//https://stackoverflow.com/questions/45823662/can-we-use-pbtxt-instead-of-pb-file-in-using-tensor-flow-model
	}
	
	public static void readSavedModel(String path) throws IOException
	{
		Path modelPath = Paths.get(path+"\\saved_model.pb");
		byte[] graph = Files.readAllBytes(modelPath);

		try(Graph g = new Graph())
		{
			g.importGraphDef(graph);
			System.out.print(g.toString());
		}
		
		
		System.out.print("one");
		System.out.print(TensorFlow.version());
	//	System.console().printf("One");
		//SavedModelBundle.Loader loader;
		//loader = SavedModelBundle.loader(path);
		
		System.out.print("two");
		//System.console().printf("two");
	//	return loader.load();
		try(SavedModelBundle modelBundle =SavedModelBundle.load(path,"")){
			// SavedModelBundle.loader(path).load();
		//	return modelBundle;
		}
		
	}
	public static void main(String[] args) throws Exception
	{
	//	readSavedModel("C:\\Users\\Andrew\\Downloads\\ReDraw-Cropped-Fine-Tuning");
	}
	
}
