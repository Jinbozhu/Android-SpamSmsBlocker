package com.example.feeling.smstest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by feeling on 2/29/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    final String TAG = "--------SmsReceiver";

    Context mContext;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        String address = "";
        String content = "";
        String time = "";

        try {
            if (bundle != null) {
                Object[] sms = (Object[]) bundle.get("pdus");
                String msg = "";
                Message message = null;

                for (Object currentObj : sms) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) currentObj);
                    content = currentMessage.getDisplayMessageBody();
                    address = currentMessage.getDisplayOriginatingAddress();
                    long timeMills = currentMessage.getTimestampMillis();
                    Date date = new Date(timeMills);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                    String dateText = format.format(date);

                    msg += address + " at " + "\t" + dateText + "\n" + content + "\n";
                    message = new Message(
                            currentMessage.getDisplayMessageBody(),
                            currentMessage.getDisplayOriginatingAddress(),
                            "ME",
                            timeMills,
                            false,
                            false
                    );
//                    dataProvider.addMessage(message);
//                    Log.v(TAG, message.toString());
                }

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                ReceiveSmsActivity inst = ReceiveSmsActivity.instance();
                inst.updateList(message);
            }

//            MainActivity m = new MainActivity();
//            m.loadLatestConversation(dataProvider.getConversationMap());

            // For debug purpose.
//            String map_tag = "-------Map Tag";
//            for (Map.Entry<String, Conversation> entry : dataProvider.getConversationMap().entrySet()) {
//                Log.v(map_tag, "" + entry.getValue());
//            }

        } catch (Exception e) {
            Log.e("SMS", "Exception: " + e);
        }

//        notify(address, content);
    }

    public void notify(String address, String text){
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(address)
                        .setContentText(text)
                        .setContentIntent(pIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("rishab", "notify ");
        mNotificationManager.notify(0, mBuilder.build());
    }
}
