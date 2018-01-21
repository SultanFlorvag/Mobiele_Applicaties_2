package com.example.michiel.mobapp_michielvanbergen;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ConversationArrayAdapter extends ArrayAdapter<Message> {

    private TextView chatText;
    private List<Message> messageList = new ArrayList<Message>();
    private Context context;

    @Override
    public void add(Message object) {
        messageList.add(object);
        super.add(object);
    }

    public void clearList(){
        messageList.clear();
    }

    public ConversationArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.messageList.size();
    }

    public Message getItem(int index) {
        return this.messageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Message messageObject = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (messageObject.sent) {
            row = inflater.inflate(R.layout.sent_message, parent, false);
        }else{
            row = inflater.inflate(R.layout.received_message, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(messageObject.message);
        return row;
    }
}
