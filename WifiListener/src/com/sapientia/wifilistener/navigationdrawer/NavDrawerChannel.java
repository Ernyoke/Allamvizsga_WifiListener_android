package com.sapientia.wifilistener.navigationdrawer;

import android.support.v4.app.Fragment;

import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.ReceiveService.STATE;

public class NavDrawerChannel extends NavDrawerItem{

	private int port;
	private int frequency;
	private String codec;
	private STATE state;
	
	public NavDrawerChannel(String title, int port, int frequency, String codec, HandleServices handleServices) {
		super(title, handleServices);
		this.port = port;
		this.frequency = frequency;
		this.codec = codec;
		this.state = STATE.STOPPED;
	}
	
	public String getTitle() {
		return this.title + " (" + port + ")"; 
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getFrequency() {
		return this.frequency;
	}
	
	public String getCodec() {
		return this.codec;
	}
	
	public void setState(STATE state) {
		this.state = state;
	}
	
	public STATE getState() {
		return this.state;
	}
	
	public String getState_str() {
		return stateToString(state);
	}
	
	
	private String stateToString(STATE state) {
		switch(state) {
		case STOPPED: {
			return "stopped";
		}
		case PAUSED: {
			return "paused";
		}
		case PLAYING: {
			return "playing";
		}
		}
		return "stopped";
	}
	
	
	public Fragment getFragment() {
		return new ChannelFragment();
	}


}
