package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.Constants;
import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainFragment extends BaseFragment {
	
	private TextView statusText;
	private Button retryButton;
	private EditText addressInput;
	private SharedPreferences sharedPreferences;
	private ProgressBar progressBar;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        retryButton = (Button) rootView.findViewById(R.id.retryBtn);
        addressInput = (EditText) rootView.findViewById(R.id.addressInput);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        
        context = getActivity();
        
        
        sharedPreferences = context.getSharedPreferences(Constants.LOGIN_PREF, Context.MODE_PRIVATE);
        
        String ipAddress = sharedPreferences.getString(Constants.SERVER_IP, "");
        addressInput.setText(ipAddress);
        
        retryButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String address = addressInput.getText().toString();
				handleServices.authentificate(address);
				statusText.setText("Authentification in progress...");
				retryButton.setEnabled(false);
				progressBar.setVisibility(View.VISIBLE);
				
				Editor editor = sharedPreferences.edit();
				editor.putString(Constants.SERVER_IP, address);
				editor.commit();
			}
		});
        
        if(handleServices.isAuthenticated()) {
        	updateStatus("connected", false);
        }
        
        return rootView;
    }
	
	public void updateStatus(String status, boolean authBtn) {
		try {
		statusText.setText(status);
		retryButton.setEnabled(authBtn);
		progressBar.setVisibility(View.INVISIBLE);
		} catch(NullPointerException ex) {
			Log.d(Constants.LOG, "nullptr");
		}
	}
}
