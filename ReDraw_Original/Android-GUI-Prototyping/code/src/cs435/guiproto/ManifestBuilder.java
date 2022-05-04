package cs435.guiproto;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates AndroidManifest.xml, which controls how the app is launched and installed.
 * 
 * @author bdpowell
 *
 */
public class ManifestBuilder {
	
	private String packageName;
	
	public ManifestBuilder(String packageName) {
		this.packageName= packageName;
	}
	
	public Document build(ActivityHolder[] activities) {
		try {
			Document out = DocumentBuilderFactory.newInstance()
					       .newDocumentBuilder()
					       .newDocument();
			
			// <manifest>
			Element manifest = out.createElement("manifest");
			manifest.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
			manifest.setAttribute("package", packageName);
			
			// <uses-sdk />
			Element apkVersion = out.createElement("uses-sdk");
			apkVersion.setAttribute("android:minSdkVersion", "19");
			apkVersion.setAttribute("android:targetSdkVersion", "19");
			apkVersion.setAttribute("android:maxSdkVersion", "25");
			manifest.appendChild(apkVersion);
			
			// <application>
			Element application = out.createElement("application");
			application.setAttribute("android:label", packageName);
			application.setAttribute("android:theme", "@style/AppTheme");
			application.setAttribute("android:icon", "@drawable/ic_launcher");
			
			if (activities.length > 1) {
				System.out.println("Warning: multi-activity support not implemented");
			}
			
			//<activity>
			ActivityHolder holder = activities[0];
			Element activity = out.createElement("activity");
			activity.setAttribute("android:name", "." + holder.getClassName());
			application.appendChild(activity);
			
			//<intent-filter>
			Element intentHolder = out.createElement("intent-filter");
			//<action />
			Element intentAction = out.createElement("action");
			intentAction.setAttribute("android:name", "android.intent.action.MAIN");
			intentHolder.appendChild(intentAction);
			//<category />
			Element intentCategory = out.createElement("category");
			intentCategory.setAttribute("android:name", "android.intent.category.LAUNCHER");
			//</intent-fitler>
			intentHolder.appendChild(intentCategory);
			
			//</activity>
			activity.appendChild(intentHolder);
			
			//</application>
			manifest.appendChild(application);
			//</manifest>
			out.appendChild(manifest);
			
			return out;	
		} catch (ParserConfigurationException e) {
			System.out.println("Could not make a DocumentBuilder. Make sure you've got Java installed right");
			e.printStackTrace();
			return null;
		}
	}
	
}
