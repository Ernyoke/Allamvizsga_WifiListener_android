package com.sapientia.wifilistener;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServerCommunicatorService extends Service{
	
	private IBinder mBinder = new LocalBinder();
	
	private InetAddress serverAddress;
	private int serverPort = 20000;
	private boolean loggedIn = false;
	
	private int clientId = 0;
	
	private ServerCommunicatorWorker worker;
	private Thread thread;
	
	public class LocalBinder extends Binder {
		ServerCommunicatorService getService() {
			return ServerCommunicatorService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Constants.LOG, "ServiceCommunicator started");
		worker = new ServerCommunicatorWorker();
		thread = new Thread(worker);
		thread.start();
		Log.d(Constants.LOG, "Worker started!");
		return START_STICKY;	
	}
	
	
	public void loginSuccess(int clientId) {
		if(clientId > 0) {
			loggedIn = true;
			this.clientId = clientId;
		}
	}
	
	public boolean isLogedIn() {
		return loggedIn;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(Constants.LOG, "CommServ distroyed!");
		worker.stopRunning();
	}
	
	public void addUsercreatedItem(NavDrawerItem item) {
		worker.addUsercreatedItem(item);
	}
	
	public ArrayList<NavDrawerItem> getFromUserList() {
		return worker.getFromUserList();
	}
	
	public ArrayList<NavDrawerItem> getFromServerList() {
		return worker.getFromServerList();
	}

}
