package com.example.feeling.spamtextblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
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
    ListView chatListView;
    EditText chatBox;
    Button sendButton;

    DatabaseHelper dbHelper;
    String contactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        contactNumber = intent.getStringExtra("contactNumber");
        dbHelper = new DatabaseHelper(this);
        String name = dbHelper.getNameFromContact(contactNumber);
        setTitle(name);
        setContentView(R.layout.activity_chat);


        chatArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getApplicationContext(),
                R.layout.chat_list_elemnt, chatArrayList);

        chatListView = (ListView) findViewById(R.id.listViewChat);
        chatListView.setAdapter(chatAdapter);
        registerForContextMenu(chatListView);

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.copy:
                copy(index);
                return true;
            case R.id.delete:
                deleteMessage(index);
                return true;
            case R.id.create_contact:
                addContact(index);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void copy(int index) {

    }

    // Modified from
    // https://github.com/commonsguy/cw-android/tree/master/Database/Constants/src/com/commonsware/android/constants
    private void deleteMessage(final int index) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("Message will be deleted.")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processDelete(index);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // ignore, just dismiss
                            }
                        })
                .show();
    }

    private void processDelete(int index) {
        long id = chatArrayList.get(index).getId();
        long deleteRes = dbHelper.deleteSms(id);
        if (deleteRes == -1) {
            Log.i(TAG, "Delete failed.");
        } else {
            Log.i(TAG, "Delete successful.");
        }
        chatArrayList.remove(index);
        chatAdapter.notifyDataSetChanged();
    }

    private void addContact(final int index) {
        Message msg = chatArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            Toast.makeText(this, "Your number already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        long contactId = dbHelper.getContactIdFromPhoneTable(phoneNumber);
        // If there is no contactId associated to phoneNumber, the
        // return value is 0.
        if (contactId != -1 && contactId != 0) {
            Toast.makeText(this, "Contact already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.create_contact_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to AlertDialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText nameInput = (EditText) promptsView.findViewById(R.id.editTextName);
        final EditText phoneInput = (EditText) promptsView.findViewById(R.id.editTextPhone);
        phoneInput.setText(phoneNumber);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.create_contact)
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // get user input and set it to result
                                // edit text
                                String contactName = nameInput.getText().toString();
                                long insertId = dbHelper.insertContact(contactName, true);
                                if (insertId == -1) {
                                    Log.i(TAG, "insert contact failed.");
                                } else {
                                    Log.i(TAG, "insert contact successful.");
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void processAddContact(int index) {

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
