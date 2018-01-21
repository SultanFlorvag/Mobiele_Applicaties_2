package com.example.michiel.mobapp_michielvanbergen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConversationActivity extends AppCompatActivity {

    private String contactName;

    private SharedPreferences savedValues;
    private String UID;

    private String contactKey;
    private String conversationKey;

    private FirebaseDatabase database;
    private DatabaseReference myConversationRef;

    private ListView listView;
    private ConversationArrayAdapter conversationArrayAdapter;

    private EditText messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        contactName = intent.getStringExtra("contactName");
        setTitle(contactName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.conversationListView);

        conversationArrayAdapter = new ConversationArrayAdapter(getApplicationContext(), R.layout.sent_message);
        listView.setAdapter(conversationArrayAdapter);

        listView.setDivider(null);
        listView.setDividerHeight(0);

        contactKey = intent.getStringExtra("contactKey");
        conversationKey = intent.getStringExtra("conversationKey");

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
        UID = savedValues.getString("UID", "");

        database = FirebaseDatabase.getInstance();

        myConversationRef = database.getReference("chat/" + conversationKey);
        myConversationRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    conversationArrayAdapter.clearList();

                    TreeMap<String,String> conversationData = new TreeMap<>();
                    conversationData.putAll((Map) dataSnapshot.getValue());

                    for (Object value : conversationData.values()) {
                        Map conversationValue = (Map) value;

                        if (conversationValue.containsKey(UID)) {
                            Object messageValue = conversationValue.get(UID);
                            Message message = new Message(true, messageValue.toString());
                            conversationArrayAdapter.add(message);
                        } else if (conversationValue.containsKey(contactKey)){
                            Object messageValue = conversationValue.get(contactKey);
                            Message message = new Message(false, messageValue.toString());
                            conversationArrayAdapter.add(message);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        listView.setTranscriptMode(listView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(conversationArrayAdapter);

        conversationArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(conversationArrayAdapter.getCount() - 1);
            }
        });

        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });

    }

    private void sendMessage() {
        if (! messageEditText.getText().toString().equals("")) {
            myConversationRef.push().child(UID).setValue(messageEditText.getText().toString());

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(50);

            messageEditText.setText("");
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
