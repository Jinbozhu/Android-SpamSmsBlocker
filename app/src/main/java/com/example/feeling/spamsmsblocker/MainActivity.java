package com.example.feeling.spamsmsblocker;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.feeling.spamsmsblocker.adapters.ChatAdapter;
import com.example.feeling.spamsmsblocker.adapters.ConversationAdapter;
import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Message;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/3/16.
 * <p/>
 * References:
 * http://blog.teamtreehouse.com/add-navigation-drawer-android
 * https://github.com/makovkastar/FloatingActionButton
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnClickListener {
    public static final String TAG = "MainActivity";

    DatabaseHelper dbHelper;

    private ListView drawerListView;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String title = "Message";

    public static List<Message> convArrayList;
    public static ArrayAdapter convAdapter;
    private ListView convListView;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(title);

        dbHelper = new DatabaseHelper(this);
        // Used to drop tables
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        dbHelper.onUpgrade(db, 0, 1);
//        db.close();

        // Navigation drawer setup.
        drawerListView = (ListView) findViewById(R.id.navList);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Initialize chatArrayList and chatAdapter here to avoid nullPointerException
        // when call them in other activities.
        ChatActivity.chatArrayList = new ArrayList<>();
        ChatActivity.chatAdapter = new ChatAdapter(getApplicationContext(),
                R.layout.chat_list_elemnt, ChatActivity.chatArrayList);

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

        // Initialize floating action button and attach to list view.
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(convListView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
                Log.d("ListViewFragment", "onScrollDown()");
            }

            @Override
            public void onScrollUp() {
                Log.d("ListViewFragment", "onScrollUp()");
            }
        }, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d("ListViewFragment", "onScrollStateChanged()");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("ListViewFragment", "onScroll()");
            }
        });

        registerForContextMenu(convListView);

        loadSmsFromDatabase();
    }

    public void loadSmsFromDatabase() {
        convArrayList.clear();
        convAdapter.notifyDataSetChanged();
        Log.i(TAG, "load Sms From Database");

        List<Message> allSms = dbHelper.getLastSmsForNumbersExceptBlocked();

        for (int i = allSms.size() - 1; i >= 0; i--) {
            convArrayList.add(allSms.get(i));
        }
        convAdapter.notifyDataSetChanged();
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
                        intent = new Intent(MainActivity.this, BlacklistActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, BlockedSmsActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
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
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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

        switch (id) {
            case R.id.mark_all_read:
                markAllRead();
                break;
            case R.id.delete_all:
                deleteAll();
                break;
            default:
                break;
        }

        // Activate the navigation drawer toggle
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void markAllRead() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.mark_all_read)
                .setMessage("Mark all messages as read?")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processMarkAllRead();
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

    private void processMarkAllRead() {
        dbHelper.markAllSmsInInboxRead();
        Log.i(TAG, "All messages are marked as read.");
        loadSmsFromDatabase();
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
        dbHelper.clearSmsInbox();
        Log.i(TAG, "All messages deleted.");

        convArrayList.clear();
        convAdapter.notifyDataSetChanged();
    }

    /**
     * References:
     * http://stackoverflow.com/questions/21720657/how-to-set-my-sms-app-default-in-android-kitkat
     * http://android-developers.blogspot.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.not_default_app)
                    .setMessage("This is not your default SMS app. Set as default app?")
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    Intent intent =
                                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                            myPackageName);
                                    startActivity(intent);
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

    private void deleteThread(final int index) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("Thread will be deleted.")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processDeleteThread(index);
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

    private void processDeleteThread(int index) {
        Message msg = convArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = msg.getRecipient();
        }

        dbHelper.deleteSmsThread(phoneNumber);
        Log.i(TAG, "Thread [" + phoneNumber + "] deleted.");

        convArrayList.remove(index);
        convAdapter.notifyDataSetChanged();
    }

    private void createContact(int index) {
        Message msg = convArrayList.get(index);
        String phoneNumber = msg.getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = msg.getRecipient();
        }

        final long phoneId = dbHelper.getPhoneId(phoneNumber);
        final boolean isPhoneExist = phoneId != 0;
        final long contactId = dbHelper.getContactIdFromPhoneTable(phoneNumber);

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
                                // get user input and set it to result edit text
                                // need to deal with phone number change
                                String contactName = nameInput.getText().toString();
                                String phoneNumber2 = phoneInput.getText().toString();
                                long insertId = dbHelper.insertContact(contactName, true);
                                if (insertId == -1) {
                                    Log.i(TAG, "insert contact failed.");
                                } else {
                                    Log.i(TAG, "insert contact successful.");
                                    Toast.makeText(getApplicationContext(), "Contact " + contactName + " created.",
                                            Toast.LENGTH_SHORT).show();
                                    // Update phone table's contact_id column
                                    if (!isPhoneExist) {
                                        long id = dbHelper.insertPhone(phoneNumber2, insertId);
                                        if (id == -1) {
                                            Log.i(TAG, "insert phone failed.");
                                        } else {
                                            Log.i(TAG, "insert phone successful.");
                                        }
                                    } else {
                                        long id = dbHelper.updatePhone(phoneNumber2, insertId);
                                        if (id == -1) {
                                            Log.i(TAG, "update phone failed.");
                                        } else {
                                            Log.i(TAG, "update phone successful.");
                                        }
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

        convAdapter.clear();
        convAdapter.notifyDataSetChanged();
        loadSmsFromDatabase();
    }

    private void addToBlacklist(final int index) {
        // Display name or phoneNumber of the contact to be blocked.
        String phoneNumber = convArrayList.get(index).getSender();
        if ("ME".equals(phoneNumber)) {
            phoneNumber = convArrayList.get(index).getRecipient();
        }
        String name = dbHelper.getNameFromContact(phoneNumber);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_blacklist)
                .setMessage("Add " + name + " to blacklist?")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                processAddToBlacklist(index);
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

    private void processAddToBlacklist(int index) {
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
            // Every time a contact is inserted, update the contactId in phone table.
            dbHelper.updatePhone(phoneNumber, insertId);
        } else if (contactId == -1) {
            Toast.makeText(this, "The phone number is not in the table.", Toast.LENGTH_SHORT).show();
        } else {
            // Block the contact.
            long resId = dbHelper.isAllowContact(contactId, false);
            if (resId == -1) {
                Log.i(TAG, "add to blacklist failed.");
            } else {
                Log.i(TAG, "added to blacklist.");
                Toast.makeText(this, "This contact is blocked.", Toast.LENGTH_SHORT).show();
//            dbHelper.moveSmsToBlocked(contactId);
            }
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

    // Maybe put it into Utils?
    public void updateList(final Message msg) {
        convArrayList.add(msg);
        convAdapter.notifyDataSetChanged();
        Log.v("------ReceiveActivity", "In updateList() method.");
    }

    @Override
    public void onClick(View v) {

    }

    public void goToCompose(View v) {
        Intent intent = new Intent(MainActivity.this, ComposeSmsActivity.class);
        startActivity(intent);
    }
}
