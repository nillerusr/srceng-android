/*
 * Decompiled with CFR 0_118.
 */
package com.nvidia;

public class PowerServiceClient {
    public static final int CAMERA_HINT_COUNT = 6;
    public static final int CAMERA_HINT_FPS = 4;
    public static final int CAMERA_HINT_PERF = 3;
    public static final int CAMERA_HINT_RESET = 5;
    public static final int CAMERA_HINT_STILL_PREVIEW_POWER = 0;
    public static final int CAMERA_HINT_VIDEO_PREVIEW_POWER = 1;
    public static final int CAMERA_HINT_VIDEO_RECORD_POWER = 2;
    public static final int POWER_HINT_APP_LAUNCH = 6;
    public static final int POWER_HINT_APP_PROFILE = 5;
    public static final int POWER_HINT_CAMERA = 11;
    public static final int POWER_HINT_COUNT = 13;
    public static final int POWER_HINT_HIGH_RES_VIDEO = 8;
    public static final int POWER_HINT_INTERACTION = 2;
    public static final int POWER_HINT_MIRACAST = 9;
    public static final int POWER_HINT_MULTITHREAD_BOOST = 12;
    public static final int POWER_HINT_SHIELD_STREAMING = 7;
    public static final int POWER_HINT_VIDEO_DECODE = 4;
    public static final int POWER_HINT_VIDEO_ENCODE = 3;
    public static final int POWER_HINT_VSYNC = 1;
    private int mNativePowerServiceClient = 0;

    public PowerServiceClient() {
        this.init();
    }

    private native void init();

    private static native void nativeClassInit();

    private native void release();

    protected void finalize() throws Throwable {
        try {
            super.finalize();
            return;
        }
        finally {
            if (this.mNativePowerServiceClient != 0) {
                this.release();
            }
        }
    }

    public native void sendPowerHint(int var1, int[] var2);
}

