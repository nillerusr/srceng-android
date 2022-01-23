package com.valvesoftware;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import java.util.HashMap;
import java.io.File;
import java.util.Locale;
import org.libsdl.app.SDLActivity;
import me.nillerusr.LauncherActivity;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;
import me.nillerusr.ExtractAssets;

public class ValveActivity2 { // not activity, i am lazy to change native methods
	private static Activity mSingleton;
	public static SharedPreferences mPref;


	public static native void setArgs(String args);
	public static native void setGameDirectoryPath(String path);
	public static native void setDataDirectoryPath(String path);
	public static native int setenv(String name, String value, int overwrite);
	private static native void nativeOnActivityResult(Activity activity, int i, int i2, Intent intent);

	public static boolean findGameinfo(String path)
	{
		File dir = new File(path);
		if( !dir.isDirectory() )
			return false;

		for( File file : dir.listFiles() )
		{
			if( file.isDirectory() )
			{
				for( File f : file.listFiles() )
				{
					if( f.getName().toLowerCase().equals("gameinfo.txt") )
						return true;
				}
			}
		}

		return false;
	}

	static public boolean preInit(Context context)
	{
		mPref = context.getSharedPreferences("mod", 0);
		String gamepath = mPref.getString("gamepath", LauncherActivity.getDefaultDir() + "/srceng");

		if( !findGameinfo(gamepath) )
			return false;

		return true;
	}

	static public void initNatives(Context context, Intent intent) {
		mPref = context.getSharedPreferences("mod", 0);
		ApplicationInfo appinf = context.getApplicationInfo();
		String gamepath = mPref.getString("gamepath", LauncherActivity.getDefaultDir() + "/srceng");

		String argv = intent.getStringExtra("id");
		String gamedir = intent.getStringExtra("gamedir");
		String gamelibdir = intent.getStringExtra("gamelibdir");
		String customVPK = intent.getStringExtra("vpk");

		if( argv == null || argv.isEmpty() )
			argv = mPref.getString("argv", "-console");

		if( gamedir == null || gamedir.isEmpty() )
			gamedir = "hl2";

		argv += "-game "+gamedir;

		if( gamelibdir != null && !gamelibdir.isEmpty() )
			setenv( "APP_MOD_LIB", gamelibdir, 1 );

		ExtractAssets.extractVPK(context, false);

		String vpks = context.getFilesDir().getPath()+"/"+ExtractAssets.VPK_NAME;
		if( customVPK != null && !customVPK.isEmpty() )
			vpks = customVPK+","+vpks;

		setenv( "EXTRAS_VPK_PATH", vpks, 1 );

		// TODO: set laungage
		//setLanguage(Locale.getDefault().toString());

		setDataDirectoryPath(appinf.dataDir);

		if (mPref.getBoolean("rodir", false))
			setGameDirectoryPath(LauncherActivity.getAndroidDataDir());
		else
			setGameDirectoryPath(gamepath);

		setArgs(argv);
	}
}
