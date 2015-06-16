package com.sapientia.wifilistener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.os.AsyncTask;
import android.util.Log;

//this class receives the raw buffer from a datagram and rebuilds it conform a set protocol
public class Protocol {
	
	byte[] buffer;
	
	long timestamp;
	PROTOCOL_ID id;
	int clientId;
	int packets;
	int packet_nr;
	int buffsize;
	
	static long packetCounter = 0;
	
	private static final int MAX_AVAILABLE = 1;
	private final Semaphore mutex = new Semaphore(MAX_AVAILABLE, true);
	
	private static final int CONTENTSIZE = 65000;
	
	ArrayList<byte[]> data = new ArrayList<byte[]>();
	private int contentLength = 0;
	
	public enum PROTOCOL_ID {
		LOGIN(1),
		LOGIN_ACK(2),
		LOGOUT(3),
		GET_LIST(4),
		LIST(5),
		SOUND(6),
		NEW_CHANNEL(7),
		NEW_CHANNEL_ACK(8),
		CLOSE_CHANNEL(9), 
		REMOVE_CHANNEL(10),
		SYNCH(11),
		SYNCH_RESP(12),
		SERVER_DOWN(13);

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
		byte[] tmpBuffer = Arrays.copyOfRange(content, 0, content.length);
		data.add(tmpBuffer);
		contentLength += content.length;
	}
	
	
	private void buildProtocol() {
		ByteBuffer tmpBuffer = ByteBuffer.wrap(buffer);
		long packetCounterTmp = tmpBuffer.getLong();
		int id = tmpBuffer.getInt();
		this.id = PROTOCOL_ID.fromIntValue(id);
		this.clientId = tmpBuffer.getInt();
		timestamp = tmpBuffer.getLong();
		packets = tmpBuffer.getInt();
		packet_nr = tmpBuffer.getInt();
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
	
	public long getTimestamp() {
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
	
	public ByteBuffer getContent() {
		ByteBuffer content = ByteBuffer.allocate(CONTENTSIZE);
		for(byte[] it : data) {
			content.put(it);
		}
		return content;
	}
	
	public static long generateTimeStamp() {
		long timestamp = System.currentTimeMillis();
		return timestamp;
	}
	
	
	public void send(final DatagramSocket socket, final InetSocketAddress address, final int port) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				int i = 0;
				long timestamp = generateTimeStamp();
				ByteBuffer buffer = ByteBuffer.allocate(65507);
				buffer.mark();
				int packetSize = 0;
				for(byte[] chunk : data) {
					try {
						mutex.acquire();
						packetCounter++;
						buffer.putLong(packetCounter);
//						packetSize += 8; //size of packetCounter;
						mutex.release();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					buffer.putInt(id.getNumVal());
					buffer.putInt(clientId);
					buffer.putLong(timestamp);
					buffer.putInt(data.size());
					buffer.putInt(i);
					buffer.putInt(chunk.length);
//					packetSize += 28;
					buffer.put(chunk);
//					packetSize += chunk.length;
					packetSize = buffer.position();
					Log.d(Constants.LOG, "Sizeof packet:" + packetSize);
					byte[] content = new byte[packetSize];
					buffer.reset();
					buffer.get(content, 0, packetSize);
					DatagramPacket packet = new DatagramPacket(content, packetSize);
					Log.d(Constants.LOG, "Arrya lenght:" + packetSize);
					packet.setPort(port);
					packet.setAddress(address.getAddress());
					Log.d(Constants.LOG, address.getAddress().toString());
					try {
						socket.send(packet);
					} catch (IOException e) {
						Log.d(Constants.LOG, "Send error:" + e.getMessage());
						e.printStackTrace();
					} catch (RuntimeException e) {
						Log.d(Constants.LOG, "Send error:" + e.getMessage());
						e.printStackTrace();
					}
				}
				return null;
			}
			
		}.execute();
	}
	
	

}
