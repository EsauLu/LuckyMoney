package com.example.grabredenvelopetools;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class LastNodeInfo {
	
	/**
	 * ���һ���������Ϣ
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
	 * ����Ƿ���ͬ�ĺ��
	 * @param info
	 * @return
	 */
	public boolean isLastNode(AccessibilityNodeInfo info){
		
		if(info==null) return true;
		try{
			AccessibilityNodeInfo parent=info.getParent().getParent();
//			Log.i("δ�����쳣1", "++++++++++++++++++++++"+toString());
//			dfs(parent, 1);
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
//			Log.i("�ָ���", "#");
			getNodeMessage(parent);
			
			if(newMessage.equals(lastMessage)){
//				Log.i("��ͬ�ĺ��", lastMessage+"");
				return true;
			}else{
//				Log.i("��ͬ�ĺ��", lastMessage+"");
				lastMessage=newMessage;
				lastSender=newSender;
				return false;
			}

		}catch(NullPointerException e){
//			Log.i("���ֿ�ָ���쳣", "++++++++++++++++++++++"+toString());
			return true;
		}catch(Exception e){
//			Log.i("δ֪�쳣", "++++++++++++++++++++++"+toString());
			return true;
		}
	}
	
	public String getSender() {
		return lastSender;
	}

	private void getNodeMessage(AccessibilityNodeInfo info){
//		Log.i("��ʼ���1", "====================================================");
//		Log.i("��һ�����:",lastMessage+"#");
//		Log.i("��ʼ���2", "====================================================");
		newMessage="";
		dfs(info, 1);
//		Log.i("������3", "====================================================");
//		Log.i("���µĺ��:",newMessage);
//		Log.i("������4", "====================================================");
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
			if(s.contains("ͷ��")){
				newSender=s.substring(0, s.length()-2);
			}
		}
//		Log.i("�����"+":"+root.getChildCount()+":"+k, root.getClassName()+"\t*****\t"+root.getContentDescription());
		
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




