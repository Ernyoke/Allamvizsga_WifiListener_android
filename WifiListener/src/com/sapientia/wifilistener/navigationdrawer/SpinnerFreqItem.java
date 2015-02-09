package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerFreqItem {
	
	private String toDisplay;
	private int freq;
	
	public SpinnerFreqItem(String toDisplay, int freq) {
		this.freq = freq;
		this.toDisplay = toDisplay;
	}

	public String getToDisplay() {
		return toDisplay;
	}

	public int getFreq() {
		return freq;
	}
	
	public String toString() {
		return toDisplay;
	}
	

}
