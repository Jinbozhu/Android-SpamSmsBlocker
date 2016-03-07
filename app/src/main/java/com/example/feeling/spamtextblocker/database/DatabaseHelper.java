package com.example.feeling.spamtextblocker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by feeling on 3/5/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Spamtextblocker.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        Log.i(TAG, "create [" + getTableName() + "]...");
        createTable(db, SmsDatabase.TABLE_NAME, SmsDatabase.SQL_CREATE_TABLE);
        createTable(db, ContactDatabase.TABLE_NAME, ContactDatabase.SQL_CREATE_TABLE);
    }

    // Create tables in onCreate()
    private void createTable(SQLiteDatabase db, String tableName, String sqlCreate) {
        Log.i(TAG, "creating table [" + tableName + "]...");
        db.execSQL(sqlCreate);
        Log.i(TAG, "table [" + tableName + "] created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        Log.i(TAG, "upgrade [" + getTableName() + "]...");
        db.execSQL(SmsDatabase.SQL_DELETE_TABLE);
        db.execSQL(ContactDatabase.SQL_DELETE_TABLE);
        onCreate(db);
    }

//    public long insert(ContentValues values) {
//        SQLiteDatabase database = this.getReadableDatabase();
//
//        long id = getWritableDatabase().insert(getTableName(), null, values);
//        Log.i(TAG, "insert into [" + getTableName() + "], id = [ " + id + "]");
//        return id;
//    }

//    protected abstract String getTableName();

}
