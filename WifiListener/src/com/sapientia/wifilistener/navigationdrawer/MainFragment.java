package com.sapientia.wifilistener.navigationdrawer;

import com.sapientia.wifilistener.HandleServices;
import com.sapientia.wifilistener.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainFragment extends Fragment {
	
	private TextView statusText;
	private Button retryButton;
	private EditText addressInput;
	
	private HandleServices handleService;
	
	public void setServiceHandler(HandleServices handleServices) {
		this.handleService = handleServices;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        retryButton = (Button) rootView.findViewById(R.id.retryBtn);
        addressInput = (EditText) rootView.findViewById(R.id.addressInput);
        
        retryButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String address = addressInput.getText().toString();
				handleService.authentificate(address, MainFragment.this);
				statusText.setText("Authentification in progress...");
				retryButton.setEnabled(false);
			}
		});
        
        return rootView;
    }
	
	public void updateStatus(boolean loginSuccess) {
		if(loginSuccess) {
			statusText.setText("Authentification succes!");
		}
		else {
			statusText.setText("Authentification failed! Would you like to retry it?");
			retryButton.setEnabled(true);
		}
	}
}
