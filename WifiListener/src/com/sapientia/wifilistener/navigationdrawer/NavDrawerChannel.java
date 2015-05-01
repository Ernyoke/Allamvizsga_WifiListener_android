package com.sapientia.wifilistener.navigationdrawer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.ReceiveService.STATE;

public class NavDrawerChannel extends NavDrawerItem implements Parcelable {

	private int port;
	private int sampleRate;
	private int sampleSize;
	private int channels;
	private int ownerId;
	private String codec;
	private STATE state;
	
	public NavDrawerChannel(String title, int port, int sampleRate, int sampleSize, int channels, String codec) {
		super(title);
		this.port = port;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		this.channels = channels;
		this.codec = codec;
		this.state = STATE.STOPPED;
	}
	
	public NavDrawerChannel(byte[] content) {
		ByteBuffer tmpBuffer  = ByteBuffer.wrap(content);
		ownerId = tmpBuffer.getInt();
		port = tmpBuffer.getShort();
		sampleRate = tmpBuffer.getInt();
		sampleSize = tmpBuffer.getInt();
		channels = tmpBuffer.getInt();
		
		//recreate codec name
		int codecSize = tmpBuffer.getInt();
		byte[] codecBuff = new byte[codecSize]; 
		tmpBuffer.get(codecBuff, 0, codecSize);
		codec = new String(codecBuff, Charset.forName("UTF-16BE"));
		
		//recreate language string
		int langSize = tmpBuffer.getInt();
		byte[] langBuff = new byte[langSize]; 
		tmpBuffer.get(langBuff, 0, langSize);
		title = new String(langBuff, Charset.forName("UTF-16BE"));
		
		this.state = STATE.STOPPED;
	}
	
	public NavDrawerChannel(Parcel in) {
		this.port = in.readInt();
		this.sampleRate = in.readInt();
		this.sampleSize = in.readInt();
		this.channels = in.readInt();
		this.ownerId = in.readInt();
		this.codec = in.readString();
		this.state = (STATE) in.readSerializable();
		
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getSubtitle() {
		StringBuilder subtitle = new StringBuilder();
		subtitle.append(this.codec);
		subtitle.append(", ");
		subtitle.append(this.sampleRate);
		subtitle.append(" Hz, ");
		subtitle.append(", Port: ");
		subtitle.append(this.port);
		return subtitle.toString();
		
	}
	
	public int getPort() {
		return this.port;
	}
	
	
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	public int getSampleSize() {
		return this.sampleSize;
	}
	
	public int getChannels() {
		return this.channels;
	}
	
	public int getOwnerId() {
		return this.ownerId;
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
		return stateToString(this.state);
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
	
	
	public BaseFragment getFragment() {
		ChannelFragment channel = new ChannelFragment();
		channel.setChannel(this);
		return channel;
	}
	
	@Override
	public int type() {
		return TYPE_CHANNEL;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(port);
		dest.writeInt(sampleRate);
		dest.writeInt(sampleSize);
		dest.writeInt(channels);
		dest.writeInt(ownerId);
		dest.writeString(codec);
		dest.writeSerializable(state);
	}


}
