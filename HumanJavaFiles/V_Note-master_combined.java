package com.example.afsal.notes;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}

package com.example.afsal.notes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package com.example.afsal.notes.controller;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.afsal.notes.model.Note;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class NoteController {

    private static final String FILENAME = "MYDATAFILE";
    private SharedPreferences dataSet;
    private Map<String, String> myMap, resultMap;

    public NoteController(Context context) {
        dataSet = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        getAllNotes();
    }

    public Note getNewNoteObject() {
        String Key = new Date().toString();
        Note obj = new Note();
        obj.setKey(Key);
        return obj;
    }

    public Map<String, String> getAllNotes() {
        resultMap = new HashMap<>();
        myMap = (Map<String, String>) dataSet.getAll();
        SortedSet<String> sorted = new TreeSet<>(myMap.keySet());
        for (String str : sorted) {
            resultMap.put(str, myMap.get(str));
        }
        myMap.clear();
        return resultMap;
    }

    public void addNewNote(Note newNote) {
        SharedPreferences.Editor myEditor = dataSet.edit();
        myEditor.putString(newNote.getKey(), newNote.getData());
        myEditor.apply();
    }

    public void deleteNote(Note note) {
        if (dataSet.contains(note.getKey())) {
            SharedPreferences.Editor myEditor = dataSet.edit();
            myEditor.remove(note.getKey());
            myEditor.apply();
        } else
            Log.e("dataHandler", "CAnt Find Note To Delete");
    }
}


package com.example.afsal.notes.pref;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link android.preference.PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public abstract class AppCompatPreferenceActivity extends PreferenceActivity {

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }
}


package com.example.afsal.notes.pref;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.afsal.notes.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_general);

    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}


package com.example.afsal.notes.model;


public class Note {
    private String key;
    private String data;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}


package com.example.afsal.notes.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.afsal.notes.R;
import com.example.afsal.notes.controller.NoteController;
import com.example.afsal.notes.model.Note;
import com.example.afsal.notes.pref.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    public static final String KEY = "NOTE_KEY";
    public static final int NEW_NOTE = 100;
    public static final int DELETE_ID = 1010;
    public static final int SHARE_ID = 1011;
    public static final String DATA = "NOTE_DATA";
    public static final int UPDATE_NOTE = 101;

    long currentNote;
    ListView listHolder;
    ArrayAdapter adapter;
    Map<String, String> noteMap;
    List<String> noteListVals;
    List<String> noteListKeys;
    NoteController dataHandeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_act);

        dataHandeler = new NoteController(this);
        listHolder = findViewById(R.id.listView);
        listHolder.setOnItemClickListener(this);

        noteMap = dataHandeler.getAllNotes();
        noteListVals = new ArrayList<>(noteMap.values());
        noteListKeys = new ArrayList<>(noteMap.keySet());

        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_lay, noteListVals);
        listHolder.setAdapter(adapter);

        registerForContextMenu(listHolder);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewNote();
            }
        });

        handleIncomingData();

        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean fullRequest = myPrefs.getBoolean("full_screen_switch", false);
        if (fullRequest) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void handleIncomingData() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    createNewNote(sharedText);
                }
            }
        }
    }

    public void createNewNote() {
        Intent newIntent = new Intent(getBaseContext(), NewNote.class);
        Note note = dataHandeler.getNewNoteObject();
        newIntent.putExtra(KEY, note.getKey());
        newIntent.putExtra(DATA, "");
        startActivityForResult(newIntent, NEW_NOTE);
    }

    public void createNewNote(String data) {
        Intent newIntent = new Intent(getBaseContext(), NewNote.class);
        Note note = dataHandeler.getNewNoteObject();
        newIntent.putExtra(KEY, note.getKey());
        newIntent.putExtra(DATA, data);
        startActivityForResult(newIntent, NEW_NOTE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Note tempNote = new Note();
        tempNote.setData(noteListVals.get(position));
        tempNote.setKey(noteListKeys.get(position));

        Intent newIntent = new Intent(getBaseContext(), NewNote.class);
        newIntent.putExtra(KEY, tempNote.getKey());
        newIntent.putExtra(DATA, tempNote.getData());
        startActivityForResult(newIntent, UPDATE_NOTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case NEW_NOTE:
                    String key = data.getStringExtra(NewNote.KEY_ID);
                    String value = data.getStringExtra(NewNote.DATA_ID);
                    Log.d("LOG", "Back From NEW_NOTE Activity --- " + data);
                    Note note = dataHandeler.getNewNoteObject();
                    note.setKey(key);
                    note.setData(value);
                    dataHandeler.addNewNote(note);
                    Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
                    Refresh();
                    break;
                case UPDATE_NOTE:
                    key = data.getStringExtra(NewNote.KEY_ID);
                    value = data.getStringExtra(NewNote.DATA_ID);
                    Log.d("LOG", "Back From UPDATE Activity --- " + data);
                    note = dataHandeler.getNewNoteObject();
                    note.setKey(key);
                    note.setData(value);
                    dataHandeler.addNewNote(note);
                    Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
                    Refresh();
                    break;
            }
        else
            Toast.makeText(this, "Note Discarded", Toast.LENGTH_LONG).show();

    }

    private void Refresh() {
        noteMap = dataHandeler.getAllNotes();
        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_lay, new ArrayList(noteMap.values()));
        noteListKeys.clear();
        noteListVals.clear();
        noteListVals = new ArrayList<>(noteMap.values());
        noteListKeys = new ArrayList<>(noteMap.keySet());
        listHolder.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        currentNote = ((AdapterView.AdapterContextMenuInfo) menuInfo).id;
        menu.add(0, DELETE_ID, 0, "Delete");
        menu.add(0, SHARE_ID, 1, "Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                int location = (int) currentNote;
                Log.d("LOG", "Item to delete - KEY - " + noteListKeys.get(location) + " - VAL - " + noteListVals.get(location));
                Note deleteNote = new Note();
                deleteNote.setData(noteListVals.get(location));
                deleteNote.setKey(noteListKeys.get(location));
                dataHandeler.deleteNote(deleteNote);
                Toast.makeText(getBaseContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                Refresh();
                break;
            case SHARE_ID:
                location = (int) currentNote;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, noteListVals.get(location));
                startActivity(Intent.createChooser(share, "Share Note With "));
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    public void loadPreferences(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}


package com.example.afsal.notes.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afsal.notes.R;

public class NewNote extends AppCompatActivity {

    public static final String TO_SAVE = "TEXT_TO_SAVE";
    public static final String DATA_ID = "1001";
    public static final String KEY_ID = "1002";
    String key, dataVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_note);
        key = getIntent().getStringExtra(MainActivity.KEY);
        dataVal = getIntent().getStringExtra(MainActivity.DATA);
        EditText editText = findViewById(R.id.input_panel);
        editText.setText(dataVal);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveNote();
                return true;
        }
        return false;
    }

    private void saveNote() {
        EditText editText = findViewById(R.id.input_panel);
        if (editText.getText().length() > 1) {
            Intent output = new Intent();
            output.putExtra(DATA_ID, editText.getText().toString());
            output.putExtra(KEY_ID, key);
            setResult(RESULT_OK, output);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        saveNote();
        super.onBackPressed();
    }

    public void discardNote(MenuItem item) {
        setResult(RESULT_CANCELED);
        Toast.makeText(getApplicationContext(), "Changes Discarded", Toast.LENGTH_SHORT).show();
        finish();
    }

}


