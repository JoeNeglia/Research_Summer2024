package com.example.mohaiminur.com.englishdictonary;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals( "com.example.mohaiminur.com.englishdictonary", appContext.getPackageName() );
    }
}


package com.example.mohaiminur.com.englishdictonary;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals( 4, 2 + 2 );
    }
}

package com.example.mohaiminur.com.englishdictonary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;


public class RecyclerViewAdapterHistory extends RecyclerView.Adapter<RecyclerViewAdapterHistory.HistoryViewHolder> {

    private ArrayList<History> histories;
    private Context context;

    public RecyclerViewAdapterHistory(Context context, ArrayList<History> histories) {
        this.histories = histories;
        this.context = context;
    }

    public  class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView enWord;
        TextView enDef;


        public HistoryViewHolder(View v) {
            super(v);
            enWord = (TextView) v.findViewById(R.id.en_word);
            enDef = (TextView) v.findViewById(R.id.en_def);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String text = histories.get(position).get_en_word();

                    Intent intent = new Intent(context, WordMeaningActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("en_word", text);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }
    }


    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout, parent, false);
        return new HistoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(HistoryViewHolder holder, final int position) {
        holder.enWord.setText(histories.get(position).get_en_word());
        holder.enDef.setText(histories.get(position).get_def());

    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}


package com.example.mohaiminur.com.englishdictonary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    SearchView search;

    static DatabaseHelper myDbHelper;
    static boolean databaseOpened=false;

    SimpleCursorAdapter suggestionAdapter;

    ArrayList<History> historyList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter historyAdapter;

    RelativeLayout emptyHistory;
    Cursor cursorHistory;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search =  (SearchView) findViewById(R.id.search_view);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                search.setIconified(false);

            }
        });


        myDbHelper = new DatabaseHelper(this);

        if(myDbHelper.checkDataBase())
        {
            openDatabase();

        }
        else
        {
            LoadDatabaseAsync task = new LoadDatabaseAsync(MainActivity.this);
            task.execute();
        }



        // setup SimpleCursorAdapter

        final String[] from = new String[] {"en_word"};
        final int[] to = new int[] {R.id.suggestion_text};

        suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                R.layout.suggestion_row, null, from, to, 0){
            @Override
            public void changeCursor(Cursor cursor) {
                super.swapCursor(cursor);
            }

        };

        search.setSuggestionsAdapter(suggestionAdapter);


        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {

                // Add clicked text to search box
                CursorAdapter ca = search.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();
                cursor.moveToPosition(position);
                String clicked_word =  cursor.getString(cursor.getColumnIndex("en_word"));
                search.setQuery(clicked_word,false);

                //search.setQuery("",false);

                search.clearFocus();
                search.setFocusable(false);

                Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("en_word",clicked_word);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                // Your code here
                return true;
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {

                String text =  search.getQuery().toString();

                Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher m = p.matcher(text);

                if(m.matches())
                {
                    Cursor c = myDbHelper.getMeaning(text);

                    if(c.getCount()==0)
                    {
                        showAlertDialog();
                    }

                    else
                    {
                        //search.setQuery("",false);
                        search.clearFocus();
                        search.setFocusable(false);

                        Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("en_word",text);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }

                }

                else
                {
                    showAlertDialog();
                }



                return false;
            }


            @Override
            public boolean onQueryTextChange(final String s) {
                search.setIconifiedByDefault(false); //Give Suggestion list margins

                Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher m = p.matcher(s);

                if(m.matches()) {
                    Cursor cursorSuggestion=myDbHelper.getSuggestions(s);
                    suggestionAdapter.changeCursor(cursorSuggestion);
                }


                return false;
            }

        });


        emptyHistory = (RelativeLayout) findViewById(R.id.empty_history);

        //recycler View
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_history);
        layoutManager = new LinearLayoutManager(MainActivity.this);

        recyclerView.setLayoutManager(layoutManager);

        fetch_history();

    }


    protected static void openDatabase()
    {
        try {
            myDbHelper.openDataBase();
            databaseOpened=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetch_history()
    {
        historyList=new ArrayList<>();
        historyAdapter = new RecyclerViewAdapterHistory(this,historyList);
        recyclerView.setAdapter(historyAdapter);

        History h;

        if(databaseOpened)
        {
            cursorHistory=myDbHelper.getHistory();
            if (cursorHistory.moveToFirst()) {
                do {
                    h= new History(cursorHistory.getString(cursorHistory.getColumnIndex("word")),cursorHistory.getString(cursorHistory.getColumnIndex("en_definition")));
                    historyList.add(h);
                }
                while (cursorHistory.moveToNext());
            }

            historyAdapter.notifyDataSetChanged();
        }


        if (historyAdapter.getItemCount() == 0)
        {
            emptyHistory.setVisibility(View.VISIBLE);
        }
        else
        {
            emptyHistory.setVisibility(View.GONE);
        }
    }

    private void showAlertDialog()
    {
        search.setQuery("",false);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("Word Not Found");
        builder.setMessage("Please search again");

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        search.clearFocus();
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_exit) {
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetch_history();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }







}


package com.example.mohaiminur.com.englishdictonary;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.io.IOException;

public class LoadDatabaseAsync extends AsyncTask <Void, Void, Boolean>  {
    private Context context;
    private AlertDialog alertDialog;
    private DatabaseHelper myDbHelper;


    public LoadDatabaseAsync(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();


        AlertDialog.Builder d = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Light_Dialog_Alert);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_diolog_database_copying, null);
        d.setTitle("Loading Database...");
        d.setView(dialogView);
        alertDialog = d.create();

        alertDialog.setCancelable(false);
        alertDialog.show();

    }


    @Override
    protected Boolean doInBackground(Void... params) {

        myDbHelper = new DatabaseHelper(context);

        try {
            myDbHelper.createDataBase();

        } catch (IOException e) {

            throw new Error ("Database was not created");
        }
        myDbHelper.close();
        return null;
    }

    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        alertDialog.dismiss();
        MainActivity.openDatabase();
    }



}


package com.example.mohaiminur.com.englishdictonary;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.mohaiminur.com.englishdictonary.fragments.FragmentAntonyms;
import com.example.mohaiminur.com.englishdictonary.fragments.FragmentDefinition;
import com.example.mohaiminur.com.englishdictonary.fragments.FragmentExample;
import com.example.mohaiminur.com.englishdictonary.fragments.FragmentSynonyms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordMeaningActivity extends AppCompatActivity {

    private ViewPager viewPager;

    String enWord;
    DatabaseHelper myDbHelper;
    Cursor c = null;

    public String enDefinition;
    public String example;
    public String synonyms;
    public String antonyms;

    TextToSpeech tts;

    boolean startedFromShare=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_meaning);

        //received values
        Bundle bundle = getIntent().getExtras();
        enWord= bundle.getString("en_word");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                startedFromShare=true;

                if (sharedText != null) {
                    Pattern p = Pattern.compile("[A-Za-z ]{1,25}");
                    Matcher m = p.matcher(sharedText);

                    if(m.matches())
                    {
                        enWord=sharedText;
                    }
                    else
                    {
                        enWord="Not Available";
                    }

                }

            }
        }



        myDbHelper = new DatabaseHelper(this);

        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }


        c = myDbHelper.getMeaning(enWord);

        if (c.moveToFirst()) {

            enDefinition= c.getString(c.getColumnIndex("en_definition"));
            example=c.getString(c.getColumnIndex("example"));
            synonyms=c.getString(c.getColumnIndex("synonyms"));
            antonyms=c.getString(c.getColumnIndex("antonyms"));

            myDbHelper.insertHistory(enWord);

        }

        else
        {
            enWord="Not Available";
        }




        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);


        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts = new TextToSpeech(WordMeaningActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        // TODO Auto-generated method stub
                        if(status == TextToSpeech.SUCCESS){
                            int result=tts.setLanguage(Locale.getDefault());
                            if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                               Log.e("error", "This Language is not supported");
                            }
                            else{
                                tts.speak(enWord, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                        else
                            Log.e("error", "Initialization Failed!");
                    }
                });
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(enWord);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        viewPager = (ViewPager) findViewById(R.id.tab_viewpager);

        if (viewPager != null){
            setupViewPager(viewPager);
        }

        //tabLayout

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });




    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        void addFrag(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }


    }


    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FragmentDefinition(), "Definition");
        adapter.addFrag(new FragmentSynonyms(), "Synonyms");
        adapter.addFrag(new FragmentAntonyms(), "Antonyms");
        adapter.addFrag(new FragmentExample(), "Example");
        viewPager.setAdapter(adapter);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            if(startedFromShare)
            {
                Intent intent = new Intent(this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else
            {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}


package com.example.mohaiminur.com.englishdictonary;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private String DB_PATH = null;
    private static String DB_NAME = "eng_dictionary.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        Log.e("Path 1", DB_PATH);

    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (!dbExist) {

            this.getReadableDatabase();
            try {
                myContext.deleteDatabase(DB_NAME);
                copyDataBase();

            } catch (IOException e) {
                throw new Error("Error copying database");
            }

        }

    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e)
        {
            //
        }
        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }


    private void copyDataBase() throws IOException {

        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
        Log.i("copyDataBase", "Database copied");


    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }


    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }




    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            this.getReadableDatabase();
            myContext.deleteDatabase(DB_NAME);
            copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public Cursor getMeaning(String text)
    {
        Cursor c= myDataBase.rawQuery("SELECT en_definition,example,synonyms,antonyms FROM words WHERE en_word==UPPER('"+text+"')",null);
        return c;
    }

    public Cursor getSuggestions(String text)
    {
        Cursor c= myDataBase.rawQuery("SELECT _id, en_word FROM words WHERE en_word LIKE '"+text+"%' LIMIT 40",null);
        return c;
    }

    public void  insertHistory(String text)
    {
        myDataBase.execSQL("INSERT INTO history(word) VALUES(UPPER('"+text+"'))");

    }

    public Cursor getHistory()
    {
        Cursor c= myDataBase.rawQuery("select distinct  word, en_definition from history h join words w on h.word==w.en_word order by h._id desc",null);
        return c;
    }


    public void  deleteHistory()
    {
        myDataBase.execSQL("DELETE  FROM history");
    }








}


package com.example.mohaiminur.com.englishdictonary;


public class History {

    private String en_word;
    private String en_def;

    public History(String en_word,String en_def)
    {
        this.en_word=en_word;
        this.en_def=en_def;
    }

    public String get_en_word()
    {
        return en_word;
    }

    public String get_def()
    {
        return en_def;
    }

}


package com.example.mohaiminur.com.englishdictonary;

import android.content.DialogInterface;
import android.database.SQLException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    DatabaseHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);


        TextView clearHistory=(TextView) findViewById(R.id.clear_history);

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                myDbHelper = new DatabaseHelper(SettingsActivity.this);
                try {
                    myDbHelper.openDataBase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                showAlertDialog();
            }
        });
    }


    private void showAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("Are you sure?");
        builder.setMessage("All the history will be deleted");

        String positiveText = "Yes";
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDbHelper.deleteHistory();
                    }
                });

        String negativeText = "No";
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}


package com.example.mohaiminur.com.englishdictonary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mohaiminur.com.englishdictonary.R;
import com.example.mohaiminur.com.englishdictonary.WordMeaningActivity;

public class FragmentSynonyms extends Fragment {
    public FragmentSynonyms() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition,container, false);//Inflate Layout

        Context context=getActivity();
        TextView text = (TextView) view.findViewById(R.id.textViewD);//Find textView Id

        String synonyms= ((WordMeaningActivity)context).synonyms;

        if(synonyms!=null)
        {
            synonyms = synonyms.replaceAll(",", ",\n");
            text.setText(synonyms);
        }
        if(synonyms==null)
        {
            text.setText("No synonyms found");
        }

        return view;
    }
}


package com.example.mohaiminur.com.englishdictonary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mohaiminur.com.englishdictonary.R;
import com.example.mohaiminur.com.englishdictonary.WordMeaningActivity;

public class FragmentDefinition extends Fragment {
    public FragmentDefinition() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition,container, false);//Inflate Layout


        Context context=getActivity();
        TextView text = (TextView) view.findViewById(R.id.textViewD);

        String en_definition= ((WordMeaningActivity)context).enDefinition;

        text.setText(en_definition);
        if(en_definition==null)
        {
            text.setText("No definition found");
        }

        return view;
    }
}


package com.example.mohaiminur.com.englishdictonary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mohaiminur.com.englishdictonary.R;
import com.example.mohaiminur.com.englishdictonary.WordMeaningActivity;

public class FragmentAntonyms extends Fragment {
    public FragmentAntonyms() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition,container, false);//Inflate Layout

        Context context=getActivity();
        TextView text = (TextView) view.findViewById(R.id.textViewD);//Find textView Id
        String antonyms= ((WordMeaningActivity)context).antonyms;
        if(antonyms!=null)
        {
            antonyms = antonyms.replaceAll(",", ",\n");
            text.setText(antonyms);
        }
        if(antonyms==null)
        {
            text.setText("No antonyms found");
        }

        return view;
    }
}


package com.example.mohaiminur.com.englishdictonary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mohaiminur.com.englishdictonary.R;
import com.example.mohaiminur.com.englishdictonary.WordMeaningActivity;

public class FragmentExample extends Fragment {
    public FragmentExample() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition,container, false);//Inflate Layout

        Context context=getActivity();

        TextView text = (TextView) view.findViewById(R.id.textViewD);//Find textView Id

        String example= ((WordMeaningActivity)context).example;
        text.setText(example);

        if(example==null)
        {
            text.setText("No Example found");
        }


        return view;
    }
}


