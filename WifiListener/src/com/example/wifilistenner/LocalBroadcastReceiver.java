package com.example.wifilistenner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class LocalBroadcastReceiver extends BroadcastReceiver{
	
	private TextView speed;
	
	public LocalBroadcastReceiver(TextView speed) {
		this.speed = speed;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		switch(action) {
		case Constants.BROADCAST: {
			int currentSpeed = intent.getIntExtra(Constants.SPEED, 0);
			speed.setText(currentSpeed + "KBps");
			break;
		}
		
		case Constants.BROADCAST_STARTED: {
			
			break;
		}
		
		case Constants.BROADCAST_STOPPED: {
			
			break;
		}
		
		}
	}

}
