package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerFreqItem extends BaseSpinner {
	
	private int freq;
	
	public SpinnerFreqItem(String toDisplay, int freq) {
		super(toDisplay);
		this.freq = freq;
	}

	public int getFreq() {
		return freq;
	}

}
