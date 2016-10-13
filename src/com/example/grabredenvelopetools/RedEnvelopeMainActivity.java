package com.example.grabredenvelopetools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RedEnvelopeMainActivity extends Activity {
	
	private ListView recordList;
	private RecordAdapter recordAdapter;
	private List<RedEnvelopInfo> redEnvelopRecord;
	private DBManager recordDB;
	private UpdataRecordBroadcast recordStateReceiver;
	private TextView RedEnvelopCount;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_envelope_main);
        
        if(isFirstRuning()){
        	firstRuningInit();
        }
        recordDB=new DBManager(this);
        recordStateReceiver=new UpdataRecordBroadcast();
        RedEnvelopCount=(TextView) findViewById(R.id.sum);
        IntentFilter itf=new IntentFilter();
        itf.addAction("com.example.grabredenvelopetools.RedEnvelopeMainActivity.UpdataRecordBroadcast");
        registerReceiver(recordStateReceiver, itf);
        initRecord();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	unregisterReceiver(recordStateReceiver);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	TextView btn=(TextView) findViewById(R.id.GrabRedEnvelopState_btn);
    	TextView tv=(TextView) findViewById(R.id.runing_state_tv);
    	if(GrabRedEnvelopeService.getGrabRedEnvelop()){
//    		btn.setText(this.getResources().getString(R.string.close_service));//*/*/***********
    		btn.setBackground(this.getResources().getDrawable(R.drawable.close_service));
    		tv.setText(this.getResources().getString(R.string.is_run));
    	}else{
//    		btn.setText(this.getResources().getString(R.string.open_service));//**************
    		btn.setBackground(this.getResources().getDrawable(R.drawable.open_service));
    		tv.setText(this.getResources().getString(R.string.is_close));
    	}
    }
    
    
    
    /************************************************************************************************
     ************************************* 按钮事件处理函数 ***********************************************
     ************************************************************************************************/
    
    /**
     * 初始化红包记录
     */
    private void initRecord(){
    	recordList=(ListView)findViewById(R.id.redEnvelopRecord_tv);
    	redEnvelopRecord=new ArrayList<RedEnvelopInfo>();
    	recordAdapter=new RecordAdapter(this, R.layout.record_item_layout, redEnvelopRecord);
    	recordList.setAdapter(recordAdapter);
    	updataRecord();
    	recordList.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(scrollState==OnScrollListener.SCROLL_STATE_IDLE){
					if(recordList.getLastVisiblePosition()==recordList.getCount()-1){
						updataRecord();
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    private void updataRecord(){
    	Cursor cursor=recordDB.queryTheCursor();
    	List<RedEnvelopInfo> tempList=new ArrayList<RedEnvelopInfo>();
    	RedEnvelopInfo tempRecord;
    	int c=cursor.getCount();
    	if(c>0&&cursor.moveToPosition(c-redEnvelopRecord.size()-1)){
        	for(int i=0;i<15;i++){
        		tempRecord=new RedEnvelopInfo();
        		tempRecord.setFlag(cursor.getInt(cursor.getColumnIndex("flag")));
        		tempRecord.setNum(cursor.getDouble(cursor.getColumnIndex("num")));
        		tempRecord.setSender(cursor.getString(cursor.getColumnIndex("sender")));
        		tempRecord.setTime(cursor.getString(cursor.getColumnIndex("date")));
        		tempList.add(tempRecord);
        		if(!cursor.moveToPrevious()){
        			break;
        		}
        	}
        	redEnvelopRecord.addAll(tempList);
        	recordAdapter.notifyDataSetChanged();
        }
    	RedEnvelopCount.setText("共"+c+"个");
    }
    
    /**
     * 自动抢红包服务开关按钮事件处理函数
     */
    public void serviceButtonOnClick(View v){
    	
    	if (GrabRedEnvelopeService.getRuningState()==false) {
    		
    		AlertDialog.Builder ad=new AlertDialog.Builder(this)
    						.setTitle("提示")
    						.setMessage("要使用自动抢红包服务，请先设置开启辅助功能（部分手机是设置开启无障碍）。")
    						.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Intent it=new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
						    		startActivity(it);
						    		Toast.makeText(RedEnvelopeMainActivity.this, 
						    				RedEnvelopeMainActivity.this.getResources().getString(R.string.open_service_activity_tip), 
						    				Toast.LENGTH_SHORT).show();
								}
							});
    		ad.create();
    		ad.show();
			return;
		}
    	
    	TextView btn=(TextView) v;
    	TextView tv=(TextView) findViewById(R.id.runing_state_tv);
    	CharSequence tvText=tv.getText();
    	String openText=this.getResources().getString(R.string.is_close);
    	String closeText=this.getResources().getString(R.string.is_run);
    	
    	if(openText.equals(tvText)){
    		GrabRedEnvelopeService.setGrabRedEnvelop(true);
//    		btn.setText(closeText);
    		btn.setBackground(this.getResources().getDrawable(R.drawable.close_service));
    		tv.setText(this.getResources().getString(R.string.is_run));
//    		Toast.makeText(this, this.getResources().getString(R.string.is_run), Toast.LENGTH_SHORT).show();
    	}else if(closeText.equals(tvText)){
    		GrabRedEnvelopeService.setGrabRedEnvelop(false);
//    		btn.setText(openText);
    		btn.setBackground(this.getResources().getDrawable(R.drawable.open_service));
    		tv.setText(this.getResources().getString(R.string.is_close));
//    		Toast.makeText(this, this.getResources().getString(R.string.is_close), Toast.LENGTH_SHORT).show();
    	}
    }

    /**
     * 记录按钮事件处理
     * @param v
     */
    public void recordButton(View v){
    	Toast.makeText(this, "详细记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置按钮事件处理
     * @param v
     */
    public void settingButton(View v){
    	
    	if (GrabRedEnvelopeService.getRuningState()==false) {
    		
    		AlertDialog.Builder ad=new AlertDialog.Builder(this)
    						.setTitle("提示")
    						.setMessage("要使用自动抢红包服务，请先设置开启辅助功能（部分手机是设置开启无障碍）。")
    						.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Intent it=new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
						    		startActivity(it);
						    		Toast.makeText(RedEnvelopeMainActivity.this, 
						    				RedEnvelopeMainActivity.this.getResources().getString(R.string.open_service_activity_tip), 
						    				Toast.LENGTH_SHORT).show();
								}
							});
    		ad.create();
    		ad.show();
			return;
		}
    	
    	Intent it=new Intent();
    	it.setAction("com.example.grabredenvelopetools.settingActivity");
    	startActivity(it);
    }
    
    /************************************************************************************************
     ***************************************** END **************************************************
     ************************************************************************************************/
    
    

    /************************************************************************************************
     *************************************** 初始化处理 *************************************************
     ************************************************************************************************/
    
    
    /**
     * 判断是否安装后首次运行程序
     */
    private boolean isFirstRuning(){
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
    	//查找是否首次运行，是则返回true，并且记录程序不是第一次运行，否则返回false
    	if(sp.getBoolean("firstRuning", true)==true){
    		
    		Editor editor=sp.edit();
    		editor.putBoolean("firstRuning", false);
    		editor.commit();
    		
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    /**
     * 安装后首次运行应用的初始化设置
     */
    private void firstRuningInit(){
    	
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
    	Editor editor=sp.edit();
    	
    	editor.putBoolean(this.getResources().getString(R.string.grabmmRedEnvelop), true);	//初始化抢微信红包的设置
    	editor.putBoolean(this.getResources().getString(R.string.grabQQRedEnvelop), true);	//初始化抢QQ普通红包的设置
    	editor.putBoolean(this.getResources().getString(R.string.grabQQpasswdEnvelop), false);	//初始化抢QQ口令红包的设置
//    	editor.putBoolean(this.getResources().getString(R.string.grabRedEnvelop), false);
    	editor.commit();
    	
    }
    
    /************************************************************************************************
     ***************************************** END **************************************************
     ************************************************************************************************/

    /**
     * 内部receiver，监听数据库变化
     * @author lu_yi
     *
     */
    public class UpdataRecordBroadcast extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		if("com.example.grabredenvelopetools.RedEnvelopeMainActivity.UpdataRecordBroadcast".equals(intent.getAction())){
    			Log.i("新红包","#");
        		Cursor cursor=recordDB.queryTheCursor();
        		RedEnvelopInfo info=new RedEnvelopInfo(cursor.getString(cursor.getColumnIndex("date")), 
        				cursor.getString(cursor.getColumnIndex("sender")), 
        				cursor.getInt(cursor.getColumnIndex("flag")), 
        				cursor.getDouble(cursor.getColumnIndex("num")));
        		redEnvelopRecord.add(0,info);
        		recordAdapter.notifyDataSetChanged();
            	RedEnvelopCount.setText("共"+recordDB.queryTheCursor().getCount()+"个");
    		}
    	}
    }

}











