package com.example.feeling.spamtextblocker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.feeling.spamtextblocker.adapters.ChatAdapter;
import com.example.feeling.spamtextblocker.adapters.ConversationAdapter;
import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/9/16.
 */
public class ChatActivity extends AppCompatActivity {
    public static final String TAG = "ChatActivity";

    public static ChatAdapter chatAdapter;
    public static ArrayList<Message> chatArrayList;
    ListView myListView;
    EditText chatBox;
    Button sendButton;

    DatabaseHelper dbHelper;
    String contactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        contactNumber = intent.getStringExtra("contactNumber");
        setTitle(contactNumber);
        setContentView(R.layout.activity_chat);

        dbHelper = new DatabaseHelper(this);

        chatArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getApplicationContext(),
                R.layout.chat_list_elemnt, chatArrayList);

        myListView = (ListView) findViewById(R.id.listViewChat);
        myListView.setAdapter(chatAdapter);

        sendButton = (Button) findViewById(R.id.sendButtonChat);
        sendButton.setEnabled(false);
        chatBox = (EditText) findViewById(R.id.editTextChat);
        chatBox.addTextChangedListener(new TextWatcher() {
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

        loadSms(contactNumber);

    }

    private void loadSms(String contactNumber) {
        chatArrayList.clear();
        List<Message> sms = dbHelper.getAllSmsForCertainNumber(contactNumber);
        for (Message msg : sms) {
            Log.i("loadSms in chat", msg.toString());
            chatArrayList.add(msg);
        }
        chatAdapter.notifyDataSetChanged();
    }

    /**
     * Check the length of message in the chat box.
     * If the length is not 0, enable send button.
     */
    private void checkMessageLength() {
        if (chatBox.getText().toString().length() > 0) {
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    /**
     * This function is called when send button is clicked.
     * After call send(), it will clear the message in chat box.
     *
     * @param v
     */
    public void send(View v) {
        send();
        clearChatBox();
    }

    private void send() {
        String sms = chatBox.getText().toString();
        try {
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
            Log.i(TAG, "in ChatActivity");
            dbHelper.insertPhone(contactNumber);
        }
        dbHelper.closeDB();         // need to close DB
//        long time = System.currentTimeMillis();

//        /**
//         * Add the posted message to chatArrayList, and call notifyDataSetChanged()
//         * on ArrayAdapter, then it will be shown on the screen immediately.
//         */
//        chatArrayList.add(new Message(0, "ME", message, contactNumber, time, true, true, false));
//        chatAdapter.notifyDataSetChanged();
    }

    /**
     * Clear chat box when send button is clicked.
     */
    private void clearChatBox() {
        chatBox.getText().clear();
    }
}
