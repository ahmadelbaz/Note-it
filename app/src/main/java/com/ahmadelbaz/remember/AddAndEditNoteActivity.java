package com.ahmadelbaz.remember;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Notification.VISIBILITY_PUBLIC;
import static com.ahmadelbaz.remember.ListNotesActivity.arrayAdapter;
import static com.ahmadelbaz.remember.ListNotesActivity.notesAddressList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesCalenderList;
import static com.ahmadelbaz.remember.ListNotesActivity.notesList;

public class AddAndEditNoteActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    SharedPreferences prefs;

    ScrollView scrollView;

    int noteId;

    EditText noteText_editText;
    EditText noteTitle_editText;

    TextView addCalender;

    String newText = "";
    String oldText = "a";
    String newTitleText = "";
    String oldTitleText = "a";
    String newDateAndTime = "";
    String oldDateAndTime = "a";

    String inMessage;
    String inTitleMessage;
    // String e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_note);

        scrollView = findViewById(R.id.scrollView);

        noteText_editText = (EditText) findViewById(R.id.noteText_editText);
        noteTitle_editText = (EditText) findViewById(R.id.noteTitle_editText);

        addCalender = findViewById(R.id.addCalender);

        Intent i = getIntent();

        if (getIntent() != null) {
            String strdata = getIntent().getExtras().getString("Unique");

            if (strdata.equals("OldNote")) {
                noteText_editText.setEnabled(false);
                noteTitle_editText.setEnabled(false);
                inMessage = oldText;
                inTitleMessage = oldTitleText;
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            } else if (strdata.equals("NewNote")) {
                inMessage = newText;
                inTitleMessage = newTitleText;
                noteText_editText.setEnabled(true);
                noteTitle_editText.setEnabled(true);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }

        noteId = i.getIntExtra("noteId", -1);

        doInAddActivity(noteText_editText, notesList, oldText, 1);
        doInAddActivity(noteTitle_editText, notesAddressList, oldTitleText, 2);
        doInCalender(noteText_editText, notesCalenderList, oldDateAndTime, 3);

        checkNightMode();
    }

    private void doInCalender(EditText editableText, List<String> editedList, String editedString, final int a) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;

        if (noteId != -1) {

            newDateAndTime = "" + editedList.get(noteId);
            oldDateAndTime = "" + editedList.get(noteId);

            addCalender.setText("" + editedList.get(noteId));


            newDateAndTime = oldDateAndTime;
        } else {

            editedList.add("");

            addCalender.setText("" + dateAndTime);

            newDateAndTime = dateAndTime;
        }
    }

    private void doInAddActivity(EditText editableText, List<String> editedList, String editedString, final int a) {

        if (noteId != -1) {

            setTitle("Edit Note");

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

            setTitle("Add Note");

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
                getMenuInflater().inflate(R.menu.edit, menu);
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
                alarmTitle.setHint("Alarm Title");
                alarmTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                alarmTitle.setText(noteTitle_editText.getText().toString());
                alarmTitle.requestFocus();


                final EditText alarmContent = new EditText(AddAndEditNoteActivity.this);
                alarmContent.setHint("Alarm Note");
                alarmContent.setInputType(InputType.TYPE_CLASS_TEXT);
                alarmContent.setText("");
                alarmContent.requestFocus();

                LinearLayout newLayout = new LinearLayout(this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.VERTICAL,
                        LinearLayout.LayoutParams.MATCH_PARENT);


                alarmTitle.setLayoutParams(lp);
                alarmContent.setLayoutParams(lp);


                alarmTitle.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                //Add Attributes of Alertdialog.builder
                new AlertDialog.Builder(AddAndEditNoteActivity.this)
                        .setTitle("Set Alarm Title")
                        .setView(alarmTitle)
                        .setPositiveButton("Next", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                inMessage = alarmContent.getText().toString();
                                inTitleMessage = alarmTitle.getText().toString();

                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


                                // Display Fragment of timepicker
                                DialogFragment timePicker = new TimePickerFragment();
                                timePicker.show(getFragmentManager(), "timePicker");

                            }
                        })
                        .setNegativeButton("Cancle", null)
                        .show();

                return true;

            case R.id.editNote:

                if (noteText_editText.isEnabled()) {
                    noteText_editText.setEnabled(false);
                    noteTitle_editText.setEnabled(false);
                    item.setTitle("Edit");
                } else {
                    noteText_editText.setEnabled(true);
                    noteTitle_editText.setEnabled(true);
                    item.setTitle("Lock");

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                return true;

            case R.id.lockNote:

                if (noteText_editText.isEnabled()) {
                    noteText_editText.setEnabled(false);
                    noteTitle_editText.setEnabled(false);
                    item.setTitle("Edit");
                } else {
                    noteText_editText.setEnabled(true);
                    noteTitle_editText.setEnabled(true);
                    item.setTitle("Lock");

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                return true;

            case R.id.saveNote:

                Calendar c = Calendar.getInstance();

                String date = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
                String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

                saveButtonPressed(noteText_editText, noteTitle_editText, notesList, "MyUsers", newText, 1);
                saveButtonPressed(noteTitle_editText, noteText_editText, notesAddressList, "MyAddressUsers", newTitleText, 2);
                saveCalender(noteText_editText, notesCalenderList, "MyCalenderUsers", newDateAndTime, 3);

                // if any field is Empty tell the user
                if (noteTitle_editText.getText().toString().isEmpty() || noteText_editText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Empty Field !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Note Saved", Toast.LENGTH_SHORT).show();
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

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to save calender
    private void saveCalender(EditText editedText, List<String> editedList, String dbKey, String editableString, int a) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
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

            finish();
        }
    }

    // Method to save title and script
    private void saveButtonPressed(EditText editedText, EditText secondaryEdit, List<String> editedList, String editableString, String dbKey, int a) {

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

        finish();
    }

    public void saveNote_button(View view) {

        Calendar c = Calendar.getInstance();

        String date = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
        String time = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        String dateAndTime = date + " - " + time;

        saveButtonPressed(noteText_editText, noteTitle_editText, notesList, "MyUsers", newText, 1);
        saveButtonPressed(noteTitle_editText, noteText_editText, notesAddressList, "MyAddressUsers", newTitleText, 2);
        saveCalender(noteText_editText, notesCalenderList, "MyCalenderUsers", newDateAndTime, 3);

        // if any field is Empty tell the user
        if (noteTitle_editText.getText().toString().isEmpty() || noteText_editText.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Empty Field !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Note Saved", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public void cancelNote_button(View view) {
        if (noteId == -1) {

            notesList.remove(notesList.size() - 1);
            arrayAdapter.notifyDataSetChanged();


            notesAddressList.remove(notesAddressList.size() - 1);
            arrayAdapter.notifyDataSetChanged();

            notesCalenderList.remove(notesCalenderList.size() - 1);
            arrayAdapter.notifyDataSetChanged();

        }

        finish();
    }

    private void checkNightMode() {

        prefs = this.getSharedPreferences("NightModeKey", Context.MODE_PRIVATE);

        if (prefs.getBoolean("NightModeIsOn", false)) {

            // Turn to Night mode

            noteText_editText.setTextColor(Color.WHITE);
            noteText_editText.setBackgroundColor(Color.BLACK);
            noteTitle_editText.setTextColor(Color.WHITE);
            noteTitle_editText.setBackgroundColor(Color.BLACK);
            scrollView.setBackgroundColor(Color.BLACK);
            addCalender.setTextColor(Color.WHITE);

        } else {

            noteText_editText.setTextColor(Color.BLACK);
            noteText_editText.setBackgroundColor(Color.WHITE);
            noteTitle_editText.setTextColor(Color.BLACK);
            noteTitle_editText.setBackgroundColor(Color.WHITE);
            scrollView.setBackgroundColor(Color.WHITE);
            addCalender.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {

        //Intent to Device Alarm with your data(Time and Note title)
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, inTitleMessage);
        intent.putExtra(AlarmClock.EXTRA_HOUR, i);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, i1);
        startActivity(intent);

    }
}