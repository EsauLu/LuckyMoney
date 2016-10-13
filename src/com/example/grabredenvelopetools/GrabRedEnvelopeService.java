package com.example.grabredenvelopetools;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
//import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class GrabRedEnvelopeService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener{
	
	/**
	 * GrabRedEnvelopeService����״̬
	 */
	private static boolean runingState=false;
	
	/**
	 * ��������״̬
	 */
	private static boolean grabRedEnvelopState=false;
	
	/**
	 * ����Ƿ���΢�ź��
	 */
	private static boolean grabmmRedEnvelop;

	/**
	 * ����Ƿ��Զ���QQ��ͨ���
	 */
	private static boolean grabQQRedEnvelop;
	
	/**
	 * ����Ƿ��Զ���QQ������
	 */
	private static boolean grabQQpasswdEnvelop;
	
	/**
	 * ����Ƿ�����Զ����ĺ���������ж��Ƿ���Ҫȫ�ַ��أ�
	 */
	private static boolean isAutoGrabRedEnvelop=false;
	
	/**
	 * ����Ƿ�����Զ����ĺ���������ж��Ƿ���Ҫȫ�ַ��أ�
	 */
	private static boolean needGetNum=false;
	
	/**
	 * QQ����ؼ���
	 */
	private String QQ_RED_ENVELOP_KEY="[QQ���]";
	
	/**
	 * QQ�������ؼ���
	 */
	private String QQ_PASSWD_RED_ENVELOP_KEY="������";
	
	/**
	 * QQ�������ؼ���
	 */
	private String QQ_CLICK_INPUT_PASSWD_KEY="����������";
	
	/**
	 * QQ�����ؼ���
	 */
	private String QQ_GET_ENVELOP_KEY="�����";
	
	/**
	 * ����QQ��Ϣ�ؼ���
	 */
	private String QQ_SEND_BUTTON_KEY="����";
	
	/**
	 * QQ_package_names
	 */
	private String QQ_PACKAGE_NAME="com.tencent.mobileqq";
	
	/**
	 * qq_chat_ui
	 */
	private String QQ_CHAT_UI="com.tencent.mobileqq.activity.SplashActivity";
	
	/**
	 * qq_red_envelope_ui
	 */
	private String QQ_RED_ENVELOP_UI="cooperation.qwallet.plugin.QWalletPluginProxyActivity";
	
	/**
	 * ΢�ź���ؼ���
	 */
	private String MM_RED_ENVELOP_KEY="[΢�ź��]";
	
	/**
	 * ΢������ҳ�����ؼ���
	 */
	private String MM_OPEN_RED_ENVELOP_KEYS="΢�ź��";
	
	/**
	 * ������
	 */
	private String MM_UNGRAB_RED_ENVELOP_KEY="�����ˣ����������";
	
	/**
	 * ΢�Ų����ؼ���
	 */
//	private String MM_GET_RED_ENVELOP_KEY=null;//����
	
	/**
	 * mm_package_names
	 */
	private String MM_PACKAGE_NAME="com.tencent.mm";
	
	/**
	 * mm_chat_ui
	 */
	private String MM_CHAT_UI="com.tencent.mm.ui.LauncherUI";
							   
	/**
	 * mm_get_red_envelop_ui
	 */
	private String MM_GET_RED_ENVELOP_UI="com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	
	/**
	 * mm_red_envelop_detail_ui
	 */
	private String MM_RED_ENVELOP_DETAIL_UI="com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

	/**
	 * ���һ�����
	 */
	private LastNodeInfo lastNodeInfo;
	
	/**
	 * �����¼���ݿ�
	 */
	private DBManager mRecord;
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		if(grabRedEnvelopState==false) {
			return;
		}
		
		final int eventType=event.getEventType();
		
		switch (eventType) {
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:	//����֪ͨ��
			dealNotification(event);
			break;

		case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:	//������������
			dealWindowContent(event);
			break;

		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:	//��������״̬
			dealWindowState(event);
			break;

		default:
			break;
		}
		
	}
	
	
	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              ֪ͨ������
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * ����֪ͨ���¼�
	 */
	private void dealNotification(AccessibilityEvent event){
		
		List<CharSequence> textList=event.getText();
		for(CharSequence text:textList){
			String s=text.toString();
			if(s.contains(MM_RED_ENVELOP_KEY)&&grabmmRedEnvelop==true){
				openNotification(event,0);
				break;
			}else if(s.contains(QQ_RED_ENVELOP_KEY)&&(grabQQRedEnvelop==true)){
				openNotification(event,1);
				break;
			}
		}
	}
	
	
	/**
	 * ����֪ͨ
	 * @param event
	 */
	private void openNotification(AccessibilityEvent event,int packageType){
		
		if(event.getParcelableData()==null||!(event.getParcelableData() instanceof Notification)) 
			return;
		
		Notification notification=(Notification)event.getParcelableData();
		PendingIntent pt=notification.contentIntent;
		String packageName=notification.contentView.getPackage().toString();
		
		try{
			if(packageName.equals(MM_PACKAGE_NAME)&&packageType==0
					||packageName.equals(QQ_PACKAGE_NAME)&&packageType==1){
//				KeyguardManager keyManager=(KeyguardManager ) getSystemService(KEYGUARD_SERVICE);
//				boolean f=keyManager.inKeyguardRestrictedInputMode();
//				Log.i("����Ƿ�����", "++++++"+f);
//				if(f){
//					Log.i("����״̬", "ִ���Զ�����"+f);
//					KeyguardManager.KeyguardLock lock=keyManager.newKeyguardLock("");
//					lock.disableKeyguard();
//					lock.reenableKeyguard();
//				}
				pt.send();
//				keyManager=null;
			}
		}catch(Exception e){
			Toast.makeText(this, "����δ֪�쳣��"+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                  ֪ͨ������   END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              ���ڶ�̬����
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	/**
	 * �����������¼�
	 */
	private void dealWindowState(AccessibilityEvent event){
		
		String className=event.getClassName().toString();

		if(className.equals(MM_CHAT_UI)){	//��΢�ź��
			if(grabmmRedEnvelop){
				openMMRedEnvelop();
			}
		}else if(className.equals(MM_GET_RED_ENVELOP_UI)){	//��΢�ź��
			getMMRedEnvelop();
		}else if(className.contains(QQ_CHAT_UI)){	//��QQ���
			if(grabQQRedEnvelop)
				getQQRedEnvelop();
		}else if(className.equals(MM_RED_ENVELOP_DETAIL_UI)){	//��ȡ΢�ź������
			getMMRedEnvelopDetail();
		}else if(className.equals(QQ_RED_ENVELOP_UI)){	//��ȡQQ�������
			getQQRedEnvelopDetail();
		}
		
	}
	private String getCurrentTask(){

        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> appTask = activityManager.getRunningTasks(1);
           
        if (appTask != null)
             if(appTask.size()>0)
                return appTask.get(0).topActivity.toString();
        return "";
       
    }

	/**
	 * ������״̬�¼�
	 */
	private void dealWindowContent(AccessibilityEvent event){	///////////////////////////////////     ����������
		
		String className=getCurrentTask();
		
//		AccessibilityNodeInfo t=getRootInActiveWindow();
//		if(t!=null){
//			Log.i("�¼�Դ����", "--------"+t.getClassName());
//			t=t.getParent();
//		}
		
//		Log.i("����", event.getPackageName()+"+++"+getCurrentTask());

		if(className.contains(MM_CHAT_UI)){	//��΢�ź��
//			Log.i("΢������", event.getPackageName()+"++******************+"+className);
			if(grabmmRedEnvelop){
				openMMRedEnvelop();
			}
		}else if(className.contains(QQ_CHAT_UI)){	//��QQ���
//			Log.i("QQ����", event.getPackageName()+"*************************"+className);
			if(grabQQRedEnvelop)
				getQQRedEnvelop();
		}
		
	}

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                 ���ڶ�̬����          END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              ΢�ź������
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * ��΢�ź��
	 */
	private void openMMRedEnvelop(){
		
		try{
			
			AccessibilityNodeInfo rootInfo=getRootInActiveWindow();
			
			if(rootInfo==null){
				return;
			}
			List<AccessibilityNodeInfo> nodeList=rootInfo.findAccessibilityNodeInfosByText(MM_OPEN_RED_ENVELOP_KEYS);

			if(nodeList.isEmpty()){
				return;
			}

			AccessibilityNodeInfo info=nodeList.get(nodeList.size()-1);
			
			if(info==null||!MM_OPEN_RED_ENVELOP_KEYS.equals(info.getText().toString())||this.lastNodeInfo.isLastNode(info)){
				return;
			}
			
			while(info!=null){
				if(info.isClickable()){
					isAutoGrabRedEnvelop=true;
					info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					return;
				}
				info=info.getParent();
			}
		}catch(Exception e){
			return;
		}
		
	}
	
	/**
	 * ΢�Ų���
	 */
	private void getMMRedEnvelop(){
		
		if(isAutoGrabRedEnvelop==false){
			return;
		}
		
		AccessibilityNodeInfo rootInfo=getRootInActiveWindow();
		
		if(rootInfo==null){
			return;
		}
			
		//���Ҳ�����ť������ҵ�����������������ִ��ȫ�ַ��ز���
		if(findGetButton(rootInfo)){
			needGetNum=true;	//��Ҫ��¼���
			return;
		}

		//û�ҵ�������ť��û���������ִ�з���
		List<AccessibilityNodeInfo> list=rootInfo.findAccessibilityNodeInfosByText(MM_UNGRAB_RED_ENVELOP_KEY);//**********************************************
		if(list!=null&&!list.isEmpty()){
			isAutoGrabRedEnvelop=false;
			performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		}
		
		if(needGetNum==true){
			needGetNum=false;
		}
		
	}
	
	/**
	 * ����΢�Ų�����ť�����
	 * @param rootInfo
	 */
	private boolean findGetButton(AccessibilityNodeInfo rootInfo){
		if(rootInfo==null){
			return false;
		}
		AccessibilityNodeInfo child;
		for(int i=rootInfo.getChildCount()-1;i>=0;i--){
			child=rootInfo.getChild(i);
			if(child==null) continue;
			if(child.getChildCount()>0){
				if(findGetButton(child)) return true;
			}else{
				if(child.getClassName().equals("android.widget.Button")){
					child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * ΢����ȡ����
	 */
	private void getMMRedEnvelopDetail(){
		if(isAutoGrabRedEnvelop==false){
			return;
		}
		if(needGetNum){
			String sender=lastNodeInfo.getSender();
			double num=0.0;
			AccessibilityNodeInfo tempRoot=getRootInActiveWindow();
			if(tempRoot!=null){
				try{
					num=Double.parseDouble(tempRoot.getChild(0).getChild(0).getChild(0).getChild(2).getText().toString());
				}catch(Exception e){
					num=-1;
				}
			}
			Date time=new Date(System.currentTimeMillis());
			saveRecord(sender, time, 0, num);
			needGetNum=false;
		}
		isAutoGrabRedEnvelop=false;
		performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
	}
	
	//*************************************************************************************************
	public void dfs(AccessibilityNodeInfo root,int k){
		if(root==null){
			return;
		}
		int n=root.getChildCount();
		CharSequence s=root.getText();
		for(int i=0;i<n;i++){
			dfs(root.getChild(i),i+1);
		}
	}

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                     ΢�ź������         END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              QQ�������
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	
	/**
	 * ��QQ���
	 */
	private void getQQRedEnvelop(){
		
		AccessibilityNodeInfo rootInfo=getRootInActiveWindow();
		if(rootInfo==null){
			return;
		}
		
		//�������
		List<AccessibilityNodeInfo> nodeList=rootInfo.findAccessibilityNodeInfosByText(QQ_GET_ENVELOP_KEY);
		if(nodeList==null){
			return;
		}
		//����
		if(nodeList!=null&&!nodeList.isEmpty()){
			QQNormalRedEnvelop(nodeList);
		}
		
		//����������
		List<AccessibilityNodeInfo> nodeList1=rootInfo.findAccessibilityNodeInfosByText(QQ_PASSWD_RED_ENVELOP_KEY);
		if(nodeList1==null||nodeList1.isEmpty()){
			return;
		}
		//����
		if(grabQQpasswdEnvelop==true){
			QQpasswdRedEnvelop(nodeList1);
		}
		
	}
	
	/**
	 * ��QQ��ͨ���
	 */
	private void QQNormalRedEnvelop(List<AccessibilityNodeInfo> nodeList){
		
		AccessibilityNodeInfo info=nodeList.get(nodeList.size()-1);
		if(info==null) {
			return;
		}
		
		AccessibilityNodeInfo parentInfo=info.getParent();
		if(parentInfo==null){
			return ;
		}
		
		if(parentInfo.isClickable()){
			isAutoGrabRedEnvelop=true;
			parentInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			
			return;
		}
		
	}
	
	/**
	 * ��QQ������
	 */
	private void QQpasswdRedEnvelop(List<AccessibilityNodeInfo> nodeList){

		
		AccessibilityNodeInfo newInfo=nodeList.get(nodeList.size()-1);
//		Log.i("getText()", "**********"+newInfo.getText());
		if(newInfo==null||!QQ_PASSWD_RED_ENVELOP_KEY.equals(newInfo.getText().toString())) {
//			Log.i((QQ_PASSWD_RED_ENVELOP_KEY.equals(newInfo.getText()))+"***getText()", QQ_PASSWD_RED_ENVELOP_KEY+"!="+newInfo.getText());
			return;
		}
		
		
		
		AccessibilityNodeInfo parentInfo=newInfo.getParent();
		if(parentInfo==null){
			return;
		}
		
		if(parentInfo.isClickable()){
			isAutoGrabRedEnvelop=true;
			parentInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}else{
			return;
		}
		
//		QQNormalRedEnvelop(nodeList);//�ȵ��������������Ͳ���ͨ���һ�������Ե��ò���ͨ����ĺ���
		
		AccessibilityNodeInfo rootNode=getRootInActiveWindow();
		List<AccessibilityNodeInfo> keyNode=rootNode.findAccessibilityNodeInfosByText(QQ_CLICK_INPUT_PASSWD_KEY);
		if(keyNode==null||keyNode.isEmpty()){
			return;
		}
		
		boolean needSend=false;//��¼�Ƿ���Ҫ���Ϳ�����ҵ�������ͣ����򲻷��ͣ�
		
		for(AccessibilityNodeInfo info:keyNode){
			
			if(info==null){
				continue;
			}
			
			AccessibilityNodeInfo infoParent=info.getParent();
			if(infoParent==null){
				continue;
			}
			
			if(infoParent.isClickable()&&infoParent.getContentDescription()==null){
				needSend=true;
				infoParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
			
		}
		
		if(!needSend){
			return;
		}
		
		rootNode=getRootInActiveWindow();
		List<AccessibilityNodeInfo> sendInfo=rootNode.findAccessibilityNodeInfosByText(QQ_SEND_BUTTON_KEY);
		if(sendInfo==null||sendInfo.isEmpty()){
			return ;
		}
		
		for(int i=sendInfo.size()-1;i>=0;i--){
			AccessibilityNodeInfo info=sendInfo.get(i);
			if(info!=null&&info.isClickable()){
				info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
		}
		
	}
	
	/**
	 * ��ȡQQ�������
	 */
	private void getQQRedEnvelopDetail(){
		if(isAutoGrabRedEnvelop==false){
			return;
		}
		
		double num=findNum();
		if(num>=0){
			String sender=findSender(getRootInActiveWindow());
			Date time=new Date(System.currentTimeMillis());
			saveRecord(sender, time, 1, num);
		}
		performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		isAutoGrabRedEnvelop=false;
	}
	
	/**
	 * ����QQ�����ý��
	 * @return
	 */
	private double findNum(){
		
		AccessibilityNodeInfo rootNode=getRootInActiveWindow();
		if(rootNode==null){
			return -1;
		}
		dfs(rootNode,1);
		List<AccessibilityNodeInfo> list=rootNode.findAccessibilityNodeInfosByText("Ԫ");
		if(list==null||list.isEmpty()){
			return -1;
		}
		
		for(AccessibilityNodeInfo info:list){
			if(info==null){
				continue;
			}
			AccessibilityNodeInfo parent=info.getParent();
			if(parent==null){
				continue;
			}
			for(int i=parent.getChildCount();i>=0;i--){
				AccessibilityNodeInfo temNode=parent.getChild(i);
				if(temNode==null){
					continue;
				}
				String s=""+temNode.getText();
				if(s.contains(".")){
					try{
						return Double.parseDouble(s);
					}catch(Exception e){
						return -1;
					}
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * ���Һ��������
	 */
	private String findSender(AccessibilityNodeInfo rootNode){

		String s="Unknow sender";
		
		if(rootNode==null){
			return s;
		}
		
		for(int i=rootNode.getChildCount();i>=0;i--){
			AccessibilityNodeInfo info=rootNode.getChild(i);
			if(info==null){
				continue;
			}
			if(info.getChildCount()==0){
				s=info.getText().toString();
				if(!s.equals("Unknow sender")&&s.contains("����")){
					return s.substring(2);
				}
			}
			s=findSender(info);
			if(!s.equals("Unknow sender")){
				return s;
			}
		}
		
		return s;
	}
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                   QQ�������          END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * ��������¼,��֪ͨ��������¼�¼
	 */
	private void saveRecord(String sender,Date time,int flag,double num){
		if(num<0){
			return;
		}
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		mRecord.add(new RedEnvelopInfo(format.format(time),sender,flag,num));
		Intent it=new Intent();
		it.setAction("com.example.grabredenvelopetools.RedEnvelopeMainActivity.UpdataRecordBroadcast");
		sendBroadcast(it);
	}
	
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

		runingState=false;
		grabRedEnvelopState=false;
		Toast.makeText(this, "�Զ�������ѹرա�<hr/>�ر�Դ��onInterrupt", Toast.LENGTH_SHORT).show();
		
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "�Զ�������ѹر�", Toast.LENGTH_SHORT).show();
		return super.onUnbind(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return this.START_STICKY;
	}
	
	@Override
	protected void onServiceConnected() {
		// TODO Auto-generated method stub
		super.onServiceConnected();
		initSetting();
		Toast.makeText(this, "�Զ�����������ѿ���", Toast.LENGTH_SHORT).show();
	}
	

    /**
     * ��ʼ��������Ϣ
     */
    private void initSetting(){
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
		
		//��������
		sp.registerOnSharedPreferenceChangeListener(this);
		
		//��ʼ����������
		runingState=true;
		grabRedEnvelopState=true;
		lastNodeInfo=new LastNodeInfo();
    	grabmmRedEnvelop=sp.getBoolean("grabmmRedEnvelop", true);	//��ȡ��΢�ź������
    	grabQQRedEnvelop=sp.getBoolean("grabQQRedEnvelop", true);	//��ȡ��QQ��ͨ�������
    	grabQQpasswdEnvelop=sp.getBoolean("grabQQpasswdEnvelop", false);	//��ȡ��QQ����������
//    	grabmmRedEnvelop=true;	//��ȡ��΢�ź������
//    	grabQQRedEnvelop=true;	//��ȡ��QQ��ͨ�������
//    	grabQQpasswdEnvelop=false);	//��ȡ��QQ����������
    	mRecord=new DBManager(this);
    	
    }
    
    /**
     * �����Ƿ������
     */
    public static void setGrabRedEnvelop(boolean State){
    	grabRedEnvelopState=State;
    }
    
    /**
     * �����Ƿ������
     */
    public static boolean getGrabRedEnvelop(){
    	return grabRedEnvelopState;
    }
    
    /**
     * ��������״̬
     * @return
     */
    public static boolean getRuningState(){
    	return runingState;
    }
	
    /**
     * ʵ��SharedPreferences.OnSharedPreferenceChangeListener�ӿ�
     */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if(key.equals("grabmmRedEnvelop")){
			grabmmRedEnvelop=sharedPreferences.getBoolean(key, false);
		}
		if(key.equals("grabQQRedEnvelop")){
			grabQQRedEnvelop=sharedPreferences.getBoolean(key, false);
		}
		if(key.equals("grabQQpasswdEnvelop")){
			grabQQpasswdEnvelop=sharedPreferences.getBoolean(key, false);
		}
	}
    
}



/*
 * 
 * 
 * 
GrabRedEnvelope

�ؼ��֣�
	
	final String NOTIFICATION_KEY="[QQ���]";

	final String CLICK_KEY="�����";

	final String GET_MESSEGE="�鿴��ȡ����";

	final String YUAN="Ԫ";

������

QQ������棺com.tencent.mobileqq.activity.SplashActivity

QQ�𿪺�����棺cooperation.qwallet.plugin.QWalletPluginProxyActivity

QQ֧�����棺com.tencent.mobileqq.activity.PayBridgeActivity

΢��������棺com.tencent.mm.ui.LauncherUI

΢�Ų������棺com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI

΢�ź��������棺com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI

������ť��com.tencent.mm:id/b2c
 */






