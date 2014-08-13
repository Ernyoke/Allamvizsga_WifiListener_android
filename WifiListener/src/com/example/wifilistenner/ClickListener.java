package com.example.wifilistenner;

import com.example.wifilistenner.ReceiveService.LocalBinder;
import com.example.wifilistenner.ReceiveService.STATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ClickListener implements View.OnClickListener{
	
	private Context context;
	private EditText portInput;
	private TextView stateDisplay;
	private ReceiveThread receiveWorker;
	
	private boolean mBound = false;
	
	private ReceiveService recService;
	
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder)service;
			recService = binder.getService();
			mBound = true;
			resumeView();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			recService = null;
		}
		
		private void resumeView() {
//			if(recService != null) {
			STATE state = recService.getRunningState();
			switch(state) {
			case STOPPED: {
				stateDisplay.setText("stopped");
				break;
			}
			case PAUSED: {
				stateDisplay.setText("paused");
				break;
			}
			case PLAYING: {
				stateDisplay.setText("playing");
				break;
			}
//			}
			}
		}
		
	};
	
	public ClickListener(Context context, EditText portInput, TextView stateDisplay) {
		this.context = context;
		this.portInput = portInput;
		this.stateDisplay = stateDisplay;
		
		//start the service
		Intent playIntent = new Intent(context, ReceiveService.class);
		context.bindService(playIntent, connection, Context.BIND_AUTO_CREATE);
		context.startService(playIntent);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.startButton : {
			int port = 0;
			try {
				port = Integer.parseInt(portInput.getText().toString());
				recService.setPort(port);
				recService.startPlaying();
				stateDisplay.setText(stateToString(recService.getRunningState()));
			}
			catch(NumberFormatException ex) {
				Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		case R.id.stopButton: {
			recService.stopPlaying();
			stateDisplay.setText(stateToString(recService.getRunningState()));
			break;
		}
		
		case R.id.pauseButton: {
			STATE state = recService.getRunningState();
			recService.pausePlayer();
			stateDisplay.setText(stateToString(state));
			break;
		}
		}
		
	}
	
	public void distroy() {
		STATE state;
		state = recService.getRunningState();
		context.unbindService(connection);
		if(state == STATE.STOPPED) {
			Intent playIntent = new Intent(context, ReceiveService.class);
			context.stopService(playIntent);
		}
	}
	
	private String stateToString(STATE state) {
		switch(state) {
		case STOPPED: {
			return "stopped";
		}
		case PAUSED: {
			return "paused";
		}
		case PLAYING: {
			return "playing";
		}
		}
		return "stopped";
	}

}
