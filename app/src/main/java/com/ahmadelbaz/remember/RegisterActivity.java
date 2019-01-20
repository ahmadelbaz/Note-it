package com.ahmadelbaz.remember;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences signPrefs;

    SharedPreferences userKeyPrefs;

    private FirebaseAuth mAuth;

    TextInputEditText register_email;
    TextInputEditText register_password;
    TextInputEditText register_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signPrefs = this.getSharedPreferences("signInAndOut", Context.MODE_PRIVATE);

        userKeyPrefs = this.getSharedPreferences("userIdKey", Context.MODE_PRIVATE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        register_email = (TextInputEditText) findViewById(R.id.register_email);
        register_password = (TextInputEditText) findViewById(R.id.register_password);
        register_confirm_password = (TextInputEditText) findViewById(R.id.register_confirm_password);
    }


    public void Register(View view) {

        String enteredEmail;
        String enteredPassword;
        String enteredConfirmPassword;

        enteredEmail = register_email.getText().toString();
        enteredPassword = register_password.getText().toString();
        enteredConfirmPassword = register_confirm_password.getText().toString();


        if (enteredEmail.isEmpty() || enteredEmail.equals(" ")) {
            register_email.setError("Fill here please");
            return;
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


        mAuth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userKeyPrefs.edit().putString("addUserIdKey", "" + mAuth.getCurrentUser().getUid()).commit();
                            signPrefs.edit().putBoolean("signInOrOut", true).commit();
                            Intent intent = new Intent(RegisterActivity.this, RestoredList.class);
                            startActivity(intent);
                            finish();
                        } else {
                        }
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
