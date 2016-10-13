package com.example.grabredenvelopetools;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordAdapter extends ArrayAdapter<RedEnvelopInfo> {
	
	private int LayoutID;
	
	public RecordAdapter(Context context, int resource, RedEnvelopInfo[] objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		LayoutID=resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LinearLayout linearView;
		
		RedEnvelopInfo info=getItem(position);
//		Log.i("适配器提取数据", "+"+position+"sender:"+info.getSender()+",time:"+info.getTime()+",flag:"+info.getFlag()+",num"+info.getNum());
		
		String s="未知来源";
		
		if(convertView==null){
			linearView=new LinearLayout(getContext());
			LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(LayoutID, linearView,true);
		}else{
			linearView=(LinearLayout)convertView;
		}

		if(info.getFlag()==0){
			s="[微信红包] ";
		}else if(info.getFlag()==1){
			s="[QQ红包] ";
		}
		
		TextView sender=(TextView) linearView.findViewById(R.id.sender);
		TextView time=(TextView) linearView.findViewById(R.id.time);
		TextView num=(TextView) linearView.findViewById(R.id.num);
		
		sender.setText(s+info.getSender());
		time.setText(info.getTime());
		num.setText(info.getNum()+"元");
		
		return linearView;
	}

	public RecordAdapter(Context context, int resource,
			List<RedEnvelopInfo> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		LayoutID=resource;
	}
	
}






