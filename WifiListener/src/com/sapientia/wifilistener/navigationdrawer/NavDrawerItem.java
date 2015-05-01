package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.HandleServices;

public abstract class NavDrawerItem {
	
	protected String title;
	protected HandleServices handleServices;
	
	public static final int TYPE_SECTION_TITLE = 0;
	public static final int TYPE_CHANNEL = 1;
	public static final int TYPE_OPTION = 2;
	
	//implicit constructor
	public NavDrawerItem() {
		//
	}
	
	public NavDrawerItem(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public abstract BaseFragment getFragment();
	
	public abstract int type();

}
