package edu.semeru.redraw.knn;

import edu.semeru.android.core.dao.ScreenDao;
import edu.semeru.redraw.model.ContainerTier;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.Position;
import edu.wm.semeru.redraw.helpers.TensorHelper;
import edu.wm.semeru.redraw.pipeline.UIDumpParser;

import com.redraw.uiautomator.tree.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.*;
import org.tensorflow.op.core.ReadFile;
import org.tensorflow.op.Scope;

public class KnnBuilder {
	final static int VECTOR_DIFFERENCE = 3;
	public static List<RootUINode> screenRoots = new ArrayList<RootUINode>();
	public static List<RootUINode> heirarchies; //?
	public static List<ContainerTier> Tiers; // only used in V3, 

	public static void main(String[] args) throws IOException {

}

	public void loadAndAssemble(String path, boolean isDirectory)
	{
		UIDumpParser udp = new UIDumpParser();
		 if(isDirectory)
		 {
			 File folder = new File(path);
				File[] fileList = folder.listFiles();
				for(int i = 0; i< fileList.length;i++)
				{
					String filename = fileList[i].getName();
					if(filename.toLowerCase().endsWith(".xml"))
					{
						RootUINode newNode =  udp.parseXml(fileList[i].getAbsolutePath());
						screenRoots.add(newNode);
					}
				}
		 }
		 else
		 {
			 RootUINode newNode =  udp.parseXml(path);
			screenRoots.add(newNode);
		 }
	}
	public void loadAndAssemble(List<RootUINode> screens)
	{
		screenRoots= screens;
	}
	public void loadAndAssembleTiers(List<ArrayList<RootUINode>> rootLists)
	{
		for(ArrayList<RootUINode> rlist : rootLists )
		{
			ContainerTier newTier = new ContainerTier();
			newTier.nodes = rlist;
			Tiers.add(newTier);
		}
	}
	
	//prefered method?
	public void loadAndAssembleTierFolders(List<String> tierFolders)
	{
		Tiers = new ArrayList<ContainerTier>();
		for(String path : tierFolders)
		{
			ContainerTier newTier = new ContainerTier(path);
			Tiers.add(newTier);
		}
	}
	
	//Correct method?
	public void knn(List<UiTreeNode> inputNodes,String fName)
	{
		List<UiTreeNode> nodeList = new ArrayList<UiTreeNode>();
		
		//get a clean list of the nodes from input
		for(int i = 0; i<inputNodes.size(); i++)
		{
			UiTreeNode clearNode = new UiTreeNode(inputNodes.get(i).getX(),
					inputNodes.get(i).getY(),
					inputNodes.get(i).getWidth(),
					inputNodes.get(i).getHeight());
			clearNode.setName(inputNodes.get(i).getName());
			clearNode.setType(inputNodes.get(i).getType());
						
			Object[] attributesArray = 	inputNodes.get(i).getAttributesArray();

			for (int j = 0; j < attributesArray.length; j++) {
				AttributePair attribute = (AttributePair) attributesArray[j];
				//result += attribute.key + "=\"" + ((attribute.value != null) ? attribute.value.replace("\"", "&quot;") : "")
				//		+ "\" ";
				clearNode.addAtrribute(attribute.key, attribute.value);
				
			}
			
			nodeList.add(clearNode);
		}
		
		int tierCount=0;
		
		while(nodeList.size()>1 && tierCount < Tiers.size())
		{
			System.out.println("constructing hierarchy level: " + tierCount +" For "+nodeList.size()+" Nodes");
			nodeList = Tiers.get(tierCount).ConstructHierarchy(nodeList);
			tierCount += 1;
		}
		
		
		RootUINode ro;
		UiTreeNode uiRoot = new UiTreeNode(nodeList);

		
		if(nodeList.size() == 1)
		{
//			ro = new RootUINode( uiRoot);
		}
		else
		{
//			ro = buildRootNode(nodeList);
//			ro.FixBounds();
		}

		ro = new RootUINode( uiRoot);
		//TODO the next two lines are insane and shouldn't be needed, but if it works...
		
		
		writeHierarchyToXML(ro,fName);		
//		writeHierarchyToXML(ro,fName);		
		
	}
	
	
	
	private void writeHierarchyToXML(RootUINode ro, String fname) {
		// TODO Auto-generated method stub
		UIDumpParser parser = new UIDumpParser();
		StringBuilder builderXml = new StringBuilder();
		builderXml.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		parser.buildXml(null, ro, builderXml);
//		kb.getHeirarchy(path, isDirectory)

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
			bw.write(builderXml.toString());
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RootUINode buildRootNode(List<UiTreeNode> nodeList) {
		RootUINode rootN = new RootUINode("","",0,0,1200,1920,"RelativeLayout");
		
		for(int i =0 ; i < nodeList.size(); i ++)
		{
			rootN.addChild(nodeList.get(i));//TODO check to make sure type works
		}
		
		return rootN;
	}

	public static List<UiTreeNode> getHeirarchyFromPath(String path,boolean isDirectory)
	{
		List<UiTreeNode> possibleContainers = new ArrayList<UiTreeNode>();
		List<UiTreeNode> components = new ArrayList<UiTreeNode>();
		UIDumpParser udp = new UIDumpParser();
		 List<String> containerTypes = new ArrayList<String>(
	                Arrays.asList("android.widget.FrameLayout", "android.widget.LinearLayout", "android.webkit.WebView",
	                        "android.widget.GridLayout", "android.widget.RelativeLayout", "android.view.View"));

		
		
		if (isDirectory)
		{
			File folder = new File(path);
			File[] fileList = folder.listFiles();
			for(int i = 0; i< fileList.length;i++)
			{
				String filename = fileList[i].getName();
				if(filename.toLowerCase().endsWith(".xml"))
				{
					RootUINode newNode =  udp.parseXml(fileList[i].getAbsolutePath());
					screenRoots.add(newNode);
					components.addAll(newNode.getLeafNodes());
					for(BasicTreeNode child :newNode.getLeafNodes())
					{
						UiTreeNode node = (UiTreeNode) child;
						
						if(containerTypes.contains(node.getAttribute("class")))
						{
							possibleContainers.add(node);
						}
						
					}
				}
			}
		}
		else
		{
			if(path.toLowerCase().endsWith(".xml"))
			{
				RootUINode newNode =  udp.parseXml(path);
				screenRoots.add(newNode);
				
				components.addAll(newNode.getLeafNodes());
				
				for(BasicTreeNode child :newNode.getLeafNodes())
				{
					UiTreeNode node = (UiTreeNode) child;
					
					if(containerTypes.contains(node.getAttribute("class")))
					{
						possibleContainers.add(node);
					}
					
				}
			}
		
		}
		
		int inputArea = 0;
		for(UiTreeNode node : components)
		{
			inputArea += node.getHeight()*node.getWidth();
		}
		for (RootUINode root :screenRoots)
		{
			 if(root.getAllChildrenList().size()-components.size()<= VECTOR_DIFFERENCE)
			 {
		//Good list length?				 
			 }
		}
		
		
		//Good list Area
		
		
		return components;
	}

	
			
			
}
