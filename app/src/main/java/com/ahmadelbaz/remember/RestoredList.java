package com.ahmadelbaz.remember;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.ahmadelbaz.remember.ListNotesActivity.notesAddressList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesCalenderList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesList;

public class RestoredList extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    FirebaseUser currentUser;

    SharedPreferences userNamePrefs;

    SharedPreferences prefs;

    ListView restoredListView;

    FloatingActionButton fab;

    ArrayAdapter<String> restoredArrayAdapter;

    List<String> restoredTitleList;
    List<String> restoredTextList;
    List<String> restoredTimeList;

    String UName;

    List<String> uniqueCodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        userNamePrefs = this.getSharedPreferences("userNameKey", Context.MODE_PRIVATE);

        userNamePrefs.edit().putString("userName", UName).commit();

        // Get current username
        currentUser = mAuth.getCurrentUser();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        DatabaseReference UNameRef = mDatabase.child("users").child("username").child(currentUser.getUid() + "");

        ValueEventListener eventUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UName = dataSnapshot.getValue().toString();

                userNamePrefs.edit().putString("userName", UName).commit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        UNameRef.addValueEventListener(eventUserListener);

        fab = findViewById(R.id.fab);

        fab.setVisibility(View.INVISIBLE);

        prefs = this.getSharedPreferences("backupAndRestoreKey", Context.MODE_PRIVATE);


        restoredListView = findViewById(R.id.addNote_listView);

        restoredTitleList = new ArrayList<String>();
        restoredTextList = new ArrayList<String>();
        restoredTimeList = new ArrayList<String>();

        restoredArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restoredTitleList);


        if (prefs.getInt("backupOrRestore", 0) == 4) {
            setTitle(R.string.received_notes);
        }


        if (prefs.getInt("backupOrRestore", 0) == 1) {
            // Backup the list in three children (Title - Text - Time) and delete any old backup data

            mDatabase.child("Backup").child("" + currentUser.getUid()).removeValue();

            for (int n = 0; n < notesAddressList.size(); n++) {
                mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteTitle").child("" + n).setValue(notesAddressList.get(n));
            }
            for (int n = 0; n < notesList.size(); n++) {
                mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteText").child("" + n).setValue(notesList.get(n));
            }
            for (int n = 0; n < notesCalenderList.size(); n++) {
                mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteTime").child("" + n).setValue(notesCalenderList.get(n));
            }

            Intent n = new Intent(RestoredList.this, ListNotesActivity.class);
            startActivity(n);
            finish();

        } else if (prefs.getInt("backupOrRestore", 0) == 2) {
            // Restore data from backup to restored list

            setTitle(R.string.restored_list);

            DatabaseReference titleRef = mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteTitle");
            DatabaseReference textRef = mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteText");
            DatabaseReference timeRef = mDatabase.child("Backup").child("" + currentUser.getUid()).child("noteTime");

            // Event listener to add title value to the list
            ValueEventListener eventTitleListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        restoredTitleList.add(ds.getValue().toString());
                    }
                    restoredArrayAdapter = new ArrayAdapter<String>(RestoredList.this,
                            android.R.layout.simple_dropdown_item_1line, restoredTitleList);
                    restoredListView.setAdapter(restoredArrayAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            // Event listener to add text value to the list
            ValueEventListener eventTextListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        restoredTextList.add(ds.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            // Event listener to add time value to the list
            ValueEventListener eventTimeListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        restoredTimeList.add(ds.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            titleRef.addListenerForSingleValueEvent(eventTitleListener);
            textRef.addListenerForSingleValueEvent(eventTextListener);
            timeRef.addListenerForSingleValueEvent(eventTimeListener);

        } else if (prefs.getInt("backupOrRestore", 0) == 4) {

            new CountDownTimer(3000, 1000) {

                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {

                    setTitle(R.string.received_notes);

                    prefs = getSharedPreferences("userNameKey", Context.MODE_PRIVATE);


                    UName = prefs.getString("userName", getString(R.string.guest));

                    // Event listener to add title value to the list
                    // First we wanna get the unique code

                    DatabaseReference uniqueCodeRef = mDatabase.child("uniqueCode").child("" + UName);

                    uniqueCodeList = new ArrayList<String>();

                    ValueEventListener eventUniqueListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                uniqueCodeList.add(ds.getKey());
                                DatabaseReference titleRef = mDatabase.child("message").child("" + UName).child("" + ds.getKey()).child("noteTitle");
                                DatabaseReference textRef = mDatabase.child("message").child("" + UName).child("" + ds.getKey()).child("noteText");
                                DatabaseReference timeRef = mDatabase.child("message").child("" + UName).child("" + ds.getKey()).child("noteTime");

                                // Event listener to add title value to the list
                                ValueEventListener eventTitleListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                        restoredTitleList.add(dataSnapshot.getValue().toString());
                                        restoredArrayAdapter = new ArrayAdapter<String>(RestoredList.this,
                                                android.R.layout.simple_dropdown_item_1line, restoredTitleList);
                                        restoredListView.setAdapter(restoredArrayAdapter);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                };

                                // Event listener to add text value to the list
                                ValueEventListener eventTextListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                        restoredTextList.add(dataSnapshot.getValue().toString());

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                };

                                // Event listener to add time value to the list
                                ValueEventListener eventTimeListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        restoredTimeList.add(dataSnapshot.getValue().toString());

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                };

                                titleRef.addListenerForSingleValueEvent(eventTitleListener);
                                textRef.addListenerForSingleValueEvent(eventTextListener);
                                timeRef.addListenerForSingleValueEvent(eventTimeListener);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };

                    uniqueCodeRef.addListenerForSingleValueEvent(eventUniqueListener);

                }
            }.start();

        } else {
            Intent in = new Intent(RestoredList.this, ListNotesActivity.class);
            startActivity(in);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_received, menu);
        getMenuInflater().inflate(R.menu.delete_all_notes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.savereceivedNotes:

                new AlertDialog.Builder(RestoredList.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(getString(R.string.you_will_send_restored_to_main))
                        .setPositiveButton(R.string.yes_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                TinyDB tinydb = new TinyDB(getApplicationContext());
                                TinyDB tinyTitledb = new TinyDB(getApplicationContext());
                                TinyDB tinyCalenderdb = new TinyDB(getApplicationContext());

                                for (int in = 0; in < restoredTitleList.size(); in++) {
                                    notesAddressList.add(restoredTitleList.get(in));
                                }
                                for (int in = 0; in < restoredTextList.size(); in++) {
                                    notesList.add(restoredTextList.get(in));
                                }
                                for (int in = 0; in < restoredTimeList.size(); in++) {
                                    notesCalenderList.add(restoredTimeList.get(in));
                                }

                                tinydb.putListString("MyUsers", (ArrayList<String>) notesList);
                                tinyTitledb.putListString("MyAddressUsers", (ArrayList<String>) notesAddressList);
                                tinyCalenderdb.putListString("MyCalenderUsers", (ArrayList<String>) notesCalenderList);

                                Intent in = new Intent(RestoredList.this, ListNotesActivity.class);
                                startActivity(in);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;

            case R.id.deleteAllNotes:

                deleteAllNotes();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // what will happen if user click a long click on note to delete(till now) it.
    private void deleteAllNotes() {

        prefs = this.getSharedPreferences("backupAndRestoreKey", Context.MODE_PRIVATE);

        if (prefs.getInt("backupOrRestore", 0) == 3) {

            new android.app.AlertDialog.Builder(RestoredList.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.are_you_sure)
                    .setMessage(R.string.you_want_delete_all_notes)
                    .setPositiveButton(R.string.yes_sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            restoredTextList.clear();
                            restoredTitleList.clear();
                            restoredTimeList.clear();
                            restoredArrayAdapter.notifyDataSetChanged();

                            Toast.makeText(RestoredList.this, "" + UName, Toast.LENGTH_SHORT).show();
                            mDatabase.child("message").child("" + UName).removeValue();
                            mDatabase.child("uniqueCode").child("" + UName).removeValue();

                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RestoredList.this, ListNotesActivity.class);
        startActivity(intent);
        finish();
    }
}