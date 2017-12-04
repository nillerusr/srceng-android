/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.res.Resources
 *  android.util.Log
 */
package com.valvesoftware;

import android.content.res.Resources;
import android.util.Log;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;
import com.valvesoftware.ValveAlarmReceiver;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ValveDownloaderService
extends DownloaderService {
    public static final byte[] SALT = new byte[]{-33, -102, 1, -14, -97, -40, 56, 32, 80, 25, 14, -102, -10, -116, 109, -48, -122, -82, -73, 113};

    @Override
    public String getAlarmReceiverClassName() {
        return ValveAlarmReceiver.class.getName();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getPublicKey() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = this.getResources().openRawResource(this.getResources().getIdentifier("raw/license", "raw", this.getPackageName()));
        byte[] arrby = new byte[1024];
        try {
            int n;
            while ((n = inputStream.read(arrby)) != -1) {
                byteArrayOutputStream.write(arrby, 0, n);
            }
            byteArrayOutputStream.close();
            inputStream.close();
            return byteArrayOutputStream.toString();
        }
        catch (Exception var3_3) {
            Log.e((String)"ValveDownloaderService", (String)("getPublicKey exception! " + var3_3));
            return byteArrayOutputStream.toString();
        }
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }
}

