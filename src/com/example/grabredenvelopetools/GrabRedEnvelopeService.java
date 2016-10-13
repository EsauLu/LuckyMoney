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
	 * GrabRedEnvelopeService运行状态
	 */
	private static boolean runingState=false;
	
	/**
	 * 标记抢红包状态
	 */
	private static boolean grabRedEnvelopState=false;
	
	/**
	 * 标记是否抢微信红包
	 */
	private static boolean grabmmRedEnvelop;

	/**
	 * 标记是否自动抢QQ普通红包
	 */
	private static boolean grabQQRedEnvelop;
	
	/**
	 * 标记是否自动抢QQ口令红包
	 */
	private static boolean grabQQpasswdEnvelop;
	
	/**
	 * 标记是否软件自动抢的红包（用于判断是否需要全局返回）
	 */
	private static boolean isAutoGrabRedEnvelop=false;
	
	/**
	 * 标记是否软件自动抢的红包（用于判断是否需要全局返回）
	 */
	private static boolean needGetNum=false;
	
	/**
	 * QQ红包关键字
	 */
	private String QQ_RED_ENVELOP_KEY="[QQ红包]";
	
	/**
	 * QQ口令红包关键字
	 */
	private String QQ_PASSWD_RED_ENVELOP_KEY="口令红包";
	
	/**
	 * QQ口令红包关键字
	 */
	private String QQ_CLICK_INPUT_PASSWD_KEY="点击输入口令";
	
	/**
	 * QQ拆红包关键字
	 */
	private String QQ_GET_ENVELOP_KEY="点击拆开";
	
	/**
	 * 发送QQ消息关键字
	 */
	private String QQ_SEND_BUTTON_KEY="发送";
	
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
	 * 微信红包关键字
	 */
	private String MM_RED_ENVELOP_KEY="[微信红包]";
	
	/**
	 * 微信聊天页面红包关键字
	 */
	private String MM_OPEN_RED_ENVELOP_KEYS="微信红包";
	
	/**
	 * 手慢了
	 */
	private String MM_UNGRAB_RED_ENVELOP_KEY="手慢了，红包派完了";
	
	/**
	 * 微信拆红包关键字
	 */
//	private String MM_GET_RED_ENVELOP_KEY=null;//拆红包
	
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
	 * 最后一个红包
	 */
	private LastNodeInfo lastNodeInfo;
	
	/**
	 * 红包记录数据库
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
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:	//监听通知栏
			dealNotification(event);
			break;

		case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:	//监听窗口内容
			dealWindowContent(event);
			break;

		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:	//监听窗口状态
			dealWindowState(event);
			break;

		default:
			break;
		}
		
	}
	
	
	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              通知栏处理
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * 处理通知栏事件
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
	 * 激活通知
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
//				Log.i("检查是否锁屏", "++++++"+f);
//				if(f){
//					Log.i("锁屏状态", "执行自动解锁"+f);
//					KeyguardManager.KeyguardLock lock=keyManager.newKeyguardLock("");
//					lock.disableKeyguard();
//					lock.reenableKeyguard();
//				}
				pt.send();
//				keyManager=null;
			}
		}catch(Exception e){
			Toast.makeText(this, "出现未知异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                  通知栏处理   END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              窗口动态处理
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	/**
	 * 处理窗口内容事件
	 */
	private void dealWindowState(AccessibilityEvent event){
		
		String className=event.getClassName().toString();

		if(className.equals(MM_CHAT_UI)){	//抢微信红包
			if(grabmmRedEnvelop){
				openMMRedEnvelop();
			}
		}else if(className.equals(MM_GET_RED_ENVELOP_UI)){	//拆微信红包
			getMMRedEnvelop();
		}else if(className.contains(QQ_CHAT_UI)){	//抢QQ红包
			if(grabQQRedEnvelop)
				getQQRedEnvelop();
		}else if(className.equals(MM_RED_ENVELOP_DETAIL_UI)){	//获取微信红包详情
			getMMRedEnvelopDetail();
		}else if(className.equals(QQ_RED_ENVELOP_UI)){	//获取QQ红包详情
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
	 * 处理窗口状态事件
	 */
	private void dealWindowContent(AccessibilityEvent event){	///////////////////////////////////     这里有问题
		
		String className=getCurrentTask();
		
//		AccessibilityNodeInfo t=getRootInActiveWindow();
//		if(t!=null){
//			Log.i("事件源内容", "--------"+t.getClassName());
//			t=t.getParent();
//		}
		
//		Log.i("内容", event.getPackageName()+"+++"+getCurrentTask());

		if(className.contains(MM_CHAT_UI)){	//抢微信红包
//			Log.i("微信内容", event.getPackageName()+"++******************+"+className);
			if(grabmmRedEnvelop){
				openMMRedEnvelop();
			}
		}else if(className.contains(QQ_CHAT_UI)){	//抢QQ红包
//			Log.i("QQ内容", event.getPackageName()+"*************************"+className);
			if(grabQQRedEnvelop)
				getQQRedEnvelop();
		}
		
	}

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                 窗口动态处理          END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              微信红包处理
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * 抢微信红包
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
	 * 微信拆红包
	 */
	private void getMMRedEnvelop(){
		
		if(isAutoGrabRedEnvelop==false){
			return;
		}
		
		AccessibilityNodeInfo rootInfo=getRootInActiveWindow();
		
		if(rootInfo==null){
			return;
		}
			
		//查找拆红包按钮，如果找到，结束函数，否则执行全局返回操作
		if(findGetButton(rootInfo)){
			needGetNum=true;	//需要记录红包
			return;
		}

		//没找到拆红包按钮，没抢到红包，执行返回
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
	 * 查找微信拆红包按钮并点击
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
	 * 微信领取详情
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
	 *                                     微信红包处理         END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	

	/* *************************************************************************************************************
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 *                                              QQ红包处理
	 * *************************************************************************************************************
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	
	/**
	 * 抢QQ红包
	 */
	private void getQQRedEnvelop(){
		
		AccessibilityNodeInfo rootInfo=getRootInActiveWindow();
		if(rootInfo==null){
			return;
		}
		
		//搜索红包
		List<AccessibilityNodeInfo> nodeList=rootInfo.findAccessibilityNodeInfosByText(QQ_GET_ENVELOP_KEY);
		if(nodeList==null){
			return;
		}
		//拆红包
		if(nodeList!=null&&!nodeList.isEmpty()){
			QQNormalRedEnvelop(nodeList);
		}
		
		//搜索口令红包
		List<AccessibilityNodeInfo> nodeList1=rootInfo.findAccessibilityNodeInfosByText(QQ_PASSWD_RED_ENVELOP_KEY);
		if(nodeList1==null||nodeList1.isEmpty()){
			return;
		}
		//拆红包
		if(grabQQpasswdEnvelop==true){
			QQpasswdRedEnvelop(nodeList1);
		}
		
	}
	
	/**
	 * 拆QQ普通红包
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
	 * 拆QQ口令红包
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
		
//		QQNormalRedEnvelop(nodeList);//先点击红包，这个步骤和拆普通红包一样，所以调用拆普通红包的函数
		
		AccessibilityNodeInfo rootNode=getRootInActiveWindow();
		List<AccessibilityNodeInfo> keyNode=rootNode.findAccessibilityNodeInfosByText(QQ_CLICK_INPUT_PASSWD_KEY);
		if(keyNode==null||keyNode.isEmpty()){
			return;
		}
		
		boolean needSend=false;//记录是否需要发送口令（若找到口令，则发送，否则不发送）
		
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
	 * 获取QQ红包详情
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
	 * 查找QQ红包获得金额
	 * @return
	 */
	private double findNum(){
		
		AccessibilityNodeInfo rootNode=getRootInActiveWindow();
		if(rootNode==null){
			return -1;
		}
		dfs(rootNode,1);
		List<AccessibilityNodeInfo> list=rootNode.findAccessibilityNodeInfosByText("元");
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
	 * 查找红包发送者
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
				if(!s.equals("Unknow sender")&&s.contains("来自")){
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
	 *                                   QQ红包处理          END
	 * *************************************************************************************************************
	 * ************************************************************************************************************/
	
	/**
	 * 保存红包记录,并通知主界面更新记录
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
		Toast.makeText(this, "自动抢红包已关闭。<hr/>关闭源：onInterrupt", Toast.LENGTH_SHORT).show();
		
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "自动抢红包已关闭", Toast.LENGTH_SHORT).show();
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
		Toast.makeText(this, "自动抢红包服务已开启", Toast.LENGTH_SHORT).show();
	}
	

    /**
     * 初始化设置信息
     */
    private void initSetting(){
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
		
		//监听设置
		sp.registerOnSharedPreferenceChangeListener(this);
		
		//初始化各项设置
		runingState=true;
		grabRedEnvelopState=true;
		lastNodeInfo=new LastNodeInfo();
    	grabmmRedEnvelop=sp.getBoolean("grabmmRedEnvelop", true);	//获取抢微信红包设置
    	grabQQRedEnvelop=sp.getBoolean("grabQQRedEnvelop", true);	//获取抢QQ普通红包设置
    	grabQQpasswdEnvelop=sp.getBoolean("grabQQpasswdEnvelop", false);	//获取抢QQ口令红包设置
//    	grabmmRedEnvelop=true;	//获取抢微信红包设置
//    	grabQQRedEnvelop=true;	//获取抢QQ普通红包设置
//    	grabQQpasswdEnvelop=false);	//获取抢QQ口令红包设置
    	mRecord=new DBManager(this);
    	
    }
    
    /**
     * 设置是否抢红包
     */
    public static void setGrabRedEnvelop(boolean State){
    	grabRedEnvelopState=State;
    }
    
    /**
     * 返回是否抢红包
     */
    public static boolean getGrabRedEnvelop(){
    	return grabRedEnvelopState;
    }
    
    /**
     * 返回运行状态
     * @return
     */
    public static boolean getRuningState(){
    	return runingState;
    }
	
    /**
     * 实现SharedPreferences.OnSharedPreferenceChangeListener接口
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

关键字：
	
	final String NOTIFICATION_KEY="[QQ红包]";

	final String CLICK_KEY="点击拆开";

	final String GET_MESSEGE="查看领取详情";

	final String YUAN="元";

类名：

QQ聊天界面：com.tencent.mobileqq.activity.SplashActivity

QQ拆开红包界面：cooperation.qwallet.plugin.QWalletPluginProxyActivity

QQ支付界面：com.tencent.mobileqq.activity.PayBridgeActivity

微信聊天界面：com.tencent.mm.ui.LauncherUI

微信拆红包界面：com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI

微信红包详情界面：com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI

拆红包按钮：com.tencent.mm:id/b2c
 */






