package com.example.grabredenvelopetools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RedEnvelopInfo {
	
	/**
	 * ���������
	 */
	private String sender;
	
	/**
	 * �������ʱ��
	 */
	private String time;
	
	/**
	 * �����ȡ���
	 */
	private double num;
	
	/**
	 * �����Դ��0��ʾ΢�ź����1��ʾQQ���
	 */
	private int flag;

	public RedEnvelopInfo() {
		// TODO Auto-generated constructor stub
		SimpleDateFormat format=new SimpleDateFormat("yyy-MM-dd  HH:mm:ss");
		this.sender="Unknow sender";
		this.time=format.format(new Date(System.currentTimeMillis()));
		this.flag=-1;
		this.num=0.0;
	}

	public RedEnvelopInfo(String time,String sender,int flag,double num) {
		// TODO Auto-generated constructor stub
		this.sender=sender;
		this.time=time;
		this.flag=flag;
		this.num=num;
	}
	
	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time=time;
	}

	public double getNum() {
		return num;
	}

	public void setNum(double num) {
		this.num = num;
	}

}
