package com.sapientia.wifilistener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sapientia.wifilistener.ReceiveService.LocalBinder;
import com.sapientia.wifilistener.ReceiveService.STATE;
import com.sapientia.wifilistener.navigationdrawer.MainFragment;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerChannel;

public class HandleServices implements AuthentificationResponse{
	
	private Context context;
	
	private boolean recBound = false;
	private boolean comBound = false;
	
	private InitApplication init = null;
	
	private ReceiveService recService;
	private ServerCommunicatorService comService;
	
	private int clientId = 0;
	
	private MainFragment fragment;
	
	private ServiceConnection recServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ReceiveService.LocalBinder binder = (ReceiveService.LocalBinder)service;
			recService = binder.getService();
			recBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			recBound = false;
			recService = null;
		}
		
		
	};
	
	private ServiceConnection comServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			comService = null;
			comBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ServerCommunicatorService.LocalBinder binder = (ServerCommunicatorService.LocalBinder)service;
			comService = binder.getService();
			comBound = true;
			
			if(!comService.isLogedIn()) {
				init.showFragment(-1);
				init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
			}
			else {
				init.showFragment(0);
				init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
			}
		}
	};
	
	
	public HandleServices(Context context, InitApplication init) {
		this.context = context;
		this.init = init;
	}
	
	public void startServices() {
		//start the ReceiveService
		Intent playIntent = new Intent(context, ReceiveService.class);
		context.bindService(playIntent, recServiceConnection, Context.BIND_AUTO_CREATE);
		context.startService(playIntent);
		//start the ServerCommunicatorService
		Intent comIntent = new Intent(context, ServerCommunicatorService.class);
		context.bindService(comIntent, comServiceConnection, Context.BIND_AUTO_CREATE);
		context.startService(comIntent);
	}
	
	public void startPlayer(NavDrawerChannel channel) {
		recService.setPort(channel.getPort());
		recService.startPlaying();
		channel.setState(recService.getRunningState());
	}
	
	public void stopPlayer(NavDrawerChannel channel) {
		recService.stopPlaying();
		channel.setState(recService.getRunningState());
	}
	
	public void pausePlayer(NavDrawerChannel channel) {
		recService.pausePlayer();
		channel.setState(recService.getRunningState());
	}
	
	public void distroy() {
		STATE state;
		state = recService.getRunningState();
		context.unbindService(recServiceConnection);
		context.unbindService(comServiceConnection);
		if(state == STATE.STOPPED) {
			Intent playIntent = new Intent(context, ReceiveService.class);
			context.stopService(playIntent);
			
			Intent comIntent = new Intent(context, ServerCommunicatorService.class);
			context.stopService(comIntent);
		}
	}
	
	public STATE getPlayerState() {
		STATE state = recService.getRunningState();
		return state;
	}

	@Override
	public void processResult(int result) {
		this.clientId = result;
		comService.loginSuccess(result);
		Log.d(Constants.LOG, "login succesful:" + result);
		if(fragment != null) {
			if(result > 0) {
				fragment.updateStatus(true);
			}
			else {
				fragment.updateStatus(false);
			}
		}
	}
	
	public void authentificate(String address) {
		//check if already is a client running in background
		if(!comService.isLogedIn()) {
			Authentification aut = new Authentification(HandleServices.this);
			aut.execute();
		}
		init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
		this.fragment = null;
	}
	
	public void authentificate(String address, MainFragment fragment) {
		//check if already is a client running in background
		if(!comService.isLogedIn()) {
			Authentification aut = new Authentification(HandleServices.this);
			aut.execute();
		}
		init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
		this.fragment = fragment;
	}
	
	public boolean isAuthentificated() {
		if(comBound) {
			return comService.isLogedIn();
		}
		return false;
	}
	
	public void addNewUsercreatedChannel(NavDrawerChannel channel) {
		if(comBound) {
			comService.addUsercreatedItem(channel);
		}
	}

}
