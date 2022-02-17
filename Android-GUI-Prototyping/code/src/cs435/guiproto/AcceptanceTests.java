package cs435.guiproto;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.xml.sax.SAXException;

/**
 * Builds and generates some of the acceptance tests as .apks, then installs them in the emulator.
 * If INSTALL is set to true, you need to have an emulator running for these tests to work.
 * You can reconfigure some constants below to set the presumed dpi/width/height of the emulator
 *
 * @author bdpowell
 */
public class AcceptanceTests {

	/*
	 * DPI, width, and height of the phone you're installing these to.
	 *
	 * Width and height should have a portrait orientation (width <= height).
         * Right now, these values correspond to the Motorola Nexus 6.
	 */
	private static final float targetWidth  = 1440;
	private static final float targetHeight = 2560;
	
	/*
	 * Set this to true to install the .apks to a running Android emulator
	 */
	private static final boolean INSTALL = true;
	
	private static final Path inDirectory  = Paths.get("acceptance-tests");
	private static final Path outDirectory = Paths.get("acceptance-tests/out");
	private static Path sdkRoot = null;

	/**
	 * Return if this is a Windows system.
	 * If not, we're going to assume it's a UNIX system.
	 */
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}
	
	/**
	 * Clear the directory of old acceptance tests and set the SDK path.
	 */
	public static void setUpTestsAndSdk() {
		// Delete old files
		try {
			// Code stolen from http://stackoverflow.com/questions/779519/delete-directories-recursively-in-java/27917071#27917071
			Files.walkFileTree(outDirectory, new SimpleFileVisitor<Path>() {
			   @Override
			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			       Files.delete(file);
			       return FileVisitResult.CONTINUE;
			   }

			   @Override
			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			       Files.delete(dir);
			       return FileVisitResult.CONTINUE;
			   }
			});
		} catch (IOException e) {
			System.out.println("Couldn't remove old dirs: ");
			e.printStackTrace();
		}
		
		// Create new folder
		System.out.println("Output files: " + outDirectory.toString());
		try {
			Files.createDirectory(outDirectory);
		} catch (IOException e) {
			System.out.println("Warning: directory already exists");
		}
		
		// Get SDK location. Use cs435.extra.GetUserName to see your name
		String username = System.getProperty("user.name");
		if (username.equals("bdpowell")) {
			// Ben's Linux
			sdkRoot = Paths.get("/home/bdpowell/Android/Sdk");
		} else if (username.equals("Ben")) {
			// Ben's Windows
			sdkRoot = Paths.get("C:/Users/Ben/AppData/Local/Android/sdk");
		} else {
			// All the other plebs
			System.out.println("Enter the location of your Android SDK:");
			sdkRoot = Paths.get(new Scanner(System.in).nextLine());
		}
	}
	
	/**
	 * Build and compile a test from scratch, then try to install it in a running emulator.
	 * 
	 * This may take upwards of 30 seconds to complete, so be patient. Some errors/messages are redirected
	 * to logs in the created projects, so check there if something goes wrong.
	 * 
	 * @param uxDump Path to UXDump
	 * @param screenshot Path to screenshot file
	 * @param projectOut Output directory of project
	 * @param projectName Name of main activity
	 * @param packageName Name of package for generated source code
	 */
	public static void buildCompileAndInstallProject(Path uxDump, Path screenshot, Path projectOut,
			String projectName, String packageName, double dpi, float inwidth, float inheight) {
		
		Constants.setGUIconstants(dpi, inwidth, inheight, targetWidth, targetHeight);
		
		try {
			ActivityHolder act = XMLParser.parseActivityFromFile(uxDump, screenshot, projectName);
			ProjectBuilder.generateProject(packageName, projectOut, sdkRoot, new ActivityHolder[] {act}, false);
			
			boolean result = ProjectBuilder.compileProject(projectOut);
			if (!result) {
				throw new IllegalStateException("Project" + projectName + "failed to build, see error.log for more");
			}
			
			// Install project.
			// On Windows: cmd.exe /c adb.exe uninstall packageName; cmd.exe /c adb.exe install debug.apk
			// On Mac/Linux: adb uninstall packageName; adb install debug.apk
			if (INSTALL) {
				Path adb = sdkRoot.resolve("platform-tools/");
				if (isWindows()) {
					adb = adb.resolve("adb.exe");
				} else {
					adb = adb.resolve("adb");
				}
				
				Path debugApk = Paths.get(projectName + "/build/outputs/apk/" + projectName + "-debug.apk");
				String[] uninstallCommand = new String[] { adb.toAbsolutePath().toString(), "uninstall", packageName };
				String[] installCommand   = new String[] { adb.toAbsolutePath().toString(), "install",   debugApk.toString() };
				String[][] commands = new String[][] {uninstallCommand, installCommand};
				if (isWindows()) {
					commands[0] = Stream.concat(Arrays.stream(commands[0]), Arrays.stream(new String[] {"cmd.exe", "/c"})).toArray(String[]::new);
					commands[1] = Stream.concat(Arrays.stream(commands[0]), Arrays.stream(new String[] {"cmd.exe", "/c"})).toArray(String[]::new);
				}
				
				for (String[] command : commands) {
					Process p;
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.directory(outDirectory.toFile());
					pb.redirectOutput(Redirect.INHERIT);
					pb.redirectError(Redirect.INHERIT);
					p = pb.start();
					p.waitFor();
				}
			} else {
				System.out.println("Skipping .apk install (see INSTALL variable)");
			}
		} catch (IOException e) {
			System.out.println("IO Exception while building/compiling project");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SAXException while building/compiling project");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Process interrupted while building/compiling project");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, SAXException {
		setUpTestsAndSdk();

		final double kevinsDpi   = 560;
		final float kevinsWidth  = 1440;
		final float kevinsHeight = 2560;
		final double jacobsDpi = 594;
		final float jacobsWidth  = 1440;
		final float jacobsHeight = 2560;
		
		buildCompileAndInstallProject(
				inDirectory.resolve("calendar/calendar.xml"),
				inDirectory.resolve("calendar/calendar.png"),
				outDirectory.resolve("Calendar"),
				"Calendar",
				"proto.calendar",
				jacobsDpi, 
				jacobsWidth,
				jacobsHeight
		);
		buildCompileAndInstallProject(
				inDirectory.resolve("clock/clock.xml"),
				inDirectory.resolve("clock/clock.png"),
				outDirectory.resolve("Clock"),
				"Clock",
				"proto.clock",
				jacobsDpi, 
				jacobsWidth,
				jacobsHeight
		);
		buildCompileAndInstallProject(
				inDirectory.resolve("date-time/datetime.xml"),
				inDirectory.resolve("date-time/datetime.png"),
				outDirectory.resolve("Datetime"),
				"Datetime",
				"proto.datetime",
				jacobsDpi, 
				jacobsWidth,
				jacobsHeight
		);
		buildCompileAndInstallProject(
				inDirectory.resolve("samsung-help/samsunghelp.xml"),
				inDirectory.resolve("samsung-help/samsunghelp.png"),
				outDirectory.resolve("Samsunghelp"),
				"Samsunghelp",
				"proto.samsung.help",
				jacobsDpi, 
				jacobsWidth,
				jacobsHeight
		);
		buildCompileAndInstallProject(
				inDirectory.resolve("samsung-message/samsungmessage.xml"),
				inDirectory.resolve("samsung-message/samsungmessage.png"),
				outDirectory.resolve("Samsungmessage"),
				"Samsungmessage",
				"proto.samsung.message",
				jacobsDpi, 
				jacobsWidth,
				jacobsHeight
		);
		buildCompileAndInstallProject(
			inDirectory.resolve("com.evancharlton.mileage/ui-dump.xml"),
			inDirectory.resolve("com.evancharlton.mileage/screen.png"),
			outDirectory.resolve("Mileage"),
			"Mileage",
			"proto.mileage",
			kevinsDpi,
			kevinsWidth,
			kevinsHeight
		);
		
		buildCompileAndInstallProject(
			inDirectory.resolve("com.morphoss.acal/ui-dump.xml"),
			inDirectory.resolve("com.morphoss.acal/screen.png"),
			outDirectory.resolve("Acal"),
			"Acal",
			"proto.acal",
			kevinsDpi,
			kevinsWidth,
			kevinsHeight
		);
		
		buildCompileAndInstallProject(
			inDirectory.resolve("MyExpenses-1/ui-dump.uix"),
			inDirectory.resolve("MyExpenses-1/screen.png"),
			outDirectory.resolve("Expense"),
			"Expense",
			"proto.expense",
			kevinsDpi,
			kevinsWidth,
			kevinsHeight
		);
      	
      	// TODO Figure out why this crashes
//		buildCompileAndInstallProject(
//				inDirectory.resolve("MyExpenses-2/ui-dump-1.xml"),
//				inDirectory.resolve("MyExpenses-2/screen-1.png"),
//				outDirectory.resolve("Expenses2"),
//				"Expenses2",
//				"proto.expense2"	
//		);
      	
		buildCompileAndInstallProject(
			inDirectory.resolve("MyExpenses-2/ui-dump-2.xml"),
			inDirectory.resolve("MyExpenses-2/screen-2.png"),
			outDirectory.resolve("Expenses3"),
			"Expenses3",
			"proto.expense3",
			kevinsDpi,
			kevinsWidth,
			kevinsHeight
		);
	}
}
