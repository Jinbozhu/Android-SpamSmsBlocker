package com.example.feeling.spamsmsblocker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.feeling.spamsmsblocker.adapters.AlAdapter;
import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/13/16.
 */
public class BlacklistActivity extends AppCompatActivity {
    public static final String TAG = "BlacklistActivity";

    private AlAdapter blAdapter;
    private List<Contact> blArrayList;
    private ListView blListView;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        setTitle("Black list");
        dbHelper = new DatabaseHelper(this);

        blArrayList = new ArrayList<>();
        blAdapter = new AlAdapter(getApplicationContext(), R.layout.contact_list_element, blArrayList);

        blListView = (ListView) findViewById(R.id.listView);
        blListView.setAdapter(blAdapter);
//        blListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(AllowlistActivity.this, ChatActivity.class);
//                // Get the message object at this position
//                Contact contact = (Contact) parent.getItemAtPosition(position);
//                long contactId = contact.getId();
//                intent.putExtra("contactId", contactId);
//                startActivity(intent);
//            }
//        });

        registerForContextMenu(blListView);

        loadBlockedContact();
    }

    private void loadBlockedContact() {
        blArrayList.clear();
        blArrayList.addAll(dbHelper.getBlockedContact());
        blAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bl_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.delete_contact:
                deleteContact(index);
                return true;
            case R.id.unblock:
                unblockContact(index);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void unblockContact(int index) {
        Contact contact = blArrayList.get(index);
        long contactId = contact.getId();

        // true indicates the contact is unblocked.
        long resId = dbHelper.isAllowContact(contactId, true);
        if (resId == -1) {
            Log.i(TAG, "Unblock failed.");
            Toast.makeText(this, "Unblock failed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Contact is unblocked.");
            Toast.makeText(this, "Contact is unblocked.", Toast.LENGTH_SHORT).show();
            // Update list view
            blArrayList.remove(index);
            blAdapter.notifyDataSetChanged();
        }
    }

    public void deleteContact(int index) {
        Contact contact = blArrayList.get(index);
        long contactId = contact.getId();
        long delId = dbHelper.deleteContact(contactId);
        if (delId == -1) {
            Log.i(TAG, "delete contact failed.");
            Toast.makeText(this, "Failed to delete contact.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "delete contact successful");
            Toast.makeText(this, "Delete contact successful.", Toast.LENGTH_SHORT).show();
            // Update list view
            blArrayList.remove(index);
            blAdapter.notifyDataSetChanged();
        }
    }
}
