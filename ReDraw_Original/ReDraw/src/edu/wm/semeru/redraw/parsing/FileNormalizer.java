package edu.wm.semeru.redraw.parsing;

import java.io.File;
import java.util.AbstractSet;
import java.util.ArrayList;

public class FileNormalizer {
	
	public static void main(String[] args){
		File datadir = new File(args[0]);
		File[] dirs = datadir.listFiles();
		ArrayList<String> confirmed = new ArrayList<String>();
		for (File dir : dirs){
			File[] xmls = dir.listFiles();
			ArrayList<String> curlist = new ArrayList<String>();
			for (File xml : xmls){
				curlist.add(xml.getName());
			}
			if (confirmed.isEmpty()){
				boolean whocares = confirmed.addAll(curlist);
			}
			else{
				boolean whocares = confirmed.retainAll(curlist);
			}
		}
		
		for (File dir : dirs){
			File[] xmls = dir.listFiles();
			for (File xml : xmls){
				if (!confirmed.contains(xml.getName())){
					xml.delete();
				}
			}
		}
	}

}
