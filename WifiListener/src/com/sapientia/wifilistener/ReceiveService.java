package com.sapientia.wifilistener;

import com.sapientia.wifilistener.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ReceiveService extends Service{
	
	private ReceiveThread receiveWorker;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder builder;
	private CallStateListener callListener;
	private TelephonyManager tmanager;
	
	public enum STATE {PLAYING, STOPPED, PAUSED};
	
	private STATE state = STATE.STOPPED;
	
	private IBinder mBinder = new LocalBinder();
	
	private int portInput;
	
	private PowerManager powerManager;
	private WakeLock wakeLock;
	private WifiLock wifiLock;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(Constants.LOG, "recService created");
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		        Constants.WAKELOCK);
		
		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
			    .createWifiLock(WifiManager.WIFI_MODE_FULL, Constants.WAKELOCK);
		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
		
		callListener = new CallStateListener(this.getApplicationContext(), this);
		
		tmanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tmanager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		this.startForeground(Constants.NOTIF_ID, showNotification());
		
		if(this.state == STATE.STOPPED) {
			Log.d(Constants.LOG, "STOPPED");
		}
		else {
			Log.d(Constants.LOG, "NOT STOPPED");
		}
		
        return START_STICKY;
    }
	
	public void setPort(int port) {
		this.portInput = port;
	}
	
	public void startPlaying() {
		if(state == STATE.STOPPED) {
			
			if(!wakeLock.isHeld()) {
				wakeLock.acquire();
				Log.d("VOICE", "aquired");
			}
			if(!wifiLock.isHeld()) {
				wifiLock.acquire();
				Log.d("VOICE", "aquired");
			}
			
			receiveWorker = new ReceiveThread(portInput, this);
			Thread thread = new Thread(receiveWorker);
			thread.start();
			builder.setContentTitle(this.getString(R.string.playing_listenin_at_port) + portInput);
			notificationManager.notify(Constants.NOTIF_ID, builder.build());
			state = STATE.PLAYING;
		}
	}
	
	public void pausePlayer() {
		if(state == STATE.PLAYING) {
			state = STATE.PAUSED;
			builder.setContentTitle(this.getString(R.string.paused_listenin_at_port) + portInput);
			notificationManager.notify(Constants.NOTIF_ID, builder.build());
			receiveWorker.pauseRec();
		}
		else {
			if(state == STATE.PAUSED) {
				state = STATE.PLAYING;
				builder.setContentTitle(this.getString(R.string.playing_listenin_at_port) + portInput);
				notificationManager.notify(Constants.NOTIF_ID, builder.build());
				receiveWorker.unPauseRec();
			}
		}
	}
	
	public void stopPlaying() {
		if(state != STATE.STOPPED) {
			receiveWorker.stopRec();
			state = STATE.STOPPED;
			builder.setContentTitle(this.getString(R.string.stopped));
			notificationManager.notify(Constants.NOTIF_ID, builder.build());
			
			wakeLock.release();
			wifiLock.release();
			Log.d("VOICE", "released");
		}
	}
	
	@Override 
	public void onDestroy() {
		if(state != STATE.STOPPED) {
			receiveWorker.stopRec();
		}
    }
	
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}
	
	public class LocalBinder extends Binder {
		public ReceiveService getService() {
			return ReceiveService.this;
		}
	}
	
	private Notification showNotification() {
		builder = new NotificationCompat.Builder(this);
		if(state == STATE.STOPPED) {
			builder.setContentTitle(this.getString(R.string.stopped));
		}
		if(state == STATE.PLAYING) {
			builder.setContentTitle(this.getString(R.string.playing_listenin_at_port) + this.portInput);
		}
		builder.setContentText(this.getString(R.string.tap_to_open));
//		builder.setLargeIcon(R.drawable.ic_launcher);
		builder.setAutoCancel(false);
		builder.setSmallIcon(R.drawable.ic_launcher);
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		
		builder.setContentIntent(resultPendingIntent);
		return builder.build();
	}
	
	public STATE getRunningState() {
		return state;
	}

}
