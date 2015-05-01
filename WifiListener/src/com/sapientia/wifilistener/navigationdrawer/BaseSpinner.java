package com.sapientia.wifilistener.navigationdrawer;

public class BaseSpinner {
	protected String toDisplay;
	
	public BaseSpinner(String toDisplay) {
		this.toDisplay = toDisplay;
	}
	
	public String getToDisplay() {
		return toDisplay;
	}
	
	public String toString() {
		return toDisplay;
	}
}
