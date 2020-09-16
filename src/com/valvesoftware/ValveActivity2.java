package com.valvesoftware;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.Display;
import android.graphics.Point;
import com.nvidia.PowerServiceClient;
import org.libsdl.app.SDLActivity;
import android.content.Context;
import in.celest.*;
import android.os.Debug;
import com.valvesoftware.GameInfo;
// not activity, just for native functions

public abstract class ValveActivity2 extends SDLActivity {
	private static  PowerServiceClient mPowerServiceClient;
	private static  ValveActivity2 mSingleton;
	public static native void clientCommand(String clientCmd); // a1batross. Requires wrapped libclient. Thread-safe.
	public static native boolean isGameUIActive(); // a1batross. Requires wrapped libclient. 
	public static native boolean shouldDrawControls(); // a1batross. Requires wrapped libclient. 

	public static native void saveGame();

	public static native void setCacheDirectoryPath(String str);

	public static native void setDocumentDirectoryPath(String str);

	public static native void setDropMip(int i);

	public static native void setMainPackFilePath(String str);

	public static native void setPatchPackFilePath(String str);

	public static native void setNativeLibPath(String str);

	public static native void setDataDirectoryPath(String str);

	private static native void nativeOnActivityResult(Activity activity, int i, int i2, Intent intent);

	public static native void TouchEvent( int fingerid, int x, int y, int action );
	public static native int setenv( String name, String value, int overwrire );
	public static native int setLibPath( String path );
	public static native int unsetLibPath( );
	public static native void setArgs( String args );
	public static native void setGame( String game );

	public abstract Class getResourceKeys();

	public abstract String getSourceGame();
	public static int mWidth, mHeight;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                mWidth = size.x;
                mHeight = size.y;
	}

	public static byte[] gpgsDownload(String urlString)
	{
		return new byte[0];
	}

	public static void setEnvs(String str)
	{
		String[] splited = str.split("\\s+");
		for( String i : splited )
		{
			String ass[] = i.split("=");
			if( ass.length > 1)
				setenv(ass[0], ass[1], 1);
		}
	}

	public static void initNatives()
	{
		ApplicationInfo appinf = getContext().getApplicationInfo();
		String gamepath = LauncherActivity.mPref.getString("gamepath", "/sdcard/srceng/");
		String argv = LauncherActivity.mPref.getString("argv", "+developer 1");
		String env = LauncherActivity.mPref.getString("env", "LIBGL_NOVBO=1");
		setMainPackFilePath(gamepath + "/" + GameInfo.main_obb);
		setPatchPackFilePath(gamepath + "/" + GameInfo.patch_obb);
		setGame(GameInfo.mod);
		setDataDirectoryPath(appinf.dataDir);
		setDocumentDirectoryPath(gamepath);
		setArgs(argv);
		setEnvs(env);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		nativeOnActivityResult(this, requestCode, resultCode, data);
	}
}

