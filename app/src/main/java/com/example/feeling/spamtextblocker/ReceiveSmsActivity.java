package com.example.feeling.spamtextblocker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 */
public class ReceiveSmsActivity extends Activity implements AdapterView.OnClickListener {
    private static ReceiveSmsActivity instance;
    public static List<Message> smsMessageList;
    ListView smsListView;
    public static ArrayAdapter arrayAdapter;

    public static ReceiveSmsActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_sms);

        smsMessageList = new ArrayList<>();
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new MyAdapter(this, R.layout.conversation_list_element, smsMessageList);
        smsListView.setAdapter(arrayAdapter);

//        smsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object listItem = smsListView.getItemAtPosition(position);
//            }
//        });

        refreshSmsInbox();
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);

        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexTime = smsInboxCursor.getColumnIndex("date");
//        String date = smsInboxCursor.getString(indexTime);
//        Long timestamp = Long.parseLong(date);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timestamp);
//        Date finalDate = calendar.getTime();

//        Date date = new Date(timeMillis);
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
//        String dateText = format.format(date);


        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;

        smsMessageList.clear();
        do {
            Message msg = new Message(
                    smsInboxCursor.getString(indexBody),
                    smsInboxCursor.getString(indexAddress),
                    "Me",
                    smsInboxCursor.getLong(indexTime),
                    true,
                    false,
                    false
            );
            smsMessageList.add(msg);
        } while (smsInboxCursor.moveToNext());
        arrayAdapter.notifyDataSetChanged();
    }

    public void updateList(final Message msg) {
        smsMessageList.add(msg);
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
//            Message msg = smsMessageList.get(position);
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
