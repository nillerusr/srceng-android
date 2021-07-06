package org.libsdl.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.EGL14;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.View;
import org.libsdl.app.SDLActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.valvesoftware.ValveActivity2;
import android.graphics.PixelFormat;
import java.lang.Math;

import me.nillerusr.LauncherActivity;

public class SDLSurface extends SurfaceView
	implements SurfaceHolder.Callback,
	View.OnKeyListener,
	View.OnTouchListener,
	SensorEventListener {

	private static SensorManager mSensorManager;
	public static boolean isTouch = true;
	public static boolean mUseVolume = false;
	Runnable visibilityRunnable = null;
	private static final String TAG = "SDLSurface";

	public SDLSurface(Context context) {
		super(context);
		this.getHolder().addCallback((SurfaceHolder.Callback)this);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus();
		this.setOnKeyListener((View.OnKeyListener)this);
		this.setOnTouchListener((View.OnTouchListener)this);
		mSensorManager = (SensorManager)context.getSystemService("sensor");

		this.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			public void onSystemUiVisibilityChange(int n) {
				if (n != SDLActivity.viewFlags)
					SDLSurface.this.postVisibilityRunnable();
			}
		});

		this.mUseVolume = LauncherActivity.mPref.getBoolean( "use_volume_buttons", false );
	}

	public static class SDLPixelFormat {
		public static final int RGBA_4444 = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
		public static final int RGBA_5551 = 0x15441002; // SDL_PIXELFORMAT_RGBA5551 
		public static final int RGBA_8888 = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
		public static final int RGBX_8888 = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
		public static final int RGB_332 = 0x14110801; // SDL_PIXELFORMAT_RGB332
		public static final int RGB_565 = 0x15151002; // SDL_PIXELFORMAT_RGB565
		public static final int RGB_888 = 0x16161804; // SDL_PIXELFORMAT_RGB888
	}

	public void enableSensor(int n, boolean bl) {
		if (bl)
			mSensorManager.registerListener((SensorEventListener)this, mSensorManager.getDefaultSensor(n), 1, null);
		else
			mSensorManager.unregisterListener((SensorEventListener)this, mSensorManager.getDefaultSensor(n));
	}

	public void onAccuracyChanged(Sensor sensor, int n) {
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	public boolean onGenericMotionEvent(MotionEvent motionEvent) {
		if ((motionEvent.getSource() & 16) != 0)
			return true;

		return false;
	}

	public boolean onKey(View view, int n, KeyEvent keyEvent) {
		if( mUseVolume ) {
			if( n == KeyEvent.KEYCODE_VOLUME_DOWN)
				SDLActivity.onNativeJoystickAxis(5, Math.abs(keyEvent.getAction()-1));
			else if( n == KeyEvent.KEYCODE_VOLUME_UP )
				SDLActivity.onNativeJoystickAxis(2, Math.abs(keyEvent.getAction()-1));
			return true;
		} else if( n == KeyEvent.KEYCODE_VOLUME_DOWN || n == KeyEvent.KEYCODE_VOLUME_UP )
			return false;

		if( n == KeyEvent.KEYCODE_BACK )
			n = KeyEvent.KEYCODE_BUTTON_B;

		if (keyEvent.getAction() == 0) {
			SDLActivity.onNativeKeyDown(n);
			return true;
		} else if (keyEvent.getAction() == 1) {
			SDLActivity.onNativeKeyUp(n);
			return true;
		}

		return false;
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == 1)
			SDLActivity.onNativeAccel(sensorEvent.values[0] / 9.80665f, sensorEvent.values[1] / 9.80665f, sensorEvent.values[2] / 9.80665f);
	}

	public boolean onTouch(View view, MotionEvent event) {
		final int touchDevId = event.getDeviceId();
		final int pointerCount = event.getPointerCount();
		int action = event.getActionMasked();
		int pointerFingerId;
		int i = -1;
		float x,y,p;

		switch(action) {
			case MotionEvent.ACTION_MOVE:
				for( i = 0; i < pointerCount; i++ )
				{
					pointerFingerId = event.getPointerId( i );
					x = event.getX( i );
					y = event.getY( i );
					p = event.getPressure( i );
					ValveActivity2.TouchEvent( pointerFingerId, x/LauncherActivity.scr_res.width, y/LauncherActivity.scr_res.height, 2 );
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				i = 0;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_POINTER_DOWN:
				// Non primary pointer up/down
				if( i == -1 )
				{
					i = event.getActionIndex();
				}

				pointerFingerId = event.getPointerId( i );

				x = event.getX( i );
				y = event.getY( i );

				if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP )
					ValveActivity2.TouchEvent( pointerFingerId, x/LauncherActivity.scr_res.width, y/LauncherActivity.scr_res.height, 1 );
				if( action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN )
					ValveActivity2.TouchEvent( pointerFingerId, x/LauncherActivity.scr_res.width, y/LauncherActivity.scr_res.height, 0 );
				break;
			case MotionEvent.ACTION_CANCEL:
				for( i = 0; i < pointerCount; i++ )
				{
					pointerFingerId = event.getPointerId( i );
					x = event.getX( i );
					y = event.getY( i );
					ValveActivity2.TouchEvent( pointerFingerId, x/LauncherActivity.scr_res.width, y/LauncherActivity.scr_res.height, 1 );
				}
				break;

			default: break;
		}

		return true;
	}

	public void onWindowFocusChanged(boolean bl) {
		super.onWindowFocusChanged(bl);
		if (bl)
			this.postVisibilityRunnable();
	}

	protected void postVisibilityRunnable() {
		if (this.visibilityRunnable == null) {
			Log.v(TAG, "posting new visibility runnable");
			this.visibilityRunnable = new Runnable(){
				private int waitTime;
				private final int waitTimeMax = 500;

				@Override
				public void run() {
					if( SDLActivity.mImmersiveMode != null )
						SDLActivity.mImmersiveMode.apply();
				}
			};
			this.getHandler().postDelayed(this.visibilityRunnable, 1500);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if( LauncherActivity.mPref.getBoolean("fixed_resolution", false) ) {
			width = LauncherActivity.mPref.getInt("resolution_width", LauncherActivity.scr_res.width);
			height = LauncherActivity.mPref.getInt("resolution_height", LauncherActivity.scr_res.height);
			holder.setFixedSize(width, height);
		}

		int sdlFormat = SDLPixelFormat.RGB_565; // by default

		switch (format) {
			case PixelFormat.RGBA_4444:
				Log.v(TAG, "pixel format RGBA_4444");
				sdlFormat = SDLPixelFormat.RGBA_4444;
				break;
			case PixelFormat.RGBA_5551:
				Log.v(TAG, "pixel format RGBA_5551");
				sdlFormat = SDLPixelFormat.RGBA_5551;
				break;
			case PixelFormat.RGBA_8888:
				Log.v(TAG, "pixel format RGBA_8888");
				sdlFormat = SDLPixelFormat.RGBA_8888;
				break;
			case PixelFormat.RGBX_8888:
				Log.v(TAG, "pixel format RGBX_8888");
				sdlFormat = SDLPixelFormat.RGBX_8888;
				break;
			case PixelFormat.RGB_332:
				Log.v(TAG, "pixel format RGB_332");
				sdlFormat = SDLPixelFormat.RGB_332;
				break;
			case PixelFormat.RGB_565:
				Log.v(TAG, "pixel format RGB_565");
				break;
			case PixelFormat.RGB_888:
				Log.v(TAG, "pixel format RGB_888");
				sdlFormat = SDLPixelFormat.RGB_888;
				break;
			default:
				Log.v(TAG, "Unknown pixel format "+format);
		}

		SDLActivity.onNativeResize(width, height, sdlFormat );
		Log.v(TAG, "Window size:" + width + "x" + height);
		SDLActivity.mIsSurfaceReady = true;
		SDLActivity.onNativeSurfaceChanged();
		SDLActivity.startApp();
	}

	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		surfaceHolder.setType(2);
		surfaceHolder.setKeepScreenOn(true);
		this.enableSensor(1, true);
	}

	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		SDLActivity.handlePause();
		SDLActivity.mIsSurfaceReady = false;
		SDLActivity.onNativeSurfaceDestroyed();
	}
}
