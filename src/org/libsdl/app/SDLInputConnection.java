/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.view.KeyEvent
 *  android.view.View
 *  android.view.inputmethod.BaseInputConnection
 */
package org.libsdl.app;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import org.libsdl.app.SDLActivity;

class SDLInputConnection
extends BaseInputConnection {
    public SDLInputConnection(View view, boolean bl) {
        super(view, bl);
    }

    public boolean commitText(CharSequence charSequence, int n) {
        this.nativeCommitText(charSequence.toString(), n);
        return super.commitText(charSequence, n);
    }

    public native void nativeCommitText(String var1, int var2);

    public native void nativeSetComposingText(String var1, int var2);

    public boolean sendKeyEvent(KeyEvent keyEvent) {
        int n = keyEvent.getKeyCode();
        if (keyEvent.getAction() == 0) {
            if (keyEvent.isPrintingKey()) {
                this.commitText(String.valueOf((char)keyEvent.getUnicodeChar()), 1);
            }
            SDLActivity.onNativeKeyDown(n);
            return true;
        }
        if (keyEvent.getAction() == 1) {
            SDLActivity.onNativeKeyUp(n);
            return true;
        }
        return super.sendKeyEvent(keyEvent);
    }

    public boolean setComposingText(CharSequence charSequence, int n) {
        this.nativeSetComposingText(charSequence.toString(), n);
        return super.setComposingText(charSequence, n);
    }
}

