package com.sapientia.wifilistener.navigationdrawer;

import java.util.ArrayList;

import android.os.Bundle;
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

public class AddNewChannelFragment extends BaseFragment {
	
	private DrawerListAdaper adapter;
	private Button doneBtn;
	private EditText portInput;
	private EditText langInput;
	private Spinner freqSpinner;
	private Spinner codecSpinner;
	private Spinner sampleSizeSpinner;
	private Spinner channelNumberSpinner;
	
	public void setAdapter(DrawerListAdaper adapter) {
		this.adapter = adapter;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.add_new_channel, container, false);
        
        portInput = (EditText) rootView.findViewById(R.id.portInput);
        langInput = (EditText) rootView.findViewById(R.id.langInput);
        freqSpinner = (Spinner) rootView.findViewById(R.id.freqSpinner);
        codecSpinner = (Spinner) rootView.findViewById(R.id.codecSpinner);
        sampleSizeSpinner = (Spinner) rootView.findViewById(R.id.sampSizeSpinner);
        channelNumberSpinner = (Spinner) rootView.findViewById(R.id.channSpinner);
        doneBtn = (Button) rootView.findViewById(R.id.doneBtn);
        
        String[] audioFormats = rootView.getResources().getStringArray(R.array.audioFormats);
        String[] audioFormatValues = rootView.getResources().getStringArray(R.array.audioFormatValues);
        
        String[] sampleRates = rootView.getResources().getStringArray(R.array.sampleRates);
        int[] sampleRateValues = rootView.getResources().getIntArray(R.array.sampleRateValues);
        
        String[] sampleSizes = rootView.getResources().getStringArray(R.array.sampleSize);
        int[] sampleSizeValues = rootView.getResources().getIntArray(R.array.sampleSizeValues);
        
        String[] channelNumbers = rootView.getResources().getStringArray(R.array.channel);
        int[] channelNumberValues = rootView.getResources().getIntArray(R.array.channelValues);
        
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
        
        ArrayList<SpinnerSampleSizeItem> spinnerSampleSizeItems = new ArrayList<SpinnerSampleSizeItem>();
        
        for(int i = 0; i < sampleSizes.length; ++i) {
        	spinnerSampleSizeItems.add(new SpinnerSampleSizeItem(sampleSizes[i], sampleSizeValues[i]));
        }
        
        ArrayAdapter<SpinnerSampleSizeItem> adapterSampleSize = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item, spinnerSampleSizeItems);
        sampleSizeSpinner.setAdapter(adapterSampleSize);
        
        ArrayList<SpinnerChannelNumberItem> spinnerChannelNumberItem = new ArrayList<SpinnerChannelNumberItem>();
        
        for(int i = 0; i < channelNumbers.length; ++i) {
        	spinnerChannelNumberItem.add(new SpinnerChannelNumberItem(channelNumbers[i], channelNumberValues[i]));
        }
        
        ArrayAdapter<SpinnerChannelNumberItem> adapterChannelNumber = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item, spinnerChannelNumberItem);
        channelNumberSpinner.setAdapter(adapterChannelNumber);
        
        context = this.getActivity();
        
        doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
				int port = Integer.parseInt(portInput.getText().toString());
				String language = langInput.getText().toString();
				SpinnerFreqItem freqItem = (SpinnerFreqItem) freqSpinner.getSelectedItem();
				int freq = freqItem.getFreq();
				SpinnerSampleSizeItem sampleSizeItem = (SpinnerSampleSizeItem) sampleSizeSpinner.getSelectedItem();
				int sampleSize = sampleSizeItem.getSampleSize();
				SpinnerChannelNumberItem channelNumberItem = (SpinnerChannelNumberItem) channelNumberSpinner.getSelectedItem();
				int channelNumber = channelNumberItem.getChannelNumber();
				SpinnerCodecItem codecItem = (SpinnerCodecItem) codecSpinner.getSelectedItem();
				String codec = codecItem.getCodec();
				NavDrawerChannel channel = new NavDrawerChannel(language, port, freq, sampleSize, channelNumber, codec);
				handleServices.addNewUsercreatedChannel(channel);
				adapter.notifyUpdate();
				
				Toast.makeText(context, "New channel added", Toast.LENGTH_SHORT).show();
				}
				catch(NumberFormatException ex) {
					Toast.makeText(context, "Invalid input!", Toast.LENGTH_SHORT).show();
					Log.d(Constants.LOG, ex.getMessage());
				}
			}
		});
         
        return rootView;
        
    }
}
