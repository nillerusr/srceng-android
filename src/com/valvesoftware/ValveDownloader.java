/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.util.AttributeSet
 *  android.view.View
 *  android.view.View$OnClickListener
 *  android.widget.Button
 *  android.widget.LinearLayout
 *  android.widget.ProgressBar
 *  android.widget.TextView
 */
package com.valvesoftware;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.valvesoftware.ValveActivity;

public class ValveDownloader
extends LinearLayout {
    Button mPauseButton;
    Button mPlayButton;
    ProgressBar mProgressBar;
    TextView mProgressText;
    Button mResumeButton;
    Button mRetryButton;

    public ValveDownloader(Context context) {
        super(context);
        this.setPadding(32, 32, 32, 32);
        this.setOrientation(1);
        this.setGravity(17);
        TextView textView = new TextView(context);
        textView.setGravity(1);
        textView.setTextSize(32.0f);
        textView.setText((CharSequence)"Content Downloader");
        this.addView((View)textView);
        this.mProgressBar = new ProgressBar(context, null, 16842872);
        this.mProgressBar.setMax(1000);
        this.addView((View)this.mProgressBar);
        this.mProgressText = new TextView(context);
        this.mProgressText.setGravity(1);
        this.addView((View)this.mProgressText);
        this.mPlayButton = new Button(context);
        this.mPlayButton.setText((CharSequence)"Play");
        this.mPlayButton.setVisibility(8);
        this.addView((View)this.mPlayButton);
        this.mPlayButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                ValveDownloader.this.startGame();
            }
        });
        this.mPauseButton = new Button(context);
        this.mPauseButton.setText((CharSequence)"Pause");
        this.mPauseButton.setVisibility(8);
        this.addView((View)this.mPauseButton);
        this.mPauseButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                ValveActivity.pauseDownload();
            }
        });
        this.mResumeButton = new Button(context);
        this.mResumeButton.setText((CharSequence)"Resume");
        this.mResumeButton.setVisibility(8);
        this.addView((View)this.mResumeButton);
        this.mResumeButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                ValveActivity.resumeDownload();
            }
        });
        this.mRetryButton = new Button(context);
        this.mRetryButton.setText((CharSequence)"Retry");
        this.mRetryButton.setVisibility(8);
        this.addView((View)this.mRetryButton);
        this.mRetryButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                ValveActivity.retryDownload();
            }
        });
    }

    private String makeSizeString(long l) {
        if (l < 1024) {
            return String.format("%dB", l);
        }
        if (l < 0x100000) {
            return String.format("%.2fKB", Float.valueOf((float)l / 1024.0f));
        }
        if (l < 0x40000000) {
            return String.format("%.2fMB", Float.valueOf((float)l / 1048576.0f));
        }
        return String.format("%.2fGB", Float.valueOf((float)l / 1.07374182E9f));
    }

    private void startGame() {
        ValveActivity.startVideo();
    }

    public void updateProgress(long l, long l2) {
        double d = (double)l / (double)l2;
        this.mProgressBar.setProgress((int)(1000.0 * d));
        this.mProgressText.setText((CharSequence)(this.makeSizeString(l) + "/" + this.makeSizeString(l2)));
    }

    public void updateProgress(DownloadProgressInfo downloadProgressInfo) {
        double d = downloadProgressInfo.mOverallTotal;
        d = (double)downloadProgressInfo.mOverallProgress / d;
        this.mProgressBar.setProgress((int)(1000.0 * d));
        this.mProgressText.setText((CharSequence)(this.makeSizeString(downloadProgressInfo.mOverallProgress) + "/" + this.makeSizeString(downloadProgressInfo.mOverallTotal) + " @ " + this.makeSizeString((long)(downloadProgressInfo.mCurrentSpeed * 1024.0f)) + "/s"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public void updateState(int n) {
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        boolean bl = false;
        switch (n) {
            default: {
                this.mProgressText.setText((CharSequence)("UNKNOWN " + n));
                n = n3;
                break;
            }
            case 1: {
                this.mProgressText.setText((CharSequence)"Waiting for download to start");
                n = n3;
                break;
            }
            case 3: {
                this.mProgressText.setText((CharSequence)"Connecting to download server");
                n = n3;
                break;
            }
            case 2: {
                this.mProgressText.setText((CharSequence)"Looking for resources to download");
                n = n3;
                break;
            }
            case 4: {
                this.mProgressText.setText((CharSequence)"Downloading resources");
                n = n3;
                break;
            }
            case 18: {
                this.mProgressText.setText((CharSequence)"Download canceled");
                bl = true;
                n = n3;
                break;
            }
            case 19: {
                this.mProgressText.setText((CharSequence)"UNKNOWN ERROR");
                bl = true;
                n = n3;
                break;
            }
            case 16: {
                this.mProgressText.setText((CharSequence)"Error: Unable to locate data on server");
                bl = true;
                n = n3;
                break;
            }
            case 15: {
                this.mProgressText.setText((CharSequence)"Error: License check failed, please purcahse the app");
                bl = true;
                n = n3;
                break;
            }
            case 9: {
                this.mProgressText.setText((CharSequence)"Paused: No cellular");
                n = 1;
                break;
            }
            case 8: {
                this.mProgressText.setText((CharSequence)"Paused: WIFI disabled");
                n = 1;
                break;
            }
            case 7: {
                this.mProgressText.setText((CharSequence)"Paused");
                n = 1;
                break;
            }
            case 12: {
                this.mProgressText.setText((CharSequence)"Paused: Roaming");
                n = 1;
                break;
            }
            case 14: {
                this.mProgressText.setText((CharSequence)"Paused: SD CARD is Unavailable");
                n = 1;
                break;
            }
            case 5: {
                this.mProgressText.setText((CharSequence)"Download finished");
                n4 = 1;
                n = n3;
            }
        }
        Button button = this.mPlayButton;
        n4 = n4 != 0 ? 0 : 8;
        button.setVisibility(n4);
        button = this.mResumeButton;
        n = n != 0 ? 0 : 8;
        button.setVisibility(n);
        button = this.mRetryButton;
        n = bl ? n2 : 8;
        button.setVisibility(n);
    }

}

