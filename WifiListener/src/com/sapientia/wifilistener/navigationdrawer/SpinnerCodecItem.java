package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerCodecItem {
	private String toDisplay;
	private String codec;
	
	public SpinnerCodecItem(String toDisplay, String codec) {
		this.codec = codec;
		this.toDisplay = toDisplay;
	}

	public String getToDisplay() {
		return toDisplay;
	}

	public String getCodec() {
		return codec;
	}
	
	public String toString() {
		return toDisplay;
	}
}
