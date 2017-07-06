package com.valvesoftware;

import android.util.Log;
import com.nvidia.PowerServiceClient;

// not activity, just for native functions

public class ValveActivity {
    private static  PowerServiceClient mPowerServiceClient;

    public static native void saveGame();

    public static native void setCacheDirectoryPath(String str);

    public static native void setDocumentDirectoryPath(String str);

    public static native void setDropMip(int i);

    public static native void setMainPackFilePath(String str);

    public static native void setPatchPackFilePath(String str);

    static    class PreloadThread implements Runnable {
        PreloadThread() {
        }

        public void run() {
            try {
                Thread.sleep(2000);
                for (String libname : new String[]{"androidwrapper", "tier0", "vstdlib", "togl", "SDL2", "steam_api", "datacache", "engine", "filesystem_stdio", "GameUI", "inputsystem", "launcher", "materialsystem", "scenefilecache", "ServerBrowser", "soundemittersystem", "studiorender", "vguimatsurface", "video_services", "vphysics", "vgui2", "shaderapidx9", "stdshader_dx9", "client", "server"}) {
                    Log.v("ValveActivity", "Loading " + libname + "...");
                    System.loadLibrary(libname);
                }
            } catch (Exception e) {
                Log.e("ValveActivity", "Error loading library: " + e);
            }
            // is it really used? Native part unimplemented on tn8
/*                mPowerServiceClient = new PowerServiceClient();
    int[] powerData = new int[0];

    mPowerServiceClient.sendPowerHint(6, powerData);
    mPowerServiceClient.sendPowerHint(2, powerData);
    mPowerServiceClient.sendPowerHint(12, powerData);*/


        }
    }

    public static void initNatives()
    {
        setMainPackFilePath("/sdcard/srceng/main.22.com.nvidia.valvesoftware.halflife2.obb");
        setPatchPackFilePath("/sdcard/srceng/patch.22.com.nvidia.valvesoftware.halflife2.obb");
        Thread preload = new Thread(new PreloadThread());
        preload.start();
        setCacheDirectoryPath("/sdcard/srceng/cache");
        setDocumentDirectoryPath("/sdcard/srceng/documents");
    }
}
