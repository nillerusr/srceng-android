/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.graphics.Canvas
 *  android.hardware.Sensor
 *  android.hardware.SensorEvent
 *  android.hardware.SensorEventListener
 *  android.hardware.SensorManager
 *  android.opengl.EGL14
 *  android.os.Handler
 *  android.util.Log
 *  android.view.KeyEvent
 *  android.view.MotionEvent
 *  android.view.SurfaceHolder
 *  android.view.SurfaceHolder$Callback
 *  android.view.SurfaceView
 *  android.view.View
 *  android.view.View$OnKeyListener
 *  android.view.View$OnSystemUiVisibilityChangeListener
 *  android.view.View$OnTouchListener
 */
package org.libsdl.app;

import android.content.Context;
import android.graphics.Canvas;
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
import android.view.View;
import org.libsdl.app.SDLActivity;

class SDLSurface
extends SurfaceView
implements SurfaceHolder.Callback,
View.OnKeyListener,
View.OnTouchListener,
SensorEventListener {
    private static float mHeight;
    private static SensorManager mSensorManager;
    private static float mWidth;
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
        mWidth = 1.0f;
        mHeight = 1.0f;
        this.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){

            public void onSystemUiVisibilityChange(int n) {
                if (n != 5894) {
                    SDLSurface.this.postVisibilityRunnable();
                }
            }
        });
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

    public void onDraw(Canvas canvas) {
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if ((motionEvent.getSource() & 16) != 0) {
            return true;
        }
        return false;
    }

    public boolean onKey(View view, int n, KeyEvent keyEvent) {
        if (n == 24 || n == 25) {
            return false;
        }
        
        if( n == KeyEvent.KEYCODE_BACK )
			n = KeyEvent.KEYCODE_ESCAPE;
        
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
                for (i = 0; i < pointerCount; i++) {
                    pointerFingerId = event.getPointerId(i);
                    x = event.getX(i) / mWidth;
                    y = event.getY(i) / mHeight;
                    p = event.getPressure(i);
                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
                // Primary pointer up/down, the index is always zero
                i = 0;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Non primary pointer up/down
                if (i == -1) {
                    i = event.getActionIndex();
                }
                
                pointerFingerId = event.getPointerId(i);
                x = event.getX(i) / mWidth;
                y = event.getY(i) / mHeight;
                p = event.getPressure(i);
                SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP ) 
                {
					SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ENTER);
					SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ENTER);
				}
				else { }
                break;
            
            default:
                break;
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
                    SDLSurface.this.setSystemUiVisibility(5894);
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
                    SDLSurface.this.visibilityRunnable = null;
                }
            };
            this.getHandler().postDelayed(this.visibilityRunnable, 1500);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public void surfaceChanged(SurfaceHolder surfaceHolder, int n, int n2, int n3) {
        int n4 = 0;
        int n5 = EGL14.eglQueryAPI();
        if (EGL14.eglBindAPI((int)12450)) {
            n4 = 1;
            EGL14.eglBindAPI((int)n5);
        }
        n4 = n4 != 0 ? 1080 : 600;
        int n6 = n2;
        n5 = n3;
        if (n4 > 0) {
            n6 = n2;
            n5 = n3;
            if (n4 < n3) {
                n6 = n2 * n4 / n3;
                surfaceHolder.setFixedSize(n6, n4);
                n5 = n4;
            }
        }
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
        mWidth = n6;
        mHeight = n5;
        SDLActivity.onNativeResize(n6, n5, n);
        Log.v((String)"SDL", (String)("Window size:" + n6 + "x" + n5));
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

