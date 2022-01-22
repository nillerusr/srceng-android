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
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;

public class ValveActivity2 { // not activity, i am lazy to change native methods
	private static ValveActivity2 mSingleton;
	public static SharedPreferences mPref;

	public static native void setArgs(String args);
	public static native void setGameDirectoryPath(String path);
	public static native void setDataDirectoryPath(String path);
	public static native int setenv(String name, String value, int overwrite);
	private static native void nativeOnActivityResult(Activity activity, int i, int i2, Intent intent);

	static public void initNatives(Context context, String argv, String gamedir, String gamelibdir, String customVPK) {
		mPref = context.getSharedPreferences("mod", 0);
		ApplicationInfo appinf = context.getApplicationInfo();
		String gamepath = mPref.getString("gamepath", LauncherActivity.getDefaultDir() + "/srceng");

		if( argv == null || argv.isEmpty() )
			argv = mPref.getString("argv", "-console");

		if( gamedir == null || gamedir.isEmpty() )
			gamedir = "hl2";

		argv = "-game "+gamedir+" "+argv;

		if( gamelibdir != null && !gamelibdir.isEmpty() )
			setenv( "APP_MOD_LIB", gamelibdir, 1 );

		String vpks = context.getFilesDir().getPath()+"/"+LauncherActivity.VPK_NAME;
		if( customVPK != null && !customVPK.isEmpty() )
			vpks = customVPK+","+vpks;

		setenv( "EXTRAS_VPK_PATH", vpks, 1 );

		// TODO: set laungage
/*
		String lang = new HashMap<String, String>() {
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

		if (mPref.getBoolean("rodir", false))
			setGameDirectoryPath(LauncherActivity.getADataDir());
		else
			setGameDirectoryPath(gamepath);

		setArgs(argv);
	}
}
