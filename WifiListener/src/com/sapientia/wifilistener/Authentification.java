package com.sapientia.wifilistener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.sapientia.wifilistener.Protocol.PROTOCOL_ID;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;


public class Authentification extends AsyncTask<Void, Void, Integer>{

	private InetAddress serverAddress;
	private int port = 10000;
	private DatagramSocket socket;
	private int clientId = 0;
	
	private int SERVER_PORT = 40000;
	
	private AuthentificationResponse response;
	
	public Authentification(AuthentificationResponse response) {
		this.response = response;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		try {
			serverAddress = InetAddress.getByName("192.168.0.107");
			socket = new DatagramSocket();
		} catch (UnknownHostException e) {
			Log.d(Constants.LOG, "Host error");
			e.printStackTrace();
		} catch (SocketException e) {
			Log.d(Constants.LOG, "Socket error");
			e.printStackTrace();
		}
		
		//create login message
		ByteBuffer buffer = ByteBuffer.allocate(100);
		String osName = "Android" + "\0";
		buffer.putInt(1);
		buffer.putInt(osName.length() * 2);
		try {
			buffer.put(osName.getBytes("UTF-16BE"));
		} catch (UnsupportedEncodingException e) {
			Log.d(Constants.LOG, "Ivalid charset!");
			e.printStackTrace();
		}
		Protocol protocol = new Protocol(PROTOCOL_ID.LOGIN, clientId, 0, buffer.array());
		protocol.send(socket, serverAddress, port);
		//wait for server response
		byte[] recBuffer = new byte[64 * 1024];
		DatagramSocket rSocket = null;
		try {
			rSocket = new DatagramSocket(SERVER_PORT);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    DatagramPacket rPacket = new DatagramPacket(recBuffer, 64 * 1024);
//	    rPacket.setPort(SERVER_PORT);
//	    rPacket.setAddress(serverAddress);
		try {
			//wait for 5 seconds
			rSocket.setSoTimeout(5000);
			rSocket.receive(rPacket);
		} catch (SocketException e) {
			e.printStackTrace();
			Log.d(Constants.LOG, e.getMessage());
		} catch (IOException e) {
			Log.d(Constants.LOG, "Package receive error: " + e.getMessage());
			e.printStackTrace();
		}
		
		if(rPacket.getLength() > 0) {
			Protocol dgram = new Protocol(rPacket.getData());
			if(dgram.getId() == PROTOCOL_ID.LOGIN_ACK && dgram.getClientId() == 0) {
				byte[] content = dgram.getData();
				ByteBuffer tmpBuffer = ByteBuffer.wrap(content);
				clientId = tmpBuffer.getInt();
				byte[] contentBuff = new byte[1];
				Protocol responseACK = new Protocol(PROTOCOL_ID.LOGIN_ACK, clientId, 0, contentBuff);
				responseACK.send(socket, serverAddress, port);
				return clientId;
			}
		}
		rSocket.close();
		return 0;
	}
	
	@Override 
	protected void onPostExecute(Integer result) {
		response.processResult(result);
	}
	

}
