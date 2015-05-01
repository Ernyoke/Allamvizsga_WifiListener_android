package com.sapientia.wifilistener;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

//the parent of RecService and ServerCommunicatorService
public class ForegroundServices extends Service {

	protected NotificationManager notificationManager;
	protected NotificationCompat.Builder builder;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	//set service to foreground mode
	public void setToForground(NotificationCompat.Builder builder) {
		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		startForeground(Constants.NOTIF_ID, builder.build());
	}
	
	public void closeForground() {
		stopForeground(true);
	}
	

}
