package com.valvesoftware;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import java.util.HashMap;
import java.util.Locale;
import org.libsdl.app.SDLActivity;
import me.nillerusr.LauncherActivity;

public abstract class ValveActivity2 extends SDLActivity {
	public static int mHeight;
	private static ValveActivity2 mSingleton;
	public static int mWidth;

	public static native void setArgs(String args);
	public static native void setGameDirectoryPath(String path);
	public static native void setDataDirectoryPath(String path);
	public static native int setenv(String name, String value, int overwrite);
	private static native void nativeOnActivityResult(Activity activity, int i, int i2, Intent intent);

	public abstract Class getResourceKeys();
	public abstract String getSourceGame();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mWidth = size.x;
		mHeight = size.y;
	}

	public static void setEnvs(String str) {
		for (String i : str.split("\\s+")) {
			String[] ass = i.split("=");
			if (ass.length > 1) {
				setenv(ass[0], ass[1], 1);
			}
		}
	}

	public static void initNatives() {
		ApplicationInfo appinf = getContext().getApplicationInfo();
		String gamepath = LauncherActivity.mPref.getString("gamepath", LauncherActivity.getDefaultDir() + "/srceng");
		String argv = LauncherActivity.mPref.getString("argv", "-console");
		String env = LauncherActivity.mPref.getString("env", "LIBGL_USEVBO=0");

		// TODO: set laungage

/*		String lang = new HashMap<String, String>() {
			{
				put("rus", "russian");
				put("bul", "bulgarian");
				put("cze", "czech");
				put("ces", "czech");
				put("dan", "danish");
				put("dum", "dutch");
				put("dut", "dutch");
				put("nld", "dutch");
				put("fin", "finnish");
				put("gem", "german");
				put("ger", "german");
				put("deu", "german");
				put("grc", "greek");
				put("gre", "greek");
				put("ell", "greek");
				put("hun", "hungarian");
				put("ita", "italian");
				put("jpn", "japanese");
				put("kor", "korean");
				put("nno", "norwegian");
				put("nob", "norwegian");
				put("nor", "norwegian");
				put("pol", "polish");
				put("cpp", "portuguese");
				put("por", "portuguese");
				put("rum", "romanian");
				put("ron", "romanian");
				put("rup", "romanian");
				put("spa", "spanish");
				put("swe", "swedish");
				put("chi", "tchinese");
				put("zho", "tchinese");
				put("chi", "tchinese");
				put("tha", "thai");
				put("tur", "turkish");
				put("crh", "turkish");
				put("ota", "turkish");
				put("ukr", "ukrainian");
			}
		}.get(Locale.getDefault().getISO3Language());

		if (lang != null)
			setLanguage(lang);*/

		setDataDirectoryPath(appinf.dataDir);

		if (LauncherActivity.mPref.getBoolean("rodir", false))
			setGameDirectoryPath(LauncherActivity.getADataDir());
		else
			setGameDirectoryPath(gamepath);

		setArgs(argv);
		setEnvs(env);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		nativeOnActivityResult(this, requestCode, resultCode, data);
	}
}
