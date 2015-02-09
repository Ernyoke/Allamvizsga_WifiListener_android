package com.sapientia.wifilistener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

public class ServerCommunicatorWorker implements Runnable{
	
	private List<NavDrawerItem> fromServer = Collections.synchronizedList(new ArrayList<NavDrawerItem>());
	private List<NavDrawerItem> fromUser = Collections.synchronizedList(new ArrayList<NavDrawerItem>());
	private boolean finish = false;

	@Override
	public void run() {
		while(true) {
			if(finish) {
				try {
					Log.d(Constants.LOG, "Worker running");
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.d(Constants.LOG, e.getMessage());
				}
				break;
			}
		}
		Log.d(Constants.LOG, "Worker closed!");
	}
	
	public void addUsercreatedItem(NavDrawerItem item) {
		fromUser.add(item);
	}
	
	public ArrayList<NavDrawerItem> getFromUserList() {
		return new ArrayList<NavDrawerItem>(fromUser);
	}
	
	public ArrayList<NavDrawerItem> getFromServerList() {
		return new ArrayList<NavDrawerItem>(fromServer);
	}
	
	public void stopRunning() {
		this.finish = true;
		Log.d(Constants.LOG, "Worker finished!");
	}

}
