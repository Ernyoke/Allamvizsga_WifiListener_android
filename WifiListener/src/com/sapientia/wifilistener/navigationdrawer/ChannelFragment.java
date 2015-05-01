package com.sapientia.wifilistener.navigationdrawer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sapientia.wifilistener.R;

public class ChannelFragment extends BaseFragment{
	
	private ImageButton startBtn;
	private ImageButton stopBtn;
	private ImageButton pauseBtn;
	
	private TextView bigTitle;
	
	private TextView language;
	private TextView port;
	private TextView codec;
	private TextView sampleRate;
	private TextView sampleSize;
	private TextView channels;
	private TextView status;
	
	private NavDrawerChannel channel;
	
	private View.OnClickListener clickListener;
	
	public ChannelFragment() {
	}
	
	public void setChannel(NavDrawerChannel channel) {
		this.channel = channel;
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
	
	public void updateButtonStatus() {
      switch(channel.getState()) {
      case STOPPED: {
      	stopBtn.setEnabled(false);
      	startBtn.setEnabled(true);
      	pauseBtn.setEnabled(false);
      	break;
      }
      
      case PLAYING: {
      	stopBtn.setEnabled(true);
      	startBtn.setEnabled(false);
      	pauseBtn.setEnabled(true);
      	break;
      }
      
      case PAUSED: {
      	startBtn.setEnabled(false);
      	stopBtn.setEnabled(true);
      	pauseBtn.setEnabled(true);
      }
      }
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.channel_fragment, container, false);
        
        startBtn = (ImageButton) rootView.findViewById(R.id.startBtn);
        stopBtn = (ImageButton) rootView.findViewById(R.id.stopBtn);
        pauseBtn = (ImageButton) rootView.findViewById(R.id.pauseBtn);
        
        bigTitle = (TextView) rootView.findViewById(R.id.bigTitle);
        
        language = (TextView) rootView.findViewById(R.id.lang);
        port = (TextView) rootView.findViewById(R.id.port);
        codec = (TextView) rootView.findViewById(R.id.codec);
        sampleRate = (TextView) rootView.findViewById(R.id.sampleRate);
        sampleSize = (TextView) rootView.findViewById(R.id.sampleSize);
        channels = (TextView) rootView.findViewById(R.id.channels);
        
        status = (TextView) rootView.findViewById(R.id.status);
        
        bigTitle.setText(channel.getTitle());
        
        language.setText(channel.getTitle());
        port.setText(channel.getPort() + "");
        codec.setText(channel.getCodec());
        sampleRate.setText(channel.getSampleRate() + "");
        sampleSize.setText(channel.getSampleSize() + "");
        channels.setText(channel.getChannels() + "");
        
        status.setText(channel.getState_str());
        
//        //speed
        this.updateButtonStatus();
         
        return rootView;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		this.setClickListeners();
	}

}
