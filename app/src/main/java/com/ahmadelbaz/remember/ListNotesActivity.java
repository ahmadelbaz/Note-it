package com.ahmadelbaz.remember;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListNotesActivity extends AppCompatActivity {

    //instantiation

    SharedPreferences prefs;

    android.support.design.widget.CoordinatorLayout menu_background;

    EditText noteText_editText;
    EditText noteTitle_editText;

    TinyDB tinydb;

    TinyDB tinyTitledb;

    ListView addNote_listView;

    int realPosetiotn;

    static List<String> notesList;

    static List<String> notesAddressList;

    static List<String> notesCalenderList;

    static ArrayAdapter<String> arrayAdapter;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        menu_background = findViewById(R.id.menu_background);

        noteText_editText = (EditText) findViewById(R.id.noteText_editText);
        noteTitle_editText = (EditText) findViewById(R.id.noteTitle_editText);

        tinydb = new TinyDB(getApplicationContext());
        tinyTitledb = new TinyDB(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ListNotesActivity.this, AddAndEditNoteActivity.class);
                intent.putExtra("Unique", "NewNote");
                startActivity(intent);
            }
        });

        addNote_listView = findViewById(R.id.addNote_listView);
        notesList = new ArrayList<String>();
        notesAddressList = new ArrayList<String>();
        notesCalenderList = new ArrayList<String>();

        notesList = tinydb.getListString("MyUsers");
        notesAddressList = tinyTitledb.getListString("MyAddressUsers");
        notesCalenderList = tinyTitledb.getListString("MyCalenderUsers");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notesAddressList);

        addNote_listView.setAdapter(arrayAdapter);

        clickEditNote();

        longClickDeletable();

        checkNightMode();
    }

    // check if user used night mode or not from settings
    private void checkNightMode() {

        prefs = this.getSharedPreferences("NightModeKey", Context.MODE_PRIVATE);

        if (prefs.getBoolean("NightModeIsOn", false)) {


            // Turn to NIght mode

            menu_background.setBackgroundColor(Color.BLACK);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_dropdown_item_1line, notesAddressList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView textView = (TextView) view.findViewById(android.R.id.text1);

                    /*YOUR CHOICE OF COLOR*/
                    textView.setTextColor(Color.WHITE);

                    return view;
                }
            };

            /*SET THE ADAPTER TO LISTVIEW*/
            addNote_listView.setAdapter(adapter);


        } else {

            menu_background.setBackgroundColor(Color.parseColor("#FFF9C4"));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_dropdown_item_1line, notesAddressList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView textView = (TextView) view.findViewById(android.R.id.text1);

                    /*YOUR CHOICE OF COLOR*/
                    textView.setTextColor(Color.BLACK);

                    return view;
                }
            };

            /*SET THE ADAPTER TO LISTVIEW*/
            addNote_listView.setAdapter(adapter);

        }
    }

    // what will happen if user click on note to read or edit it
    private void clickEditNote() {
        addNote_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String selectedFromList = addNote_listView.getItemAtPosition(position).toString();

                for (int n = 0; n < notesAddressList.size(); n++) {
                    if (selectedFromList == notesAddressList.get(n)) {
                        realPosetiotn = n;
                    }
                }

                Intent intent = new Intent(ListNotesActivity.this, AddAndEditNoteActivity.class);

                intent.putExtra("noteId", realPosetiotn);
                intent.putExtra("Unique", "OldNote");

                startActivity(intent);

            }
        });
    }

    // what will happen if user click a long click on note to delete(till now) it
    private void longClickDeletable() {


        addNote_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {

                new AlertDialog.Builder(ListNotesActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are You Sure?")
                        .setMessage("Do You Want to Delete this Note?")
                        .setPositiveButton("Yes, Sure", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tinydb.remove("MyUsers");
                                tinyTitledb.remove("MyAddressUsers");
                                tinyTitledb.remove("MyCalenderUsers");
                                notesList.remove(pos);
                                notesAddressList.remove(pos);
                                notesCalenderList.remove(pos);
                                arrayAdapter.notifyDataSetChanged();

                                tinydb.putListString("MyUsers", (ArrayList<String>) notesList);
                                tinyTitledb.putListString("MyAddressUsers", (ArrayList<String>) notesAddressList);
                                tinyTitledb.putListString("MyCalenderUsers", (ArrayList<String>) notesCalenderList);
                                refreshMenu();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        getMenuInflater().inflate(R.menu.refresh, menu);
        getMenuInflater().inflate(R.menu.search, menu);
        getMenuInflater().inflate(R.menu.settings, menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final ArrayList<String> userList = new ArrayList<>();

                for (String user1 : notesAddressList) {

                    if (user1.toLowerCase().contains(s.toLowerCase())) {
                        userList.add(user1);
                    }
                }

                for (int n = 0; n < notesList.size(); n++) {

                    if (notesList.get(n).toLowerCase().contains(s.toLowerCase())) {
                        userList.add(notesAddressList.get(n));
                    }
                }
                Set<String> set = new HashSet<>(userList);
                userList.clear();
                userList.addAll(set);

                prefs = getApplicationContext().getSharedPreferences("NightModeKey", Context.MODE_PRIVATE);

                if (prefs.getBoolean("NightModeIsOn", false)) {


                    // Turn to Night mode

                    menu_background.setBackgroundColor(Color.BLACK);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            ListNotesActivity.this, android.R.layout.simple_dropdown_item_1line, userList) {

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            TextView textView = (TextView) view.findViewById(android.R.id.text1);

                            /*YOUR CHOICE OF COLOR*/
                            textView.setTextColor(Color.WHITE);

                            return view;
                        }
                    };

                    ListView lv = (ListView) findViewById(R.id.addNote_listView);

                    lv.setAdapter(adapter);

                } else {

                    //Turn to light mode

                    menu_background.setBackgroundColor(Color.parseColor("#FFF9C4"));

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            ListNotesActivity.this, android.R.layout.simple_dropdown_item_1line, userList) {

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            TextView textView = (TextView) view.findViewById(android.R.id.text1);

                            /*YOUR CHOICE OF COLOR*/
                            textView.setTextColor(Color.BLACK);

                            return view;
                        }
                    };

                    /*SET THE ADAPTER TO LISTVIEW*/
                    addNote_listView.setAdapter(adapter);

                }

                if (s.isEmpty()) {
                    refreshMenu();
                }

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.deleteAllNotes:

                new AlertDialog.Builder(ListNotesActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are You Sure?")
                        .setMessage("Do You Want to Delete All Notes?")
                        .setPositiveButton("Yes, Sure", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notesList.clear();
                                notesAddressList.clear();
                                notesCalenderList.clear();
                                arrayAdapter.notifyDataSetChanged();
                                tinydb.remove("MyUsers");
                                tinyTitledb.remove("MyAddressUsers");
                                tinyTitledb.remove("MyCalenderUsers");
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

            case R.id.refreshList:
                refreshMenu();
                return true;

            case R.id.settingsOption:
                Intent intent = new Intent(ListNotesActivity.this, Settings.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // method to refresh the list
    private void refreshMenu() {

        checkNightMode();

        onResume();
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (doubleBackToExitPressedOnce) {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                finish();
                return super.onKeyDown(keyCode, event);
            }
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
            return false;
        }
        return false;
    }
}