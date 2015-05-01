package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.R;


public class NavDrawerRefresh extends NavDrawerOption {
	
	public NavDrawerRefresh(String title) {
		super(title);
		icon = R.drawable.ic_action_refresh;
	}

	@Override
	public BaseFragment getFragment() {
		return new RefreshListFragment();
	}
	

}
