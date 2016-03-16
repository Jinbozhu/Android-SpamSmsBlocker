package com.example.feeling.spamsmsblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.feeling.spamsmsblocker.adapters.ConversationAdapter;
import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 * <p/>
 * References:
 * http://blog.teamtreehouse.com/add-navigation-drawer-android
 */
public class BlockedSmsActivity extends AppCompatActivity {
    public static final String TAG = "BlockedSmsActivity";

    DatabaseHelper dbHelper;

    public static List<Message> blockedArrayList;
    public static ArrayAdapter blockedAdapter;
    private ListView blockedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Blocked messages");
        setContentView(R.layout.activity_listview);

        dbHelper = new DatabaseHelper(this);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        blockedArrayList = new ArrayList<>();
        blockedListView = (ListView) findViewById(R.id.listView);
        blockedAdapter = new ConversationAdapter(this, R.layout.conversation_list_element, blockedArrayList);
        blockedListView.setAdapter(blockedAdapter);
        blockedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BlockedSmsActivity.this, ChatActivity.class);
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

        registerForContextMenu(blockedListView);

        loadSmsFromDatabase();
    }

    public void loadSmsFromDatabase() {
        blockedArrayList.clear();
        List<Message> allSms = dbHelper.getLastBlockedSmsForCertainNumber();

        for (int i = allSms.size() - 1; i >= 0; i--) {
            blockedArrayList.add(allSms.get(i));
        }
        blockedAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blocked, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.delete_all) {
            deleteAll();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_all)
                .setMessage("All messages will be deleted.")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processDeleteAll();
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

    private void processDeleteAll() {
        dbHelper.clearBlockedSms();
        Log.i(TAG, "All messages deleted.");
        Toast.makeText(this, "All blocked messages deleted.", Toast.LENGTH_SHORT).show();

        blockedArrayList.clear();
        blockedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blocked_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.delete_message:
                deleteMessage(index);
                return true;
            case R.id.not_spam:
                restoreToInbox(index);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Modified from
     * https://github.com/commonsguy/cw-android/tree/master/Database/Constants/src/com/commonsware/android/constants
     */
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
        long id = blockedArrayList.get(index).getId();
        long deleteRes = dbHelper.deleteSms(id);
        if (deleteRes == -1) {
            Log.i(TAG, "Delete failed.");
        } else {
            Log.i(TAG, "Delete successful.");
            blockedArrayList.remove(index);
            blockedAdapter.notifyDataSetChanged();
        }
    }

    private void restoreToInbox(final int index) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.not_spam)
                .setMessage("Message will be moved to inbox.")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processRestore(index);
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

    private void processRestore(int index) {
        Message msg = blockedArrayList.get(index);
        long id = msg.getId();
        long updateId = dbHelper.markSmsNotSpam(id);
        if (updateId == -1) {
            Log.i(TAG, "Restore failed.");
            Toast.makeText(this, "Restore failed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Restore successful.");
            Toast.makeText(this, "Restore failed.", Toast.LENGTH_SHORT).show();

            blockedArrayList.remove(index);
            blockedAdapter.notifyDataSetChanged();
        }
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
//        blockedArrayList.clear();
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
//            blockedArrayList.add(msg);
//        } while (smsInboxCursor.moveToNext());
//        blockedAdapter.notifyDataSetChanged();
//    }

    public void updateList(final Message msg) {
        blockedArrayList.add(msg);
        blockedAdapter.notifyDataSetChanged();
        Log.v("------ReceiveActivity", "In updateList() method.");
    }

    public void goToCompose(View v) {
        Intent intent = new Intent(BlockedSmsActivity.this, ComposeSmsActivity.class);
        startActivity(intent);
    }
}
