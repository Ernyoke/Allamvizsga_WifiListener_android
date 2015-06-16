package com.sapientia.wifilistener;

import com.sapientia.wifilistener.R;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ReceiveService extends ForegroundServices {
	
	private ReceiveThread receiveWorker;
	private CallStateListener callListener;
	private TelephonyManager tmanager;
	
	public enum STATE {PLAYING, STOPPED, PAUSED};
	
	private STATE state = STATE.STOPPED;
	
	private IBinder mBinder = new LocalBinder();
	
	private int portInput;
	
	private PowerManager powerManager;
//	private WakeLock wakeLock;
	private WifiLock wifiLock;
	private MulticastLock multicastLock;
	
	private NotificationCompat.Builder builder;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		Log.d(Constants.LOG, "recService created");
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//		        Constants.WAKELOCK);
		
//		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//			    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, Constants.WAKELOCK);
		
//		multicastLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//				.createMulticastLock("multicastlock");
		
		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
		
		callListener = new CallStateListener(this.getApplicationContext(), this);
		
		tmanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tmanager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		if(this.state == STATE.STOPPED) {
			Log.d(Constants.LOG, "STOPPED");
		}
		else {
			Log.d(Constants.LOG, "NOT STOPPED");
		}
		
        return START_STICKY;
    }
	
	public void setToForground(NotificationCompat.Builder builder) {
		super.setToForground(builder);
		this.builder = builder;
		if(state == STATE.STOPPED) {
			builder.setContentTitle(this.getString(R.string.stopped));
			notificationManager.notify(Constants.NOTIF_ID, builder.build());
		}
		if(state == STATE.PLAYING) {
			builder.setContentTitle(this.getString(R.string.playing_listenin_at_port) + this.portInput);
			notificationManager.notify(Constants.NOTIF_ID, builder.build());
		}
	}
	
	
	public void setPort(int port) {
		this.portInput = port;
	}
	
	public void startPlaying(int portInput, String codec, int sampleRate, int sampleSize, int channels) {
		if(state == STATE.PLAYING) {
			this.stopPlaying();
		}
		if(state == STATE.STOPPED) {
			
//			if(!wakeLock.isHeld()) {
//				wakeLock.acquire();
//				Log.d("VOICE", "aquired");
//			}
//			if(!wifiLock.isHeld()) {
//				wifiLock.acquire();
//				Log.d("VOICE", "wifilock aquired");
//			}
//			if(!multicastLock.isHeld()) {
//				multicastLock.acquire();
//			}
			
			receiveWorker = new ReceiveThread(portInput, codec, sampleRate, sampleSize, channels, this);
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
			
//			wakeLock.release();
//			wifiLock.release();
//			multicastLock.release();
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
	
	public STATE getRunningState() {
		return state;
	}

}
