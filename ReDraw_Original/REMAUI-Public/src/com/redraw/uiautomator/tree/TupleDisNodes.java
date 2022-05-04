package com.redraw.uiautomator.tree;

import edu.wm.cs.semeru.redraw.JSNode;

public class TupleDisNodes implements Comparable<TupleDisNodes>{
	public float dis;
	/**
	 * @return the dis
	 */
	public float getDis() {
		return dis;
	}


	/**
	 * @param dis the dis to set
	 */
	public void setDis(float dis) {
		this.dis = dis;
	}


	/**
	 * @return the dsNode
	 */
	public JSNode getDsNode() {
		return dsNode;
	}


	/**
	 * @param dsNode the dsNode to set
	 */
	public void setDsNode(JSNode dsNode) {
		this.dsNode = dsNode;
	}


	/**
	 * @return the uiNode
	 */
	public UiTreeNode getUiNode() {
		return uiNode;
	}


	/**
	 * @param uiNode the uiNode to set
	 */
	public void setUiNode(UiTreeNode uiNode) {
		this.uiNode = uiNode;
	}

	public JSNode dsNode;
	public UiTreeNode uiNode;
	
	
	public TupleDisNodes(float dis, JSNode dsNode, UiTreeNode uiNode){
		this.dis = dis;
		this.dsNode = dsNode;
		this.uiNode = uiNode;
	}
	
	
	public String toString(){
		return "Weight: " + dis + ", " + dsNode.toString() + ",  ==> " + uiNode.toString(); 
	}
	
	@Override
    public int compareTo(final TupleDisNodes o) {
        return Float.compare(this.dis, o.dis);
    }
	

}
