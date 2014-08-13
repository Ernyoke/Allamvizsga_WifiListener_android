package com.example.wifilistenner;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	private Button startButton;
	private Button stopButton;
	private Button pauseButton;
	private EditText portInput;
	private TextView trafficTextView;
	private TextView timer;
	private TextView stateDisplay;
	
	private int traffic;
	
	private ClickListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		pauseButton = (Button) findViewById(R.id.pauseButton);
		
		portInput = (EditText) findViewById(R.id.portInput);
		
		trafficTextView = (TextView) findViewById(R.id.traffic);
		
		stateDisplay = (TextView) findViewById(R.id.state);
		
		
		LocalBroadcastReceiver localRec = new LocalBroadcastReceiver(trafficTextView);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(
	           localRec, new IntentFilter(Constants.BROADCAST));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		listener = new ClickListener(this.getApplicationContext(), portInput, stateDisplay);
//		listener.resumeView();
		
		startButton.setOnClickListener(listener);
		stopButton.setOnClickListener(listener);
		pauseButton.setOnClickListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		listener.distroy();
	}
	
}
