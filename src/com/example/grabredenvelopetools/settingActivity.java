package com.example.grabredenvelopetools;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Toast;

public class settingActivity extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preference);
		setOnclicListener();
	}
	
	/**
	 * 为各个选项处理点击事件
	 */
	private void setOnclicListener(){
		Preference accessibilitySetting=findPreference("accessibility_setting");
		Preference teach=findPreference("teach");
		Preference aboutAuthor=findPreference("about_author");
		
		accessibilitySetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				Intent it=new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
	    		startActivity(it);
				return false;
			}
		});

		
		teach.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
//				Toast.makeText(settingActivity.this, "暂无更新！", Toast.LENGTH_SHORT).show();
				Intent it=new Intent();
				it.setAction("com.example.grabredenvelopetools.TeachActivity");
				startActivity(it);
				return false;
			}
		});

		
		aboutAuthor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		});
				
	}

	public void Back(View v){
		super.onBackPressed();
	}
}
