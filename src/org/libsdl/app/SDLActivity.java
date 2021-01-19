package org.libsdl.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioTrack;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import in.celest.LauncherActivity;
import com.valvesoftware.ValveActivity2;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.widget.AbsoluteLayout;
import java.nio.FloatBuffer;

public class SDLActivity extends Activity {
	static final int COMMAND_CHANGE_TITLE = 1;
	static final int COMMAND_TEXTEDIT_HIDE = 3;
	static final int COMMAND_UNUSED = 2;
	protected static final int COMMAND_USER = 32768;
	private static final String TAG = "SDL";
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;
	private static EGLConfig mEGLConfig;
	private static EGLContext mEGLContext;
	private static EGLDisplay mEGLDisplay;
	private static EGLSurface mEGLSurface;
	private static int mGLMajor;
	private static int mGLMinor;
	public static boolean mHasFocus;
	public static boolean mIsPaused;
	public static boolean mIsSurfaceReady;
	public static boolean keyboardVisible;
	public static ViewGroup mLayout;
	private static Thread mSDLThread;
	private static SDLActivity mSingleton;
	public static SDLSurface mSurface;
	public static View mTextEdit;
	Handler commandHandler;
	public static View mDecorView;
	public static RelativeLayout tch;
	public static RelativeLayout dpd;
	public static ImmersiveMode mImmersiveMode;
	public static final int sdk = Integer.valueOf( Build.VERSION.SDK );

	/* renamed from: org.libsdl.app.SDLActivity.1 */
	static class C00231 implements Runnable {
		C00231() {
		}

		public void run() {
			SDLActivity.mAudioTrack.play();
			SDLActivity.nativeRunAudioThread();
		}
	}

	protected static class SDLCommandHandler extends Handler {
		protected SDLCommandHandler() {
		}

		public void handleMessage(Message msg) {
			Context context = SDLActivity.getContext();
			if (context == null) {
				Log.e(SDLActivity.TAG, "error handling message, getContext() returned null");
				return;
			}
			switch (msg.arg1) {
				case SDLActivity.COMMAND_CHANGE_TITLE /*1*/:
					if (context instanceof Activity) {
						((Activity) context).setTitle((String) msg.obj);
					} else {
						Log.e(SDLActivity.TAG, "error handling message, getContext() returned no Activity");
					}
				case SDLActivity.COMMAND_TEXTEDIT_HIDE /*3*/:
					if (SDLActivity.mTextEdit != null) {
						SDLActivity.mTextEdit.setVisibility(8);
						((InputMethodManager) context.getSystemService("input_method")).hideSoftInputFromWindow(SDLActivity.mTextEdit.getWindowToken(), 0);
					}
				default:
					if ((context instanceof SDLActivity) && !((SDLActivity) context).onUnhandledMessage(msg.arg1, msg.obj)) {
						Log.e(SDLActivity.TAG, "error handling message, command is " + msg.arg1);
					}
			}
		}
	}

	static class ShowTextInputTask implements Runnable
	{
		/*
		 * This is used to regulate the pan&scan method to have some offset from
		 * the bottom edge of the input region and the top edge of an input
		 * method (soft keyboard)
		 */
		private int show;

		public ShowTextInputTask( int show1 ) 
		{
			show = show1;
		}

		@Override
		public void run() 
		{
			InputMethodManager imm = ( InputMethodManager )getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
			
			if( mTextEdit == null )
			{
				mTextEdit = new DummyEdit( getContext() );
				mLayout.addView( mTextEdit );
			}

			if( show == 1 )
			{
				mTextEdit.setVisibility( View.VISIBLE );
				mTextEdit.requestFocus();
				imm.showSoftInput( mTextEdit, 0 );
				keyboardVisible = true;
                                if( SDLActivity.mImmersiveMode != null )
                                        SDLActivity.mImmersiveMode.apply();
			}
			else
			{
				mTextEdit.setVisibility( View.GONE );
				imm.hideSoftInputFromWindow( mTextEdit.getWindowToken(), 0 );
				keyboardVisible = false;
                                if( SDLActivity.mImmersiveMode != null )
                                        SDLActivity.mImmersiveMode.apply();
			}
		}
	}

	public static void showKeyboard( int show )
	{
		// Transfer the task to the main thread as a Runnable
		mSingleton.runOnUiThread( new ShowTextInputTask( show ) );
	}

	public static native void initAssetManager(AssetManager assetManager);

	public static native void nativeInit();

	public static native void nativePause();

	public static native void nativeQuit();

	public static native void nativeResume();

	public static native void nativeRunAudioThread();

	public static native void onNativeAccel(float f, float f2, float f3);

	public static native void onNativeJoystickAxis(int i, float f);

	public static native void onNativeJoystickHat(int i, int i2);

	public static native void onNativeKeyDown(int i);

	public static native void onNativeKeyUp(int i);

	public static native void onNativeResize(int i, int i2, int i3);

	public static native void onNativeSurfaceChanged();

	public static native void onNativeSurfaceDestroyed();

	public static native void onNativeTouch(int i, int i2, int i3, float f, float f2, float f3);

	public SDLActivity() {
		this.commandHandler = new SDLCommandHandler();
	}

	static {
		mIsPaused = false;
		mIsSurfaceReady = false;
		mHasFocus = true;
		mEGLContext = null;
		//System.loadLibrary("first");
		System.loadLibrary("SDL2");
		System.loadLibrary("main");
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSingleton = this;
		com.valvesoftware.ValveActivity2.initNatives();
		initAssetManager(getAssets());
		startSDL();
		 mDecorView = getWindow().getDecorView();
		// Immersive Mode is available only at >KitKat
		Boolean enableImmersive = ( sdk >= 19 ) && ( LauncherActivity.mPref.getBoolean( "immersive_mode", true ) );
		if( enableImmersive )
			mImmersiveMode = new ImmersiveMode_v19();
		else mImmersiveMode = new ImmersiveMode();

		if( SDLActivity.mImmersiveMode != null )
			SDLActivity.mImmersiveMode.apply();

		if (sdk >= 28)
			getWindow().getAttributes().layoutInDisplayCutoutMode = 1;
	}

	public static void startSDL() {
		mLayout = new FrameLayout(mSingleton);
		mSurface = new SDLSurface(mSingleton.getApplication());
		SurfaceHolder holder = mSurface.getHolder();
		holder.setType( SurfaceHolder.SURFACE_TYPE_GPU );
		mLayout.addView(mSurface);
		mSingleton.setContentView(mLayout);
	}

	public static void quit() {
		mSingleton.finish();
		System.exit(0);
	}

	protected void onPause() {
		super.onPause();
		handlePause();
		if (mAudioTrack != null) {
			mAudioTrack.flush();
			mAudioTrack.pause();
		}
	}

	protected void onResume() {
		super.onResume();
		handleResume();
		if (mAudioTrack != null) {
			mAudioTrack.play();
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mHasFocus = hasFocus;
		if (hasFocus) {
			handleResume();
		}
		if( mImmersiveMode != null )
                {
                        mImmersiveMode.apply();
                }
	}

	protected void onDestroy() {
		super.onDestroy();
		nativeQuit();
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				Log.v(TAG, "Problem stopping thread: " + e);
			}
			mSDLThread = null;
		}
	}

	public static void handlePause() {
		if (!mIsPaused && mIsSurfaceReady) {
			mIsPaused = true;
			nativePause();
			mSurface.enableSensor(COMMAND_CHANGE_TITLE, false);
		}
	}

	public static void handleResume() {
		if (mIsPaused && mIsSurfaceReady && mHasFocus) {
			mIsPaused = false;
			nativeResume();
			mSurface.enableSensor(COMMAND_CHANGE_TITLE, true);
		}
	}

	protected boolean onUnhandledMessage(int command, Object param) {
		return false;
	}

	boolean sendCommand(int command, Object data) {
		Message msg = this.commandHandler.obtainMessage();
		msg.arg1 = command;
		msg.obj = data;
		return this.commandHandler.sendMessage(msg);
	}

	public static boolean createGLContext(int majorVersion, int minorVersion, int[] attribs) {
		return initEGL(majorVersion, minorVersion, attribs);
	}

	public static void flipBuffers() {
		flipEGL();
	}

	public static boolean setActivityTitle(String title) {
		return mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	public static boolean sendMessage(int command, int param) {
		return mSingleton.sendCommand(command, Integer.valueOf(param));
	}

	public static Context getContext() {
		return mSingleton;
	}

	public static void startApp() {
		if (mSDLThread == null) {
			mSDLThread = new Thread(new SDLMain(), "SDLThread");
			mSDLThread.start();
		} else if (mIsPaused) {
			nativeResume();
			mIsPaused = false;
		}
	}

	public static boolean showTextInput(int show) {
		return mSingleton.commandHandler.post(new ShowTextInputTask(show));
	}

	public static boolean initEGL(int majorVersion, int minorVersion, int[] attribs) {
		//System.loadLibrary("tierhook");
		String str = ((("Debug-infos:" + "\n OS Version: " + System.getProperty("os.version") + "(" + VERSION.INCREMENTAL + ")") + "\n OS API Level: " + VERSION.SDK) + "\n Device: " + Build.DEVICE) + "\n Model (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")";
		try {
			String bigGLVar = System.getenv("USE_BIG_GL");
			if (bigGLVar == null) {
				bigGLVar = "0";
			}
			Log.v(TAG, "USE_BIG_GL = " + bigGLVar);
			boolean tryBigGL = bigGLVar.equals("1");
			EGLDisplay display;
			int[] version;
			EGLConfig[] configs;
			int[] num_config;
			if (mEGLDisplay == null && tryBigGL) {
				Log.v(TAG, "Attempting to create Big GL Context");
				display = EGL14.eglGetDisplay(0);
				EGL14.eglBindAPI(12450);
				version = new int[COMMAND_UNUSED];
				EGL14.eglInitialize(display, version, 0, version, COMMAND_CHANGE_TITLE);
				configs = new EGLConfig[COMMAND_CHANGE_TITLE];
				num_config = new int[COMMAND_CHANGE_TITLE];
				if (!EGL14.eglChooseConfig(display, new int[]{12339, 4, 12352, 8, 12324, 8, 12323, 8, 12322, 8, 12325, 24, 12326, 8, 12344}, 0, configs, 0, COMMAND_CHANGE_TITLE, num_config, 0) || num_config[0] == 0) {
					Log.v(TAG, "No EGL config available for Big GL");
					return false;
				}
				mEGLDisplay = display;
				mEGLConfig = configs[0];
				mGLMajor = 4;
				mGLMinor = 0;
			} else {
				Log.v(TAG, "Starting up OpenGL ES " + majorVersion + "." + minorVersion);
				display = EGL14.eglGetDisplay(0);
				version = new int[COMMAND_UNUSED];
				EGL14.eglInitialize(display, version, 0, version, COMMAND_CHANGE_TITLE);
				configs = new EGLConfig[COMMAND_CHANGE_TITLE];
				num_config = new int[COMMAND_CHANGE_TITLE];
				if (!EGL14.eglChooseConfig(display, new int[]{12339, 4, 12352, 4, 12324, 8, 12323, 8, 12322, 8, 12325, 24, 12326, 8, 12344}, 0, configs, 0, COMMAND_CHANGE_TITLE, num_config, 0) || num_config[0] == 0) {
					Log.e(TAG, "No EGL config available");
					return false;
				}
				mEGLDisplay = display;
				mEGLConfig = configs[0];
				mGLMajor = majorVersion;
				mGLMinor = minorVersion;
			}
			return createEGLSurface();
		} catch (Exception e) {
			Log.v(TAG, e + "");
			StackTraceElement[] arr$ = e.getStackTrace();
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; i$ += COMMAND_CHANGE_TITLE) {
				Log.v(TAG, arr$[i$].toString());
			}
			return false;
		}
	}

	public static boolean createEGLContext() {
		int[] esAttrs = new int[COMMAND_TEXTEDIT_HIDE];
		esAttrs[0] = 12440;
		esAttrs[COMMAND_CHANGE_TITLE] = mGLMajor;
		esAttrs[COMMAND_UNUSED] = 12344;
		int[] glAttrs = new int[COMMAND_CHANGE_TITLE];
		glAttrs[0] = 12344;
		EGLDisplay eGLDisplay = mEGLDisplay;
		EGLConfig eGLConfig = mEGLConfig;
		EGLContext eGLContext = EGL14.EGL_NO_CONTEXT;
		if (mGLMajor > COMMAND_UNUSED) {
			esAttrs = glAttrs;
		}
		mEGLContext = EGL14.eglCreateContext(eGLDisplay, eGLConfig, eGLContext, esAttrs, 0);
		if (mEGLContext != EGL14.EGL_NO_CONTEXT) {
			return true;
		}
		Log.e(TAG, "Couldn't create context");
		return false;
	}

	public static boolean createEGLSurface() {
		if (mEGLDisplay == null || mEGLConfig == null) {
			Log.e(TAG, "Surface creation failed, display = " + mEGLDisplay + ", config = " + mEGLConfig);
			return false;
		}
		if (mEGLContext == null) {
			createEGLContext();
		}
		int[] surfaceAttribs = new int[COMMAND_CHANGE_TITLE];
		surfaceAttribs[0] = 12344;
		EGLSurface surface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurface, surfaceAttribs, 0);
		if (surface == EGL14.EGL_NO_SURFACE) {
			Log.e(TAG, "Couldn't create surface");
			return false;
		}
		if (!(EGL14.eglGetCurrentContext() == mEGLContext || EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext))) {
			Log.e(TAG, "Old EGL Context doesnt work, trying with a new one");
			createEGLContext();
			if (!EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext)) {
				Log.e(TAG, "Failed making EGL Context current");
				return false;
			}
		}
		mEGLSurface = surface;
		return true;
	}


	public static void flipEGL() {
		try {
			EGL14.eglWaitNative(12379);
			EGL14.eglWaitGL();{}
			EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
		} catch (Exception e) {
			Log.v(TAG, "flipEGL(): " + e);
			StackTraceElement[] arr$ = e.getStackTrace();
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; i$ += COMMAND_CHANGE_TITLE) {
				Log.v(TAG, arr$[i$].toString());
			}
		}
	}

	public static void audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
		int channelConfig;
		int audioFormat;
		int i;
		String str;
		if (isStereo) {
			channelConfig = COMMAND_TEXTEDIT_HIDE;
		} else {
			channelConfig = COMMAND_UNUSED;
		}
		if (is16Bit) {
			audioFormat = COMMAND_UNUSED;
		} else {
			audioFormat = COMMAND_TEXTEDIT_HIDE;
		}
		if (isStereo) {
			i = COMMAND_UNUSED;
		} else {
			i = COMMAND_CHANGE_TITLE;
		}
		int frameSize = i * (is16Bit ? COMMAND_UNUSED : COMMAND_CHANGE_TITLE);
		String str2 = TAG;
		StringBuilder append = new StringBuilder().append("SDL audio: wanted ").append(isStereo ? "stereo" : "mono").append(" ");
		if (is16Bit) {
			str = "16-bit";
		} else {
			str = "8-bit";
		}
		Log.v(str2, append.append(str).append(" ").append(((float) sampleRate) / 1000.0f).append("kHz, ").append(desiredFrames).append(" frames buffer").toString());
		desiredFrames = Math.max(desiredFrames, ((AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize) - 1) / frameSize);
		mAudioTrack = new AudioTrack(COMMAND_TEXTEDIT_HIDE, sampleRate, channelConfig, audioFormat, desiredFrames * frameSize, COMMAND_CHANGE_TITLE);
		audioStartThread();
		Log.v(TAG, "SDL audio: got " + (mAudioTrack.getChannelCount() >= COMMAND_UNUSED ? "stereo" : "mono") + " " + (mAudioTrack.getAudioFormat() == COMMAND_UNUSED ? "16-bit" : "8-bit") + " " + (((float) mAudioTrack.getSampleRate()) / 1000.0f) + "kHz, " + desiredFrames + " frames buffer");
	}

	public static void audioStartThread() {
		mAudioThread = new Thread(new C00231());
		mAudioThread.setPriority(10);
		mAudioThread.start();
	}

	public static void audioWriteShortBuffer(short[] buffer) {
		int i = 0;
		while (i < buffer.length) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			} else {
				Log.w(TAG, "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioWriteByteBuffer(byte[] buffer) {
		int i = 0;
		while (i < buffer.length) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			} else {
				Log.w(TAG, "SDL audio: error return from write(byte)");
				return;
			}
		}
	}

	public static void audioQuit() {
		if (mAudioThread != null) {
			try {
				mAudioThread.join();
			} catch (Exception e) {
				Log.v(TAG, "Problem stopping audio thread: " + e);
			}
			mAudioThread = null;
		}
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}

	public boolean dispatchGenericMotionEvent(MotionEvent event) {
		if ((event.getSource() & 8194) != 0) {
			return false;
		}
		if ((event.getSource() & 16) != 0) {
//			Log.v("HL2EVENT", "AXIS_X: " + String.valueOf(event.getAxisValue(MotionEvent.AXIS_X)));
//			Log.v("HL2EVENT", "AXIS_Y: " + String.valueOf(event.getAxisValue(MotionEvent.AXIS_Y)));
			onNativeJoystickAxis(0, event.getAxisValue(MotionEvent.AXIS_X));
			onNativeJoystickAxis(1, event.getAxisValue(MotionEvent.AXIS_Y));
			onNativeJoystickAxis(3, event.getAxisValue(MotionEvent.AXIS_RZ));
			onNativeJoystickAxis(4, event.getAxisValue(MotionEvent.AXIS_Z));
			onNativeJoystickAxis(2, Math.max(event.getAxisValue(17), event.getAxisValue(23)));
			onNativeJoystickAxis(5, Math.max(event.getAxisValue(18), event.getAxisValue(22)));
			int axisValue = 0;
			int axisHatX = (int) event.getAxisValue(15);
			int axisHatY = (int) event.getAxisValue(16);
			if (axisHatX == COMMAND_CHANGE_TITLE) {
				axisValue = 0 | COMMAND_UNUSED;
			} else if (axisHatX == -1) {
				axisValue = 0 | 8;
			}
			if (axisHatY == COMMAND_CHANGE_TITLE) {
				axisValue |= 4;
			} else if (axisHatY == -1) {
				axisValue |= COMMAND_CHANGE_TITLE;
			}
			onNativeJoystickHat(0, axisValue);
		}
		return super.dispatchGenericMotionEvent(event);
	}
}
class ImmersiveMode
{
	void apply()
	{
		//stub
	}
}

class ImmersiveMode_v19 extends ImmersiveMode
{
	@Override
	void apply()
	{
		if( !SDLActivity.keyboardVisible )
			SDLActivity.mDecorView.setSystemUiVisibility(
				0x00000100   // View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| 0x00000200 // View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| 0x00000400 // View.SYSTEM_UI_FLAG_LAYOUT_FULSCREEN
				| 0x00000002 // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| 0x00000004 // View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| 0x00001000 // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				);
		else
                        SDLActivity.mDecorView.setSystemUiVisibility( 0 );
	}
}
