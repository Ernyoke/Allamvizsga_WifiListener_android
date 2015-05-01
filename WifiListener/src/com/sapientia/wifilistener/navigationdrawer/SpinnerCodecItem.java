package com.sapientia.wifilistener.navigationdrawer;

public class SpinnerCodecItem extends BaseSpinner {
	private String codec;
	
	public SpinnerCodecItem(String toDisplay, String codec) {
		super(toDisplay);
		this.codec = codec;
	}

	public String getCodec() {
		return codec;
	}
	
}
