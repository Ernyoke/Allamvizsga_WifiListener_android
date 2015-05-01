package com.sapientia.wifilistener;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class SoundChunk {
	
	int sampleRate;
	int sampleSize;
	int channels;
	int codecSize;
	String codec;
	int buffsize;
	byte[] soundPacket;
	
	public static final int CODEC_SIZE = 20;
	
	public SoundChunk(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		sampleRate = buffer.getInt();
		sampleSize = buffer.getInt();
		channels = buffer.getInt();
		codecSize = buffer.getInt();
		byte[] codecBuff = new byte[codecSize];
		buffer.get(codecBuff, 0, codecSize);
		codec = new String(codecBuff, Charset.forName("UTF-16BE"));
		buffsize = buffer.getInt();
		soundPacket = new byte[buffsize];
		buffer.get(soundPacket, 0, buffsize);
	}
	
	public byte[] getRawsound() {
		return soundPacket;
	}
	
	public int getSoundSize() {
		return buffsize;
	}

}
