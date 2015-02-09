package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ChannelFragment extends Fragment{
	
	private Button startBtn;
	private Button stopBtn;
	private Button pauseBtn;
	
	private TextView language;
	private TextView port;
	private TextView codec;
	private TextView frequency;
	private TextView status;
	private TextView speed;
	
	private NavDrawerChannel channel;
	
	private HandleServices handleServices;
	
	private View.OnClickListener clickListener;
	
	public ChannelFragment() {
	}
	
	public void setChannel(NavDrawerChannel channel) {
		this.channel = channel;
	}
	
	public void setServiceHandler(HandleServices handleServices) {
		this.handleServices = handleServices;
	}
	
	private void setClickListeners() {
		clickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()) {
				case R.id.startBtn : {
					handleServices.startPlayer(channel);
					break;
				}
				case R.id.stopBtn : {
					handleServices.stopPlayer(channel);
					break;
				}
				case R.id.pauseBtn : {
					handleServices.pausePlayer(channel);
					break;
				}
				}
			}
		};
		startBtn.setOnClickListener(clickListener);
		stopBtn.setOnClickListener(clickListener);
		pauseBtn.setOnClickListener(clickListener);
	}
	
	public void asd() {
		//
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.channel_fragment, container, false);
        
        startBtn = (Button) rootView.findViewById(R.id.startBtn);
        stopBtn = (Button) rootView.findViewById(R.id.stopBtn);
        pauseBtn = (Button) rootView.findViewById(R.id.pauseBtn);
        
        language = (TextView) rootView.findViewById(R.id.lang);
        port = (TextView) rootView.findViewById(R.id.port);
        codec = (TextView) rootView.findViewById(R.id.codec);
        frequency = (TextView) rootView.findViewById(R.id.frequency);
        status = (TextView) rootView.findViewById(R.id.status);
        speed = (TextView) rootView.findViewById(R.id.speed);
        
        language.setText(channel.getTitle());
        port.setText(channel.getPort() + "");
        codec.setText(channel.getCodec());
        frequency.setText(channel.getFrequency() + "");
//        status.setText(channel.getState());
        //speed
         
        return rootView;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		this.setClickListeners();
	}

}
