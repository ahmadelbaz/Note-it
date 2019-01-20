package com.ahmadelbaz.remember;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    SharedPreferences prefs;

    SharedPreferences userKeyPrefs;

    ListView restoredListView;

    FloatingActionButton fab;

    ArrayAdapter<String> restoredArrayAdapter;

    List<String> restoredTitleList;
    List<String> restoredTextList;
    List<String> restoredTimeList;

    private String USERKEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        fab = findViewById(R.id.fab);

        fab.setVisibility(View.INVISIBLE);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        prefs = this.getSharedPreferences("backupAndRestoreKey", Context.MODE_PRIVATE);

        userKeyPrefs = this.getSharedPreferences("userIdKey", Context.MODE_PRIVATE);

        USERKEY = userKeyPrefs.getString("addUserIdKey", null);

        restoredListView = findViewById(R.id.addNote_listView);

        restoredTitleList = new ArrayList<String>();
        restoredTextList = new ArrayList<String>();
        restoredTimeList = new ArrayList<String>();

        restoredArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restoredTitleList);

        if (prefs.getInt("backupOrRestore", 0) == 1) {
            // Backup the list in three children (Title - Text - Time) and delete any old backup data

            mDatabase.child("" + USERKEY).removeValue();

            for (int n = 0; n < notesAddressList.size(); n++) {
                mDatabase.child("" + USERKEY).child("noteTitle").child("" + n).setValue(notesAddressList.get(n));
            }
            for (int n = 0; n < notesList.size(); n++) {
                mDatabase.child("" + USERKEY).child("noteText").child("" + n).setValue(notesList.get(n));
            }
            for (int n = 0; n < notesCalenderList.size(); n++) {
                mDatabase.child("" + USERKEY).child("noteTime").child("" + n).setValue(notesCalenderList.get(n));
            }

            Intent n = new Intent(RestoredList.this, ListNotesActivity.class);
            startActivity(n);
            finish();

        } else if (prefs.getInt("backupOrRestore", 0) == 2) {
            // Restore data from backup to restored list

            DatabaseReference titleRef = mDatabase.child("" + USERKEY).child("noteTitle");
            DatabaseReference textRef = mDatabase.child("" + USERKEY).child("noteText");
            DatabaseReference timeRef = mDatabase.child("" + USERKEY).child("noteTime");

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

        } else {
            Intent in = new Intent(RestoredList.this, ListNotesActivity.class);
            startActivity(in);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.saveNote:

                new AlertDialog.Builder(RestoredList.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are You Sure?")
                        .setMessage("You will send the Restored notes to the main list, " +
                                "But this restored list will be deleted")
                        .setPositiveButton("Yes, Sure", new DialogInterface.OnClickListener() {
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
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RestoredList.this, ListNotesActivity.class);
        startActivity(intent);
        finish();
    }
}