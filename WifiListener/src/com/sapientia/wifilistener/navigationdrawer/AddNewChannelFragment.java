package com.sapientia.wifilistener.navigationdrawer;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sapientia.wifilistener.Constants;
import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.R;

public class AddNewChannelFragment extends Fragment {
	
	private DrawerListAdaper adapter;
	private Button doneBtn;
	private EditText portInput;
	private EditText langInput;
	private Spinner freqSpinner;
	private Spinner codecSpinner;
	
	private HandleServices handleServices;
	
	public AddNewChannelFragment(DrawerListAdaper adapter, HandleServices handleServices) {
		this.adapter = adapter;
		this.handleServices = handleServices;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.add_new_channel, container, false);
        
        portInput = (EditText) rootView.findViewById(R.id.portInput);
        langInput = (EditText) rootView.findViewById(R.id.langInput);
        freqSpinner = (Spinner) rootView.findViewById(R.id.freqSpinner);
        codecSpinner = (Spinner) rootView.findViewById(R.id.codecSpinner);
        doneBtn = (Button) rootView.findViewById(R.id.doneBtn);
        
        String[] audioFormats = rootView.getResources().getStringArray(R.array.audioFormats);
        String[] audioFormatValues = rootView.getResources().getStringArray(R.array.audioFormatValues);
        String[] sampleRates = rootView.getResources().getStringArray(R.array.sampleRates);
        int[] sampleRateValues = rootView.getResources().getIntArray(R.array.sampleRateValues);
        
        ArrayList<SpinnerCodecItem> spinnerCodecItems = new ArrayList<SpinnerCodecItem>();
        
        for(int i = 0; i < audioFormats.length; ++i) {
        	spinnerCodecItems.add(new SpinnerCodecItem(audioFormats[i], audioFormatValues[i]));
        }
        
        ArrayAdapter<SpinnerCodecItem> adapterCodec = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item, spinnerCodecItems);
        codecSpinner.setAdapter(adapterCodec);
        
        ArrayList<SpinnerFreqItem> spinnerFreqItems = new ArrayList<SpinnerFreqItem>();
        
        for(int i = 0; i < sampleRates.length; ++i) {
        	spinnerFreqItems.add(new SpinnerFreqItem(sampleRates[i], sampleRateValues[i]));
        }
        
        ArrayAdapter<SpinnerFreqItem> adapterFreq = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item, spinnerFreqItems);
        freqSpinner.setAdapter(adapterFreq);
        
        doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
				int port = Integer.parseInt(portInput.getText().toString());
				String language = langInput.getText().toString();
				SpinnerFreqItem freqItem = (SpinnerFreqItem) freqSpinner.getSelectedItem();
				int freq = freqItem.getFreq();
				SpinnerCodecItem codecItem = (SpinnerCodecItem) codecSpinner.getSelectedItem();
				String codec = codecItem.getCodec();
				NavDrawerChannel channel = new NavDrawerChannel(language, port, freq, codec, handleServices);
				adapter.addChannel(channel);
				handleServices.addNewUsercreatedChannel(channel);
				}
				catch(NumberFormatException ex) {
					//Toast.makeText(, text, duration)
					Log.d(Constants.LOG, ex.getMessage());
				}
			}
		});
         
        return rootView;
    }
}

//public class MainActivity extends ActionBarActivity {
//	
//	private Button startButton;
//	private Button stopButton;
//	private Button pauseButton;
//	private EditText portInput;
//	private TextView trafficTextView;
//	private TextView stateDisplay;
//	
//	private ClickListener listener;
//	
//	private static final int RESULT_SETTINGS = 1;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		
//		startButton = (Button) findViewById(R.id.startButton);
//		stopButton = (Button) findViewById(R.id.stopButton);
//		pauseButton = (Button) findViewById(R.id.pauseButton);
//		
//		portInput = (EditText) findViewById(R.id.portInput);
//		
//		trafficTextView = (TextView) findViewById(R.id.traffic);
//		
//		stateDisplay = (TextView) findViewById(R.id.state);
//		
//		
//		LocalBroadcastReceiver localRec = new LocalBroadcastReceiver(trafficTextView);
//		
//		LocalBroadcastManager.getInstance(this).registerReceiver(
//	           localRec, new IntentFilter(Constants.BROADCAST));
//		
//		//initialize settings and set default values at first start
//		SharedPreferences settingsPref = this.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE);
//		Editor editSettings = settingsPref.edit();
//		if(!settingsPref.contains(Constants.SETTINGS_SAMPLERATE)) {
//			editSettings.putInt(Constants.SETTINGS_SAMPLERATE, 8000);
//			editSettings.commit();
//		}
//		if(!settingsPref.contains(Constants.SETTINGS_AUDIOFORMAT)) {
//			editSettings.putString(Constants.SETTINGS_AUDIOFORMAT, "ENCODING_PCM_16BIT");
//			editSettings.commit();
//		}
//	}
//	
//	@Override
//	public void onResume() {
//		super.onResume();
//		listener = new ClickListener(this.getApplicationContext(), portInput, stateDisplay);
//		
//		startButton.setOnClickListener(listener);
//		stopButton.setOnClickListener(listener);
//		pauseButton.setOnClickListener(listener);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			Intent i = new Intent(this,  SettingsActivity.class);
//            startActivityForResult(i, RESULT_SETTINGS);
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//	
//	
//	@Override
//	public void onPause() {
//		super.onStop();
//		listener.distroy();
//	}
