package com.example.feeling.spamtextblocker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.feeling.spamtextblocker.R;
import com.example.feeling.spamtextblocker.models.Contact;
import com.example.feeling.spamtextblocker.models.Message;

import java.util.Date;
import java.util.List;

/**
 * Created by feeling on 3/9/16.
 */
public class AlAdapter extends ArrayAdapter<Contact> {
    int resource;
    Context context;

    TextView name;

    public AlAdapter(Context _context, int _resource, List<Contact> items) {
        super(_context, _resource, items);
        resource = _resource;
        context = _context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout newView;
        Contact contact = getItem(position);

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
        name = (TextView) newView.findViewById(R.id.contact_name);
        name.setText(contact.getName());
        name.setTextColor(Color.BLACK);

        return newView;
    }
}
