package com.example.grabredenvelopetools;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TeachActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teach_layout);
	}
	
	public void back(View v){
		super.onBackPressed();
	}

}
