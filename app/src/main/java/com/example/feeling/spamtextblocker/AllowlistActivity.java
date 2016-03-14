package com.example.feeling.spamtextblocker;

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

import com.example.feeling.spamtextblocker.adapters.AlAdapter;
import com.example.feeling.spamtextblocker.database.DatabaseHelper;
import com.example.feeling.spamtextblocker.models.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feeling on 3/13/16.
 */
public class AllowlistActivity extends AppCompatActivity {
    public static final String TAG = "AllowlistActivity";

    private AlAdapter alAdapter;
    private List<Contact> alArrayList;
    private ListView alListView;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        setTitle("Allow list");
        dbHelper = new DatabaseHelper(this);

        alArrayList = new ArrayList<>();
        alAdapter = new AlAdapter(getApplicationContext(), R.layout.contact_list_element, alArrayList);

        alListView = (ListView) findViewById(R.id.listView);
        alListView.setAdapter(alAdapter);
//        alListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        registerForContextMenu(alListView);

        loadAllowedContact();
    }

    private void loadAllowedContact() {
        alArrayList.clear();
        alArrayList.addAll(dbHelper.getAllowedContact());
        alAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.al_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.delete_contact:
                deleteContact(index);
                return true;
            case R.id.add_to_blacklist:
                addToBlacklist(index);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void addToBlacklist(int index) {
        Contact contact = alArrayList.get(index);
        long contactId = contact.getId();

        // Block the contact.
        long resId = dbHelper.isAllowContact(contactId, false);
        if (resId == -1) {
            Log.i(TAG, "add to blacklist failed.");
            Toast.makeText(this, "Block contact failed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "added to blacklist.");
            Toast.makeText(this, "Contact blocked.", Toast.LENGTH_SHORT).show();
            // Update list view
            alArrayList.remove(index);
            alAdapter.notifyDataSetChanged();
        }
    }

    public void deleteContact(int index) {
        Contact contact = alArrayList.get(index);
        long contactId = contact.getId();
        long delId = dbHelper.deleteContact(contactId);
        if (delId == -1) {
            Log.i(TAG, "delete contact failed.");
            Toast.makeText(this, "Failed to delete contact.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "delete contact successful");
            Toast.makeText(this, "Delete contact successful.", Toast.LENGTH_SHORT).show();
            // Update list view
            alArrayList.remove(index);
            alAdapter.notifyDataSetChanged();
        }
    }
}
