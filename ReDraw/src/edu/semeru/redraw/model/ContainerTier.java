package edu.semeru.redraw.model;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.redraw.uiautomator.tree.AttributePair;
import com.redraw.uiautomator.tree.RootUINode;
import com.redraw.uiautomator.tree.UiTreeNode;

import edu.wm.semeru.redraw.pipeline.UIDumpParser;

//Used for KnnBuilder alg v 3, if alg v3 is wrong remove
public class ContainerTier {
	public ArrayList<RootUINode> nodes = new ArrayList<RootUINode> ();

	public ContainerTier()
	{
		
	}
	
	//Take neighborhood as a parameter
	public ContainerTier(Neighborhood nh)
	{
		for(int i = 0; i<nh.rootData.size(); i++)
		{
			
		}
	}
	
	//For now the path to Tier should be a folder containing xml files with one screen in each file
	//The tiers are adapted from the container-neighborhood.mat file.
	//A potential place for things to go wrong outside of this code is in parsing the mat file for input
	public ContainerTier(String pathToTier)
	{
		UIDumpParser udp  = new UIDumpParser();
		File folder = new File(pathToTier);
		File[] listOfFiles = folder.listFiles();
		System.out.println("Tier Started:"+pathToTier);
		for(int i = 0; i < listOfFiles.length; i++){
			
			String filename = listOfFiles[i].getName();
			
			if(filename.endsWith(".xml")||filename.endsWith(".XML"))
			{
				
				RootUINode newNode = udp.parseXml(pathToTier+File.separator+filename) ;
				if (newNode.getChildCount() != 1)
				{
					System.out.println("Error:"+pathToTier+File.separator+filename);
				}
				RootUINode root =new RootUINode( newNode.getChildren().get(0));
				
				nodes.add(root);
			}
			
		}

			
	}
	
	public List<UiTreeNode> ConstructHierarchy(List<UiTreeNode> inputNodes)
	{
		int count = 0;
		
		List<UiTreeNode> containerNodes = new ArrayList<UiTreeNode>();
		
		while (inputNodes.size()>1)
		{
			Rectangle[] bboxes = getBBoxes(inputNodes);
			int[] chopNdx = new int[inputNodes.size()];//chop ndx should maybe be tier nodes length
			int[] toChop = new int[inputNodes.size()];
			double curMax = -1;
			double score;
			int maxNdx = -1;
			//choppingBlock, toChop ChoppingBlock used later?
			
			for(int i= 0; i<nodes.size(); i++)
			{
				if (nodes.get(i).getLeafNodes().size()>inputNodes.size()) //try with children 
				{
					continue;
				}
				Rectangle[] curBoxes = getBBoxes(nodes.get(i).getLeafNodes());
				chopNdx = new int[curBoxes.length];
				
				
				score = getAggregateScores(curBoxes,bboxes,chopNdx);//here
				
				List<UiTreeNode> choppedNodes = new ArrayList<UiTreeNode>();
				
				for(int j : chopNdx)
				{
					choppedNodes.add(inputNodes.get(j));
				}
				
				if(count > 0)
				{
					if(!canGroupMoreNodes(choppedNodes,containerNodes))//nodes.get(i).getLeafNodes()))
					{
						continue;
					}
				}
				if(score>curMax)
				{
					curMax = score;
					maxNdx = i;
					toChop = new int[chopNdx.length];
					toChop = chopNdx;
				}
			}
			
			if (maxNdx == -1)
			{
				//concatenate input nodes onto the end?
				System.out.println("Concatanating "+inputNodes.size() +" Nodes");
				containerNodes.addAll(inputNodes);
				inputNodes.clear();
			}
			else
			{
				List<UiTreeNode> choppedNodes = new ArrayList<UiTreeNode>();
				
				//just supposed to get to chop and chop that, not the while chop ndx
				for(int j : toChop)
				{
					choppedNodes.add(inputNodes.get(j));
				}
				inputNodes.removeAll(choppedNodes);
				
				containerNodes.add(buildContainer(nodes.get(maxNdx),choppedNodes,inputNodes));
				//check build container again
				//reset chopNdx and chop nodes
				
			}
			
			count += 1;
			
		}
		System.out.println("Created: "+count+" containers");
		System.out.println("Returned: "+containerNodes.size()+" containers");
		if (inputNodes.size() > 0)
		{
			containerNodes.addAll(inputNodes);
			inputNodes.clear();
		}
		return containerNodes;
	}


	private UiTreeNode buildContainer(RootUINode node, List<UiTreeNode> childNodes,
			List<UiTreeNode> inputNodes) {
		
		Rectangle bBox = new Rectangle(node.getLeft(),node.getTop(),node.getWidth(),node.getHeight());
		
		UiTreeNode Container = new UiTreeNode(bBox.x,bBox.y,bBox.width,bBox.height);
		Container.setType(node.type);
		
//		Container.addAtrribute("text", "");//TODO is there not a text property?
	//	Container.addAtrribute("class",node.type);
		
		Object[] attributes = node.getAttributesArray();
		for (Object object : attributes) {
			AttributePair pair = (AttributePair) object;
			if (pair.key.equals("bounds"))
			{
				continue;
			}
			Container.addAtrribute(pair.key, pair.value);
			if (pair.key.equals("class")) {
				Container.setType(pair.value);
			}
		}
		for(int i = 0; i< childNodes.size(); i++)
		{
			Container.addChild(childNodes.get(i));
		}
		
		Container.FixTightBounds(); 
		bBox = new Rectangle(Container.getLeft(),Container.getTop(),Container.getWidth(),Container.getHeight());
		
		Container.addAtrribute("bounds",  "[" + Container.getX() + "," +  Container.getY() + "][" + ( Container.getWidth() + Container.getX())
				+ "," + (Container.getHeight() + Container.getY()) + "]");

		List<UiTreeNode>subsumedNodes = getSubsumption(bBox,inputNodes);
		
		System.out.println(Container.getAttribute("bounds"));

		

		System.out.print(" Nodes Chopped ");
		System.out.println(childNodes.size());		
		System.out.print(" Nodes subsumed ");
		System.out.println(subsumedNodes.size());
		
		
		for(int i = 0; i<subsumedNodes.size(); i++)
		{
			Container.addChild(subsumedNodes.get(i));
			inputNodes.remove(subsumedNodes.get(i));
		}
		
		
		return Container;
	}


	private List<UiTreeNode> getSubsumption(Rectangle bBox, List<UiTreeNode> inputNodes) {

		List<UiTreeNode> subSumedNodes = new ArrayList<UiTreeNode>(); 
		
		for(int i = 0; i<inputNodes.size(); i++)
		{
			UiTreeNode curNode = inputNodes.get(i);
			Rectangle curBBox = new Rectangle(curNode.getLeft()+1,curNode.getTop()+1,curNode.getWidth()-2,curNode.getHeight()-2);

			if(bBox.contains(curBBox))
			{
				subSumedNodes.add(inputNodes.get(i));
			}
			
		}
		
		
		return subSumedNodes;
	}

	//Checking to see if we can draw a container without intersecting others
	private boolean canGroupMoreNodes(List<UiTreeNode> inputNodes, List<UiTreeNode> heirarchies2) {
		if(inputNodes.size()== 0)
		{
			return false;
		}
		Rectangle tightBounds = findTightBounds(inputNodes);
		for (int i = 0; i< heirarchies2.size(); i++)
		{
			Rectangle bbox = new Rectangle( heirarchies2.get(i).getX(),heirarchies2.get(i).getY(),heirarchies2.get(i).getWidth(),heirarchies2.get(i).getHeight());
			
			boolean intersect = tightBounds.intersects(bbox) ||tightBounds.contains(bbox) ||bbox.contains(tightBounds);
			if (intersect)
			{
				return false;
			}
			
		}

		return true;
	}

	private Rectangle findTightBounds(List<UiTreeNode> inputNodes) {
		// TODO Auto-generated method stub
		if (inputNodes.size() <=0)
		{
			return new Rectangle(0,0,0,0);
		}
		int[] bounds = new int[4];
		bounds[0]= inputNodes.get(0).getX();
		bounds[1]= inputNodes.get(0).getY();
		bounds[2]= inputNodes.get(0).getRight();
		bounds[3]= inputNodes.get(0).getBottom();
		for(int i = 1; i<inputNodes.size(); i++)
		{
			if(bounds[0]> inputNodes.get(i).getX())
			{
				bounds[0] = inputNodes.get(i).getX();
			}
			if(bounds[1]> inputNodes.get(i).getY())
			{
				bounds[1] = inputNodes.get(i).getY();
			}
			if(bounds[2]< inputNodes.get(i).getRight())
			{
				bounds[2] = inputNodes.get(i).getRight();
			}
			if(bounds[3]< inputNodes.get(i).getBottom())
			{
				bounds[3] = inputNodes.get(i).getBottom();
			}
			
		}
		
		return new Rectangle(bounds[0],bounds[1],bounds[2]-bounds[0],bounds[3]-bounds[1]);
	}
	
	
	private double getAggregateScores(Rectangle[] curBoxes, Rectangle[] bboxes, int[] chopNdx) {
		
		double [][] scoreMat = new double [curBoxes.length][bboxes.length];
		double scoreSum = 0;
		
		scoreMat = bboxOverlapRatio (curBoxes,bboxes);
		
		double [] scores = new double [curBoxes.length];
		
		java.util.Arrays.fill(scores,0);
		//potential chance for errors getting the rows and columns clear
		for(int i = 0; i<scoreMat.length; i++)
		{
			int[] loc = matrixMax(scoreMat);
			
			scores[i] = scoreMat[loc[0]][loc[1]];
			scoreSum += scores[i];
			//negative out rows
			for(int j = 0; j<scoreMat.length; j++)
			{
				scoreMat[j][loc[1]]= -1;
			}
			//negative out columns
			for(int j = 0; j<scoreMat[loc[0]].length; j++)
			{
				scoreMat[loc[0]][j]= -1;
			}
			
			chopNdx[i] = loc[1];
		}
		//Also check that this part is right
		//It's calculated differently from matlab, but I believe it should end up the same.
		return  scoreSum/scores.length;
	}
	
	private int[] matrixMax(double [][] mat)
	{
		double maxi = mat [0][0];
		int[] loc = new int[2];
		loc[0] = 0;
		loc[1] = 0;
		for(int i = 0 ; i < mat.length; i++)
		{
			for(int j = 0 ; j< mat[i].length; j++)
			{
				if (maxi <mat[i][j])
				{
					maxi = mat[i][j];
					loc[0] = i;
					loc[1] = j;
				}
			}
		}
		return loc;
	}


	private double[][] bboxOverlapRatio(Rectangle[] curBoxes, Rectangle[] bboxes) {
		// Originally a mat lab function. Hand coded without a guide, potential for errors here
		
		double [][] scoreMat = new double [curBoxes.length][bboxes.length];
		
		for(int i = 0 ; i < curBoxes.length; i++)
		{
			for(int j = 0 ; j< bboxes.length; j++)
			{
				scoreMat[i][j] = computeRatio(curBoxes[i],bboxes[j]);
			}
		}
		
		return scoreMat;
	}


	private double computeRatio( Rectangle targBox, Rectangle inBox) {
		// Potential for errors here, be carefull
		Rectangle intersect = inBox.intersection(targBox);
		
		if(intersect.isEmpty())
		{
			return 0.0;
		}
		
		double intersectArea = intersect.getHeight()*intersect.getWidth();
		double inArea = inBox.getHeight()*inBox.getWidth();
		double targArea = targBox.getHeight() * targBox.getWidth();
		
		double Ratio = intersectArea/(targArea + inArea - intersectArea);
		
		return (Ratio);
	}

	private Rectangle[] getBBoxes(List<UiTreeNode> inputNodes) {
		
		Rectangle[] boxes = new Rectangle[inputNodes.size()];
		
		for(int i = 0; i< inputNodes.size(); i++)
		{
			boxes[i] = new Rectangle();
			boxes[i].height = inputNodes.get(i).getHeight();
			boxes[i].width = inputNodes.get(i).getWidth();
			boxes[i].x = inputNodes.get(i).getX();
			boxes[i].y = inputNodes.get(i).getY();
		}
		
		
		return boxes;
	}
	
}
