package com.ahmadelbaz.remember;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {

    DatabaseReference ref;

    SharedPreferences signPrefs;

    private FirebaseAuth mAuth;

    TextInputEditText register_email;
    TextInputEditText register_username;
    TextInputEditText register_password;
    TextInputEditText register_confirm_password;

    Button loginBtn;

    boolean taskS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ref = FirebaseDatabase.getInstance().getReference();

        signPrefs = this.getSharedPreferences("signInAndOut", Context.MODE_PRIVATE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        register_email = (TextInputEditText) findViewById(R.id.register_email);
        register_username = (TextInputEditText) findViewById(R.id.register_username);
        register_password = (TextInputEditText) findViewById(R.id.register_password);
        register_confirm_password = (TextInputEditText) findViewById(R.id.register_confirm_password);
    }


    public void Register(View view) {

        final String enteredEmail;
        final String enteredUsername;
        final String enteredPassword;
        String enteredConfirmPassword;

        enteredEmail = register_email.getText().toString();
        enteredUsername = register_username.getText().toString();
        enteredPassword = register_password.getText().toString();
        enteredConfirmPassword = register_confirm_password.getText().toString();


        if (enteredEmail.isEmpty() || enteredEmail.equals(" ")) {
            register_email.setError("Fill here please");
            return;
        }

        if (enteredUsername.isEmpty() || enteredUsername.equals(" ")) {
            register_username.setError("Fill here please");
            return;
        }

        for (int n = 0; n < enteredUsername.length(); n++) {
            if (!Character.isLetterOrDigit(enteredUsername.charAt(n))) {
                register_username.setError("Cannot contain space or symbol");
                return;
            }
        }

        if (enteredPassword.isEmpty() || enteredPassword.equals(" ")) {
            register_password.setError("Fill here please");
            return;
        }

        if (enteredConfirmPassword.isEmpty() || enteredConfirmPassword.equals(" ")) {
            register_confirm_password.setError("Fill here please");
            return;
        }

        if (enteredPassword.length() < 8) {
            register_password.setError("Password must be more than 8 characters");
        }

        if (!enteredConfirmPassword.equals(enteredPassword)) {
            register_password.setError("Password not match !");
            return;
        }


        FirebaseUser currentUser = mAuth.getCurrentUser();

        ref.child("users").child("AllUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(enteredUsername)) {
                    // use "username" already exists
                    // Let the user know he needs to pick another username.
                    register_username.setError("User name is already exists");
                    onStop();
                    return;
                } else {
                    // User does not exist. NOW call createUserWithEmailAndPassword
                    mAuth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        // getuser
                                        FirebaseUser currentUser = mAuth.getCurrentUser();

                                        ref.child("users").child("username").child("" + currentUser.getUid()).setValue(enteredUsername);
                                        ref.child("users").child("AllUsers").child(enteredUsername).setValue(true);
                                        ref.child("uniqueCode").child("" + enteredUsername);
                                        Toast.makeText(RegisterActivity.this, register_username.getText().toString() + " Signed in",
                                                Toast.LENGTH_SHORT).show();
                                        signPrefs.edit().putBoolean("signInOrOut", true).commit();
                                        Intent intent = new Intent(RegisterActivity.this, RestoredList.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void openLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, ListNotesActivity.class);
        startActivity(intent);
        finish();
    }
}
