/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.res.Resources
 *  android.os.Bundle
 */
package com.nvidia.valvesoftware.portal;

import android.content.res.Resources;
import android.os.Bundle;
import com.valvesoftware.ValveActivity;

public class Portal
extends ValveActivity {
    @Override
    protected void onCreate(Bundle object) {
        super.onCreate((Bundle)object);
        Object object2 = this.getResources();
        int n = object2.getInteger(2131165187);
        object = object2.getString(2131034141);
        object2 = object2.getString(2131034142);
        super.checkAndStart(n, Long.parseLong((String)object, 10), Long.parseLong((String)object2, 10));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

