package com.example.feeling.spamsmsblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Message;

/**
 * Created by feeling on 3/1/16.
 */
public class ComposeSmsActivity extends AppCompatActivity {
    public static final String TAG = "ComposeSmsActivity";
    public String title = "Compose";

    static DatabaseHelper dbHelper;
    ImageButton sendButton;
    EditText phoneNoText;
    EditText messageText;
    boolean phoneOK;
    boolean msgOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        setTitle(title);
        dbHelper = new DatabaseHelper(this);

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setEnabled(false);
        phoneNoText = (EditText) findViewById(R.id.editTextPhoneNo);
        phoneNoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkPhoneNoLength();
            }
        });
        messageText = (EditText) findViewById(R.id.editTextSMS);
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkMessageLength();
            }
        });
    }

    /**
     * Check the length of message in the chat box.
     * If the length is not 0, enable send button.
     */
    private void checkPhoneNoLength() {
        if (phoneNoText.getText().toString().length() > 0) {
            phoneOK = true;
            sendButton.setEnabled(msgOK);
            if (msgOK) {
                sendButton.setBackgroundResource(R.drawable.send_enabled);
            }
        } else {
            phoneOK = false;
            sendButton.setEnabled(false);
            sendButton.setBackgroundResource(R.drawable.send_disabled);
        }
    }

    /**
     * Check the length of message in the chat box.
     * If the length is not 0, enable send button.
     */
    private void checkMessageLength() {
        if (messageText.getText().toString().length() > 0) {
            msgOK = true;
            sendButton.setEnabled(phoneOK);
            if (phoneOK) {
                sendButton.setBackgroundResource(R.drawable.send_enabled);
            }
        } else {
            msgOK = false;
            sendButton.setEnabled(false);
            sendButton.setBackgroundResource(R.drawable.send_disabled);
        }
    }

    public void send(View v) {
        sendSms();
        String phoneNumber = phoneNoText.getText().toString();
        Intent intent = new Intent(ComposeSmsActivity.this, ChatActivity.class);
        intent.putExtra("contactNumber", phoneNumber);
        startActivity(intent);

        clearFields();
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
        // Call ChatActivity.chatArrayList throws NullPointerException.
        MainActivity.convArrayList.add(0, message);
        MainActivity.convAdapter.notifyDataSetChanged();
        ChatActivity.chatArrayList.add(message);
        ChatActivity.chatAdapter.notifyDataSetChanged();

        if (!dbHelper.containsPhone(contactNumber)) {
            Log.i(TAG, "insert phone number to phone table.");
            dbHelper.insertPhone(contactNumber);
        }
        dbHelper.closeDB();         // need to close DB
    }

    /**
     * Clear chat box when send button is clicked.
     */
    private void clearFields() {
        phoneNoText.getText().clear();
        messageText.getText().clear();
    }
}
