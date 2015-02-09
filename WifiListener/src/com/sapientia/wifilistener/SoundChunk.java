package com.sapientia.wifilistener;

import java.nio.ByteBuffer;

public class SoundChunk {
	
	int frekv;
	int channels;
	char[] codec;
	int buffsize;
	byte[] soundPacket;
	
	public static final int CODEC_SIZE = 20;
	
	public SoundChunk(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		frekv = buffer.getInt();
		channels = buffer.getInt();
		byte[] tempCodec = new byte[CODEC_SIZE];
		buffer.get(tempCodec, 0, CODEC_SIZE);
		codec = tempCodec.toString().toCharArray();
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
