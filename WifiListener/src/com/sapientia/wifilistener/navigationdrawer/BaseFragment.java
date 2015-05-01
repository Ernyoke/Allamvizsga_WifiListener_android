package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.HandleServices;

import android.content.Context;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment{
	
	protected HandleServices handleServices;
	
	protected Context context;
	
	public void setServiceHandler(HandleServices handleServices) {
		this.handleServices = handleServices;
	}
	
}
