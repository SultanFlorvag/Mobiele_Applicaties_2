package com.example.michiel.mobapp_michielvanbergen;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

//    private ProgressDialog progress;

    private ArrayList<Contact> contacts;
    private ListView listView;
    private SimpleAdapter contactsAdapter;

    private SharedPreferences savedValues;
    private String UID;

    private FirebaseDatabase database;
    private DatabaseReference myChatPerProfileRef;
    private DatabaseReference myContactRef;

    NotificationManager notificationManager;

    private Context context;

    private DatabaseReference myNewConversationRef;
    private DatabaseReference myNewChatRef;
    private DatabaseReference myNewContactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        progress = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
//        progress.setMessage("Syncing your conversations...");
//        progress.setCancelable(false);
//        progress.show();

        contacts = new ArrayList();
        listView = (ListView) findViewById(R.id.chatsListView);

        listView.setDivider(null);
        listView.setDividerHeight(0);

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
        UID = savedValues.getString("UID", "");

        database = FirebaseDatabase.getInstance();

        myChatPerProfileRef = database.getReference("chat_per_profile/" + this.UID);
        myChatPerProfileRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Map data = (Map) dataSnapshot.getValue();

                    for (final Object key : data.keySet()) {

                        myContactRef = database.getReference("profile/" + key);
                        myContactRef.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final Map contactData = (Map) dataSnapshot.getValue();

                                    final ArrayList<String> dataValues = (ArrayList<String>) data.get(key);
                                    final String conversationKey = dataValues.get(0).toString();

                                    myContactRef = database.getReference("chat/" + conversationKey);
                                    Query lastMessageQuery = myContactRef.limitToLast(1);
                                    lastMessageQuery.addValueEventListener(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.exists()) {
                                                final Map conversationData = (Map) dataSnapshot.getValue();

                                                for (Object value : conversationData.values()) {
                                                    Map conversationValue = (Map) value;

                                                    boolean existsInContacts = false;
                                                    int index = -1;

                                                    for (Contact contact : contacts) {
                                                        if (contact.getContactKey().equals(key.toString())) {
                                                            index = contacts.indexOf(contact);
                                                            existsInContacts = true;
                                                        }
                                                    }

                                                    if (existsInContacts) {
                                                        Contact contact = new Contact();

                                                        contact.setContactKey(key.toString());

                                                        String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();
                                                        contact.setName(contactName);

                                                        String lastMessage;

                                                        if (conversationValue.containsKey(UID)) {
                                                            Object lastMessageValue = conversationValue.get(UID);
                                                            lastMessage = "You: " + lastMessageValue.toString();
                                                        } else if (conversationValue.containsKey(key)) {
                                                            Object lastMessageValue = conversationValue.get(key);
                                                            lastMessage = contactData.get("firstName") + ": " + lastMessageValue.toString();
                                                        } else {
                                                            lastMessage = null;
                                                        }

                                                        contact.setLastMessage(lastMessage);

                                                        contact.setConversationKey(conversationKey);

                                                        contacts.set(index, contact);

                                                    } else {
                                                        Contact contact = new Contact();

                                                        contact.setContactKey(key.toString());

                                                        String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();
                                                        contact.setName(contactName);

                                                        String lastMessage;

                                                        if (conversationValue.containsKey(UID)) {
                                                            Object lastMessageValue = conversationValue.get(UID);
                                                            lastMessage = "You: " + lastMessageValue.toString();
                                                        } else if (conversationValue.containsKey(key)) {
                                                            Object lastMessageValue = conversationValue.get(key);
                                                            lastMessage = contactData.get("firstName") + ": " + lastMessageValue.toString();
                                                        } else {
                                                            lastMessage = null;
                                                        }

                                                        contact.setLastMessage(lastMessage);

                                                        contact.setConversationKey(conversationKey);

                                                        contacts.add(contact);
                                                    }

                                                    MainActivity.this.updateDisplay();
                                                }
                                            } else {

                                                boolean existsInContacts = false;
                                                int index = -1;

                                                for (Contact contact : contacts) {
                                                    if (contact.getContactKey().equals(key.toString())) {
                                                        index = contacts.indexOf(contact);
                                                        existsInContacts = true;
                                                    }
                                                }

                                                if (existsInContacts) {
                                                    Contact contact = new Contact();

                                                    contact.setContactKey(key.toString());

                                                    String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();
                                                    contact.setName(contactName);

                                                    contact.setConversationKey(conversationKey);

                                                    contacts.set(index, contact);

                                                } else {
                                                    Contact contact = new Contact();

                                                    contact.setContactKey(key.toString());

                                                    String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();
                                                    contact.setName(contactName);

                                                    contact.setConversationKey(conversationKey);

                                                    contacts.add(contact);
                                                }

                                                MainActivity.this.updateDisplay();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {

                                        }

                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        Intent notificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);
        startService(notificationServiceIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void updateDisplay()
    {
        // create a List of Map<String, ?> objects
        ArrayList<HashMap<String, String>> contactsMap =  new ArrayList<HashMap<String, String>>();
        for (Contact contact : contacts) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", contact.getName());
            map.put("message_received", contact.getLastMessage());
            contactsMap.add(map);
        }

        // create the resource, from, and to variables
        int resource = R.layout.contacts_list_row;
        String[] from = {"name", "message_received"};
        int[] to = {R.id.name, R.id.message};

        // create and set the adapter
        contactsAdapter = new SimpleAdapter(this, contactsMap, R.layout.contacts_list_row, from, to);
        listView.setAdapter(contactsAdapter);

        listView.setOnItemClickListener(this);

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                progress.dismiss();
//            }
//        }, 1000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // get the item at the specified position
        Contact contact = contacts.get(position);

        // create an intent
        Intent myIntent = new Intent(MainActivity.this, ConversationActivity.class);
        myIntent.putExtra("contactKey", contact.getContactKey());
        myIntent.putExtra("contactName", contact.getName());
        myIntent.putExtra("conversationKey", contact.getConversationKey());
        MainActivity.this.startActivity(myIntent);

        try {
            notificationManager.cancel(position);
        } catch (Exception e){

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edit_username) {
            Intent myIntent = new Intent(MainActivity.this, UserNameActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_camera) {
            Intent myIntent = new Intent(MainActivity.this, ScannerActivity.class);
            MainActivity.this.startActivityForResult(myIntent, 0);
        } else if (id == R.id.nav_qr) {
            Intent myIntent = new Intent(MainActivity.this, QrCodeActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            MainActivity.this.startActivity(myIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == resultCode){
            final String contactKey = data.getStringExtra("contactKey");

            context = getApplicationContext();

            myNewConversationRef= database.getReference("chat_per_profile/" + this.UID + "/" + contactKey);
            myNewConversationRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        final ArrayList conversationData = (ArrayList) dataSnapshot.getValue();
                        final String conversationKey = conversationData.get(0).toString();

                        for (Contact contact : contacts) {
                            if (contact.getConversationKey().equals(conversationKey)) {
                                int position = contacts.indexOf(contact);

                                Toast toast = Toast.makeText(context, "You are already talking to each other", Toast.LENGTH_SHORT);
                                toast.show();

                                Intent myIntent = new Intent(MainActivity.this, ConversationActivity.class);
                                myIntent.putExtra("contactKey", contact.getContactKey());
                                myIntent.putExtra("contactName", contact.getName());
                                myIntent.putExtra("conversationKey", contact.getConversationKey());
                                MainActivity.this.startActivity(myIntent);

                                try {
                                    notificationManager.cancel(position);
                                } catch (Exception e){

                                }
                            }
                        }
                    } else {
                        newConversation(contactKey);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    protected void newConversation(final String contactKey){

        myNewContactRef = database.getReference("profile/" + contactKey);
        myNewContactRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Map contactData = (Map) dataSnapshot.getValue();
                    final String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();

                    myNewChatRef = database.getReference("chat/");
                    myNewContactRef.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String chatKey = myNewChatRef.push().getKey();
                            myNewConversationRef.child("0").setValue(chatKey);
                            myNewConversationRef = database.getReference("chat_per_profile/" + contactKey + "/" + UID);
                            myNewConversationRef.child("0").setValue(chatKey);

                            Intent myIntent = new Intent(MainActivity.this, ConversationActivity.class);
                            myIntent.putExtra("contactKey", contactKey);
                            myIntent.putExtra("contactName", contactName);
                            myIntent.putExtra("conversationKey", chatKey);
                            MainActivity.this.startActivity(myIntent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast toast = Toast.makeText(context, "this code does not belong to a ChitChat account", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
