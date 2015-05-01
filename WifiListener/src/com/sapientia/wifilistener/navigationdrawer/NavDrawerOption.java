package com.sapientia.wifilistener.navigationdrawer;

public abstract class NavDrawerOption extends NavDrawerItem {
	
	protected int icon = 0;
	
	public NavDrawerOption(String title) {
		super(title);
	}

	@Override
	public abstract BaseFragment getFragment();

	@Override
	public int type() {
		return TYPE_OPTION;
	}
	
	public int getIconRes() {
		return icon;
	}

}
