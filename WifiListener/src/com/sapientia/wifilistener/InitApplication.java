package com.sapientia.wifilistener;

import java.util.ArrayList;

import com.sapientia.wifilistener.navigationdrawer.NavDrawerChannel;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

public interface InitApplication {
	public abstract void showFragment(int pos);
	public abstract void initNavDrawerModel(ArrayList<NavDrawerItem> fromServer, ArrayList<NavDrawerItem> fromUser);
	public abstract void initNavDrawerModelFromServer(ArrayList<NavDrawerItem> fromServer);
	public abstract void adapterDataChanged();
	public abstract void initAuthFrame();
	public abstract void authResult(boolean result);
}
