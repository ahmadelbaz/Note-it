package com.ahmadelbaz.remember;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
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

public class LoginActivity extends AppCompatActivity {

    SharedPreferences signPrefs;

    SharedPreferences userKeyPrefs;

    private FirebaseAuth mAuth;

    TextInputEditText login_email;
    TextInputEditText login_password;

    DatabaseReference ref;

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ref = FirebaseDatabase.getInstance().getReference();

        signPrefs = this.getSharedPreferences("signInAndOut", Context.MODE_PRIVATE);

        userKeyPrefs = this.getSharedPreferences("userIdKey", Context.MODE_PRIVATE);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        login_email = (TextInputEditText) findViewById(R.id.login_email);
        login_password = (TextInputEditText) findViewById(R.id.login_password);


        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            Intent intent = new Intent(LoginActivity.this, RestoredList.class);
            startActivity(intent);
            finish();
        }
    }

    public void openRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void Login(View view) {

        final String enteredEmail;
        final String enteredPassword;

        enteredEmail = login_email.getText().toString();
        enteredPassword = login_password.getText().toString();

        if (enteredEmail.isEmpty() || enteredEmail.equals(" ")) {
            login_email.setError("Fill here please");
            return;
        }

        if (enteredPassword.isEmpty() || enteredPassword.equals(" ")) {
            login_password.setError("Fill here please");
            return;
        }

        mAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Get username
                            currentUser = mAuth.getCurrentUser();

                            DatabaseReference UNameRef = ref.child("users").child("username").child(currentUser.getUid() + "");

                            ValueEventListener eventUserListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String UName = dataSnapshot.getValue().toString();
                                    Toast.makeText(LoginActivity.this, UName + " Signed in",
                                            Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };

                            UNameRef.addValueEventListener(eventUserListener);


                            userKeyPrefs.edit().putString("addUserIdKey", "" + mAuth.getCurrentUser().getUid()).commit();

                            // Sign in success, update UI with the signed-in user's information
                            signPrefs.edit().putBoolean("signInOrOut", true).commit();
                            Intent intent = new Intent(LoginActivity.this, RestoredList.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            login_password.setError("Wrong Email or Password");
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, ListNotesActivity.class);
        startActivity(intent);
        finish();
    }

    public void updatePassword(View view) {

        final EditText alarmTitle = new EditText(LoginActivity.this);
        alarmTitle.setHint("Alarm Title");
        alarmTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        alarmTitle.setText(login_email.getText().toString());
        alarmTitle.requestFocus();

        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Set Alarm Title")
                .setView(alarmTitle)
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mAuth.getInstance().sendPasswordResetEmail("" + alarmTitle.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Email sent",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .show();
    }
}
