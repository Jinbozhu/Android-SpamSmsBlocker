package com.example.feeling.spamtextblocker;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feeling.spamtextblocker.adapters.ConversationAdapter;
import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 * <p/>
 * References:
 * http://blog.teamtreehouse.com/add-navigation-drawer-android
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnClickListener {
    public static final String TAG = "MainActivity";

    DatabaseHelper dbHelper;

    private ListView drawerListView;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    public static List<Message> convArrayList;
    public static ArrayAdapter convAdapter;
    private ListView convListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTitle("Message");
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        drawerListView = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        convArrayList = new ArrayList<>();
        convListView = (ListView) findViewById(R.id.SMSList);
        convAdapter = new ConversationAdapter(this, R.layout.conversation_list_element, convArrayList);
        convListView.setAdapter(convAdapter);
        convListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
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

        registerForContextMenu(convListView);

        loadSmsFromDatabase();
    }

    private void addDrawerItems() {
        String[] drawerArray = {"Allow list", "Black list", "Blocked messages", "Settings"};
        drawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerArray);
        drawerListView.setAdapter(drawerAdapter);

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, AllowlistActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        final String myPackageName = getPackageName();
        TextView notDefaultApp = (TextView) findViewById(R.id.notDefaultApp);
        Button setDefaultApp = (Button) findViewById(R.id.setDefaultApp);

        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            notDefaultApp.setVisibility(View.VISIBLE);
            setDefaultApp.setVisibility(View.VISIBLE);

            // Set up a button that allows the user to change the default SMS app
            setDefaultApp.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent =
                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            myPackageName);
                    startActivity(intent);
                }
            });
        } else {
            // App is the default.
            // Hide the "not currently set as the default SMS app" interface
            notDefaultApp.setVisibility(View.GONE);
            setDefaultApp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conv_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.delete_thread:
                deleteThread(index);
                return true;
            case R.id.create_contact:
                createContact(index);
                return true;
            case R.id.add_to_blacklist:
                addToBlacklist(index);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteThread(int index) {
        Message msg = convArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = msg.getRecipient();
        }

        dbHelper.deleteSmsThread(phoneNumber);

        convArrayList.remove(index);
        convAdapter.notifyDataSetChanged();
    }

    private void createContact(int index) {
        Message msg = convArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = msg.getRecipient();
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
                                String phoneNumber2 = phoneInput.getText().toString();
                                long insertId = dbHelper.insertContact(contactName, true);
                                if (insertId == -1) {
                                    Log.i(TAG, "insert contact failed.");
                                } else {
                                    Log.i(TAG, "insert contact successful.");
                                    // Update phone table's contact_id column
                                    long id = dbHelper.updatePhone(phoneNumber2, insertId);
                                    if (id == -1) {
                                        Log.i(TAG, "update phone failed.");
                                    } else {
                                        Log.i(TAG, "update phone successful.");
                                    }
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
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

    private void addToBlacklist(int index) {
        Message msg = convArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = msg.getRecipient();
        }

        long contactId = dbHelper.getContactIdFromPhoneTable(phoneNumber);
        if (contactId == 0) {
            // This number is not in the contact book, insert it
            // and mark it as blocked.
            long insertId = dbHelper.insertContact(phoneNumber, false);
            // Every time a contact is inserted, update the phone table.
            dbHelper.updatePhone(phoneNumber, insertId);
        } else if (contactId == -1) {
            Toast.makeText(this, "The phone number is not in the table.", Toast.LENGTH_SHORT).show();
        } else {
            // Block the contact.
            long resId = dbHelper.blockContact(contactId);
            if (resId == -1) {
                Log.i(TAG, "add to blacklist failed.");
            } else {
                Log.i(TAG, "added to blacklist.");
//            dbHelper.moveSmsToBlocked(contactId);
            }
        }
    }

    public void loadSmsFromDatabase() {
        convArrayList.clear();
        List<Message> allSms = dbHelper.getLastSmsForCertainNumber();

        for (int i = allSms.size() - 1; i >= 0; i--) {
            convArrayList.add(allSms.get(i));
        }
        convAdapter.notifyDataSetChanged();
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
//        convArrayList.clear();
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
//            convArrayList.add(msg);
//        } while (smsInboxCursor.moveToNext());
//        convAdapter.notifyDataSetChanged();
//    }

    public void updateList(final Message msg) {
        convArrayList.add(msg);
        convAdapter.notifyDataSetChanged();
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
//            Message msg = convArrayList.get(position);
//            String content = msg.getContent();
//            String address = msg.getSender();
//
//        }
//    }
    public void goToCompose(View v) {
        Intent intent = new Intent(MainActivity.this, ComposeSmsActivity.class);
        startActivity(intent);
    }
}
