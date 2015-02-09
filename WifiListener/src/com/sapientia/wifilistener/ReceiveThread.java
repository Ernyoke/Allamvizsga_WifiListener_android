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
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class ReceiveThread implements Runnable{
	
	private ConcurrentSkipListMap<BigInteger, SoundChunk> buffer;
	private boolean isEnabled;
	private boolean isPaused;
	private int port;
	private int packetLength;
	private AudioTrack voice;
	private DatagramSocket rSocket = null;
	private Context context;
	
	private Timer timer;
	private UpdateSpeedTask task;
	
	private NotificationManager notificationManager;
	private WifiManager wifiManager;
	private PowerManager powerManager;
	
	private SharedPreferences sharedSettings;
	
	private byte[] recBuffer;
	
	public static final int UDP_MAX_SIZE = 64 * 1024;
	
	public ReceiveThread(int port, Context context) {
		this.buffer = new ConcurrentSkipListMap<BigInteger, SoundChunk>();
		isEnabled = true;
		isPaused = false;
		this.port = port;
		this.context = context;
		
		timer = new Timer();
		task = new UpdateSpeedTask(context);
		timer.scheduleAtFixedRate(task, 0, 1000);
		
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		sharedSettings = context.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE);
		
		int sampleRate = sharedSettings.getInt(Constants.SETTINGS_SAMPLERATE, 8000);
		String audioFormat = sharedSettings.getString(Constants.SETTINGS_AUDIOFORMAT, "ENCODING_PCM_16BIT");
		
		switch(audioFormat) {
			case "ENCODING_PCM_8BIT" : {
				try {
					voice = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
			                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, 
			                16384, AudioTrack.MODE_STREAM);
					voice.play();
					}
					catch(IllegalArgumentException e) {
						Toast.makeText(context, context.getString(R.string.not_supported), 
								   Toast.LENGTH_LONG).show();
					}
				break;
			}
			case "ENCODING_PCM_16BIT" : {
				try {
					voice = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
			                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
			                16384, AudioTrack.MODE_STREAM);
					voice.play();
				}
				catch(IllegalArgumentException e) {
					Toast.makeText(context, context.getString(R.string.not_supported), 
							   Toast.LENGTH_LONG).show();
				}
				break;
			}
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
		WifiManager.MulticastLock lock = wifiManager.createMulticastLock("Log_Tag");
		lock.acquire();
		Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
		
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
		while(isEnabled) {
			
			if(!isPaused) {
				try {
					rSocket.receive(rPacket);
					packetLength = rPacket.getLength();
					task.setData(packetLength);
					//rebuild packet
					Protocol protocol = new Protocol(recBuffer);
					BigInteger timeStamp = protocol.getTimestamp();
					byte[] data = protocol.getData();
					SoundChunk soundchunk = new SoundChunk(data);
					buffer.put(timeStamp, soundchunk);
					Log.d(Constants.LOG, timeStamp.toString());
					//removing timestamp from the beginning of the packet
					if(buffer.size() >= 5) {
						Iterator<Entry<BigInteger, SoundChunk>> iter = buffer.entrySet().iterator();
						while(iter.hasNext()) { 
							BigInteger time = iter.next().getKey();
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
				break;
			}
		}
		
	}
	
}
