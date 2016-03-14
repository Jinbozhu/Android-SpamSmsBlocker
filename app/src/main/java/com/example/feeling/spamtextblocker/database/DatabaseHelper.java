package com.example.feeling.spamtextblocker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.feeling.spamtextblocker.models.Contact;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by feeling on 3/5/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Spamtextblocker.db";

    public static final String TABLE_NAME_SMS = "sms";
    public static final String TABLE_NAME_CONTACT = "contacts";
    public static final String TABLE_NAME_PHONE = "phone";

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
    public static final String CONTACT_COL_IS_ALLOWED = "isAllowed";

    public static final String CREATE_TABLE_CONTACT =
            "CREATE TABLE " + TABLE_NAME_CONTACT + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    CONTACT_COL_NAME + " TEXT, " +
                    CONTACT_COL_IS_ALLOWED + " INTEGER" + ")";

    // Table phone
    public static final String PHONE_COL_NUMBER = "number";
    public static final String PHONE_COL_CONTACT_ID = "contact_id";

    public static final String CREATE_TABLE_PHONE =
            "CREATE TABLE " + TABLE_NAME_PHONE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    PHONE_COL_NUMBER + " TEXT, " +
                    PHONE_COL_CONTACT_ID + " INTEGER, " +
                    "FOREIGN KEY(" + PHONE_COL_CONTACT_ID + ")" +
                    " REFERENCES " + TABLE_NAME_CONTACT +
                    "(" + COL_ID + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("onCreate", "creating contact...");
        db.execSQL(CREATE_TABLE_CONTACT);
        Log.i("onCreate", "creating sms...");
        db.execSQL(CREATE_TABLE_SMS);
        Log.i("onCreate", "creating phone...");
        db.execSQL(CREATE_TABLE_PHONE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PHONE);
        onCreate(db);
    }

    // sms table operations
    public long insertSms(Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SMS_COL_SENDER, msg.getSender());
        values.put(SMS_COL_CONTENT, msg.getContent());
        values.put(SMS_COL_RECIPIENT, msg.getRecipient());
        values.put(SMS_COL_TIME, msg.getTime());
        // seems that we can directly put true (1) and false (0) into it
        values.put(SMS_COL_IS_DELIVERED, msg.isDelivered());
        values.put(SMS_COL_IS_READ, msg.isRead());
        values.put(SMS_COL_IS_SPAM, msg.isSpam());

        return db.insert(TABLE_NAME_SMS, null, values);
    }

    public List<Message> getAllSms() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME_SMS;
        return fetchFromDatabase(selectQuery);
    }

    public List<Message> getLatestSmsForEachContact() {
        List<Message> msg = new ArrayList<>();

        return msg;
    }

    public List<Message> getAllSmsForCertainContact(String name) {
        List<Message> msg = new ArrayList<>();

        return msg;
    }

    public List<Message> getLastBlockedSmsForCertainNumber() {
        List<Message> sms = new ArrayList<>();

        String selection = SMS_COL_IS_SPAM + " = ?";
        String[] selectionArgs = {String.valueOf(1)};
        String orderBy = SMS_COL_TIME + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_SMS, null, selection, selectionArgs,
                null, null, orderBy, String.valueOf(1));
        if (cursor.moveToFirst()) {
            long id = cursor.getInt(cursor.getColumnIndex(COL_ID));
            String sender = cursor.getString(cursor.getColumnIndex(SMS_COL_SENDER));
            String content = cursor.getString(cursor.getColumnIndex(SMS_COL_CONTENT));
            String recipient = cursor.getString(cursor.getColumnIndex(SMS_COL_RECIPIENT));
            long time = cursor.getInt(cursor.getColumnIndex(SMS_COL_TIME));
            boolean isDelivered = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_DELIVERED)) == 1;
            boolean isRead = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_READ)) == 1;
            boolean isSpam = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_SPAM)) == 1;

            Message msg = new Message(id, sender, content, recipient, time, isDelivered, isRead, isSpam);
            sms.add(msg);
        }
        cursor.close();
        closeDB();

        return sms;
    }

    public List<Message> getLastSmsForCertainNumber() {
        List<Message> sms = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        numbers.addAll(getPhone());

        for (String phoneNumber : numbers) {
            String selectQuery = "SELECT * FROM " + TABLE_NAME_SMS + " WHERE " +
                    SMS_COL_SENDER + " = '" + phoneNumber + "' OR " +
                    SMS_COL_RECIPIENT + " = '" + phoneNumber +
                    "' ORDER BY " + SMS_COL_TIME + " DESC LIMIT 1";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                long id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String sender = cursor.getString(cursor.getColumnIndex(SMS_COL_SENDER));
                String content = cursor.getString(cursor.getColumnIndex(SMS_COL_CONTENT));
                String recipient = cursor.getString(cursor.getColumnIndex(SMS_COL_RECIPIENT));
                long time = cursor.getInt(cursor.getColumnIndex(SMS_COL_TIME));
                boolean isDelivered = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_DELIVERED)) == 1;
                boolean isRead = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_READ)) == 1;
                boolean isSpam = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_SPAM)) == 1;

                Message msg = new Message(id, sender, content, recipient, time, isDelivered, isRead, isSpam);
                sms.add(msg);
            }
            cursor.close();
            closeDB();
        }

        return sms;
    }

    public List<Message> getAllSmsForCertainNumber(String phoneNumber) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME_SMS + " WHERE " +
                SMS_COL_SENDER + " = '" + phoneNumber + "' OR " +
                SMS_COL_RECIPIENT + " = '" + phoneNumber +
                "' ORDER BY " + SMS_COL_TIME;

        return fetchFromDatabase(selectQuery);
    }

    private List<Message> fetchFromDatabase(String selectQuery) {
        List<Message> sms = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String sender = cursor.getString(cursor.getColumnIndex(SMS_COL_SENDER));
                String content = cursor.getString(cursor.getColumnIndex(SMS_COL_CONTENT));
                String recipient = cursor.getString(cursor.getColumnIndex(SMS_COL_RECIPIENT));
                long time = cursor.getInt(cursor.getColumnIndex(SMS_COL_TIME));
                boolean isDelivered = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_DELIVERED)) == 1;
                boolean isRead = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_READ)) == 1;
                boolean isSpam = cursor.getInt(cursor.getColumnIndex(SMS_COL_IS_SPAM)) == 1;

                Message msg = new Message(id, sender, content, recipient, time, isDelivered, isRead, isSpam);
                Log.i("fetchFromDatabase", msg.toString());
                sms.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDB();

        return sms;
    }

    /**
     * Delete a message from sms table.
     *
     * @param id is the id of the message to be deleted.
     * @return the id of the message that is deleted.
     * Return -1 is deletion fails.
     */
    public long deleteSms(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ?";
        return db.delete(TABLE_NAME_SMS, selection, new String[]{String.valueOf(id)});
    }

    public void deleteSmsThread(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = SMS_COL_SENDER + " = ? OR " + SMS_COL_RECIPIENT + " = ?";
        db.delete(TABLE_NAME_SMS, selection, new String[]{phoneNumber, phoneNumber});
    }

    public long markSmsNotSpam(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SMS_COL_IS_SPAM, false);

        String selection = COL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.update(TABLE_NAME_SMS, values, selection, selectionArgs);
    }

    public void moveSmsToBlocked(long id) {

    }

    // Phone table operations
    // Check if the phone table has the given number or not
    public boolean containsPhone(String phoneNumber) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME_PHONE + " WHERE " +
                PHONE_COL_NUMBER + " = '" + phoneNumber + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.i(TAG, String.valueOf(cursor.getCount()));
        boolean res = cursor.getCount() != 0;
        cursor.close();
        closeDB();
        return res;
    }

    public long insertPhone(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PHONE_COL_NUMBER, phoneNumber);

        return db.insert(TABLE_NAME_PHONE, null, values);
    }

    public List<String> getPhone() {
        List<String> numbers = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_PHONE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String number = cursor.getString(cursor.getColumnIndex(PHONE_COL_NUMBER));
                numbers.add(number);
                Log.i("getPhone", number);
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDB();

        return numbers;
    }

    /**
     * If there is no contactId associated with phoneNumber,
     * getInt() will return 0. (contact_id column is null)
     * <p>
     * If it returns -1, it means there is no such number in phone table.
     *
     * @param phoneNumber
     * @return
     */
    public long getContactIdFromPhoneTable(String phoneNumber) {
        long contactId = -1;

        String selectQuery = "SELECT DISTINCT " + PHONE_COL_CONTACT_ID + " FROM " +
                TABLE_NAME_PHONE + " WHERE " + PHONE_COL_NUMBER + " = '" + phoneNumber + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            // If there is no contactId associated with phoneNumber,
            // getInt() will return 0. (contact_id column is null)
            contactId = cursor.getInt(cursor.getColumnIndex(PHONE_COL_CONTACT_ID));
        }
        cursor.close();
        closeDB();
        return contactId;
    }

    public long updatePhone(String phoneNumber, long contactId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PHONE_COL_CONTACT_ID, contactId);

        String selection = PHONE_COL_NUMBER + " = ?";
        String[] selectionArgs = {phoneNumber};

        return db.update(TABLE_NAME_PHONE, values, selection, selectionArgs);
    }

    public Set<String> getBlockedPhone() {
        Set<String> blockedPhone = new HashSet<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_PHONE + " WHERE " +
                PHONE_COL_CONTACT_ID + " IN (SELECT " + COL_ID + " FROM " +
                TABLE_NAME_CONTACT + " WHERE " + CONTACT_COL_IS_ALLOWED + " = 0)";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String number = cursor.getString(cursor.getColumnIndex(PHONE_COL_NUMBER));
                blockedPhone.add(number);
                Log.i("get blocked Phone", number);
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDB();
        return blockedPhone;
    }

    public long deletePhone(long id) {
        return 1;
    }

    // Before delete a contact, we need to delete the phone numbers
    // of the contact first.
    public long deletePhoneOfContact(long contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = PHONE_COL_CONTACT_ID + " = ?";
        return db.delete(TABLE_NAME_PHONE, selection, new String[]{String.valueOf(contactId)});
    }

    // Table contact operations
    public boolean containsContact(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME_CONTACT + " WHERE " +
                COL_ID + " = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.i(TAG, String.valueOf(cursor.getCount()));
        boolean res = cursor.getCount() != 0;
        cursor.close();
        closeDB();
        return res;
    }

    public List<Contact> getAllowedContact() {
        List<Contact> allowedContact = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
//        String[] projection = {CONTACT_COL_NAME};
        String selection = CONTACT_COL_IS_ALLOWED + " = ?";
        String[] selectionArgs = {String.valueOf(1)};
        Cursor cursor = db.query(
                TABLE_NAME_CONTACT,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(CONTACT_COL_NAME));
                Contact contact = new Contact(id, name, true);
                allowedContact.add(contact);
                Log.i("get Allowed contact", contact.getName());
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDB();

        return allowedContact;
    }

    public List<Contact> getBlockedContact() {
        List<Contact> blockedContact = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
//        String[] projection = {CONTACT_COL_NAME};
        String selection = CONTACT_COL_IS_ALLOWED + " = ?";
        String[] selectionArgs = {String.valueOf(0)};
        Cursor cursor = db.query(
                TABLE_NAME_CONTACT,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(CONTACT_COL_NAME));
                Contact contact = new Contact(id, name, true);
                blockedContact.add(contact);
                Log.i("get blocked contact", contact.getName());
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDB();

        return blockedContact;
    }

    public void updateContact(long id, String name, boolean isAllowed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_COL_NAME, name);
        values.put(CONTACT_COL_IS_ALLOWED, isAllowed);

        db.update(TABLE_NAME_CONTACT, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Block or unblock contact.
     * If isAllowed is true, we add the contact to allow list.
     * If isAllowed is false, we add the contact to black list.
     *
     * @param id
     * @param isAllowed
     * @return
     */
    public long isAllowContact(long id, boolean isAllowed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_COL_IS_ALLOWED, isAllowed);

        return db.update(TABLE_NAME_CONTACT, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long insertContact(String name, boolean isAllowed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_COL_NAME, name);
        values.put(CONTACT_COL_IS_ALLOWED, isAllowed);

        return db.insert(TABLE_NAME_CONTACT, null, values);
    }

    public long deleteContact(long id) {
        // Need to delete the phone numbers of this contact first
        // because contact_id is a foreign key.
        deletePhoneOfContact(id);
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ?";
        return db.delete(TABLE_NAME_CONTACT, selection, new String[]{String.valueOf(id)});
    }

    // If phone's contact_id column is not null, get the name from contact
    // Otherwise, show the number.
    public String getNameFromContact(String phoneNumber) {
        String name = phoneNumber;

        String selectQuery = "SELECT * FROM " + TABLE_NAME_PHONE + " WHERE " +
                PHONE_COL_NUMBER + " = '" + phoneNumber + "' LIMIT 1";

        long id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(PHONE_COL_CONTACT_ID));
        }
        // id == -1 means it does not contain this phone number
        // id == 0 means there is no contact id associated with this phone
        if (id != -1 && id != 0) {
            cursor.close();
            closeDB();
            String query = "SELECT *  FROM " + TABLE_NAME_CONTACT +
                    " WHERE " + COL_ID + " = " + id;
            db = this.getReadableDatabase();
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(CONTACT_COL_NAME));
            }
        }

        cursor.close();
        closeDB();
        return name;
    }

    // close database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
