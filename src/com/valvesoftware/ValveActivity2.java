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

	static	class PreloadThread implements Runnable {
		PreloadThread() {
		}

		public void run() {
			try {
				Thread.sleep(2000);
				for (String libname : new String[]{"androidwrapper", "tier0", "tierhook" , "vstdlib", "togl", "SDL2", "steam_api", "datacache", "engine", "filesystem_stdio", "GameUI", "inputsystem", "launcher", "materialsystem", "scenefilecache", "ServerBrowser", "soundemittersystem", "studiorender", "vguimatsurface", "video_services", "vphysics", "vgui2", "shaderapidx9", "stdshader_dx9", "client", "server"}) {
					Log.v("ValveActivity2", "Loading " + libname + "...");
					System.loadLibrary(libname);
				}
			} catch (Exception e) {
				Log.e("ValveActivity", "Error loading library: " + e);
			}
			// is it really used? Native part unimplemented on tn8
/*				mPowerServiceClient = new PowerServiceClient();
	int[] powerData = new int[0];

	mPowerServiceClient.sendPowerHint(6, powerData);
	mPowerServiceClient.sendPowerHint(2, powerData);
	mPowerServiceClient.sendPowerHint(12, powerData);*/

		}
	}
	public static void initNatives()
	{
		ApplicationInfo appinf = getContext().getApplicationInfo();
		String gamepath = LauncherActivity.mPref.getString("gamepath", "/sdcard/srceng/");
		String argv = LauncherActivity.mPref.getString("argv", "+developer 1");
		setMainPackFilePath(gamepath + "/main.22.com.nvidia.valvesoftware.halflife2.obb");
		setPatchPackFilePath(gamepath + "/patch.22.com.nvidia.valvesoftware.halflife2.obb");
		setDataDirectoryPath(appinf.dataDir);
		//setNativeLibPath(appinf.nativeLibraryDir);
		//Thread preload = new Thread(new PreloadThread());
		//preload.start();
		//setCacheDirectoryPath(gamepath + "/cache");
		setDocumentDirectoryPath(gamepath);
		setArgs(argv);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		nativeOnActivityResult(this, requestCode, resultCode, data);
	}
}

