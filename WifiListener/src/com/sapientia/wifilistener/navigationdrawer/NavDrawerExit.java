package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.R;


public class NavDrawerExit extends NavDrawerOption {
	
	public NavDrawerExit(String title) {
		super(title);
		icon = R.drawable.ic_action_exit;
	}

	@Override
	public BaseFragment getFragment() {
		return new ExitFragment();
	}

}
