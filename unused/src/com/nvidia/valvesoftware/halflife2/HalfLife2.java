/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.res.Resources
 *  android.os.Bundle
 */
package com.nvidia.valvesoftware.halflife2;

import android.content.res.Resources;
import android.os.Bundle;
import com.valvesoftware.ValveActivity;

import com.nvidia.valvesoftware.halflife2.R;

public class HalfLife2
extends ValveActivity {
    @Override
    protected void onCreate(Bundle object) {
        super.onCreate((Bundle)object);
        String mainSize = getString( R.string.hl2_obb_mainSize );
        String patchSize = getString( R.string.hl2_obb_patchSize );
        super.checkAndStart(22, Long.parseLong(mainSize, 10), Long.parseLong(patchSize, 10));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

