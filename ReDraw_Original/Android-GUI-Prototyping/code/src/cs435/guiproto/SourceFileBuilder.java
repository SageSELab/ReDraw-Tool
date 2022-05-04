package cs435.guiproto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generate Java source code for an activity.
 * @author bdpowell
 *
 */
public class SourceFileBuilder {
	
	private static final String outputTemplate = 
	       "package %PACKAGENAME%;\n"
	     + "import android.app.Activity;\n"
	     + "import android.os.Bundle;\n"
	     + "public class %ACTIVITYNAME% extends Activity {\n"
	     + "    @Override\n"
	     + "    protected void onCreate(Bundle savedInstanceState) {\n"
	     + "        super.onCreate(savedInstanceState);\n"
	     + "        setContentView(R.layout.%LAYOUTNAME%);\n"
	     + "    }\n"
	     + "}";
	
	private String packageName;

	/**
	 * Create a new source file.
	 *
	 * @param packageName The package name for the generated file.
	 */
	public SourceFileBuilder(String packageName) {
		this.packageName = packageName;
		// TODO Ensure that package name is valid
	}
	
	/**
	 * Create the source file for a given activity.
	 *
	 * @param out        Stream to write the source file to
	 * @param activity   The activity to create
	 * @throws IOException If the file can't be written to
	 */
	public void build(File out, ActivityHolder activity) throws IOException {
		/*
		 * The activity name should be in CamelCase, and (in accordance with
		 * tradition and, indeed, state law!) we must change it to snake_case.xml
		 */
		String activityName = activity.getClassName();
		String layoutName = activityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
		String output = outputTemplate.replaceAll("%ACTIVITYNAME%", activityName)
				                      .replaceAll("%LAYOUTNAME%",   layoutName)
				                      .replaceAll("%PACKAGENAME%",  packageName);
		
		// TODO Move IO outside of this function
		FileWriter writer = new FileWriter(out);
		writer.write(output);
		writer.close();
	}
	
}
