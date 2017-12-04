/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.view.KeyEvent
 *  android.view.View
 *  android.view.View$OnKeyListener
 *  android.view.inputmethod.EditorInfo
 *  android.view.inputmethod.InputConnection
 */
package org.libsdl.app;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLInputConnection;

class DummyEdit
extends View
implements View.OnKeyListener {
    InputConnection ic;

    public DummyEdit(Context context) {
        super(context);
        this.setFocusableInTouchMode(true);
        this.setFocusable(true);
        this.setOnKeyListener((View.OnKeyListener)this);
    }

    public boolean onCheckIsTextEditor() {
        return true;
    }

    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        this.ic = new SDLInputConnection(this, true);
        editorInfo.imeOptions = 301989888;
        return this.ic;
    }

    public boolean onKey(View view, int n, KeyEvent keyEvent) {
        if (keyEvent.isPrintingKey()) {
            if (keyEvent.getAction() == 0) {
                this.ic.commitText((CharSequence)String.valueOf((char)keyEvent.getUnicodeChar()), 1);
            }
            return true;
        }
        if (keyEvent.getAction() == 0) {
            SDLActivity.onNativeKeyDown(n);
            return true;
        }
        if (keyEvent.getAction() == 1) {
            SDLActivity.onNativeKeyUp(n);
            return true;
        }
        return false;
    }
}

