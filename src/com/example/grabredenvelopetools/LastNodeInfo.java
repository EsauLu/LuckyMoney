package com.example.grabredenvelopetools;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class LastNodeInfo {
	
	/**
	 * 最后一个红包的信息
	 */
	private String lastMessage;	
	
	private String newMessage;
	
	private String lastSender;
	
	private String newSender;
	
	public LastNodeInfo() {
		// TODO Auto-generated constructor stub
		lastMessage="";
		lastSender="Unknow sender";
		newMessage="";
		newSender="";
	}
	
	/**
	 * 检查是否相同的红包
	 * @param info
	 * @return
	 */
	public boolean isLastNode(AccessibilityNodeInfo info){
		
		if(info==null) return true;
		try{
			AccessibilityNodeInfo parent=info.getParent().getParent();
//			Log.i("未出现异常1", "++++++++++++++++++++++"+toString());
//			dfs(parent, 1);
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
//			Log.i("分隔行", "#");
			getNodeMessage(parent);
			
			if(newMessage.equals(lastMessage)){
//				Log.i("相同的红包", lastMessage+"");
				return true;
			}else{
//				Log.i("不同的红包", lastMessage+"");
				lastMessage=newMessage;
				lastSender=newSender;
				return false;
			}

		}catch(NullPointerException e){
//			Log.i("出现空指针异常", "++++++++++++++++++++++"+toString());
			return true;
		}catch(Exception e){
//			Log.i("未知异常", "++++++++++++++++++++++"+toString());
			return true;
		}
	}
	
	public String getSender() {
		return lastSender;
	}

	private void getNodeMessage(AccessibilityNodeInfo info){
//		Log.i("开始检查1", "====================================================");
//		Log.i("上一个红包:",lastMessage+"#");
//		Log.i("开始检查2", "====================================================");
		newMessage="";
		dfs(info, 1);
//		Log.i("检查结束3", "====================================================");
//		Log.i("最新的红包:",newMessage);
//		Log.i("检查结束4", "====================================================");
	}

	
	//*************************************************************************************************
	public void dfs(AccessibilityNodeInfo root,int k){
		if(root==null){
			return;
		}
		
//		int n=root.getChildCount();
		CharSequence s1=root.getText();
		CharSequence s2=root.getContentDescription();
		if(s1!=null){
			newMessage+=s1.toString();
		}
		if(s2!=null){
			String s=s2.toString();
			newMessage+=s;
			if(s.contains("头像")){
				newSender=s.substring(0, s.length()-2);
			}
		}
//		Log.i("检查中"+":"+root.getChildCount()+":"+k, root.getClassName()+"\t*****\t"+root.getContentDescription());
		
		for(int i=root.getChildCount()-1;i>=0;i--){
			dfs(root.getChild(i),i+1);
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+newMessage+","+lastMessage+","+newSender+","+lastSender+"]";
	}
	
}




