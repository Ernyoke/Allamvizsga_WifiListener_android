package com.sapientia.wifilistener;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import com.sapientia.wifilistener.ReceiveService.STATE;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerChannel;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

public class HandleServices {
	
	private Context context;
	
	private boolean recBound = false;
	private boolean comBound = false;
	
	private InitApplication init = null;
	
	private ReceiveService recService = null;
	private ServerCommunicatorService comService = null;
	
	
	private ServerCommunicatorReceiver servComRec;
	
	private NotificationCompat.Builder builder;
	
	private NavDrawerChannel lastStartedChannel = null;
	
	
	
	private ServiceConnection recServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ReceiveService.LocalBinder binder = (ReceiveService.LocalBinder)service;
			recService = binder.getService();
			recService.setToForground(builder);
			recService.startForeground(Constants.NOTIF_ID, builder.build());
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
			comService.setToForground(builder);
			comBound = true;
			
			init.showFragment(-1);
			init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
		}
	};
	
	
	public HandleServices(Context context, InitApplication init) {
		this.context = context;
		this.init = init;
		this.servComRec = new ServerCommunicatorReceiver();
		createNotification();
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
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.SERV_BROADCAST);
		intentFilter.addCategory(Constants.SERV_AUTH);
		intentFilter.addCategory(Constants.SERV_CHLIST_UPDATE);
		intentFilter.addCategory(Constants.SERV_DOWN);
		intentFilter.addCategory(Constants.SERV_NEW_CHLIST);
		LocalBroadcastManager.getInstance(context).registerReceiver(servComRec, intentFilter);
	}
	
	public void startPlayer(NavDrawerChannel channel) {
		recService.setPort(channel.getPort());
		if(recService.getRunningState() != STATE.STOPPED) {
			try {
			stopPlayer(lastStartedChannel);
			lastStartedChannel.setState(STATE.STOPPED);
			}
			catch(NullPointerException ex) {
				//
			}
		}
		recService.startPlaying(channel.getPort(), channel.getCodec(), channel.getSampleRate(), 
				channel.getSampleSize(), channel.getChannels());
		channel.setState(recService.getRunningState());
		lastStartedChannel = channel;
		init.adapterDataChanged();
	}
	
	public void stopPlayer(NavDrawerChannel channel) {
		if(channel.getState() != STATE.STOPPED) {
			recService.stopPlaying();
			channel.setState(recService.getRunningState());
		}
		init.adapterDataChanged();
		lastStartedChannel = null;
	}
	
	public void pausePlayer(NavDrawerChannel channel) {
		recService.pausePlayer();
		channel.setState(recService.getRunningState());
		init.adapterDataChanged();
	}
	
	public void distroy() {
		
		STATE state;
		state = recService.getRunningState();

		recService.stopPlaying();
		comService.disconnectFromServer();
		unbindServices();
		
		if(state == STATE.STOPPED) {
			Intent playIntent = new Intent(context, ReceiveService.class);
			context.stopService(playIntent);
			
			Intent comIntent = new Intent(context, ServerCommunicatorService.class);
			context.stopService(comIntent);
		}
		
		LocalBroadcastManager.getInstance(context).unregisterReceiver(servComRec);
		
	}
	
	public void unbindServices() {
		if(recBound) {
			context.unbindService(recServiceConnection);
			recBound = false;
		}
		if(comBound) {
			context.unbindService(comServiceConnection);
			comBound = false;
		}
	}
	
	public STATE getPlayerState() {
		STATE state = recService.getRunningState();
		return state;
	}
	
	public void authentificate(String address) {
		//check if already is a client running in background
		if(!comService.isLogedIn()) {
			comService.authentificate(address);
		}
		init.initNavDrawerModel(comService.getFromServerList(), comService.getFromUserList());
	}
	
	public void logoff() {
		//
	}
	
	//check if the client is authenticated
	public boolean isAuthenticated() {
		if(comService != null) {
			return comService.isLogedIn();
		}
		return false;
	}
	
	//add new Channel
	public void addNewUsercreatedChannel(NavDrawerChannel channel) {
		if(comBound) {
			comService.addUsercreatedItem(channel);
		}
	}
	
	//create the Notification for the forground services
	protected void createNotification() {
		builder = new NotificationCompat.Builder(context);
		builder.setContentText(context.getString(R.string.tap_to_open));
		builder.setAutoCancel(false);
		builder.setSmallIcon(R.drawable.ic_launcher_white);
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		
		//return back a builder, within this the notification can be modified
		builder.setContentIntent(resultPendingIntent);
	}
	
	public void refreshChannelList() {
		comService.requestChannelListFromServer();
	}

	
	//broadcast receiver class, receives messages from ServerCommunicatorService
	private class ServerCommunicatorReceiver extends BroadcastReceiver{
		 
		 @Override
		 public void onReceive(Context arg0, Intent intent) {
			 if(intent.hasCategory(Constants.SERV_AUTH)) {
				 boolean loginSucces = intent.getBooleanExtra(Constants.SERV_AUTH_SUCCESS, false);
				 init.authResult(loginSucces);
			 }
			 if(intent.hasCategory(Constants.SERV_NEW_CHLIST)) {
				 ArrayList<NavDrawerItem> fromServer = comService.getFromServerList();
				 init.initNavDrawerModelFromServer(fromServer);
			 }
			 if(intent.hasCategory(Constants.SERV_DOWN)) {
				 if(lastStartedChannel != null) {
					 stopPlayer(lastStartedChannel);
				 }
				 init.initAuthFrame();
			 }
			 if(intent.hasCategory(Constants.SERV_CHLIST_UPDATE)) {
				 init.adapterDataChanged();
			 }
		 }
		 
		}
	

}



