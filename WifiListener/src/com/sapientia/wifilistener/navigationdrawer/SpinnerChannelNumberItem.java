package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerChannelNumberItem extends BaseSpinner {
	private int channels;
	
	public SpinnerChannelNumberItem(String toDisplay, int channels) {
		super(toDisplay);
		this.channels = channels;
	}

	public int getChannelNumber() {
		return channels;
	}
}
