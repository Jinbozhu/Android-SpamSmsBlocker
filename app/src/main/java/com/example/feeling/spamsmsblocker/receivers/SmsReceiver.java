package com.example.feeling.spamsmsblocker.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.feeling.spamsmsblocker.ChatActivity;
import com.example.feeling.spamsmsblocker.MainActivity;
import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by feeling on 2/29/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    final String TAG = "SmsReceiver";
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    Set<String> blackList;
    Set<String> allowList;

    public SmsReceiver() {
        blackList = new HashSet<>();
        allowList = new HashSet<>();
    }

    private void loadBlockListFromDataBase() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dbHelper = new DatabaseHelper(context);
        blackList = dbHelper.getBlockedPhone();
        db = dbHelper.getWritableDatabase();

//        loadBlockListFromDataBase(context);
//        loadAllowListFromPhone(context);

        Bundle bundle = intent.getExtras();

        String sender = "";
        String content = "";
        long timeMillis = 0;
        String time = "";

        try {
            if (bundle != null) {
                Object[] sms = (Object[]) bundle.get("pdus");
                String msg = "";
                Message message;

                for (Object currentObj : sms) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) currentObj);
                    content = currentMessage.getDisplayMessageBody();
                    sender = currentMessage.getDisplayOriginatingAddress();
                    timeMillis = currentMessage.getTimestampMillis();

                    boolean isSpam = blackList.contains(sender);
                    Log.i(TAG, "is spam: " + isSpam);

                    Date date = new Date(timeMillis);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                    String dateText = format.format(date);

                    msg += sender + " at " + dateText + "\n" + content + "\n";

                    message = new Message(0, sender, content, "ME", timeMillis, true, false, isSpam);

//                    insertSmsToDataBase(context, message);

                    // Insert operation returns the id of the inserted row.
                    // If it fails, it will return -1.
                    long id = dbHelper.insertSms(message);
                    if (id == -1) {
                        Log.i(TAG, "Insert sms to database failed.");
                        Toast.makeText(context, "Insert sms failed.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "Inserted sms to database.");
//                        Toast.makeText(context, "sms is inserted.", Toast.LENGTH_SHORT).show();
                    }

                    // Assign the id in the database to the "id" field
                    // so that when I want to delete a message, I can
                    // find it using the id of the message.
                    message.setId(id);
//                    Toast.makeText(context, String.valueOf(message.getId()), Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "msg Id: " + message.getId() + " " + message.toString());

//                    saveMsgToSystem(context, sender, content, timeMillis);

                    if (!isSpam) {
                        // TODO: need better update strategy
                        // Update message list in conversation thread
                        // and in chat room simultaneously
                        MainActivity.convArrayList.add(0, message);
                        MainActivity.convAdapter.notifyDataSetChanged();
                        ChatActivity.chatArrayList.add(message);
                        ChatActivity.chatAdapter.notifyDataSetChanged();

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                        // Get instance of Vibrator from current Context
                        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 300 milliseconds
                        v.vibrate(300);
                    }

                    if (!dbHelper.containsPhone(sender)) {
                        Log.i(TAG, "Write sender to phone table: " + sender);
                        dbHelper.insertPhone(sender);
                    }
                }

                dbHelper.closeDB();         // need to close DB
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception: " + e.getMessage());
            e.printStackTrace();
        }
//        notify(context, sender, content);
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

    // Giving me "nullPointerException when call getPackageName()"
    public void notify(Context mContext, String sender, String text){
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(sender)
                        .setContentText(text)
                        .setContentIntent(pIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "notify ");
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * Write to content://sms/sent works. Even though I want to
     * write to content://sms/inbox, the message goes to sent box.
     * <p/>
     * In MainActivity, if query from content://sms/inbox,
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
