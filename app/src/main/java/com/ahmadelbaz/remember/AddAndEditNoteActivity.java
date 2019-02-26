package com.ahmadelbaz.remember;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ahmadelbaz.remember.ListNotesActivity.arrayAdapter;
import static com.ahmadelbaz.remember.ListNotesActivity.notesAddressList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesCalenderList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesList;

public class AddAndEditNoteActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    //instantiation

    SharedPreferences prefs;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    ScrollView scrollView;

    int noteId;

    EditText noteText_editText;
    EditText noteTitle_editText;

    TextView addCalender_textView;

    String newText = "";
    String oldText = "a";
    String newTitleText = "";
    String oldTitleText = "a";
    String newDateAndTime = "";
    String oldDateAndTime = "a";

    String inMessage;
    String inTitleMessage;

    String usernameOfReceiver;

    String UName;

    int dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_note);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current username
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            DatabaseReference UNameRef = mDatabase.child("users").child("username").child(currentUser.getUid() + "");


            ValueEventListener eventUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    UName = dataSnapshot.getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            UNameRef.addValueEventListener(eventUserListener);

        } else {
            UName = getString(R.string.guest);
        }
        // get the time
        Calendar c = Calendar.getInstance();
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        scrollView = findViewById(R.id.scrollView);

        noteText_editText = (EditText) findViewById(R.id.noteText_editText);
        noteTitle_editText = (EditText) findViewById(R.id.noteTitle_editText);

        addCalender_textView = findViewById(R.id.addCalender_textView);

        // Get the intent to know is that a new note or an old one
        Intent i = getIntent();

        if (getIntent() != null) {
            String strdata = getIntent().getExtras().getString("Unique");

            // if this note is an old one
            if (strdata.equals("OldNote")) {
                /*noteText_editText.setEnabled(false);
                noteTitle_editText.setEnabled(false);*/
                inMessage = oldText;
                inTitleMessage = oldTitleText;
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
            // if this note is a new one
            else if (strdata.equals("NewNote")) {
                inMessage = newText;
                inTitleMessage = newTitleText;
                noteText_editText.setEnabled(true);
                noteTitle_editText.setEnabled(true);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }

        // get the note id if it is an old one
        // if this note is a new note so the id
        // will be -1
        noteId = i.getIntExtra("noteId", -1);

        doInAddActivity(noteText_editText, notesList, oldText, 1);
        doInAddActivity(noteTitle_editText, notesAddressList, oldTitleText, 2);
        getNoteTimeData(noteText_editText, notesCalenderList, oldDateAndTime, 3);

        checkNightMode();
    }

    // get note time data from shared preference
    private void getNoteTimeData(EditText editableText, List<String> editedList, String editedString, final int a) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;

        if (noteId != -1) {

            newDateAndTime = "" + editedList.get(noteId);
            oldDateAndTime = "" + editedList.get(noteId);

            addCalender_textView.setText("" + editedList.get(noteId));


            newDateAndTime = oldDateAndTime;
        } else {

            editedList.add("");

            addCalender_textView.setText("" + dateAndTime);

            newDateAndTime = dateAndTime;
        }
    }

    // get note title and text data from shared preference
    private void doInAddActivity(EditText editableText, List<String> editedList, String editedString, final int a) {

        if (noteId != -1) {

            setTitle(R.string.edit_note);

            if (a == 1) {
                newText = "" + editedList.get(noteId);
                oldText = "" + editedList.get(noteId);
            } else if (a == 2) {
                newTitleText = "" + editedList.get(noteId);
                oldTitleText = "" + editedList.get(noteId);
            }

            editableText.setText("" + editedList.get(noteId));

            editableText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (a == 1) {
                        newText = "" + editable;
                    } else if (a == 2) {
                        newTitleText = "" + editable;
                    }
                }
            });

        } else {

            setTitle(R.string.add_note);

            editedList.add("");
            editableText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if (a == 1) {
                        newText = "" + editable;
                    } else if (a == 2) {
                        newTitleText = "" + editable;
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_reminder, menu);

        if (getIntent() != null) {
            String strdata = getIntent().getExtras().getString("Unique");

            if (strdata.equals("OldNote")) {
                getMenuInflater().inflate(R.menu.lock, menu);
            } else if (strdata.equals("NewNote")) {
                getMenuInflater().inflate(R.menu.lock, menu);
            }
        }

        getMenuInflater().inflate(R.menu.save, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.addReminder:


                final EditText alarmTitle = new EditText(AddAndEditNoteActivity.this);
                alarmTitle.setHint(R.string.alarm_title);
                alarmTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                alarmTitle.setText(noteTitle_editText.getText().toString());
                alarmTitle.requestFocus();


                final EditText alarmContent = new EditText(AddAndEditNoteActivity.this);
                alarmContent.setHint(R.string.alarm_note);
                alarmContent.setInputType(InputType.TYPE_CLASS_TEXT);
                alarmContent.setText("");
                alarmContent.requestFocus();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.VERTICAL,
                        LinearLayout.LayoutParams.MATCH_PARENT);


                alarmTitle.setLayoutParams(lp);
                alarmContent.setLayoutParams(lp);


                alarmTitle.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


                //Add Attributes of AlertDialog.builder
                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                        .setTitle(R.string.set_alarm_title)
                        .setView(alarmTitle)
                        .setPositiveButton(getString(R.string.next), new DialogInterface.OnClickListener() {


                            String daysList[] = {getString(R.string.saturday), getString(R.string.sunday),
                                    getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday),
                                    getString(R.string.thursday), getString(R.string.friday)};

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                                        .setTitle(R.string.pick_day)
                                        .setSingleChoiceItems(daysList, Calendar.DAY_OF_WEEK, null)
                                        .setPositiveButton(getString(R.string.all_correct), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {


                                                dialog.dismiss();
                                                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                                                dayOfWeek = selectedPosition;


                                                inMessage = alarmContent.getText().toString();
                                                inTitleMessage = alarmTitle.getText().toString();

                                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                                                // Display Fragment of timepicker
                                                DialogFragment timePicker = new TimePickerFragment();
                                                timePicker.show(getFragmentManager(), "timePicker");


                                            }
                                        })
                                        .setNegativeButton(getString(R.string.cancel), null)
                                        .show();

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

                return true;

            case R.id.editNote:

                if (noteText_editText.isEnabled()) {
                    noteText_editText.setEnabled(false);
                    noteTitle_editText.setEnabled(false);
                    item.setTitle(R.string.edit);
                } else {
                    noteText_editText.setEnabled(true);
                    noteTitle_editText.setEnabled(true);
                    item.setTitle(R.string.lock);

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                return true;

            case R.id.lockNote:

                if (noteText_editText.isEnabled()) {
                    noteText_editText.setEnabled(false);
                    noteTitle_editText.setEnabled(false);
                    item.setTitle(R.string.edit);
                } else {
                    noteText_editText.setEnabled(true);
                    noteTitle_editText.setEnabled(true);
                    item.setTitle(R.string.lock);

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                return true;

            case R.id.saveNote:

                Calendar c = Calendar.getInstance();

                String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
                String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

                saveButtonPressed(noteText_editText, noteTitle_editText, notesList, "MyUsers", newText, 1);
                saveButtonPressed(noteTitle_editText, noteText_editText, notesAddressList, "MyAddressUsers", newTitleText, 2);
                saveCalender(noteText_editText, notesCalenderList, "MyCalenderUsers", newDateAndTime, 3);

                // if any field is Empty tell the user
                if (noteTitle_editText.getText().toString().isEmpty() || noteText_editText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.empty_field, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_SHORT).show();
                }

                return true;

            case android.R.id.home:

                if (noteId == -1) {

                    notesList.remove(notesList.size() - 1);
                    arrayAdapter.notifyDataSetChanged();


                    notesAddressList.remove(notesAddressList.size() - 1);
                    arrayAdapter.notifyDataSetChanged();

                    notesCalenderList.remove(notesCalenderList.size() - 1);
                    arrayAdapter.notifyDataSetChanged();

                }

                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                    startActivity(in);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to save calender
    private void saveCalender(EditText editedText, List<String> editedList, String dbKey, String editableString, int a) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;

        editableString = dateAndTime;

        if (noteId != -1) {

            if (!editedText.getText().toString().matches("")) {
                editedList.set(noteId, "" + editableString);
                arrayAdapter.notifyDataSetChanged();
            } else {

                notesList.set(noteId, "" + oldText);
                notesAddressList.set(noteId, "" + oldTitleText);
                notesCalenderList.set(noteId, "" + oldDateAndTime);

                arrayAdapter.notifyDataSetChanged();

                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                    startActivity(in);
                }
                finish();
            }

        } else {

            if (editableString != null) {

                if (!editedText.getText().toString().matches("")) {
                    editedList.set(notesList.size() - 1, "" + editableString);
                    arrayAdapter.notifyDataSetChanged();
                } else {

                    editedList.remove(editedList.size() - 1);
                    arrayAdapter.notifyDataSetChanged();

                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                        startActivity(in);
                    }
                    finish();
                }
            }
        }
        if (editableString != null) {

            TinyDB tinydb = new TinyDB(getApplicationContext());
            TinyDB tinyTitledb = new TinyDB(getApplicationContext());
            TinyDB tinyCalenderdb = new TinyDB(getApplicationContext());


            tinydb.putListString("MyUsers", (ArrayList<String>) notesList);
            tinyTitledb.putListString("MyAddressUsers", (ArrayList<String>) notesAddressList);
            tinyCalenderdb.putListString("MyCalenderUsers", (ArrayList<String>) notesCalenderList);

            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                startActivity(in);
            }
            finish();
        }
    }

    // Method to save title and text
    private void saveButtonPressed(EditText editedText, EditText secondaryEdit, List<String> editedList, String editableString, String dbKey, int a) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;


        if (noteTitle_editText.getText().toString().isEmpty()) {

            if (noteText_editText.getText().toString().length() <= 40) {
                noteTitle_editText.setText("" + noteText_editText.getText().toString());
            } else if (noteText_editText.getText().toString().length() > 40) {
                String newTitle = "";
                for (int n = 0; n <= 39; n++) {
                    newTitle += "" + noteText_editText.getText().toString().charAt(n);
                }
                noteTitle_editText.setText(newTitle + "...");
            }
        }

        if (a == 1) {
            editableString = newText;
        } else if (a == 2) {
            editableString = newTitleText;
        }
        if (noteId != -1) {

            if (!editedText.getText().toString().matches("") && !secondaryEdit.getText().toString().matches("")) {
                editedList.set(noteId, "" + editableString);
                arrayAdapter.notifyDataSetChanged();
            } else {

                notesList.set(noteId, "" + oldText);
                notesAddressList.set(noteId, "" + oldTitleText);
                notesCalenderList.set(noteId, "" + oldDateAndTime);

                arrayAdapter.notifyDataSetChanged();

                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                    startActivity(in);
                }
                finish();
            }

        } else {

            if (editableString != null) {

                if (!editedText.getText().toString().matches("") && !secondaryEdit.getText().toString().matches("")) {
                    editedList.set(notesList.size() - 1, "" + editableString);
                    arrayAdapter.notifyDataSetChanged();
                } else {

                    editedList.remove(editedList.size() - 1);
                    arrayAdapter.notifyDataSetChanged();

                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                        startActivity(in);
                    }
                    finish();
                }
            }
        }

        if (editableString != null) {

            TinyDB tinydb = new TinyDB(getApplicationContext());
            TinyDB tinyTitledb = new TinyDB(getApplicationContext());
            TinyDB tinyCalenderdb = new TinyDB(getApplicationContext());


            tinydb.putListString("MyUsers", (ArrayList<String>) notesList);
            tinyTitledb.putListString("MyAddressUsers", (ArrayList<String>) notesAddressList);
            tinyCalenderdb.putListString("MyCalenderUsers", (ArrayList<String>) notesCalenderList);

            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
                startActivity(in);
            }
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (noteId == -1) {
            if (noteText_editText.getText().toString().matches("")) {
                notesList.remove(notesList.size() - 1);
                notesCalenderList.remove(notesCalenderList.size() - 1);
                arrayAdapter.notifyDataSetChanged();
            }

            if (noteTitle_editText.getText().toString().matches("")) {
                notesAddressList.remove(notesAddressList.size() - 1);
                arrayAdapter.notifyDataSetChanged();
            }
        }

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
            startActivity(in);
        }
        finish();
    }

    // what will happen if user press save button
    public void saveNote_button(View view) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;

        saveButtonPressed(noteText_editText, noteTitle_editText, notesList, "MyUsers", newText, 1);
        saveButtonPressed(noteTitle_editText, noteText_editText, notesAddressList, "MyAddressUsers", newTitleText, 2);
        saveCalender(noteText_editText, notesCalenderList, "MyCalenderUsers", newDateAndTime, 3);

        // if any field is Empty tell the user
        if (noteTitle_editText.getText().toString().isEmpty() || noteText_editText.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.empty_field, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_SHORT).show();
        }

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
            startActivity(in);
        }
        finish();
    }

    // what will happen if user press cancle button
    public void cancelNote_button(View view) {
        if (noteId == -1) {

            notesList.remove(notesList.size() - 1);
            arrayAdapter.notifyDataSetChanged();

            notesAddressList.remove(notesAddressList.size() - 1);
            arrayAdapter.notifyDataSetChanged();

            notesCalenderList.remove(notesCalenderList.size() - 1);
            arrayAdapter.notifyDataSetChanged();

        }

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Intent in = new Intent(AddAndEditNoteActivity.this, ListNotesActivity.class);
            startActivity(in);
        }
        finish();
    }

    // check if user used night mode or not from settings
    private void checkNightMode() {

        prefs = this.getSharedPreferences("NightModeKey", Context.MODE_PRIVATE);

        if (prefs.getBoolean("NightModeIsOn", false)) {

            // Turn to Night mode

            noteText_editText.setTextColor(Color.WHITE);
            noteText_editText.setBackgroundColor(Color.BLACK);
            noteTitle_editText.setTextColor(Color.WHITE);
            noteTitle_editText.setBackgroundColor(Color.BLACK);
            scrollView.setBackgroundColor(Color.BLACK);
            addCalender_textView.setTextColor(Color.WHITE);

        } else {

            noteText_editText.setTextColor(Color.BLACK);
            noteText_editText.setBackgroundColor(Color.WHITE);
            noteTitle_editText.setTextColor(Color.BLACK);
            noteTitle_editText.setBackgroundColor(Color.WHITE);
            scrollView.setBackgroundColor(Color.WHITE);
            addCalender_textView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minutes) {

        //Intent to Device Alarm with your data(Time and Note title)
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, inTitleMessage);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        intent.putExtra(AlarmClock.EXTRA_DAYS, dayOfWeek);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        startActivity(intent);

    }

    public void sendlNote_button(View view) {

        //Let user choose between send the note to user or to share it external


        String[] arrayStrings = {getString(R.string.send_to_user), getString(R.string.share_external)};

        new AlertDialog.Builder(AddAndEditNoteActivity.this)
                .setTitle(R.string.choose_option)
                .setSingleChoiceItems(arrayStrings, 0, null)
                .setPositiveButton(R.string.all_correct, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        // Do something useful withe the position of the selected radio button

                        if (selectedPosition == 0) {
                            //send the note to specific user by username

                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if (currentUser == null) {

                                Intent in = new Intent(AddAndEditNoteActivity.this, LoginActivity.class);
                                startActivity(in);

                            } else {

                                // create editText for the alert dialog
                                final EditText sendNoteUsername = new EditText(AddAndEditNoteActivity.this);
                                sendNoteUsername.setHint(R.string.enter_username);
                                sendNoteUsername.setInputType(InputType.TYPE_CLASS_TEXT);
                                sendNoteUsername.requestFocus();

                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.VERTICAL,
                                        LinearLayout.LayoutParams.MATCH_PARENT);

                                sendNoteUsername.setLayoutParams(lp);
                                sendNoteUsername.requestFocus();
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                                // Alert dialog to write username in.
                                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                                        .setTitle(R.string.choose_receiver)
                                        .setView(sendNoteUsername)
                                        .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                usernameOfReceiver = sendNoteUsername.getText().toString();

                                                // Check if this username is exists or not
                                                mDatabase.child("users").child("AllUsers").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (!dataSnapshot.hasChild(usernameOfReceiver)) {
                                                            // use "username" already exists
                                                            // Let the user know he needs to pick another username.
                                                            Toast.makeText(AddAndEditNoteActivity.this, R.string.wrong_username, Toast.LENGTH_SHORT).show();
                                                            onStop();
                                                            return;
                                                        } else {

                                                            // Check if the user wrote his own username
                                                            if (usernameOfReceiver.equals(UName)) {

                                                                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                                        .setTitle(R.string.error)
                                                                        .setMessage(getString(R.string.you_cant_send_message_to_your_self))
                                                                        .setNegativeButton(R.string.all_correct, null)
                                                                        .show();

                                                                Toast.makeText(AddAndEditNoteActivity.this, getString(R.string.you_cant_send_message_to_your_self), Toast.LENGTH_SHORT).show();

                                                                // Check if the user let the input empty
                                                            } else if (usernameOfReceiver.isEmpty()) {

                                                                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                                        .setTitle(R.string.error)
                                                                        .setMessage(getString(R.string.you_have_to_enter_username))
                                                                        .setNegativeButton(R.string.all_correct, null)
                                                                        .show();

                                                                Toast.makeText(AddAndEditNoteActivity.this, getString(R.string.you_have_to_enter_username), Toast.LENGTH_SHORT).show();

                                                            } else {

                                                                // Get the time of sending the message
                                                                Calendar c = Calendar.getInstance();

                                                                String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
                                                                String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

                                                                String dateAndTime = date + " - " + time;

                                                                //Create Unique code for every message with the current time and the username of the user.
                                                                String UniqueCode = "" + c.getTimeInMillis() + UName;

                                                                mDatabase.child("message").child("" + usernameOfReceiver)
                                                                        .child("" + UniqueCode)
                                                                        .child("noteTitle").setValue("" + noteTitle_editText.getText().toString() + " / From " + UName);

                                                                mDatabase.child("message").child("" + usernameOfReceiver)
                                                                        .child("" + UniqueCode)
                                                                        .child("noteText").setValue("" + noteText_editText.getText().toString());

                                                                mDatabase.child("message").child("" + usernameOfReceiver)
                                                                        .child("" + UniqueCode)
                                                                        .child("noteTime").setValue("" + dateAndTime);

                                                                // Save the message unique code
                                                                mDatabase.child("uniqueCode").child("" + usernameOfReceiver).child("" + UniqueCode).setValue(true);

                                                                Toast.makeText(AddAndEditNoteActivity.this, getString(R.string.note_sent_to) + usernameOfReceiver, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                            }

                        } else if (selectedPosition == 1) {
                            //share the note external as a text to another app

                            // Get the time of sending the message
                            Calendar c = Calendar.getInstance();

                            String date = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
                            String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

                            String dateAndTime = date + " : " + time;

                            String textToShare = "" + noteTitle_editText.getText().toString() + "\n" + dateAndTime
                                    + "\n" + noteText_editText.getText().toString();

                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, noteTitle_editText.getText().toString());
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare);
                            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }
}