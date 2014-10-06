package com.sapientia.wifilistener;

import com.sapientia.wifilistener.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;


public class SettingsActivity extends PreferenceActivity {
	
	private ListPreference prefSampleRate;
	private ListPreference prefAudioFormat;
	private SharedPreferences settingsPref;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	 
	        addPreferencesFromResource(R.xml.settings);
	        
	        settingsPref = this.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE);
	        
	        //set default settings
	        prefSampleRate = (ListPreference) findPreference("sampleRate");
	        prefSampleRate.setValue(settingsPref.getInt(Constants.SETTINGS_SAMPLERATE, 8000) + "");
	        prefAudioFormat = (ListPreference) findPreference("audioFormat");
	        String format = settingsPref.getString(Constants.SETTINGS_AUDIOFORMAT, "ENCODING_PCM_16BIT");
	        Log.d(Constants.LOG, format + "asd");
	        prefAudioFormat.setValue(format);
	    }
	 
	 @Override
	 	public void onPause() {
			 super.onPause();
			 //save changes on exit
			 Editor editSettings = settingsPref.edit();
			 editSettings.putInt(Constants.SETTINGS_SAMPLERATE, Integer.parseInt(prefSampleRate.getValue()));
			 editSettings.putString(Constants.SETTINGS_AUDIOFORMAT, prefAudioFormat.getValue());
			 editSettings.commit();
			 
	 }
}
