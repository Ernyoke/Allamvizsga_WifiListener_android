package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerSampleSizeItem extends BaseSpinner {
	
	private int sampleSize;
	
	public SpinnerSampleSizeItem(String toDisplay, int sampleSize) {
		super(toDisplay);
		this.sampleSize = sampleSize;
	}

	public int getSampleSize() {
		return sampleSize;
	}
}
