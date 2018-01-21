package com.example.michiel.mobapp_michielvanbergen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class UserNameActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    private EditText FirstNameEditText;
    private EditText LastNameEditText;

    private Button ConfirmButton;

    private String UID;
    private SharedPreferences savedValues;

    private FirebaseDatabase database;
    private DatabaseReference myProfileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        FirstNameEditText = (EditText) this.findViewById(R.id.FirstNameEditText);
        LastNameEditText = (EditText) this.findViewById(R.id.LastNameEditText);
        ConfirmButton = (Button) this.findViewById(R.id.ConfirmButton);

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
        UID = savedValues.getString("UID", "");

        database = FirebaseDatabase.getInstance();

        myProfileRef = database.getReference("profile/" + this.UID);
        myProfileRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Map data = (Map) dataSnapshot.getValue();
                    if(data.containsKey("firstName")) {
                        FirstNameEditText.setText(data.get("firstName").toString());
                    }
                    if(data.containsKey("lastName")) {
                        LastNameEditText.setText(data.get("lastName").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ConfirmButton.setOnClickListener(this);

        context = getApplicationContext();
    }

    @Override
    public void onClick(View v) {
        if(!FirstNameEditText.getText().toString().equals("") && !FirstNameEditText.getText().toString().equals(" ")) {
            if(!LastNameEditText.getText().toString().equals("") && !LastNameEditText.getText().toString().equals(" ")) {
                myProfileRef.child("firstName").setValue(FirstNameEditText.getText().toString());
                myProfileRef.child("lastName").setValue(LastNameEditText.getText().toString());

                finish();
            } else {
                Toast toast = Toast.makeText(context, "Lastname can not be empty", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(context, "Firstname can not be empty", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}
