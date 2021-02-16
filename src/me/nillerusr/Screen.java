package me.nillerusr;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import java.lang.reflect.Method;
import android.view.WindowManager;

public class Screen {
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();

	public static class Resolution {
		public int width;
		public int height;

		public Resolution() {
			this.height = 1;
			this.width = 1;
		}
	}

	public static Resolution getResolution( Context context ) {
		Resolution result = new Resolution();

		WindowManager ww = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = ww.getDefaultDisplay();

    if (Build.VERSION.SDK_INT >= 17){
        //new pleasant way to get real metrics
        DisplayMetrics realMetrics = new DisplayMetrics();
        display.getRealMetrics(realMetrics);
        result.width = realMetrics.widthPixels;
        result.height = realMetrics.heightPixels;
    } else if (Build.VERSION.SDK_INT >= 14) {
        //reflection for this weird in-between time
        try {
            Method mGetRawH = Display.class.getMethod("getRawHeight");
            Method mGetRawW = Display.class.getMethod("getRawWidth");
            result.width = (Integer) mGetRawW.invoke(display);
            result.height = (Integer) mGetRawH.invoke(display);
        } catch (Exception e) {
            //this may not be 100% accurate, but it's all we've got
            result.width = display.getWidth();
            result.height = display.getHeight();
        }

    } else {
        //This should be close, as lower API devices should not have window navigation bars
        result.width = display.getWidth();
        result.height = display.getHeight();
    }

    if( result.height > result.width )
    {
    	int temp = result.height;
    	result.height = result.width;
    	result.width = temp;
    }
/*
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

		if( displayMetrics.heightPixels > displayMetrics.widthPixels )
		{
			result.height = displayMetrics.heightPixels;
			result.width = displayMetrics.widthPixels;
		}
		else
		{
			result.width = displayMetrics.heightPixels;
			result.height = displayMetrics.widthPixels;
		}
*/
		return result;
	}
}
