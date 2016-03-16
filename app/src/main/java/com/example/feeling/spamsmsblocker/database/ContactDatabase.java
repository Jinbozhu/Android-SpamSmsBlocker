package com.example.feeling.spamsmsblocker.database;

import android.content.ContentValues;
import android.content.Context;

import com.example.feeling.spamsmsblocker.models.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/5/16.
 */
public class ContactDatabase extends DatabaseHelper {
    public static final String TAG = "ContactDatabase";
    public static final String TABLE_NAME = "contacts";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NUMBER = "number";
    public static final String COL_IS_ALLOWED = "isAllowed";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COL_NAME + " TEXT, " +
                    COL_NUMBER + " TEXT, " +
                    COL_IS_ALLOWED + " INTEGER" + ")";

    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ContactDatabase(Context context) {
        super(context);
    }

    public long insertContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact == null");
        }

        ContentValues values = new ContentValues();
        values.put(COL_NAME, contact.getName());
        // When we insert a contact, there is only one number for the contact
//        values.put(COL_NUMBER, contact.getNumbers().get(0));
        values.put(COL_IS_ALLOWED, contact.isAllowed());

//        return insert(values);
        return 1;
    }

    public void updateContact() {

    }

    public List<Contact> getBlockedContact() {
        List<Contact> blockedContact = new ArrayList<>();

        return blockedContact;
    }
}
