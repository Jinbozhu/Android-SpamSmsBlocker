package com.example.feeling.spamsmsblocker.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.feeling.spamsmsblocker.R;
import com.example.feeling.spamsmsblocker.database.DatabaseHelper;
import com.example.feeling.spamsmsblocker.models.Message;

import java.util.Date;
import java.util.List;

/**
 * Created by shobhit on 1/24/16.
 * Copied from Prof. Luca class code
 */
public class ConversationAdapter extends ArrayAdapter<Message> {
    DatabaseHelper dbHelper;

    int resource;
    Context context;

    TextView msgText;
    TextView contact;
    TextView time;

    public ConversationAdapter(Context _context, int _resource, List<Message> items) {
        super(_context, _resource, items);
        resource = _resource;
        context = _context;

        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout newView;
        Message msg = getItem(position);

        // Inflate a new view if necessary.
        if (convertView == null) {
            newView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
            vi.inflate(resource, newView, true);
        } else {
            newView = (LinearLayout) convertView;
        }

        // Fills in the view.
        msgText = (TextView) newView.findViewById(R.id.latestConversation);
        contact = (TextView) newView.findViewById(R.id.contact);
        time = (TextView) newView.findViewById(R.id.time);
        msgText.setText(msg.getContent());
        String sender = msg.getSender();
        if ("ME".equals(sender)) {
            sender = msg.getRecipient();
        }
        String name = dbHelper.getNameFromContact(sender);
        contact.setText(name);
        // Convert timestamp from long integer to human-readable format.
        long millis = msg.getTime();
        String date = DateFormat.format("MMM dd", new Date(millis)).toString();
        time.setText(date);

        // If message is not read, set font to bold.
        if (!msg.isRead()) {
            msgText.setTypeface(null, Typeface.BOLD);
            contact.setTypeface(null, Typeface.BOLD);
            time.setTypeface(null, Typeface.BOLD);
        }

        return newView;
    }
}
