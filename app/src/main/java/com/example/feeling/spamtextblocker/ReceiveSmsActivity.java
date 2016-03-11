package com.example.feeling.spamtextblocker;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.feeling.spamtextblocker.adapters.ConversationAdapter;
import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 */
public class ReceiveSmsActivity extends Activity implements AdapterView.OnClickListener {
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    public static List<Message> smsList;
    public static ArrayAdapter arrayAdapter;
    ListView smsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_sms);

        dbHelper = new DatabaseHelper(this);

        smsList = new ArrayList<>();
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ConversationAdapter(this, R.layout.conversation_list_element, smsList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ReceiveSmsActivity.this, ChatActivity.class);
                // Get the message object at this position
                Message msg = (Message) parent.getItemAtPosition(position);
                String contactNumber = msg.getSender();
                if ("ME".equals(contactNumber)) {
                    contactNumber = msg.getRecipient();
                }
                intent.putExtra("contactNumber", contactNumber);
                startActivity(intent);
            }
        });

//        smsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object listItem = smsListView.getItemAtPosition(position);
//            }
//        });

//        refreshSmsInbox();
        loadSmsFromDatabase();
    }

    public void loadSmsFromDatabase() {
        smsList.clear();
        List<Message> allSms = dbHelper.getLastSmsForCertainNumber();

        for (int i = allSms.size() - 1; i >= 0; i--) {
            smsList.add(allSms.get(i));
        }
        arrayAdapter.notifyDataSetChanged();
    }

//    public void refreshSmsInbox() {
//        ContentResolver contentResolver = getContentResolver();
//        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
//
//        int indexBody = smsInboxCursor.getColumnIndex("body");
//        int indexAddress = smsInboxCursor.getColumnIndex("address");
//        int indexTime = smsInboxCursor.getColumnIndex("date");
////        String date = smsInboxCursor.getString(indexTime);
////        Long timestamp = Long.parseLong(date);
////        Calendar calendar = Calendar.getInstance();
////        calendar.setTimeInMillis(timestamp);
////        Date finalDate = calendar.getTime();
//
////        Date date = new Date(timeMillis);
////        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
////        String dateText = format.format(date);
//
//
//        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
//
//        smsList.clear();
//        do {
//            Message msg = new Message(
//                    smsInboxCursor.getString(indexBody),
//                    smsInboxCursor.getString(indexAddress),
//                    "Me",
//                    smsInboxCursor.getLong(indexTime),
//                    true,
//                    false,
//                    false
//            );
//            smsList.add(msg);
//        } while (smsInboxCursor.moveToNext());
//        arrayAdapter.notifyDataSetChanged();
//    }

    public void updateList(final Message msg) {
        smsList.add(msg);
        arrayAdapter.notifyDataSetChanged();
        Log.v("------ReceiveActivity", "In updateList() method.");
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * From https://www.youtube.com/watch?v=t4Szfni9luM
     * at 12:00, but not used
     */
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        try {
//            Message msg = smsList.get(position);
//            String content = msg.getContent();
//            String address = msg.getSender();
//
//        }
//    }

    public void goToCompose(View v) {
        Intent intent = new Intent(ReceiveSmsActivity.this, ComposeSmsActivity.class);
        startActivity(intent);
    }
}
