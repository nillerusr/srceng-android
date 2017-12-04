/*
 * Decompiled with CFR 0_118.
 */
package org.libsdl.app;

import org.libsdl.app.SDLActivity;

class SDLMain
implements Runnable {
    SDLMain() {
    }

    @Override
    public void run() {
        SDLActivity.nativeInit();
        SDLActivity.quit();
    }
}

