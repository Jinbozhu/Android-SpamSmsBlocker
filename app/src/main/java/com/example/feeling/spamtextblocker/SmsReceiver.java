package com.example.feeling.spamtextblocker;

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
    //    SmsDatabase smsDatabase = new SmsDatabase(mContext);
    DatabaseHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

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
        sqLiteDatabase = dbHelper.getWritableDatabase();

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
                            content,
                            sender,
                            "ME",
                            timeMillis,
                            true,
                            false,
                            false
                    );

                    insertSmsToDataBase(context, sender, content, "ME", timeMillis, true, false, false);
                    saveMsgToSystem(context, sender, content, timeMillis);

                    // Update message list simutaenouly
                    ReceiveSmsActivity.smsMessageList.add(0, message);
                    ReceiveSmsActivity.arrayAdapter.notifyDataSetChanged();
                }

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                Log.v(TAG, msg);

                // Get instance of Vibrator from current Context
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 300 milliseconds
                v.vibrate(300);
            }


            // For debug purpose.
//            String map_tag = "-------Map Tag";
//            for (Map.Entry<String, Conversation> entry : dataProvider.getConversationMap().entrySet()) {
//                Log.v(map_tag, "" + entry.getValue());
//            }
        } catch (Exception e) {
            Log.e("SMS", "Exception: " + e);
        }

//        notify(sender, content);
    }

    // Modified from https://www.youtube.com/watch?v=g4_1UOFNLEY
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

    private boolean insertSmsToDataBase(Context context, String sender, String content, String recipient,
                                     long time, boolean isDelivered, boolean isRead, boolean isSpam) {
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("content", content);
        values.put("recipient", recipient);
        values.put("time", time);
        values.put("isDelivered", isDelivered);
        values.put("isRead", isRead);
        values.put("isSpam", isSpam);
        long res = sqLiteDatabase.insert(SmsDatabase.TABLE_NAME, null, values);
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
