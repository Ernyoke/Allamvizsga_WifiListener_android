package com.sapientia.wifilistener.navigationdrawer;

import android.support.v4.app.Fragment;
import com.sapientia.wifilistener.HandleServices;


public class NavDrawerItem {
	
	protected String title;
	protected HandleServices handleServices;
	
	public NavDrawerItem(String title, HandleServices handleServices) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

}
