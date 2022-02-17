package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import edu.semeru.android.core.helpers.device.DeviceHelper;
import edu.semeru.android.core.helpers.device.DeviceHelper.DeviceHelperBuilder;

public class TestCompareHierarchies {

    public static void main(String[] args) {
        // String execute;
        // execute = TerminalHelper.executeCommand("cd
        // /scratch/mjcurcio.scratch/SEMERUdata/ReDraw-Output;"
        // + "/scratch/mjcurcio.scratch/SEMERUdata/ReDraw-Output/gradlew
        // build");
        // System.out.println("executeCommand: " + execute);

        String sdkPath = "/Users/semeru/Applications/android-sdk";

        DeviceHelperBuilder builder = new DeviceHelperBuilder(sdkPath, "09103097");
        DeviceHelper deviceHelper = builder.buildDevice();

        String outputRoot = "/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation";
        String packageName = outputRoot + File.separator + "apks";

        String apksPath = "";

        Collection<File> apks = FileUtils.listFiles(new File(apksPath), new String[] { "apk" }, true);

        for (File apk : apks) {

            deviceHelper.unInstallAndInstallApp(apk.getAbsolutePath(), packageName);
            deviceHelper.startAPK("edu.wm.semeru.remaui_app", "edu.wm.semeru.remaui_app.MainActivity");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            deviceHelper.getAndPullUIDump(outputRoot, "ui-dump.xml");
            break;
        }
    }
}
