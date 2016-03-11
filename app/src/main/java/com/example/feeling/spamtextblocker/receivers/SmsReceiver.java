package com.example.feeling.spamtextblocker.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.feeling.spamtextblocker.ChatActivity;
import com.example.feeling.spamtextblocker.ReceiveSmsActivity;
import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.database.SmsDatabase;
import com.example.feeling.spamtextblocker.models.Contact;
import com.example.feeling.spamtextblocker.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by feeling on 2/29/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    final String TAG = "--------SmsReceiver";
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    List<String> blockList;
    List<String> allowList;

    public SmsReceiver() {
        blockList = new ArrayList<>();
        allowList = new ArrayList<>();
    }

    private void loadBlockListFromDataBase(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        loadBlockListFromDataBase(context);
        loadAllowListFromPhone(context);

        Bundle bundle = intent.getExtras();

        String sender = "";
        String content = "";
        long timeMillis = 0;
        String time = "";

        try {
            if (bundle != null) {
                Object[] sms = (Object[]) bundle.get("pdus");
                String msg = "";
                Message message = null;

                for (Object currentObj : sms) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) currentObj);
                    content = currentMessage.getDisplayMessageBody();
                    sender = currentMessage.getDisplayOriginatingAddress();
                    timeMillis = currentMessage.getTimestampMillis();

                    Date date = new Date(timeMillis);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                    String dateText = format.format(date);

                    msg += sender + " at " + "\t" + dateText + "\n" + content + "\n";

                    message = new Message(
                            0,              // temp id
                            sender,
                            content,
                            "ME",
                            timeMillis,
                            true,
                            false,
                            false
                    );

//                    insertSmsToDataBase(context, message);

//                    SQLiteDatabase db = dbHelper.getWritableDatabase();
//                    dbHelper.onUpgrade(db, 1,2);

                    // Insert operation returns the id of the inserted row.
                    // If it fails, it will return -1.
                    long id = dbHelper.insertSms(message);
                    if (id != -1) {
                        Toast.makeText(context, "Data is inserted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Insert failed.", Toast.LENGTH_SHORT).show();
                    }

                    // Assign the id in the database to the "id" field
                    // so that when I want to delete a message, I can
                    // find it using the id of the message.
                    message.setId(id);
                    Toast.makeText(context, String.valueOf(message.getId()), Toast.LENGTH_SHORT).show();

                    saveMsgToSystem(context, sender, content, timeMillis);

                    // Update message list in conversation thread
                    // and in chat room simultaneously
                    ReceiveSmsActivity.convArrayList.add(0, message);
                    ReceiveSmsActivity.convAdapter.notifyDataSetChanged();
                    ChatActivity.chatArrayList.add(message);
                    ChatActivity.chatAdapter.notifyDataSetChanged();

                    if (!dbHelper.containsPhone(sender)) {
                        Log.i(TAG, "in smsReceiver containsphone");
                        dbHelper.insertPhone(sender);
                    }
                    dbHelper.closeDB();         // need to close DB
                }

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                Log.v(TAG, msg);

                // Get instance of Vibrator from current Context
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 300 milliseconds
                v.vibrate(300);
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception: " + e);
        }

//        notify(sender, content);
    }

    // References: https://www.youtube.com/watch?v=g4_1UOFNLEY
    // http://www.higherpass.com/android/tutorials/working-with-android-contacts/
    private void loadAllowListFromPhone(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) return;

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
            if (phoneCursor == null || phoneCursor.getCount() <= 0) return;
            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.i("phoneNumber in contacts", phoneNumber);
                allowList.add(phoneNumber);
            }
            phoneCursor.close();
        }

        cursor.close();
    }

    private boolean insertSmsToDataBase(Context context, Message message) {
        ContentValues values = new ContentValues();
        values.put("sender", message.getSender());
        values.put("content", message.getContent());
        values.put("recipient", message.getRecipient());
        values.put("time", message.getTime());
        values.put("isDelivered", message.isDelivered());
        values.put("isRead", message.isRead());
        values.put("isSpam", message.isSpam());

        long res = db.insert(SmsDatabase.TABLE_NAME, null, values);
        boolean flag = res != -1;
        if (flag) {
            Toast.makeText(context, "Data is inserted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Insert failed.", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    // Giving me "nullPointerException when call getPackageName()"
//    public void notify(String sender, String text){
//        Intent intent = new Intent(mContext, MainActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(mContext)
//                        .setContentTitle(sender)
//                        .setContentText(text)
//                        .setContentIntent(pIntent);
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        Log.i("rishab", "notify ");
//        mNotificationManager.notify(0, mBuilder.build());
//    }

    /**
     * Write to content://sms/sent works. Even though I want to
     * write to content://sms/inbox, the message goes to sent box.
     * <p/>
     * In ReceiveSmsActivity, if query from content://sms/inbox,
     * I cannot get the latest messages received. But if query from
     * content://sms/sent, I have access to those newly arrived messages.
     * <p/>
     * But if I write to and query from both "content://sms",
     * I'll get all messages from the listView.
     * <p/>
     * If I don't call this method, the message will not be stored
     * in the phone, thus not visible in the listView.
     *
     * @param context
     * @param phone
     * @param msg
     * @param timeMillis
     */
    public static void saveMsgToSystem(Context context, String phone, String msg, long timeMillis) {
        ContentValues values = new ContentValues();
        values.put("date", timeMillis);
        values.put("read", 0);
        // 1 for receive, 2 for send  
        values.put("type", 2);
        values.put("address", phone);
        values.put("body", msg);
        context.getContentResolver().insert(Uri.parse("content://sms"), values);
    }
}
