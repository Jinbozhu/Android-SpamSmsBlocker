package com.example.feeling.spamtextblocker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.models.Message;

/**
 * Created by feeling on 3/1/16.
 */
public class ComposeSmsActivity extends Activity {
    public static final String TAG = "ComposeSmsActivity";
    static DatabaseHelper dbHelper;
    Button sendSmsButton;
    EditText phoneNoText;
    EditText messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        dbHelper = new DatabaseHelper(this);

        sendSmsButton = (Button) findViewById(R.id.sendSmsButton);
        phoneNoText = (EditText) findViewById(R.id.editTextPhoneNo);
        messageText = (EditText) findViewById(R.id.editTextSMS);
    }

    public void send(View v) {
        sendSms();
        // TODO: start new intent
    }
    public void sendSms() {
        final String contactNumber = phoneNoText.getText().toString();
        final String sms = messageText.getText().toString();

        try {
//            String SMS_SENT = "SMS_SENT";
//            String SMS_DELIVERED = "SMS_DELIVERED";
//
//            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
//            PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);
//
//            // For when the SMS has been sent
//            registerReceiver(new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    switch (getResultCode()) {
//                        case Activity.RESULT_OK:
//                            Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
//                            break;
//                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                            Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
//                            break;
//                        case SmsManager.RESULT_ERROR_NO_SERVICE:
//                            Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
//                            break;
//                        case SmsManager.RESULT_ERROR_NULL_PDU:
//                            Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
//                            break;
//                        case SmsManager.RESULT_ERROR_RADIO_OFF:
//                            Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                }
//            }, new IntentFilter(SMS_SENT));
//
//            // For when the SMS has been delivered
//            registerReceiver(new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    switch (getResultCode()) {
//                        case Activity.RESULT_OK:
//                            Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
//
//                            break;
//                        case Activity.RESULT_CANCELED:
//                            Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                }
//            }, new IntentFilter(SMS_DELIVERED));

//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(contactNumber, null, sms, sentPendingIntent, deliveredPendingIntent);SmsManager smsManager = SmsManager.getDefault();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contactNumber, null, sms, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis();
        Message message = new Message(0, "ME", sms, contactNumber, time, false, true, false);

        // Insert operation returns the id of the inserted row.
        // If it fails, it will return -1.
        long id = dbHelper.insertSms(message);

        // Assign the id in the database to the "id" field
        // so that when I want to delete a message, I can
        // find it using the id of the message.
        message.setId(id);

        // Update message list in conversation thread
        // and in chat room simultaneously
        ReceiveSmsActivity.convArrayList.add(0, message);
        ReceiveSmsActivity.convAdapter.notifyDataSetChanged();
        ChatActivity.chatArrayList.add(message);
        ChatActivity.chatAdapter.notifyDataSetChanged();

        if (!dbHelper.containsPhone(contactNumber)) {
            Log.i(TAG, "in ComposeSmsActivity");
            dbHelper.insertPhone(contactNumber);
        }
        dbHelper.closeDB();         // need to close DB
    }

    public void goToInbox(View v) {
        Intent intent = new Intent(ComposeSmsActivity.this, ReceiveSmsActivity.class);
        startActivity(intent);
    }
}
