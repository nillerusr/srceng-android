package com.valvesoftware;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import com.nvidia.PowerServiceClient;
import java.util.HashMap;
import java.util.Locale;
import org.libsdl.app.SDLActivity;
import in.celest.LauncherActivity;

public abstract class ValveActivity2 extends SDLActivity {
    public static int mHeight;
    private static PowerServiceClient mPowerServiceClient;
    private static ValveActivity2 mSingleton;
    public static int mWidth;

    public static native void TouchEvent(int i, float f, float f2, int i2);

    public static native void clientCommand(String str);

    public static native boolean isGameUIActive();

    private static native void nativeOnActivityResult(Activity activity, int i, int i2, Intent intent);

    public static native void saveGame();

    public static native void setArgs(String str);

    public static native void setCacheDirectoryPath(String str);

    public static native void setDataDirectoryPath(String str);

    public static native void setDocumentDirectoryPath(String str);

    public static native void setDropMip(int i);

    public static native void setGame(String str);

    public static native void setLanguage(String str);

    public static native int setLibPath(String str);

    public static native void setMainPackFilePath(String str);

    public static native void setNativeLibPath(String str);

    public static native void setPatchPackFilePath(String str);

    public static native int setenv(String str, String str2, int i);

    public static native boolean shouldDrawControls();

    public static native void showTouch(boolean z);

    public static native int unsetLibPath();

    public static native void setExtrasPackFilePath(String str);

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

    public static byte[] gpgsDownload(String urlString) {
        return new byte[0];
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
	int game = LauncherActivity.mPref.getInt("game", GameInfo.GAME_HL2);
        setDataDirectoryPath(appinf.dataDir);
        showTouch(LauncherActivity.mPref.getBoolean("show_touch", true));
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
            }
        }.get(Locale.getDefault().getISO3Language());
        if (lang != null) {
            setLanguage(lang);
        }
        if (LauncherActivity.mPref.getBoolean("rodir", false)) {
            setDocumentDirectoryPath(LauncherActivity.getADataDir());
        } else {
            setDocumentDirectoryPath(gamepath);
        }
        setArgs(argv);
        setEnvs(env);

        if( game == GameInfo.GAME_HL2 )
        {
            setMainPackFilePath(gamepath + "/" + GameInfo.hl2.main_obb);
            setPatchPackFilePath(gamepath + "/" + GameInfo.hl2.patch_obb);
            setGame(GameInfo.hl2.mod);
            setenv("LIBRARY_SERVER", GameInfo.hl2.server, 1);
            setenv("LIBRARY_CLIENT", GameInfo.hl2.client, 1);
        }
        else if( game == GameInfo.GAME_HL2EP1 )
        {
            setMainPackFilePath(gamepath + "/" + GameInfo.hl2ep1.main_obb);
            setPatchPackFilePath(gamepath + "/" + GameInfo.hl2ep1.patch_obb);
            setGame(GameInfo.hl2ep1.mod);
            setenv("LIBRARY_SERVER", GameInfo.hl2ep1.server, 1);
            setenv("LIBRARY_CLIENT", GameInfo.hl2ep1.client, 1);
        }
        else if( game == GameInfo.GAME_HL2EP2 )
        {
            setMainPackFilePath(gamepath + "/" + GameInfo.hl2ep2.main_obb);
            setPatchPackFilePath(gamepath + "/" + GameInfo.hl2ep2.patch_obb);
            setExtrasPackFilePath(gamepath + "/" + GameInfo.hl2ep2.extras_obb);
            setGame(GameInfo.hl2ep2.mod);
            setenv("LIBRARY_SERVER", GameInfo.hl2ep2.server, 1);
            setenv("LIBRARY_CLIENT", GameInfo.hl2ep2.client, 1);
        }
        else if( game == GameInfo.GAME_PORTAL )
        {
            setMainPackFilePath(gamepath + "/" + GameInfo.portal.main_obb);
            setPatchPackFilePath(gamepath + "/" + GameInfo.portal.patch_obb);
            setGame(GameInfo.portal.mod);
            setenv("LIBRARY_SERVER", GameInfo.portal.server, 1);
            setenv("LIBRARY_CLIENT", GameInfo.portal.client, 1);
        }

	if( LauncherActivity.found_main_obb != null )
            setMainPackFilePath(gamepath + "/" + LauncherActivity.found_main_obb);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        nativeOnActivityResult(this, requestCode, resultCode, data);
    }
}
