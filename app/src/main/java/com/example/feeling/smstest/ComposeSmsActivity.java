package com.example.feeling.smstest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by feeling on 3/1/16.
 */
public class ComposeSmsActivity extends Activity {
    Button sendSmsButton;
    EditText toPhoneNumber;
    EditText smsMessageET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        sendSmsButton = (Button) findViewById(R.id.sendSmsButton);
        toPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNo);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });
    }

    private void sendSms() {
        String toPhone = toPhoneNumber.getText().toString();
        String smsMessage = smsMessageET.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(toPhone, null, smsMessage, null, null);

            Toast.makeText(this, "SMS sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInbox(View v) {
        Intent intent = new Intent(ComposeSmsActivity.this, ReceiveSmsActivity.class);
        startActivity(intent);
    }
}
