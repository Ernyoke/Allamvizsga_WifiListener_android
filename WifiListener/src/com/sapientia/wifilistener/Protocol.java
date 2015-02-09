package com.sapientia.wifilistener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

//this class receives the raw buffer from a datagram and rebuilds it conform a set protocol
public class Protocol {
	
	byte[] buffer;
	
	BigInteger timestamp;
	PROTOCOL_ID id;
	int clientId;
	int packets;
	int packet_nr;
	int buffsize;
	
	private static final int CONTENTSIZE = 65000;
	
	ArrayList<byte[]> data = new ArrayList<byte[]>();
	private int contentLength = 0;
	
	public enum PROTOCOL_ID {
		LOGIN(1),
		LOGIN_ACK(2),
		LOGOUT(3),
		GET_LIST(4),
		SOUND(5);

	    private int numVal;

	    PROTOCOL_ID(int numVal) {
	        this.numVal = numVal;
	    }

	    public int getNumVal() {
	        return numVal;
	    }
	    
	    public static final Map<Integer, PROTOCOL_ID> intValues = new HashMap<>();
	    
	    static {
	        for (PROTOCOL_ID value : values()) {
	            intValues.put(value.numVal, value);
	        }
	    }
	 
	    public static PROTOCOL_ID fromIntValue(Integer dbValue) {
	        // this returns null for invalid value, check for null and throw exception if you need it
	        return intValues.get(dbValue);
	    }
	}
	
	public static final int TIMESTAMP_SIZE = 8;
	
	public Protocol(byte[] buffer) {
		this.buffer = buffer;
		buildProtocol();
	}
	
	public Protocol(PROTOCOL_ID id, int clientId, long timeStamp, byte[] content) {
		this.id = id;
		this.clientId = clientId;
		splitContent(content);
	}
	
	private void splitContent(byte[] content) {
		while(content.length > CONTENTSIZE) {
			byte[] tmpBuffer = Arrays.copyOfRange(content, 0, CONTENTSIZE);
			data.add(tmpBuffer);
			contentLength += content.length;
		}
		byte[] tmpBuffer = Arrays.copyOfRange(content, 0, CONTENTSIZE);
		data.add(tmpBuffer);
		contentLength += content.length;
	}
	
	
	private void buildProtocol() {
		ByteBuffer tmpBuffer = ByteBuffer.wrap(buffer);
		int id = tmpBuffer.getInt();
		this.id = PROTOCOL_ID.fromIntValue(id);
		this.clientId = tmpBuffer.getInt();
//		byte[] timestampBuff = new byte[TIMESTAMP_SIZE];
//		tmpBuffer.get(timestampBuff, 0, TIMESTAMP_SIZE);
		long timp = tmpBuffer.getLong();
//		timestamp = new BigInteger(timestampBuff);
		packets = tmpBuffer.getInt();
		packet_nr = tmpBuffer.getInt();
		buffsize = tmpBuffer.getInt();
		//double
		buffsize = tmpBuffer.getInt();
		byte[] tmpData = new byte[buffsize];
		tmpBuffer.get(tmpData, 0, buffsize);
		data.add(tmpData);
	}
	
	public PROTOCOL_ID getId() {
		return this.id;
	}
	
	public int getClientId() {
		return this.clientId;
	}
	
	public byte[] getData() {
		ByteArrayOutputStream out = new ByteArrayOutputStream( );
		for(byte[] chunk : data) {
			try {
				out.write(chunk);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out.toByteArray();
	}
	
	public BigInteger getTimestamp() {
		return timestamp;
	}
	
	public int getPackets() {
		return packets;
	}
	
	public int getPacketNr() {
		return packet_nr;
	}
	
	public int getDataSize() {
		return buffsize;
	}
	
	
	public void send(DatagramSocket socket, InetAddress address, int port) {
		int i = 0;
		long asd = 65484789;
		for(byte[] chunk : data) {
			ByteBuffer buffer = ByteBuffer.allocate(65507);
			buffer.putInt(id.getNumVal());
			buffer.putInt(clientId);
			buffer.putLong(asd);
			buffer.putInt(data.size());
			buffer.putInt(i);
			buffer.putInt(chunk.length);
			buffer.putInt(chunk.length);
			buffer.put(chunk);
			DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);
			packet.setPort(port);
			packet.setAddress(address);
			try {
				socket.send(packet);
			} catch (IOException e) {
				Log.d(Constants.LOG, "Send error:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	

}
