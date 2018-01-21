package com.example.michiel.mobapp_michielvanbergen;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    private EditText EmailEditText;
    private EditText PasswordEditText;
    private Button SignInButton;
    private Button SignUpButton;

    private String EmailString = "";
    private String PasswordString = "";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String UID;

    private SharedPreferences savedValues;

    private FirebaseDatabase database;
    private DatabaseReference myProfileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EmailEditText = (EditText) this.findViewById(R.id.FirstNameEditText);
        PasswordEditText = (EditText) this.findViewById(R.id.LastNameEditText);
        SignInButton = (Button) this.findViewById(R.id.ConfirmButton);
        SignUpButton = (Button) this.findViewById(R.id.SignUpButton);

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    SignInButton.setEnabled(false);
                    SignInButton.setAlpha(0.5f);
                    SignUpButton.setEnabled(false);
                    SignUpButton.setAlpha(0.5f);

                    database = FirebaseDatabase.getInstance();

                    UID = (String) mAuth.getCurrentUser().getUid();

                    myProfileRef = database.getReference("profile/" + UID);
                    myProfileRef.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final Map data = (Map) dataSnapshot.getValue();
                                if(data.containsKey("firstName") && data.containsKey("lastName")) {
                                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    LoginActivity.this.startActivity(myIntent);
                                } else {
                                    Intent myIntent = new Intent(LoginActivity.this, UserNameActivity.class);
                                    LoginActivity.this.startActivity(myIntent);
                                }
                            } else {
                                Intent myIntent = new Intent(LoginActivity.this, UserNameActivity.class);
                                LoginActivity.this.startActivity(myIntent);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // User is signed out
                }
            }
        };

        EmailString = savedValues.getString("EmailString", "");
        PasswordString = savedValues.getString("PasswordString", "");

        EmailEditText.setText(EmailString);
        PasswordEditText.setText(PasswordString);

        SignInButton.setOnClickListener(this);
        SignUpButton.setOnClickListener(this);

        context = getApplicationContext();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        if( v == SignInButton) {
            signIn();
        }
        if( v == SignUpButton) {
            Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            LoginActivity.this.startActivity(myIntent);
        }
    }

    private void signIn(){
        EmailString = EmailEditText.getText().toString();
        PasswordString = PasswordEditText.getText().toString();

        try {
            mAuth.signInWithEmailAndPassword(EmailString, PasswordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                UID = (String) mAuth.getCurrentUser().getUid();

                                SharedPreferences.Editor editor = savedValues.edit();
                                editor.putString("EmailString", EmailString);
                                editor.putString("PasswordString", PasswordString);

                                editor.putString("UID", UID);
                                editor.commit();

                                updateWidgets(context);
                            } else {
                                Toast toast = Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
        } catch (Exception e) {
            Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
