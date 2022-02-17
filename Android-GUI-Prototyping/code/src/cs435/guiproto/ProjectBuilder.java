

package cs435.guiproto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Generate the files for an Android project given one or more activities.
 * 
 * This also contains methods for building (via gradle) and installing
 * (via adb) projects, although they are only used in Acceptance tests.
 */
public class ProjectBuilder {
	
	/**
	 * Generate an Android project at the root folder from the given activities.
	 * The root folder should not yet exist.
	 * 
	 * @param folder
	 * @param activities
	 * @throws IOException If the project cannot be created.
	 */
	public static void generateProject(String packageName, Path root, Path sdkRoot, ActivityHolder activities[], boolean abs) throws IOException {
		if (activities.length > 1) {
			System.out.println("WARNING: No support for multiple activities yet!");
		}
		// TODO Add precondition: activities.length > 0
		// TODO Add precondition: project directory must not exist yet
		
		Path resourceDir = Paths.get("/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/Android-GUI-Prototyping/code/resources");                        // set of resources to copy into the project
		Path packageDir  = Paths.get(packageName.replaceAll("\\.", "/")); // package directory (for source code)
		Path sourceDir   = Files.createDirectories(root.resolve("src/main/java/").resolve(packageDir)); // source code dir
		Path layoutDir   = Files.createDirectories(root.resolve("src/main/res/layout"));    // .xml layouts
		Path drawableDir = Files.createDirectories(root.resolve("src/main/res/drawable/")); // drawable resources
		Path valuesDir   = Files.createDirectories(root.resolve("src/main/res/values-v25/"));   // strings, colors, styles
		Path values19Dir   = Files.createDirectories(root.resolve("src/main/res/values/"));   // strings, colors, styles
		Path highResDir  = Files.createDirectories(root.resolve("src/main/res/drawable-xxhdpi")); // background images
		Files.createDirectories(root.resolve("gradle/wrapper"));
		
		// Generate source files
		ActivityHolder activity = activities[0];
		SourceFileBuilder sourceFileBuilder = new SourceFileBuilder(packageName);
		sourceFileBuilder.build(sourceDir.resolve(activity.getClassName() + ".java").toFile(), activity);
		
		// Generate styles
		// TODO When merging in multiple activities, concatenate every activity's views into one big list and pass it
		// to this function once.
		Document styles = StyleBuilder.buildStyleXML(activity.getViewList(), true);
		writeXML(valuesDir.resolve("styles.xml").toFile(), styles);
		
		Document styles2 = StyleBuilder.buildStyleXML(activity.getViewList(), false);
		writeXML(values19Dir.resolve("styles.xml").toFile(), styles2);
		
		// Generate layout files
		Document layout = activity.buildLayoutXMLDocument(abs);
		writeXML(layoutDir.resolve(activity.getLayoutName()).toFile(), layout);
		
		// Generate AndroidManifest.xml
		ManifestBuilder manifestBuilder = new ManifestBuilder(packageName);
		writeXML(root.resolve("src/main/AndroidManifest.xml").toFile(), manifestBuilder.build(activities));
		
		// Generate resources
		activity.generateResources(root);
		
		// Generate/copy in build code, "static" resources
		writeLocalProperties(root.resolve("local.properties").toFile(), sdkRoot);
		writeBuildDotGradle(root.resolve("build.gradle").toFile(), packageName);
		Files.copy(resourceDir.resolve("ic_launcher.png"),           drawableDir.resolve("ic_launcher.png"));
		Files.copy(resourceDir.resolve("gradlew"),                   root.resolve("gradlew"));
		Files.copy(resourceDir.resolve("gradlew.bat"),               root.resolve("gradlew.bat"));
		Files.copy(resourceDir.resolve("gradle-wrapper.jar"),        root.resolve("gradle/wrapper/gradle-wrapper.jar"));
		Files.copy(resourceDir.resolve("gradle-wrapper.properties"), root.resolve("gradle/wrapper/gradle-wrapper.properties"));
	}
	
	/**
	 * Compile a generated android source project into an .apk
	 * @param root
	 * @return Whether or not compilation is successful
	 */
	public static boolean compileProject(Path root) {
		try {
			String[] cmd;
			String osName = System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("windows")) {
				cmd = new String[] {"cmd.exe", "/c", "gradlew.bat", "build"};
			} else {
				cmd = new String[] {"./gradlew", "build", "--info"};
			}
			
			Redirect log = Redirect.to(root.resolve("build.log").toFile());
			Redirect errors = Redirect.to(root.resolve("error.log").toFile());
			System.out.println("Project log: " + log.toString());
			
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(root.toFile());
			pb.redirectOutput(log);
			pb.redirectError(errors);
			Process p = pb.start();
			p.waitFor();
			return p.exitValue() == 0;
		} catch (IOException e) {
			System.out.println("Compilation failed due to IOError: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// Why should this happen?
			System.out.println("Thread interrupted?");
			return false;
		}
	}
	
	private static void writeLocalProperties(File out, Path sdkPath) throws IOException {
		String template = "sdk.dir=" + sdkPath.toAbsolutePath().toString();
		template = template.replace("\\" , "\\\\");
		template = template.replace(":", "\\:");
		
		
		FileWriter writer = new FileWriter(out);
		writer.write(template);
		writer.close();
	}
	
	private static void writeBuildDotGradle(File out, String packageName) throws IOException {
		final String template =
				  "buildscript {\n"
				+ "    repositories {\n"
				+ "        jcenter()\n"
				+ "    }\n"
				+ "\n"
				+ "    dependencies {\n"
				+ "         classpath 'com.android.tools.build:gradle:1.1.3'\n"
				+ "    }\n"
				+ "}\n"
				+ "\n"
				+ "apply plugin: 'com.android.application'\n"
				+ "\n"
				+ "android {\n"
				+ "  compileSdkVersion 26\n"
				+ "  buildToolsVersion \"26.0.0\"\n"
				+ "}\n";
		FileWriter writer = new FileWriter(out);
		writer.write(template);
		writer.close();
	}
	
	private static void writeXML(File out, Document doc) throws IOException {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance()
									  .newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new IOException(e.getMessage());
		}
	}
}

