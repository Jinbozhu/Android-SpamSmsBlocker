package com.example.feeling.spamtextblocker.database;

import android.content.ContentValues;
import android.content.Context;

import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/5/16.
 */
public class SmsDatabase extends DatabaseHelper {
    public static final String TAG = "SmsDatabase";
    public static final String TABLE_NAME = "sms";

    public static final String COL_ID = "id";
    public static final String COL_SENDER = "sender";
    public static final String COL_CONTENT = "content";
    public static final String COL_RECIPIENT = "recipient";
    public static final String COL_TIME = "time";
    public static final String COL_IS_DELIVERED = "isDelivered";
    public static final String COL_IS_READ = "isRead";
    public static final String COL_IS_SPAM = "isSpam";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COL_SENDER + " TEXT, " +
                    COL_CONTENT + " TEXT, " +
                    COL_RECIPIENT + " TEXT, " +
                    COL_TIME + " INTEGER, " +
                    COL_IS_DELIVERED + " INTEGER, " +
                    COL_IS_READ + " INTEGER, " +
                    COL_IS_SPAM + " INTEGER" + ")";

    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public SmsDatabase(Context context) {
        super(context);
    }

//    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public long insertSms(Message msg) {
        if (msg == null) {
            throw new IllegalArgumentException("msg == null");
        }

        ContentValues values = new ContentValues();
        values.put(COL_SENDER, msg.getSender());
        values.put(COL_CONTENT, msg.getContent());
        values.put(COL_RECIPIENT, msg.getRecipient());
        values.put(COL_TIME, msg.getTime());
        values.put(COL_IS_DELIVERED, msg.isDelivered());
        values.put(COL_IS_READ, msg.isRead());
        values.put(COL_IS_SPAM, msg.isSpam());

//        return insert(values);
        return 1;
    }

    public List<Message> getAllowedMessage() {
        List<Message> allowedMessage = new ArrayList<>();
        // TODO
        return allowedMessage;
    }

    public List<Message> getBlockedMessage() {
        List<Message> blockedMessage = new ArrayList<>();
        // TODO
        return blockedMessage;
    }
}
