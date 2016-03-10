package com.example.feeling.spamtextblocker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
    public static String LOG_TAG = "ChatActivity";

    public ChatAdapter chatAdapter;
    public List<Message> arrayList;
    ListView myListView;
    EditText chatBox;
    Button sendButton;

    DatabaseHelper dbHelper;
    String contactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        contactNumber = intent.getStringExtra("contactNumber");

        dbHelper = new DatabaseHelper(this);

        arrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, R.layout.chat_list_elemnt, arrayList);
        myListView = (ListView) findViewById(R.id.listViewChat);
        myListView.setAdapter(chatAdapter);

        sendButton = (Button) findViewById(R.id.sendButtonChat);
        sendButton.setEnabled(false);
        chatBox = (EditText) findViewById(R.id.editTextChat);
        chatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkMessageLength();
            }
        });

        loadSms(contactNumber);
    }

    private void loadSms(String contactNumber) {
        arrayList.clear();
        List<Message> sms = dbHelper.getAllSmsForCertainNumber(contactNumber);
        for (Message msg : sms) {
            Log.i("loadSms in chat", msg.toString());
            arrayList.add(msg);
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
        final String message = chatBox.getText().toString();
        long time = System.currentTimeMillis();

        /**
         * Add the posted message to arrayList, and call notifyDataSetChanged()
         * on ArrayAdapter, then it will be shown on the screen immediately.
         */
        arrayList.add(new Message(0, "ME", message, contactNumber, time, true, true, false));
        chatAdapter.notifyDataSetChanged();

    }

    /**
     * Clear chat box when send button is clicked.
     */
    private void clearChatBox() {
        chatBox.getText().clear();
    }
}
