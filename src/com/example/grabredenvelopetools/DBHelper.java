package com.example.grabredenvelopetools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

	public static final String DATA_BASE_NAME="RedEnvelopRecord.db";
	public static final String TABLE_NAME="record";
	private static final int VERSION=1;

	public DBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATA_BASE_NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +  
                " (date varchar(30), sender varchar(20), flag integer, num DOUBLE)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub  
        db.execSQL("ALTER TABLE "+TABLE_NAME+" ADD COLUMN other STRING"); 
	}

}
