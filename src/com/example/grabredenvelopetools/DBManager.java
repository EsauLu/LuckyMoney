package com.example.grabredenvelopetools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

	private SQLiteDatabase db;
	
	private DBHelper dbHelper;
		
	public DBManager(Context context) {
		// TODO Auto-generated constructor stub
		dbHelper=new DBHelper(context);
		try{
			db=dbHelper.getWritableDatabase();
		}catch(Exception e){
			db=dbHelper.getReadableDatabase();
		}
	}
	
	/**
	 * 插入记录
	 */
	public void add(RedEnvelopInfo info){
		db.execSQL("INSERT INTO "+DBHelper.TABLE_NAME+" VALUES (?,?,?,?)", new Object[]{info.getTime(),info.getSender(),info.getFlag(),info.getNum()});
	}
	
	/**
	 * 查询记录
	 * @return
	 */
	public Cursor queryTheCursor() {
		
		Cursor c=db.rawQuery("SELECT * FROM "+DBHelper.TABLE_NAME,null);
		c.moveToLast();
		
		return c;
	}

	
	/**
	 * 查询记录
	 * @return
	 */
	public Cursor getSum() {
		
		Cursor c=db.rawQuery("SELECT * FROM "+DBHelper.TABLE_NAME,null);		
		return c;
	}
	
}
