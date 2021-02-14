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
import java.lang.Math;

import in.celest.LauncherActivity;

public class SDLSurface
        extends SurfaceView
        implements SurfaceHolder.Callback,
        View.OnKeyListener,
        View.OnTouchListener,
        SensorEventListener {
    public static float mHeight;
    private static SensorManager mSensorManager;
    public static float mWidth;
    public static boolean isTouch = true;
    public static boolean mUseVolume = false;

    final int desiredVisibility = 5894;
    Runnable visibilityRunnable = null;
    public SDLSurface(Context context) {
        super(context);
        this.getHolder().addCallback((SurfaceHolder.Callback)this);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();
        this.setOnKeyListener((View.OnKeyListener)this);
        this.setOnTouchListener((View.OnTouchListener)this);
        mSensorManager = (SensorManager)context.getSystemService("sensor");
        mWidth = 1.f;
        mHeight = 1.f;
        this.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){

            public void onSystemUiVisibilityChange(int n) {
                if (n != 5894) {
                    SDLSurface.this.postVisibilityRunnable();
                }
            }
        });
	this.mUseVolume = LauncherActivity.mPref.getBoolean( "use_volume_buttons", false );
    }

    public void enableSensor(int n, boolean bl) {
        if (bl) {
            mSensorManager.registerListener((SensorEventListener)this, mSensorManager.getDefaultSensor(n), 1, null);
            return;
        }
        mSensorManager.unregisterListener((SensorEventListener)this, mSensorManager.getDefaultSensor(n));
    }

    public void onAccuracyChanged(Sensor sensor, int n) {
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if ((motionEvent.getSource() & 16) != 0) {
            return true;
        }
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
        }
        if (keyEvent.getAction() == 1) {
            SDLActivity.onNativeKeyUp(n);
            return true;
        }
        return false;
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 1) {
            SDLActivity.onNativeAccel(sensorEvent.values[0] / 9.80665f, sensorEvent.values[1] / 9.80665f, sensorEvent.values[2] / 9.80665f);
        }
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
                    ValveActivity2.TouchEvent( pointerFingerId, x/mWidth, y/mHeight, 2 );
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
                Log.v((String)"SDL", (String)"x: "+x);
                Log.v((String)"SDL", (String)"y: "+y);
                if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP )
                    ValveActivity2.TouchEvent( pointerFingerId, x/mWidth, y/mHeight, 1 );
                if( action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN )
                    ValveActivity2.TouchEvent( pointerFingerId, x/mWidth, y/mHeight, 0 );
                break;
            case MotionEvent.ACTION_CANCEL:
                for( i = 0; i < pointerCount; i++ )
                {
                    pointerFingerId = event.getPointerId( i );
                    x = event.getX( i );
                    y = event.getY( i );
                    ValveActivity2.TouchEvent( pointerFingerId, x/mWidth, y/mHeight, 1 );
                }
                break;

            default: break;
        }

        return true;
    }

    public void onWindowFocusChanged(boolean bl) {
        super.onWindowFocusChanged(bl);
        if (bl) {
            this.postVisibilityRunnable();
        }
    }

    protected void postVisibilityRunnable() {
        if (this.visibilityRunnable == null) {
            Log.v((String)"SDL", (String)"posting new visibility runnable");
            this.visibilityRunnable = new Runnable(){
                private int waitTime;
                private final int waitTimeMax = 500;

                @Override
                public void run() {
                    if( SDLActivity.mImmersiveMode != null )
                        SDLActivity.mImmersiveMode.apply();
                    /*SDLSurface.this.setSystemUiVisibility(5894);
                    if (SDLSurface.this.getSystemUiVisibility() != 5894) {
                        this.waitTime += 500;
                        if (this.waitTime < waitTimeMax) {
                            SDLSurface.this.getHandler().postDelayed((Runnable)this, (long)this.waitTime);
                            return;
                        }
                        Log.e((String)"SDL", (String)"removing visibility runnable, failed to set visibility");
                        SDLSurface.this.visibilityRunnable = null;
                        return;
                    }
                    Log.v((String)"SDL", (String)"removing visibility runnable, successfully set visibility");
                    SDLSurface.this.visibilityRunnable = null;*/
                }
            };
            this.getHandler().postDelayed(this.visibilityRunnable, 1500);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public void surfaceChanged(SurfaceHolder surfaceHolder, int n, int n2, int n3) {
        mWidth = n2;
        mHeight = n3;
        n2 = 353701890;
        switch (n) {
            default: {
                Log.v((String)"SDL", (String)("pixel format unknown " + n));
                n = n2;
                break;
            }
            case 8: {
                Log.v((String)"SDL", (String)"pixel format A_8");
                n = n2;
                break;
            }
            case 10: {
                Log.v((String)"SDL", (String)"pixel format LA_88");
                n = n2;
                break;
            }
            case 9: {
                Log.v((String)"SDL", (String)"pixel format L_8");
                n = n2;
                break;
            }
            case 7: {
                Log.v((String)"SDL", (String)"pixel format RGBA_4444");
                n = 356651010;
                break;
            }
            case 6: {
                Log.v((String)"SDL", (String)"pixel format RGBA_5551");
                n = 356782082;
                break;
            }
            case 1: {
                Log.v((String)"SDL", (String)"pixel format RGBA_8888");
                n = 373694468;
                break;
            }
            case 2: {
                Log.v((String)"SDL", (String)"pixel format RGBX_8888");
                n = 371595268;
                break;
            }
            case 11: {
                Log.v((String)"SDL", (String)"pixel format RGB_332");
                n = 336660481;
                break;
            }
            case 4: {
                Log.v((String)"SDL", (String)"pixel format RGB_565");
                n = 353701890;
                break;
            }
            case 3: {
                Log.v((String)"SDL", (String)"pixel format RGB_888");
                n = 370546692;
            }
        }

        SDLActivity.onNativeResize((int)mWidth, (int)mHeight, n);
        Log.v((String)"SDL", (String)("Window size:" + mWidth + "x" + mHeight));
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
