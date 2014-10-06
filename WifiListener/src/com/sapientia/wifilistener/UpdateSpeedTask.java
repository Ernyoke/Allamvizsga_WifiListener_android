package com.sapientia.wifilistener;

import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class UpdateSpeedTask extends TimerTask{
	
	private Context context;
	private int dataReceived;
	
	public UpdateSpeedTask(Context context) {
		this.context = context;
		this.dataReceived = 0;
	}
	
	public void setData(int dataReceived) {
		this.dataReceived += dataReceived;
	}

	@Override
	public void run() {
		Intent intent = new Intent(Constants.BROADCAST);
		intent.setAction(Constants.BROADCAST);
		intent.putExtra(Constants.SPEED, dataReceived / 1024);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		this.dataReceived = 0;
	}

}
