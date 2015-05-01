package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.R;


public class NavDrawerAddNewChannel extends NavDrawerOption {
	
	private DrawerListAdaper adapter;

	public NavDrawerAddNewChannel(String title, DrawerListAdaper adapter) {
		super(title);
		this.adapter = adapter;
		icon = R.drawable.ic_action_new;
	}
	
	@Override
	public BaseFragment getFragment() {
		AddNewChannelFragment fragment = new AddNewChannelFragment();
		fragment.setAdapter(adapter);
		return fragment;
	}

}
