package com.sapientia.wifilistener;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.ConcurrentSkipListMap;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class ReceiveThread implements Runnable{
	
	private ConcurrentSkipListMap<Long, SoundChunk> buffer;
	private volatile boolean isEnabled;
	private volatile boolean isPaused;
	private int port;
	private int packetLength;
	private AudioTrack voice;
	private DatagramSocket rSocket = null;
	private Context context;
	
	private Timer timer;
	private UpdateSpeedTask task;
	
	private NotificationManager notificationManager;
	private WifiManager wifiManager;
	private WifiLock wifilock;
	private WakeLock wakelock;
	private PowerManager powerManager;
	
	private SharedPreferences sharedSettings;
	
	private byte[] recBuffer;
	
	public static final int UDP_MAX_SIZE = 64 * 1024;
	
	public ReceiveThread(int port, String codec, int sampleRate, int sampleSize, int channels, Context context) {
		this.buffer = new ConcurrentSkipListMap<Long, SoundChunk>();
		isEnabled = true;
		isPaused = false;
		this.port = port;
		this.context = context;
		
		timer = new Timer();
		task = new UpdateSpeedTask(context);
		timer.scheduleAtFixedRate(task, 0, 1000);
		
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
		int audioFormat_encoding = AudioFormat.ENCODING_PCM_16BIT;
		
		if(codec.equals("audio/pcm") && sampleSize == 8) {
			audioFormat_encoding = AudioFormat.ENCODING_PCM_8BIT;
		}
		
		if(codec.equals("audio/pcm") && sampleSize == 16) {
			audioFormat_encoding = AudioFormat.ENCODING_PCM_16BIT;
		}
		
		int audioFormat_channel;
		
		switch(channels) {
		case 1: {
			audioFormat_channel = AudioFormat.CHANNEL_OUT_MONO;
			break;
		}
		case 2:  {
			audioFormat_channel = AudioFormat.CHANNEL_OUT_STEREO;
			break;
		}
		default: {
			audioFormat_channel = AudioFormat.CHANNEL_OUT_MONO;
		}
		}
		
		try {
			voice = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
	                audioFormat_channel, audioFormat_encoding, 
	                16384, AudioTrack.MODE_STREAM);
			voice.play();
		}
		catch(IllegalArgumentException e) {
			Toast.makeText(context, context.getString(R.string.not_supported), 
					   Toast.LENGTH_LONG).show();
		}
		
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		
	}

	public void stopRec() {
		if(isEnabled) {
			rSocket.close();
			try{
			voice.stop();
			}
			catch(IllegalStateException e) {
				Log.d(Constants.LOG, e.getMessage());
				e.printStackTrace();
			}
			voice.release();
			isEnabled = false;
			Intent intent = new Intent(Constants.BROADCAST);
			intent.setAction(Constants.BROADCAST_STOPPED);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			timer.cancel();
		}
	}
	
	public void pauseRec() {
		isPaused = true;
		voice.pause();
		voice.flush();
	}
	
	public void unPauseRec() {
		isPaused = false;
		voice.play();
	}


	@Override
	public void run() {
		wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "locsss");
		WifiManager.MulticastLock lock = wifiManager.createMulticastLock("multicast_lock_kurva_jo_anyadat");
//		wifilock.acquire();
		
//		if(!wifilock.isHeld()) {
//			wifilock.acquire();
//			Log.d(Constants.LOG, "wifilock aquired!");
//		}
//		else {
//			Log.d(Constants.LOG, "wifilock not aquired!");
//		}
//		wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "asdasd");
//		if(!wakelock.isHeld()) {
//			wakelock.acquire();
//			Log.d(Constants.LOG, "wakelock aquired!");
//		}
//		else {
//			Log.d(Constants.LOG, "wakelock not aquired!");
//		}
		if(!lock.isHeld()) {
			lock.acquire();
			Log.d(Constants.LOG, "multicast aquired!");
		}
		else {
			Log.d(Constants.LOG, "multicast not aquired!");
		}
		
		//buffer for receiving UDP packets
		recBuffer = new byte[64 * 1024];
	    DatagramPacket rPacket = new DatagramPacket(recBuffer, 64 * 1024);
	    try {
			rSocket = new DatagramSocket(port);
			rSocket.setBroadcast(true);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.LOG, e.getMessage());
		}
	    Log.d(Constants.LOG, "player should start!");
		while(isEnabled) {
			
			if(!isPaused) {
				try {
					
					rSocket.receive(rPacket);
					packetLength = rPacket.getLength();
					task.setData(packetLength);
					//rebuild packet
					Protocol protocol = new Protocol(recBuffer);
					long timeStamp = protocol.getTimestamp();
					byte[] data = protocol.getData();
					SoundChunk soundchunk = new SoundChunk(data);
					buffer.put(timeStamp, soundchunk);
//					Log.d(Constants.LOG, timeStamp + "");
					if(buffer.size() >= 2) {
						Iterator<Entry<Long, SoundChunk>> iter = buffer.entrySet().iterator();
						while(iter.hasNext()) { 
							Long time = iter.next().getKey();
							SoundChunk sample = buffer.get(time);
							voice.write(sample.getRawsound(), 0, sample.getSoundSize());
						}
						buffer.clear();
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(Constants.LOG, e.getMessage());
				}
			}
			else {
				//
			}
			if(!isEnabled) {
				lock.release();
//				wifilock.release();
//				wakelock.release();
				break;
			}
			else {
//				Log.d(Constants.LOG, "isenabled!");
			}
		}
//		wakelock.release();
//		wifilock.release();
		
	}
	
}
