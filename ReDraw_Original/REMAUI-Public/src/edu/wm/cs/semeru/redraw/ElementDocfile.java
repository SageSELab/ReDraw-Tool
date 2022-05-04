package edu.wm.cs.semeru.redraw;


import org.jsoup.nodes.Element;

public class ElementDocfile {
	private Element ele;
	private String doc;
	
	public Element getEle() {
		return ele;
	}

	public String getDoc() {
		return doc;
	}
	
	
	public ElementDocfile(Element ele, String doc){
		this.ele = ele;
		this.doc = doc;
	}

}
