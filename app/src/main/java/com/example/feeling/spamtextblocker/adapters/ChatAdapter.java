package com.example.feeling.spamtextblocker.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.feeling.spamtextblocker.R;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.Date;
import java.util.List;

/**
 * Created by feeling on 3/9/16.
 */
public class ChatAdapter extends ArrayAdapter<Message> {
    int resource;
    Context context;

    TextView msgText;
    TextView time;

    public ChatAdapter(Context _context, int _resource, List<Message> items) {
        super(_context, _resource, items);
        resource = _resource;
        context = _context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout newView;
        Message element = getItem(position);

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
        msgText = (TextView) newView.findViewById(R.id.chat_content);
        time = (TextView) newView.findViewById(R.id.chat_time);
        msgText.setText(element.getContent());
        // Convert timestamp from long integer to human-readable format.
        long millis = element.getTime();
        String date = DateFormat.format("h:mm aa", new Date(millis)).toString();
        time.setText(date);

        return newView;
    }
}