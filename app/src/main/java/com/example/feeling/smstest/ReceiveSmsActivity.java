package com.example.feeling.smstest;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 */
public class ReceiveSmsActivity extends Activity implements AdapterView.OnClickListener {
    private static ReceiveSmsActivity inst;
    List<Message> smsMessageList;
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static ReceiveSmsActivity instance() {
        return inst;
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
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        Date date = new Date(timeMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        String dateText = format.format(date);

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            Message msg = new Message(
                    smsInboxCursor.getString(indexBody),
                    smsInboxCursor.getString(indexAddress),
                    "Me",
                    dateText,
                    false,
                    false
            );
            smsMessageList.add(msg);
        } while (smsInboxCursor.moveToNext());
        arrayAdapter.notifyDataSetChanged();
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

    }
}
