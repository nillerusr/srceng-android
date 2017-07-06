package com.valvesoftware;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;
import android.view.InputDevice;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.nvidia.PowerServiceClient;
import java.util.HashMap;
import java.util.Map;
import org.libsdl.app.SDLActivity;

public class ValveActivity extends SDLActivity implements IDownloaderClient {
    private static ValveActivity mSingleton;
    private static boolean mUseGooglePlayDownloader;
    private BroadcastReceiver mCustomDownloaderReceiverProgress;
    private BroadcastReceiver mCustomDownloaderReceiverStatus;
    private int mDataVersion;
    private IStub mDownloaderClientStub;
    private ValveDownloader mDownloaderView;
    private long mMainDataSize;
    private long mPatchDataSize;
    private PowerServiceClient mPowerServiceClient;
    private Thread mPreLoadThread;
    private IDownloaderService mRemoteService;

    /* renamed from: com.valvesoftware.ValveActivity.1 */
    class C00151 extends BroadcastReceiver {
        C00151() {
        }

        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int status = bundle.getInt(ValveCustomDownloaderService.STATUS);
                Log.v("mCustomDownloaderReceiverStatus", "status = " + status);
                ValveActivity.mSingleton.onDownloadStateChanged(status);
            }
        }
    }

    /* renamed from: com.valvesoftware.ValveActivity.2 */
    class C00162 extends BroadcastReceiver {
        C00162() {
        }

        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                long bytesTotal = bundle.getLong(ValveCustomDownloaderService.BYTES_TOTAL);
                ValveActivity.this.mDownloaderView.updateProgress(bundle.getLong(ValveCustomDownloaderService.BYTES_DOWNLOADED), bytesTotal);
            }
        }
    }

    /* renamed from: com.valvesoftware.ValveActivity.3 */
    class C00173 implements OnClickListener {
        C00173() {
        }

        public void onClick(DialogInterface dialog, int id) {
            ValveActivity.mSingleton.startDownloader();
        }
    }

    /* renamed from: com.valvesoftware.ValveActivity.4 */
    class C00184 implements OnClickListener {
        C00184() {
        }

        public void onClick(DialogInterface dialog, int id) {
            ValveActivity.mSingleton.finish();
            System.exit(0);
        }
    }

    class PreloadThread implements Runnable {
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
        }
    }

    public static native void saveGame();

    public static native void setCacheDirectoryPath(String str);

    public static native void setDocumentDirectoryPath(String str);

    public static native void setDropMip(int i);

    public static native void setMainPackFilePath(String str);

    public static native void setPatchPackFilePath(String str);

    public ValveActivity() {
        this.mCustomDownloaderReceiverStatus = new C00151();
        this.mCustomDownloaderReceiverProgress = new C00162();
    }

    static {
        mUseGooglePlayDownloader = false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        mSingleton = this;
        setCacheDirectoryPath(getCacheDir().getAbsolutePath());
        setDocumentDirectoryPath(getFilesDir().getAbsolutePath());
        MemoryInfo meminfo = new MemoryInfo();
        ((ActivityManager) getSystemService("activity")).getMemoryInfo(meminfo);
        Log.v("ValveActivity", "Total System Memory     " + meminfo.totalMem + " bytes");
        Log.v("ValveActivity", "Available System Memory " + meminfo.availMem + " bytes");
    }

    protected void onStart() {
        if (this.mDownloaderClientStub != null) {
            this.mDownloaderClientStub.connect(this);
        }
        super.onStart();
    }

    protected void onResume() {
        if (this.mDownloaderClientStub != null) {
            this.mDownloaderClientStub.connect(this);
        }
        super.onResume();
        registerReceiver(this.mCustomDownloaderReceiverStatus, new IntentFilter(getApplicationContext().getPackageName() + ValveCustomDownloaderService.NOTIFICATION_STATUS));
        registerReceiver(this.mCustomDownloaderReceiverProgress, new IntentFilter(getApplicationContext().getPackageName() + ValveCustomDownloaderService.NOTIFICATION_PROGRESS));
    }

    protected void onPause() {
        saveGame();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        super.onPause();
        unregisterReceiver(this.mCustomDownloaderReceiverStatus);
        unregisterReceiver(this.mCustomDownloaderReceiverProgress);
    }

    protected void onStop() {
        if (this.mDownloaderClientStub != null) {
            this.mDownloaderClientStub.disconnect(this);
        }
        super.onStop();
    }

    public void onServiceConnected(Messenger m) {
        Log.v("ValveActivity", "onServiceConnected()");
        this.mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        this.mRemoteService.onClientUpdated(this.mDownloaderClientStub.getMessenger());
    }

    public void onDownloadStateChanged(int newState) {
        Log.v("ValveActivity", "onDownloadStateChanged(" + newState + ")");
        this.mDownloaderView.updateState(newState);
    }

    public void onDownloadProgress(DownloadProgressInfo progress) {
        Log.v("ValveActivity", "onDownloadProgress()");
        this.mDownloaderView.updateProgress(progress);
    }

    public static void pauseDownload() {
        ValveActivity valveActivity = mSingleton;
        if (mUseGooglePlayDownloader && mSingleton != null && mSingleton.mRemoteService != null) {
            Log.v("ValveActivity", "pauseDownload()");
            mSingleton.mRemoteService.requestPauseDownload();
        }
    }

    public static void resumeDownload() {
        ValveActivity valveActivity = mSingleton;
        if (!mUseGooglePlayDownloader) {
            mSingleton.startCustomDownloader();
        } else if (mSingleton != null && mSingleton.mRemoteService != null) {
            Log.v("ValveActivity", "resumeDownload()");
            mSingleton.mRemoteService.requestContinueDownload();
        }
    }

    public static void retryDownload() {
        ValveActivity valveActivity = mSingleton;
        if (mUseGooglePlayDownloader) {
            resumeDownload();
        } else {
            mSingleton.startCustomDownloader();
        }
    }

    protected void checkAndStart(int dataVersion, long mainSize, long patchSize) {
        this.mDataVersion = dataVersion;
        this.mMainDataSize = mainSize;
        this.mPatchDataSize = patchSize;
        if (isGameControllerConnected()) {
            startDownloader();
            return;
        }
        Builder builder = new Builder(this);
        builder.setMessage("This game requires a game controller. Please connect one now.");
        builder.setPositiveButton("Ignore", new C00173());
        builder.setNegativeButton("Quit", new C00184());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void startPreload() {
        if (this.mPreLoadThread != null) {
        }
    }

    private void finishPreload() {
        if (this.mPreLoadThread != null) {
            try {
                this.mPreLoadThread.join();
                this.mPreLoadThread = null;
            } catch (Exception e) {
                Log.e("ValveActivity", "Join failed: " + e);
            }
        }
    }

    private boolean isGameControllerConnected() {
        for (int deviceId : InputDevice.getDeviceIds()) {
            int sources = InputDevice.getDevice(deviceId).getSources();
            if ((sources & 1025) == 1025 || (sources & 16777232) == 16777232) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDownloadRequired() {
        int dataVersion = this.mDataVersion;
        long mainSize = this.mMainDataSize;
        long patchSize = this.mPatchDataSize;
        Log.v("ValveActivity", "checkDownloadRequired(" + dataVersion + ", " + mainSize + ", " + patchSize + ")");
        String mainName = Helpers.getExpansionAPKFileName(this, true, dataVersion);
        String patchName = Helpers.getExpansionAPKFileName(this, false, dataVersion);
        setMainPackFilePath(Helpers.generateSaveFileName(this, mainName));
        setPatchPackFilePath(Helpers.generateSaveFileName(this, patchName));
        if (Helpers.doesFileExist(this, mainName, mainSize, false) && Helpers.doesFileExist(this, patchName, patchSize, false)) {
            return false;
        }
        return true;
    }

    private void startGooglePlayDownloader() {
        try {
            Intent launchIntent = getIntent();
            Intent intentToLauchThisActivityFromNotificiation = new Intent(this, getClass());
            intentToLauchThisActivityFromNotificiation.setFlags(335544320);
            intentToLauchThisActivityFromNotificiation.setAction(launchIntent.getAction());
            if (launchIntent.getCategories() != null) {
                for (String category : launchIntent.getCategories()) {
                    intentToLauchThisActivityFromNotificiation.addCategory(category);
                }
            }
            int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired((Context) this, PendingIntent.getActivity(this, 0, intentToLauchThisActivityFromNotificiation, 134217728), ValveDownloaderService.class);
            Log.v("ValveActivity", "startResult = " + startResult);
            if (startResult == 0) {
                startVideo();
                return;
            }
            this.mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, ValveDownloaderService.class);
            this.mDownloaderView = new ValveDownloader(this);
            setContentView(this.mDownloaderView);
            this.mDownloaderClientStub.connect(this);
        } catch (NameNotFoundException e) {
            Log.e("ValveActivity", "Cannot find own package!");
        }
    }

    private void startCustomDownloader() {
        Map<String, String> prefixMap = new HashMap();
        prefixMap.put("com.nvidia.valvesoftware.halflife2", "http://download.nvidia.com/tegrazone/payload/valve/halflife2/");
        prefixMap.put("com.nvidia.valvesoftware.halflife2ep1", "http://download.nvidia.com/tegrazone/payload/valve/halflife2ep1/");
        prefixMap.put("com.nvidia.valvesoftware.halflife2ep2", "http://download.nvidia.com/tegrazone/payload/valve/halflife2ep2/");
        prefixMap.put("com.nvidia.valvesoftware.portal", "http://download.nvidia.com/tegrazone/payload/valve/portal/");
        String obbRoot = Helpers.getSaveFilePath(this);
        String mainName = Helpers.getExpansionAPKFileName(this, true, this.mDataVersion);
        String patchName = Helpers.getExpansionAPKFileName(this, false, this.mDataVersion);
        String urlPrefix = (String) prefixMap.get(getPackageName());
        if (urlPrefix.length() == 0) {
            Log.e("ValveActivity", "unable to retrieve prefix for package " + getPackageName());
        }
        String mainURLPath = urlPrefix + mainName;
        String patchURLPath = urlPrefix + patchName;
        Log.v("ValveActivity", "startCustomDownloader obbRoot      = " + obbRoot);
        Log.v("ValveActivity", "startCustomDownloader mainName     = " + mainName);
        Log.v("ValveActivity", "startCustomDownloader patchName    = " + patchName);
        Intent intent = new Intent(this, ValveCustomDownloaderService.class);
        intent.putExtra(ValveCustomDownloaderService.OBB_ROOT, obbRoot);
        intent.putExtra(ValveCustomDownloaderService.MAIN_NAME, mainName);
        intent.putExtra(ValveCustomDownloaderService.MAIN_URL, mainURLPath);
        intent.putExtra(ValveCustomDownloaderService.MAIN_SIZE, this.mMainDataSize);
        intent.putExtra(ValveCustomDownloaderService.PATCH_NAME, patchName);
        intent.putExtra(ValveCustomDownloaderService.PATCH_URL, patchURLPath);
        intent.putExtra(ValveCustomDownloaderService.PATCH_SIZE, this.mPatchDataSize);
        startService(intent);
        this.mDownloaderView = new ValveDownloader(this);
        setContentView(this.mDownloaderView);
    }

    public static void startDownloader() {
        if (!mSingleton.checkDownloadRequired()) {
            startVideo();
        } else if (mUseGooglePlayDownloader) {
            mSingleton.startGooglePlayDownloader();
        } else {
            mSingleton.startCustomDownloader();
        }
    }

    public static void startVideo() {
        Log.v("ValveActivity", "startVideo");
        mSingleton.setContentView(new ValveSplashScreen(mSingleton, mSingleton.getAssets()));
        if (mSingleton.mDownloaderView != null) {
            mSingleton.mDownloaderView = null;
        }
        mSingleton.startPreload();
    }

    public static void startGame() {
        mSingleton.finishPreload();
        SDLActivity.startSDL();
        if (mSingleton.mPowerServiceClient != null) {
            int[] powerData = new int[0];
            mSingleton.mPowerServiceClient.sendPowerHint(6, powerData);
            mSingleton.mPowerServiceClient.sendPowerHint(2, powerData);
            mSingleton.mPowerServiceClient.sendPowerHint(12, powerData);
        }
    }
}
