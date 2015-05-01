package com.sapientia.wifilistener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.sapientia.wifilistener.Protocol.PROTOCOL_ID;
import com.sapientia.wifilistener.ServerCommunicatorService.IncomingHandler;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

public class ServerCommunicatorWorker implements Runnable{
	
//	private List<NavDrawerItem> fromServer = Collections.synchronizedList(new ArrayList<NavDrawerItem>());
//	private List<NavDrawerItem> fromUser = Collections.synchronizedList(new ArrayList<NavDrawerItem>());
	private boolean finish = false;
	
	private Messenger messenger;
	private DatagramSocket rSocket;
	
	private ArrayList<Protocol> channelListChunks = new ArrayList<Protocol>();
	private long channelListTimeStamp = 0;
	
	private final int SERVER_ID = 0;
	
	public ServerCommunicatorWorker(Messenger receiver, DatagramSocket socket) {
		messenger = receiver;
		this.rSocket = socket;
	}

	@Override
	public void run() {
		byte[] recBuffer = new byte[64 * 1024];
//		DatagramSocket rSocket = null;
		DatagramPacket rPacket = new DatagramPacket(recBuffer, 64 * 1024);
//		try {
//			Log.d(Constants.LOG, "Create new socket");
//			DatagramChannel dgramCh = DatagramChannel.open();
//			rSocket = dgramCh.socket();
//			rSocket.setReuseAddress(true);
//			rSocket.bind(new InetSocketAddress(ServerCommunicatorService.SERVER_PORT));
//		} catch (SocketException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Log.d(Constants.LOG, e1.getMessage());
//		} catch (IOException e) {
//			Log.d(Constants.LOG, "Package receive error: " + e.getMessage());
//			e.printStackTrace();
//		}
		
		while(!finish) {
			try {
				rSocket.receive(rPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(rPacket.getLength() > 0) {
				Protocol dgram = new Protocol(rPacket.getData());
				switch(dgram.getId()) {
				case LOGIN_ACK : {
					//security check if the datagram really comes from the server
					if(dgram.getClientId() == 0) {
						//get my own clientId
						byte[] content = dgram.getData();
						ByteBuffer tmpBuffer = ByteBuffer.wrap(content);
						int clientId = tmpBuffer.getInt();
						//send back my clientId to the Service
//						callbacks.authentificationResponse(clientId);
						Bundle data = new Bundle();
						data.putInt(IncomingHandler.CONTENT_KEY, clientId);
						sendMessage(IncomingHandler.LOGIN_ACK, data);
					}
					break;
				}
				case LIST : {
					handleChannelListChunks(dgram);
					break;
				}
				case NEW_CHANNEL: {
					handleNewChannel(dgram);
					break;
				}
				case REMOVE_CHANNEL: {
					handleCloseChannel(dgram);
					break;
				}
				case SYNCH: {
					handleSynchronization(dgram);
					break;
				}
				case SERVER_DOWN: {
					handleServerDown(dgram);
					break;
				}
				default: {
					//throw datagram and break
				}
				}
			}
		}
		rSocket.close();
	}
	
	private void handleChannelListChunks(Protocol chunk) {
		if(chunk.getClientId() == SERVER_ID) {
			if(chunk.getTimestamp() != channelListTimeStamp) {
				//clear the list for new datas
				channelListChunks.clear();
				channelListTimeStamp = chunk.getTimestamp();
			}
			channelListChunks.add(chunk);
			//check if all packets did arrive
			if(chunk.getPackets() == channelListChunks.size()) {
				//sort chunks in order to deserialize them
				Collections.sort(channelListChunks, new Comparator<Protocol>() {
	
					@Override
					public int compare(Protocol lhs, Protocol rhs) {
						if(lhs.getPacketNr() < rhs.getPacketNr()) {
							return -1;
						}
						return 1;
					}
					
				});
				
				ByteBuffer content = ByteBuffer.allocate(65000);
				for(Protocol it : channelListChunks) {
					content.put(it.getContent().array());
				}
				Bundle data = new Bundle();
				data.putByteArray(IncomingHandler.CONTENT_KEY, content.array());
				sendMessage(IncomingHandler.LIST, data);
			}
		}
	}
	
	private void handleSynchronization(Protocol dgram) {
		if(dgram.getClientId() == SERVER_ID) {
//			callbacks.synchResponse();
			sendMessage(IncomingHandler.SYNCH);
		}
	}
	
	private void handleNewChannel(Protocol dgram) {
		if(dgram.getClientId() == SERVER_ID) {
//			callbacks.newChannel(dgram.getContent().array());
			Bundle data = new Bundle();
			data.putByteArray(IncomingHandler.CONTENT_KEY, dgram.getContent().array());
			sendMessage(IncomingHandler.NEW_CHANNEL, data);
		}
		
	}
	
	private void handleCloseChannel(Protocol dgram) {
		if(dgram.getClientId() == SERVER_ID) {
//			callbacks.deleteChannel(dgram.getContent().array());
			Bundle data = new Bundle();
			data.putByteArray(IncomingHandler.CONTENT_KEY, dgram.getContent().array());
			sendMessage(IncomingHandler.CLOSE_CHANNEL, data);
		}
	}
	
	private void handleServerDown(Protocol dgram) {
		if(dgram.getClientId() == SERVER_ID) {
//			callbacks.serverDown();
			sendMessage(IncomingHandler.SERVER_DOWN);
		}
	}
	
	
	public void stopRunning() {
		this.finish = true;
		Log.d(Constants.LOG, "Worker finished!");
	}
	
	private void sendMessage(int what, Bundle bundle) {
		Message message = Message.obtain();
		message.what = what;
		message.setData(bundle);
		try {
			messenger.send(message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendMessage(int what) {
		Message message = Message.obtain();
		message.what = what;
		try {
			messenger.send(message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
