package com.contactshistory;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {

	public static final String ID = "_id";
	public static final String KEY = "_key";
	public static final String DATE = "date";
	public static final String LOCATION = "location";
	public static final String ADDRESS = "address";

	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "contactsh";
	private static final String DATABASE_TABLE = "people";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table people (_id integer primary key autoincrement, _key text not null, date text not null, location text not null, address text not null);";

	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	DBAdapter(Context context)
	{
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
							  int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion
					+ " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS people");
			onCreate(db);
		}
	}

	DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}

	//closes the database
	public void close()
	{
		DBHelper.close();
	}

	//insert row into the database
	public long insertContact(String key, String date, String location, String address)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY, key);
		initialValues.put(DATE, date);
		initialValues.put(LOCATION, location);
		initialValues.put(ADDRESS, address);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	//deletes a particular row by Key
	public boolean deleteContact(String key)
	{
		db.execSQL("DELETE FROM "+DATABASE_TABLE+" WHERE "+KEY+" = '"+key+"';");

		return true;
	}


	//deletes a particular row  by key and date
	public boolean deleteContactByKeyAndDate(String key, String date)
	{
		db.execSQL("DELETE FROM "+DATABASE_TABLE+" WHERE "+KEY+" = '"+key+"' and "+DATE+" = '"+date+"';");

		return true;
	}

	//deletes all records in table
	public boolean deleteAll()
	{
		db.execSQL("DELETE FROM "+DATABASE_TABLE+";");
		return true;
	}

	//retrieves all rows
	Cursor getAllContacts()
	{
		return db.query(DATABASE_TABLE, new String[] {
						KEY,
						DATE,
						LOCATION,
						ADDRESS},
				null,
				null,
				null,
				null,
				null);
	}

	//retrieves a particular row
	public Cursor getContact(String key) throws SQLException
	{
		Cursor rows =
				db.query(true, DATABASE_TABLE, new String[] {
								KEY,
								DATE,
								LOCATION,
								ADDRESS},
						KEY + "=" + key,
						null,
						null,
						null,
						null,
						null);
		if (rows != null) {
			rows.moveToFirst();
		}
		return rows;
	}

	//updates a row
	public boolean updateContact(String old_key, String new_key)
	{
		ContentValues args = new ContentValues();
		args.put(KEY, new_key);
		return db.update(DATABASE_TABLE, args,
				KEY + "=" + "'" + old_key +"'", null) > 0;
	}

}
