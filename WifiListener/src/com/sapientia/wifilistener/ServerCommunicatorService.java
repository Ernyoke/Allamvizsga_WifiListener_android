package com.sapientia.wifilistener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.sapientia.wifilistener.Protocol.PROTOCOL_ID;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerChannel;
import com.sapientia.wifilistener.navigationdrawer.NavDrawerItem;

public class ServerCommunicatorService extends ForegroundServices {
	
	private IBinder mBinder = new LocalBinder();
	
	//communicaton ports
	public static int CLIENT_PORT = 10000;
	public static int SERVER_PORT = 40000;
	
	public static int LISTENER = 2;
	
	private boolean loggedIn = false;
	
	private int clientId = 0;
	private InetSocketAddress serverAddress;
	private DatagramSocket socket;
	
	private ServerCommunicatorWorker worker;
	private Thread thread;
	
	private LocalBroadcastManager notifyGui;
	private long loginTimeStamp = 0;
	private CountDownTimer loginCountDownTimer = null;
	
	private PowerManager powerManager;
	private WakeLock wakeLock;
	private WifiLock wifiLock;
	
	private ArrayList<NavDrawerItem> userCreated = new ArrayList<NavDrawerItem>();
	private ArrayList<NavDrawerItem> fromServer = new ArrayList<NavDrawerItem>();
	
	public class IncomingHandler extends Handler {
		
		public static final String CONTENT_KEY = "CONTENT";
		
		public static final int LOGIN_ACK = 1;
		public static final int LIST = 2;
		public static final int SYNCH = 3;
		public static final int SERVER_DOWN = 4;
		public static final int NEW_CHANNEL = 5;
		public static final int CLOSE_CHANNEL = 6;
		
		@Override
        public void handleMessage(Message msg) {
			Bundle content;
            switch (msg.what) {
            case LOGIN_ACK: {
            	content = msg.getData();
            	int id = content.getInt(CONTENT_KEY);
                authentificationResponse(id);
                break;
            }
            case LIST: {
                content = msg.getData();
                byte[] byteArray = content.getByteArray(CONTENT_KEY);
                newChannelList(byteArray);
                break;
            }
            case SYNCH: {
            	synchResponse();
            	break;
            }
            case SERVER_DOWN: {
            	serverDown();
            	break;
            }
            case NEW_CHANNEL: {
            	content = msg.getData();
                byte[] byteArray = content.getByteArray(CONTENT_KEY);
                newChannel(byteArray);
                break;
            }
            case CLOSE_CHANNEL: {
            	content = msg.getData();
                byte[] byteArray = content.getByteArray(CONTENT_KEY);
                deleteChannel(byteArray);
                break;
            }
            default:
                super.handleMessage(msg);
            }
        }
	}
	
	private Messenger messenger = new Messenger(new IncomingHandler());
	
	public class LocalBinder extends Binder {
		ServerCommunicatorService getService() {
			return ServerCommunicatorService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Constants.LOG, "ServiceCommunicator started");
		Log.d(Constants.LOG, "Worker started!");
		
//		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//		        Constants.WAKELOCK);
//		
//		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//			    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, Constants.WAKELOCK);
//		
//		if(!wakeLock.isHeld()) {
//			wakeLock.acquire();
//			Log.d("VOICE", "aquired");
//		}
//		if(!wifiLock.isHeld()) {
//			wifiLock.acquire();
//			Log.d("VOICE", "aquired");
//		}
		
		notifyGui = LocalBroadcastManager.getInstance(this);
		
		try {
			DatagramChannel channel = DatagramChannel.open();
		    socket = channel.socket();
		    socket.setReuseAddress(true);
//			socket = new DatagramSocket(null);
			
		} catch (SocketException e) {
			Log.d(Constants.LOG, "Socket error");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return START_STICKY;	
	}
	
	public boolean isLogedIn() {
		return loggedIn;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(Constants.LOG, "CommServ destroyed!");
		try {
		worker.stopRunning();
		}
		catch(NullPointerException ex) {
			
		}
	}
	
	public void addUsercreatedItem(NavDrawerItem item) {
		userCreated.add(item);
	}
	
	public ArrayList<NavDrawerItem> getFromUserList() {
		return userCreated;
	}
	
	public ArrayList<NavDrawerItem> getFromServerList() {
		return fromServer;
	}
	
	public void authentificate(String serverAddress) {
		
		if(!Ipv4AddressValidate(serverAddress)) {
			Intent intent = new Intent(Constants.SERV_BROADCAST);
			Toast.makeText(this, "Invalid IP address!", Toast.LENGTH_SHORT).show();
			intent.addCategory(Constants.SERV_AUTH);
			intent.putExtra(Constants.SERV_AUTH_SUCCESS, false);
			notifyGui.sendBroadcast(intent);
			return;
		}
		
		try {
		this.serverAddress = new InetSocketAddress(InetAddress.getByName(serverAddress), 10000);
		socket.bind(new InetSocketAddress(ServerCommunicatorService.SERVER_PORT));
		worker = new ServerCommunicatorWorker(messenger, socket);
		thread = new Thread(worker);
		thread.start();
		}
		catch (UnknownHostException e) {
			Log.d(Constants.LOG, "Host error");
			Toast.makeText(this, "Invalid IP address!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		} 
		catch (SocketException e) {
			Log.d(Constants.LOG, e.getMessage());
			e.printStackTrace();
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(100);
		String osName = "Android" + "\0";
		buffer.putInt(LISTENER);
		buffer.putInt(osName.length() * 2);
		try {
			buffer.put(osName.getBytes("UTF-16BE"));
		} catch (UnsupportedEncodingException e) {
			Log.d(Constants.LOG, "Ivalid charset!");
			e.printStackTrace();
		}
		Protocol protocol = new Protocol(PROTOCOL_ID.LOGIN, clientId, 0, buffer.array());
		protocol.send(socket, this.serverAddress, CLIENT_PORT);
		
		//wait for 5 seconds for response
		loginCountDownTimer = new CountDownTimer(5000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				//nothing to do here
			}

			@Override
			public void onFinish() {
				if(!loggedIn) {
					Intent intent = new Intent(Constants.SERV_BROADCAST);
					intent.addCategory(Constants.SERV_AUTH);
					intent.putExtra(Constants.SERV_AUTH_SUCCESS, false);
					notifyGui.sendBroadcast(intent);
				}
			}
			
		}.start();
	}
	
	public void requestChannelListFromServer() {
		Protocol protocol = new Protocol(PROTOCOL_ID.GET_LIST, clientId, 0, new byte[1]);
		protocol.send(socket, serverAddress, CLIENT_PORT);
	}

	public void authentificationResponse(int clientId) {
		Intent intent = new Intent(Constants.SERV_BROADCAST);
		//cancel the timer if the response arrived
		if(loginCountDownTimer != null) {
			loginCountDownTimer.cancel();
		}
		//check if clientId is valid
		if(clientId > 0) {
			loggedIn = true;
			this.clientId = clientId;
			//notify gui
			intent.addCategory(Constants.SERV_AUTH);
			intent.putExtra(Constants.SERV_AUTH_SUCCESS, true);
			notifyGui.sendBroadcast(intent);
			//responde with login ack
			Protocol protocol = new Protocol(PROTOCOL_ID.LOGIN_ACK, clientId, 0, new byte[1]);
			protocol.send(socket, serverAddress, CLIENT_PORT);
			
			//request channel list
			this.requestChannelListFromServer();
		}
		else {
//			intent.p
		}
	}

	public void newChannelList(byte[] array) {
		ByteBuffer content = ByteBuffer.wrap(array);
		int listSize = content.getInt();
		for(int i = 0; i < listSize; ++i) {
			int channelContentSize = content.getInt();
			byte[] channelContetBuffer = new byte[channelContentSize];
			content.get(channelContetBuffer, 0, channelContentSize);
			NavDrawerChannel channel = new NavDrawerChannel(channelContetBuffer);
			fromServer.add(channel);
		}
		//notify gui with the updates
		Intent intent = new Intent(Constants.SERV_BROADCAST);
		intent.addCategory(Constants.SERV_NEW_CHLIST);
		notifyGui.sendBroadcast(intent);
	}

	public void newChannel(byte[] response) {
		NavDrawerChannel channel = new NavDrawerChannel(response);
		fromServer.add(channel);
		//notify gui with the updates
		Intent intent = new Intent(Constants.SERV_BROADCAST);
		intent.addCategory(Constants.SERV_CHLIST_UPDATE);
		notifyGui.sendBroadcast(intent);
	}

	public void deleteChannel(byte[] response) {
		ByteBuffer content = ByteBuffer.wrap(response);
		int id = content.getInt();
		for(int i = 0; i < fromServer.size(); ++i) {
			NavDrawerChannel channel = (NavDrawerChannel)fromServer.get(i);
			if(channel.getOwnerId() == id) {
				fromServer.remove(i);
				break;
			}
		}
		//notify gui with the updates
		Intent intent = new Intent(Constants.SERV_BROADCAST);
		intent.addCategory(Constants.SERV_CHLIST_UPDATE);
		notifyGui.sendBroadcast(intent);
	}

	public void serverDown() {
		//
		loggedIn = false;
		//notify gui
		Intent intent = new Intent(Constants.SERV_BROADCAST);
		intent.addCategory(Constants.SERV_DOWN);
		notifyGui.sendBroadcast(intent);
	}

	public void synchResponse() {
		if(isLogedIn()) {
			Log.d(Constants.LOG, "synch");
			String buff = "SYNCH";
			Protocol protocol = new Protocol(PROTOCOL_ID.SYNCH_RESP, clientId, 0, buff.getBytes());
			protocol.send(socket, serverAddress, CLIENT_PORT);
		}
	}
	
	public void disconnectFromServer() {
		
		if(loggedIn) {
			
//			wakeLock.release();
//			wifiLock.release();
			
			Protocol protocol = new Protocol(PROTOCOL_ID.LOGOUT, clientId, 0, new byte[1]);
			protocol.send(socket, serverAddress, CLIENT_PORT);
		}
		loggedIn = false;
	}
	
	public boolean Ipv4AddressValidate(String address) {
		String IPADDRESS_PATTERN = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(address);
		return matcher.matches();
	}

}
