package edu.wm.semeru.redraw.pipeline;

import java.io.File;

public class FileList {

	public static String outputDir;
	public static int count;
	public static void main(String[] args){
		File dir = new File(args[0]);
		count = 0;
		recursiveHelper(dir);
	}
	
	public static void recursiveHelper(File file){
		
		File[] children = file.listFiles();
		for (File ch : children){
			if (ch.isDirectory()){
				recursiveHelper(ch);
			}
			else{
//				String extension = ch.getName().substring(ch.getName().lastIndexOf('.'));
				if (ch.getName().equalsIgnoreCase("ui-dump.xml")){
					File parentFile = ch.getParentFile();
					String num = parentFile.getName();
					String parent = parentFile.getParentFile().getName();
					ch.renameTo(new File(ch.getParentFile().getAbsolutePath() + File.separator + parent + "-" + num + ".xml"));
				}
				else{
					continue;
				}
			}
		}
	}
}
