package com.sapientia.wifilistener.navigationdrawer;

public class NavDrawerSectionTitle extends NavDrawerItem {
	
	public NavDrawerSectionTitle(String title) {
		super(title);
	}

	@Override
	public BaseFragment getFragment() {
		return null;
	}
	
	@Override
	public int type() {
		return TYPE_SECTION_TITLE;
	}

}
