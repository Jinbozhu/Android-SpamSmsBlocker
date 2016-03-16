package com.example.feeling.spamsmsblocker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.feeling.spamsmsblocker.R;
import com.example.feeling.spamsmsblocker.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by feeling on 3/9/16.
 */
public class ChatAdapter extends ArrayAdapter<Message> {
    int resource;
    Context context;

    TextView content;
    TextView time;

    public ChatAdapter(Context _context, int _resource, List<Message> items) {
        super(_context, _resource, items);
        resource = _resource;
        context = _context;
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
        content = (TextView) newView.findViewById(R.id.chat_content);
        time = (TextView) newView.findViewById(R.id.chat_time);
        content.setText(msg.getContent());
        // Convert timestamp from long integer to human-readable format.
        long millis = msg.getTime();
        Log.i("time in mills", String.valueOf(millis));
//        String date = DateFormat.format("h:mm a", new Date(millis)).toString();
        String date = new SimpleDateFormat("h:mm a").format(new Date(millis));
        time.setText(date);

        LinearLayout singleMessageContainer
                = (LinearLayout) newView.findViewById(R.id.singleMessage);

        if ("ME".equals(msg.getSender()) && msg.isDelivered()) {
            newView.setGravity(Gravity.END);
            singleMessageContainer.setBackgroundResource(R.drawable.right_bubble_green);
        } else if ("ME".equals(msg.getSender()) && !msg.isDelivered()) {
            newView.setGravity(Gravity.END);
            singleMessageContainer.setBackgroundResource(R.drawable.right_bubble_green);
        } else {
            newView.setGravity(Gravity.START);
            content.setTextColor(Color.BLACK);
            time.setTextColor(Color.BLACK);
            singleMessageContainer.setBackgroundResource(R.drawable.left_bubble_gray);
        }

        return newView;
    }
}
