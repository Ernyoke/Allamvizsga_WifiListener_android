package com.sapientia.wifilistener;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallStateListener extends PhoneStateListener {
	
	private ReceiveService service;
	boolean paused = false;
	
	public CallStateListener(Context context, ReceiveService service) {
		this.service = service;
	}
	
	public void onCallStateChanged(int state, String incomingNumber) {
	      switch (state) {
	          case TelephonyManager.CALL_STATE_RINGING: {
	        	  if(!paused) {
	        		  service.pausePlayer();
	        		  Log.d(Constants.LOG, "Incomming call from number" + incomingNumber + ". Player paused.");
	        		  paused = true;
	        	  }
		          break;
	          }
	          case TelephonyManager.CALL_STATE_IDLE: {
	        	  if(paused) {
	        		  service.pausePlayer();
	        		  Log.d(Constants.LOG, "Call ended. Player unpaused.");
	        		  paused = false;
	        	  }
	          }
	          
	      }
	  }

}
