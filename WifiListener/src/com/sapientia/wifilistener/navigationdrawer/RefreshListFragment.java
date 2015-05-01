package com.sapientia.wifilistener.navigationdrawer;

import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sapientia.wifilistener.R;

public class RefreshListFragment extends BaseFragment {
	
	private TextView statusTV;
	private ProgressBar refreshProgress;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.refresh_fragment, container, false); 
        statusTV = (TextView) rootView.findViewById(R.id.refreshState);
        refreshProgress = (ProgressBar) rootView.findViewById(R.id.refreshProgressBar);
        
        handleServices.refreshChannelList();
        
        return rootView;
	}
	
	public void refreshOk() {
		refreshProgress.setVisibility(View.INVISIBLE);
		statusTV.setText("Channellist refreshed succesfull!");
	}
	
	public void refreshFailed() {
		refreshProgress.setVisibility(View.INVISIBLE);
		statusTV.setText("Channellist failed to refresh!");
	}

}
