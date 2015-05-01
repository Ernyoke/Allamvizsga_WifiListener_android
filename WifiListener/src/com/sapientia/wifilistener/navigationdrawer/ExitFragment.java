package com.sapientia.wifilistener.navigationdrawer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sapientia.wifilistener.R;


public class ExitFragment extends BaseFragment {
	
	private Button exitButton;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.exit_fragment, container, false); 
        
        exitButton = (Button) rootView.findViewById(R.id.exitButton);
        
        exitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//close running services and disconnect from the server
				handleServices.distroy();
				
				//exti the application
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
        
        return rootView;
	}
	
	
}
