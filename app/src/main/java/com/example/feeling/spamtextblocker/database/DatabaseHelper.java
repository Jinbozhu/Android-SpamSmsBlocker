package com.example.feeling.spamtextblocker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/5/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Spamtextblocker.db";

    public static final String TABLE_NAME_SMS = "sms";
    public static final String TABLE_NAME_CONTACT = "contacts";

    // Common column name
    public static final String COL_ID = "id";

    // Table sms
    public static final String SMS_COL_SENDER = "sender";
    public static final String SMS_COL_CONTENT = "content";
    public static final String SMS_COL_RECIPIENT = "recipient";
    public static final String SMS_COL_TIME = "time";
    public static final String SMS_COL_IS_DELIVERED = "isDelivered";
    public static final String SMS_COL_IS_READ = "isRead";
    public static final String SMS_COL_IS_SPAM = "isSpam";

    public static final String CREATE_TABLE_SMS =
            "CREATE TABLE " + TABLE_NAME_SMS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    SMS_COL_SENDER + " TEXT, " +
                    SMS_COL_CONTENT + " TEXT, " +
                    SMS_COL_RECIPIENT + " TEXT, " +
                    SMS_COL_TIME + " INTEGER, " +
                    SMS_COL_IS_DELIVERED + " INTEGER, " +
                    SMS_COL_IS_READ + " INTEGER, " +
                    SMS_COL_IS_SPAM + " INTEGER" + ")";

    // Table contacts
    public static final String CONTACT_COL_NAME = "name";
    public static final String CONTACT_COL_NUMBER = "number";
    public static final String CONTACT_COL_IS_ALLOWED = "isAllowed";

    public static final String CREATE_TABLE_CONTACT =
            "CREATE TABLE " + TABLE_NAME_CONTACT + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    CONTACT_COL_NAME + " TEXT, " +
                    CONTACT_COL_NUMBER + " TEXT, " +
                    CONTACT_COL_IS_ALLOWED + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_SMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SMS);
        onCreate(db);
    }

    public long insertSms(Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SMS_COL_SENDER, msg.getSender());
        values.put(SMS_COL_CONTENT, msg.getContent());
        values.put(SMS_COL_RECIPIENT, msg.getRecipient());
        values.put(SMS_COL_TIME, msg.getTime());
        values.put(SMS_COL_IS_DELIVERED, msg.isDelivered());
        values.put(SMS_COL_IS_READ, msg.isRead());
        values.put(SMS_COL_IS_SPAM, msg.isSpam());

        return db.insert(TABLE_NAME_SMS, null, values);
    }

    public List<Message> getAllSms() {
        List<Message> sms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        return sms;
    }

    // close database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
