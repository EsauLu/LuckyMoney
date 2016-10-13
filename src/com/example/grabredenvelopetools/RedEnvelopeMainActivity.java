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
     ************************************* ��ť�¼������� ***********************************************
     ************************************************************************************************/
    
    /**
     * ��ʼ�������¼
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
    	RedEnvelopCount.setText("��"+c+"��");
    }
    
    /**
     * �Զ���������񿪹ذ�ť�¼�������
     */
    public void serviceButtonOnClick(View v){
    	
    	if (GrabRedEnvelopeService.getRuningState()==false) {
    		
    		AlertDialog.Builder ad=new AlertDialog.Builder(this)
    						.setTitle("��ʾ")
    						.setMessage("Ҫʹ���Զ�����������������ÿ����������ܣ������ֻ������ÿ������ϰ�����")
    						.setPositiveButton("ȥ����", new DialogInterface.OnClickListener() {
								
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
     * ��¼��ť�¼�����
     * @param v
     */
    public void recordButton(View v){
    	Toast.makeText(this, "��ϸ��¼", Toast.LENGTH_SHORT).show();
    }

    /**
     * ���ð�ť�¼�����
     * @param v
     */
    public void settingButton(View v){
    	
    	if (GrabRedEnvelopeService.getRuningState()==false) {
    		
    		AlertDialog.Builder ad=new AlertDialog.Builder(this)
    						.setTitle("��ʾ")
    						.setMessage("Ҫʹ���Զ�����������������ÿ����������ܣ������ֻ������ÿ������ϰ�����")
    						.setPositiveButton("ȥ����", new DialogInterface.OnClickListener() {
								
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
     *************************************** ��ʼ������ *************************************************
     ************************************************************************************************/
    
    
    /**
     * �ж��Ƿ�װ���״����г���
     */
    private boolean isFirstRuning(){
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
    	//�����Ƿ��״����У����򷵻�true�����Ҽ�¼�����ǵ�һ�����У����򷵻�false
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
     * ��װ���״�����Ӧ�õĳ�ʼ������
     */
    private void firstRuningInit(){
    	
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
    	Editor editor=sp.edit();
    	
    	editor.putBoolean(this.getResources().getString(R.string.grabmmRedEnvelop), true);	//��ʼ����΢�ź��������
    	editor.putBoolean(this.getResources().getString(R.string.grabQQRedEnvelop), true);	//��ʼ����QQ��ͨ���������
    	editor.putBoolean(this.getResources().getString(R.string.grabQQpasswdEnvelop), false);	//��ʼ����QQ������������
//    	editor.putBoolean(this.getResources().getString(R.string.grabRedEnvelop), false);
    	editor.commit();
    	
    }
    
    /************************************************************************************************
     ***************************************** END **************************************************
     ************************************************************************************************/

    /**
     * �ڲ�receiver���������ݿ�仯
     * @author lu_yi
     *
     */
    public class UpdataRecordBroadcast extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		if("com.example.grabredenvelopetools.RedEnvelopeMainActivity.UpdataRecordBroadcast".equals(intent.getAction())){
    			Log.i("�º��","#");
        		Cursor cursor=recordDB.queryTheCursor();
        		RedEnvelopInfo info=new RedEnvelopInfo(cursor.getString(cursor.getColumnIndex("date")), 
        				cursor.getString(cursor.getColumnIndex("sender")), 
        				cursor.getInt(cursor.getColumnIndex("flag")), 
        				cursor.getDouble(cursor.getColumnIndex("num")));
        		redEnvelopRecord.add(0,info);
        		recordAdapter.notifyDataSetChanged();
            	RedEnvelopCount.setText("��"+recordDB.queryTheCursor().getCount()+"��");
    		}
    	}
    }

}











