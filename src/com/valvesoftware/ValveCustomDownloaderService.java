package com.valvesoftware;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.vending.expansion.downloader.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ValveCustomDownloaderService extends IntentService {
    public static final String BYTES_DOWNLOADED = "bytes_downloaded";
    public static final String BYTES_TOTAL = "bytes_total";
    public static final String MAIN_NAME = "main_name";
    public static final String MAIN_SIZE = "main_size";
    public static final String MAIN_URL = "main_url";
    public static final String NOTIFICATION_PROGRESS = ".ValveCustomDownloaderService.PROGRESS";
    public static final String NOTIFICATION_STATUS = ".ValveCustomDownloaderService.STATUS";
    public static final String OBB_ROOT = "obb_root";
    public static final String PATCH_NAME = "patch_name";
    public static final String PATCH_SIZE = "patch_size";
    public static final String PATCH_URL = "patch_url";
    public static final String STATUS = "status";
    private long mBytesDownloaded;
    private long mTotalSize;

    public ValveCustomDownloaderService() {
        super("ValveCustomDownloaderService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.v("ValveCustomDownloaderService", "onHandleIntent");
        String obbRoot = intent.getStringExtra(OBB_ROOT);
        String mainURL = intent.getStringExtra(MAIN_URL);
        String mainName = intent.getStringExtra(MAIN_NAME);
        long mainSize = intent.getLongExtra(MAIN_SIZE, 0);
        String patchURL = intent.getStringExtra(PATCH_URL);
        String patchName = intent.getStringExtra(PATCH_NAME);
        long patchSize = intent.getLongExtra(PATCH_SIZE, 0);
        updateStatus(3);
        this.mTotalSize = mainSize + patchSize;
        this.mBytesDownloaded = 0;
        this.mBytesDownloaded += downloadFile(mainURL, obbRoot, mainName, mainSize);
        this.mBytesDownloaded += downloadFile(patchURL, obbRoot, patchName, patchSize);
        if (this.mBytesDownloaded == this.mTotalSize) {
            updateStatus(5);
        } else {
            updateStatus(19);
            Log.v("ValveCustomDownloaderService", "Total size mismatch! " + this.mTotalSize + " != " + this.mBytesDownloaded);
        }
        stopSelf();
    }

    private long downloadFile(String urlPath, String obbRoot, String fileName, long fileSize) {
        updateStatus(5);
        return fileSize;
    }

    private void updateStatus(int status) {
        Intent intent = new Intent(getApplicationContext().getPackageName() + NOTIFICATION_STATUS);
        intent.putExtra(STATUS, status);
        sendBroadcast(intent);
    }

    private void updateProgress(long bytesDownloaded) {
        Intent intent = new Intent(getApplicationContext().getPackageName() + NOTIFICATION_PROGRESS);
        intent.putExtra(BYTES_DOWNLOADED, this.mBytesDownloaded + bytesDownloaded);
        intent.putExtra(BYTES_TOTAL, this.mTotalSize);
        sendBroadcast(intent);
    }
}
