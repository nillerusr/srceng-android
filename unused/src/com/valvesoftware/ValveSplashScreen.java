package com.valvesoftware;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class ValveSplashScreen extends SurfaceView implements OnCompletionListener, Callback {
    private AssetManager mAssetManager;
    private MediaPlayer mMediaPlayer;

    public ValveSplashScreen(Context context, AssetManager am) {
        super(context);
        this.mAssetManager = am;
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("SDL", "SDLMediaSurface::surfaceCreated()");
        holder.setType(0);
        try {
            AssetFileDescriptor valveMovie = this.mAssetManager.openFd("valve.mp4");
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setDataSource(valveMovie.getFileDescriptor(), valveMovie.getStartOffset(), valveMovie.getLength());
            this.mMediaPlayer.setDisplay(getHolder());
            this.mMediaPlayer.setVideoScalingMode(2);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.setOnCompletionListener(this);
            this.mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("SDL", "error: " + e.getMessage(), e);
            onCompletion(null);
        }
    }

    public void onCompletion(MediaPlayer arg0) {
        Log.v("SDL", "onCompletion called");
        ValveActivity.startGame();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("SDL", "SDLMediaSurface::surfaceDestroyed()");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("SDL", "SDLMediaSurface::surfaceChanged()");
    }
}
