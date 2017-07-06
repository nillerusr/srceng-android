/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.BroadcastReceiver
 *  android.content.Context
 *  android.content.Intent
 *  android.content.pm.PackageManager
 *  android.content.pm.PackageManager$NameNotFoundException
 */
package com.valvesoftware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.valvesoftware.ValveDownloaderService;

public class ValveAlarmReceiver
extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, ValveDownloaderService.class);
            return;
        }
        catch (PackageManager.NameNotFoundException var1_2) {
            var1_2.printStackTrace();
            return;
        }
    }
}

