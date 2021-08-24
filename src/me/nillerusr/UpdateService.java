package me.nillerusr;

import android.content.*;
import java.io.*;
import android.os.IBinder;
import java.util.concurrent.TimeUnit;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.valvesoftware.source.R;

public class UpdateService extends Service {
	NotificationManager nm;
	Bundle extras;
	static boolean service_work = false;

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if( !service_work )
		{
			service_work = true;
			try {
				extras = intent.getExtras();
				sendNotif();
			} catch( Exception e ) { }
		}
		return START_NOT_STICKY;
	}

	private void sendNotif() {

		Notification notif = new Notification(R.drawable.ic_launcher, "Update avalible", System.currentTimeMillis());
		notif.contentView = new RemoteViews(getPackageName(), R.layout.update_notify);

		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(extras.get("update_url").toString()));
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, browserIntent, 0);

		notif.contentIntent = pIntent;
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		notif.defaults |= Notification.DEFAULT_LIGHTS; // LED
		notif.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
		notif.defaults |= Notification.DEFAULT_SOUND; // Sound
		notif.priority |= Notification.PRIORITY_HIGH;

		nm.notify(1, notif);
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}
}
