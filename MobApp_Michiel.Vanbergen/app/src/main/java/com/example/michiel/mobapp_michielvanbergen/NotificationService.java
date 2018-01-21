package com.example.michiel.mobapp_michielvanbergen;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class NotificationService extends IntentService {

    private ArrayList<Contact> contacts;

    private SharedPreferences savedValues;
    private String UID;

    private FirebaseDatabase database;
    private DatabaseReference myChatPerProfileRef;
    private DatabaseReference myContactRef;

    NotificationManager notificationManager;
    String notificationChannelId;
    NotificationChannel notificationChannel;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        contacts = new ArrayList();

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
                                                        Contact contact = contacts.get(index);

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
                                                            if (contact.getLastMessage() == null) {
                                                                addNotification(contactName, lastMessageValue.toString(), index);
                                                            } else if (!contact.getLastMessage().equals(lastMessage)) {
                                                                addNotification(contactName, lastMessageValue.toString(), index);
                                                            }
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

                                                    String lastMessage = null;
                                                    contact.setLastMessage(lastMessage);

                                                    contact.setConversationKey(conversationKey);

                                                    contacts.set(index, contact);

                                                } else {
                                                    Contact contact = new Contact();

                                                    contact.setContactKey(key.toString());

                                                    String contactName = contactData.get("firstName").toString() + " " + contactData.get("lastName").toString();
                                                    contact.setName(contactName);

                                                    String lastMessage = null;
                                                    contact.setLastMessage(lastMessage);

                                                    contact.setConversationKey(conversationKey);

                                                    contacts.add(contact);
                                                }
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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannelId = "notification_channel";

        notificationChannel = new NotificationChannel(notificationChannelId, "notifications", NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("message notifications");
        notificationChannel.enableLights(true);

        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setVibrationPattern(new long[] {0, 250, 250, 250});

        notificationManager.createNotificationChannel(notificationChannel);

        return START_STICKY;
    }

    private void addNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setColor(0xFF03a9f4);
        Intent resultIntent = new Intent(this, LoginActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(LoginActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
