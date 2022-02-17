package test.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.semeru.android.core.helpers.AAPTParser;
import edu.semeru.android.core.helpers.AAPTParser.ApkInfo;

public class AAPTParserTest {
	private static String aaptPath = "Testing/aapt";
	private Map<String, ApkInfo> oracle;
	private AAPTParser parser;
	
	@Before
	public void setup() {
		parser = new AAPTParser();
        oracle = new HashMap<String, ApkInfo>();
        Type type = new TypeToken<Map<String, ApkInfo>>(){}.getType();
        try {
        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        	oracle = gson.fromJson(new FileReader("Testing/apkinfo"), type);
        } catch (FileNotFoundException e) {
        	System.err.println("Missing test oracle!!!");
        	fail();
        }
	}
	
	
	@Test
	public void test() {
		String[] apks = new File("Testing/apks").list();
		for (String apk : apks) {
			if (!oracle.containsKey(apk)) {
				System.err.println("Apk '" + apk + "' not in test oracle!!!");
				continue;
			}
			ApkInfo info = parser.analyzeApk(aaptPath, "Testing/apks/" + apk);
			assertTrue("Parsed apk does not match oracle: " + apk + "!!!", oracle.get(apk).equals(info));
		}
	}

}
