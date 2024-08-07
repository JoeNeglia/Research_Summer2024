package com.example.branchmemo;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.branchmemo", appContext.getPackageName());
    }
}

package com.example.branchmemo;

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
        assertEquals(4, 2 + 2);
    }
}

//package com.example.branchmemo;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.EditText;
//
//import java.util.List;
//
//public class ViewNoteActivity extends AppCompatActivity {
//    public static Context mContext;
//    Toolbar toolbar;
//    public static ActionBar actionBar;
//    EditText notename, titleTxt, contentTxt;
//
//    @SuppressLint("WrongViewCast")
//    @Overrides
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_view_note);
//        mContext = this;
//
//        toolbar = findViewById(R.id.toolbar_viewnote);
//        setSupportActionBar(toolbar);
//        actionBar = getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(false);//기본 제목
//        actionBar.setDisplayHomeAsUpEnabled(true); //툴바의 뒤로가기 버튼
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.view_note_menu, menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                MainActivity.DBModel.updateNote(memoCode, L_noteName.getText().toString(), L_VosDate, 0);
//                return true;
//            case R.id.action_branch:
//
//                return true;
//            case R.id.action_save:
//                NewnoteActivity.insertData(notename, titleTxt,  contentTxt, NewnoteActivity.mContext);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}

package com.example.branchmemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Date;
import java.util.List;

public class ViewBranchActivity extends AppCompatActivity {
    public static Context mContext;
    Toolbar toolbar;
    public static ActionBar actionBar;
    public RecyclerView rv;
    String memoCode;
    EditText L_title, L_content, L_noteName;
    String L_preTitle;
    TextView L_Date;
    Button L_Btn;
    ImageView L_branch;
    CheckBox L_chxbox;
    Date L_VosDate;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_branch);
        mContext = this;

        Intent memoIntent = getIntent();
        memoCode = memoIntent.getStringExtra("code");

        toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목
        actionBar.setDisplayHomeAsUpEnabled(true); //툴바의 뒤로가기 버튼

        L_noteName = findViewById(R.id.notename);
        L_title = findViewById(R.id.L_title);
        L_content = findViewById(R.id.L_content);
        L_Date = findViewById(R.id.L_Date);
        L_Btn = findViewById(R.id.L_savebtn);
        L_branch = findViewById(R.id.L_image);
        L_chxbox = findViewById(R.id.saveCheckbox);

        rv = findViewById(R.id.rec);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        MainActivity.DBModel.loadNote(memoCode); //getData
        L_preTitle = L_title.getText().toString();

        L_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title;
                String input_title = L_title.getText().toString().replace(" ", "");
                if(input_title.length()<=0 || input_title==null){
                    title = input_title;
                }else{
                    title = L_title.getText().toString();
                }
                Date date = new Date(System.currentTimeMillis());
                MemoVo memo = new MemoVo(memoCode, title, L_content.getText().toString(), date);

                String notename = L_noteName.getText().toString();
                MainActivity.DBModel.updateNote(memoCode, notename, L_VosDate, 1);

                if(L_chxbox.isChecked()){
                    MainActivity.DBModel.updateMemo(memo, notename);
                }else{
                    MainActivity.DBModel.insertMemo(memo, notename);
                }

            }
        });

    }//end of onCreate

    public void getData(final List<MemoVo> memoVos, final String Notename) {

            MemoVo last_memo = memoVos.remove(memoVos.size() - 1);
            L_VosDate = last_memo.getDateval();
            L_noteName.setText(Notename);
            L_title.setText(last_memo.getTitle());
            L_content.setText(last_memo.getContentbody());
            L_Date.setText(MainActivity.date.format(last_memo.getDateval())+" "+MainActivity.time24.format(last_memo.getDateval()));
            if(memoVos.size()>0){
                L_branch.setImageResource(R.drawable.finish);
            }else{
                L_branch.setImageResource(R.drawable.circle);
            }

            MemoAdapter adapter = new MemoAdapter(memoVos);
            rv.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_branch_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MainActivity.DBModel.updateNote(memoCode, L_noteName.getText().toString(), L_VosDate, 0);
                return true;
            case R.id.action_delete:
                deleteThis();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteThis(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title).setMessage(R.string.dialog_message)
            .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //return;
                }
            })
            .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.DBModel.deleteNote(memoCode);
                }
            });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

package com.example.branchmemo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MemoVo.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class MemoDatabase extends RoomDatabase {
    private static MemoDatabase INSTANCE;

    public abstract MemoDao memeDao();

    public static synchronized MemoDatabase getAppDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, MemoDatabase.class, "Memo-db")
                    .build();
        }
        return INSTANCE;
    }
    public static void destroyInstance(){
        INSTANCE = null;
    }

}


package com.example.branchmemo;

import androidx.room.TypeConverter;

import java.sql.Date;

public class DateConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}


package com.example.branchmemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Date;

public class NewnoteActivity extends AppCompatActivity {
    public static Context mContext;
    Toolbar toolbar;
    public static ActionBar actionBar;
    EditText notename, titleTxt, contentTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        mContext = this;

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목
        actionBar.setDisplayHomeAsUpEnabled(true); //툴바의 뒤로가기 버튼

        notename = findViewById(R.id.Totaltitle);
        titleTxt = findViewById(R.id.TitleView);
        contentTxt = findViewById(R.id.ContentView);

    }//end of onCreate

    public static void insertData(EditText notename, EditText titleTxt,EditText contentTxt, Context context) {
        String code, title, content;
        Date date = new Date(System.currentTimeMillis());

        //content
        content = contentTxt.getText().toString().replace("\n", " ");
        //title
        title = titleTxt.getText().toString();
        if (content == null || content.length() == 0) { //검열
            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show();
        } else {
            if (title == null || title.length() == 0) { //대체 여부 결정
                String temp_title;
                if (content.length()>25) {
                    temp_title = content.substring(25);
                }else{
                    temp_title = content;
                }
                title = temp_title;
            }
            content = contentTxt.getText().toString();
            code = (new CodeCreater()).getNewCode();

            String notenameTxt;
            if(notename.getText().toString().length() ==0 || notename.getText().toString()==null){
                notenameTxt = title;
            }else notenameTxt = notename.getText().toString();

            MemoVo memo = new MemoVo(code, title, content, date);
            MemoListVo memolist = new MemoListVo(memo.getCode(), notenameTxt, memo.getDateval());

            MainActivity.DBModel.creatNewMemo(memo, memolist); //finish
        }//end of if
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                insertData(notename, titleTxt,  contentTxt, NewnoteActivity.mContext);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.example.branchmemo;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Scanner;
//
//public class LocaldataDAO {
//    File file;
//    public LocaldataDAO() throws IOException {
//        file = new File("../../../../../../localcode.txt");
//        FileWriter write = new FileWriter(file, true);
//        write.write(""); write.flush();
//        write.close();
//    }
//
//    public String readcode() throws IOException {
//        Scanner scan = new Scanner(file);
//        String data = "";
//        while(scan.hasNextLine()){
//            data += scan.nextLine()+"/";
//        }
//        data = data.substring(0, data.length()-1);
//        scan.close();
//        return data; //"[code]/[code]/...[code]"
//    }
//
//    public void writecode(String code) throws IOException {
//        FileWriter write = new FileWriter(file, true);
//        write.write("\ncode"); //한줄 추가가 목적
//        write.flush();
//        write.close();
//    }
//
//    public void deletecode(){
//
//    }
//
//}


package com.example.branchmemo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.sql.Date;

public class MainViewModel extends AndroidViewModel {

    //**[IMPORTANT] Instance of DBRepository
    private DBRepository repository;

    //Constructor
    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new DBRepository(application);
    }

    //Memo
    synchronized public void insertMemo(MemoVo memo, String notename){
        repository.insertMemo(memo, notename);
    }
    synchronized public void deleteMemo(MemoVo memo) { repository.deleteMemo(memo); }
    public MemoDao getMemoDao(){ return repository.getMemoDao(); }
    synchronized public void updateMemo(MemoVo memo, String notename){
        repository.updateMemo(memo, notename);
    }

    synchronized public void insertMemoList(MemoListVo memoList){
        repository.insertMemoList(memoList);
    }
    synchronized public void deleteMemoList(String code) { repository.deleteMemoList(code); }
    public MemoListDao getMemoListDao(){ return repository.getMemoListDao(); }

    //Other functions
    synchronized public int selectCode(String code) { return repository.selectCode(code); }
    synchronized public void viewNote(int pos){  repository.viewNote(pos); }
    synchronized public void loadNote(String code){ repository.loadNote(code);}

    synchronized public void creatNewMemo(MemoVo memo, MemoListVo memolist) {
        repository.createNew(memo, memolist);
    }

    synchronized public void deleteNote(String memoCode) {
        repository.deleteNote(memoCode);
    }

    synchronized public void loadNoteList() { repository.loadNoteList(); }

    synchronized public void updateNote(String memoCode, String title, Date date, int flg) {
        repository.updateNote(memoCode, title, date, flg);
    }
}


package com.example.branchmemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static Context mContext;
    public static MainViewModel DBModel;

    Toolbar toolbar;
    public static ActionBar actionBar;
    private static Handler mHandler ;
    TextView Date_top_1, Date_top_2, Date_bottom, NoneText;
    private RecyclerView rv;

    static SimpleDateFormat ap = new SimpleDateFormat("a", Locale.ENGLISH);
    static SimpleDateFormat time = new SimpleDateFormat("hh:mm");
    static SimpleDateFormat time24 = new SimpleDateFormat("HH:mm");
    static SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        DBModel = new MainViewModel(getApplication());

        //bar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //툴바의 뒤로가기 버튼

        //Clock
        Date_top_1 = findViewById(R.id.a_view);
        Date_top_2 = findViewById(R.id.time_view);
        Date_bottom = findViewById(R.id.date_view);
        NoneText = findViewById(R.id.NoneText);
        NoneText.setVisibility(View.GONE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Calendar cal = Calendar.getInstance();
                Date_top_1.setText(ap.format(cal.getTime()));
                Date_top_2.setText(time.format(cal.getTime()));
                Date_bottom.setText(date.format(cal.getTime()));
            }
        };

        class NewRunnable implements Runnable {
            @Override
            public void run() {
                while (true) {
                    mHandler.sendEmptyMessage(0) ;
                    try {
                        Thread.sleep(1000) ;
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }
                }
            }
        }
        NewRunnable nr = new NewRunnable() ;
        Thread t = new Thread(nr) ;
        t.start() ;

        findViewById(R.id.newBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(mContext, NewnoteActivity.class));
            }
        });

        rv = findViewById(R.id.MemoListRecyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(mLayoutManager);
        MainActivity.DBModel.loadNoteList(); //getData

    }//end of onCreate

    public void getData(final List<MemoListVo> memoListVos) {
        MemoListAdapter adapter = new MemoListAdapter(memoListVos);
        rv.setAdapter(adapter);
        if(adapter.getItemCount()==0){
            NoneText.setVisibility(View.VISIBLE);
        }else NoneText.setVisibility(View.GONE);
    }

    public static int DPtoPX(Context context, float dp){
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }
}

package com.example.branchmemo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Date;

@Entity(tableName = "memo_data")
public class MemoVo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String code;
    private String title;
    private String contentbody;
    @TypeConverters(DateConverters.class)
    private Date dateval;

    public MemoVo() { }

    public MemoVo(String code, String title, String content, Date date){
        setCode(code);
        setTitle(title);
        setContentbody(content);
        setDateval(date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) { this.code = code; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentbody() {
        return contentbody;
    }

    public void setContentbody(String contentbody) {
        this.contentbody = contentbody;
    }

    public Date getDateval() {
        return dateval;
    }

    public void setDateval(Date dateval) { this.dateval = dateval; }
}


package com.example.branchmemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


import java.sql.Date;
import java.util.List;

public class DBRepository {
    //**[IMPORTANT] Instance of DAO
    private MemoDao memoDao;
    private MemoListDao memoListDao;
    //DAO Getter
    public MemoDao getMemoDao() {
        return memoDao;
    }
    public MemoListDao getMemoListDao() {
        return memoListDao;
    }

    //Constructor
    public DBRepository(Context context) {
        MemoDatabase memoDatabase = MemoDatabase.getAppDatabase(context);
        MemoListDatabase memoListDatabase = MemoListDatabase.getAppDatabase(context);
        memoDao = memoDatabase.memeDao();
        memoListDao = memoListDatabase.memoListDao();
    }

    //----------------------------------------------------------------------------------------------


    private int codeCount;//GetCodeExistAsyncTask

    //----------------------------------------------------------------------------------------------
    //For User Methods

    synchronized public void viewNote(int pos){
        ViewMemoAsyncTask task = new ViewMemoAsyncTask(memoListDao);
        task.execute(pos);
    }
    synchronized public void loadNote(String code){
        LoadMemoAsyncTask task = new LoadMemoAsyncTask(memoDao, memoListDao);
        task.execute(code);
    }

    synchronized public void insertMemoList(MemoListVo memoList){
        InsertMemoListAsyncTask task = new InsertMemoListAsyncTask(memoListDao);
        task.execute(memoList);
    }

    synchronized public void deleteMemoList(String code){
        DeleteMemoListAsyncTask task = new DeleteMemoListAsyncTask(memoListDao);
        task.execute(code);
    }

    synchronized public void insertMemo(MemoVo memo, String notename){
        InsertMemoAsyncTask task = new InsertMemoAsyncTask(memoDao, memoListDao, notename);
        task.execute(memo);
    }

    synchronized public void deleteMemo(MemoVo memo){
        DeleteMemoAsyncTask task = new DeleteMemoAsyncTask(memoDao);
        task.execute(memo);

    }

    synchronized public void updateMemo(MemoVo memo, String notename){
        UpdateMemoAsyncTask task = new UpdateMemoAsyncTask(memoDao, memoListDao, notename);
        task.execute(memo);
    }

    synchronized public int selectCode(String code){
        GetCodeExistAsyncTask task = new GetCodeExistAsyncTask(memoListDao);
        task.repository = this;
        task.execute(code);
        return codeCount;
    }

    synchronized public void createNew(MemoVo memo, MemoListVo memolist) {
        CreateNewNoteAsyncTask task = new CreateNewNoteAsyncTask(memoDao, memoListDao, memo, memolist);
        task.execute();
    }

    synchronized public void deleteNote(String memoCode) {
        DeleteNoteAsyncTask task = new DeleteNoteAsyncTask(memoDao, memoListDao);
        task.execute(memoCode);
    }

    synchronized public void loadNoteList() {
        LoadNoteListAsyncTask task = new LoadNoteListAsyncTask(memoListDao);
        task.execute();
    }

    synchronized public void updateNote(String memoCode, String title, Date date, int flg) {
        UpdateNoteAsyncTask task = new UpdateNoteAsyncTask(memoListDao, title, date, flg);
        task.execute(memoCode);
    }

    //----------------------------------------------------------------------------------------------
    //AsyncTask Classes

    private static class UpdateNoteAsyncTask extends AsyncTask<String, Void, List<MemoListVo>>{
        private MemoListDao memoListDao;
        private String title;
        private Date date;
        private int flg;
        public UpdateNoteAsyncTask(MemoListDao memoListDao, String title, Date date, int flg){
            this.memoListDao = memoListDao;
            this.title = title;
            this.date = date;
            this.flg = flg;
        }
        @Override
        protected List<MemoListVo> doInBackground(String... code) {
            memoListDao.updateNote(code[0], title, date);
            return memoListDao.getAll();

        }
        @Override
        protected void onPostExecute(List<MemoListVo> list) {
            ((MainActivity)MainActivity.mContext).getData(list);
            if(flg==0) ((Activity) ViewBranchActivity.mContext).finish();//백버튼 동작시
        }
    }

    private static class LoadNoteListAsyncTask extends AsyncTask<Void, Void, List<MemoListVo>>{
        private MemoListDao memoListDao;
        public LoadNoteListAsyncTask(MemoListDao memoListDao){
            this.memoListDao = memoListDao;
        }
        @Override
        protected List<MemoListVo> doInBackground(Void... Void) {
            return memoListDao.getAll();
        }
        @Override
        protected void onPostExecute(List<MemoListVo> list) {
            ((MainActivity)MainActivity.mContext).getData(list);
        }
    }

    private static class DeleteNoteAsyncTask extends AsyncTask<String, Void, Void>{
        private MemoDao memoDao;
        private MemoListDao memoListDao;
        public DeleteNoteAsyncTask(MemoDao memoDao, MemoListDao memoListDao){
            this.memoDao = memoDao;
            this.memoListDao = memoListDao;
        }
        @Override
        protected Void doInBackground(String... code) {
            memoDao.delete(code[0]);
            memoListDao.delete(code[0]);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent close = new Intent(ViewBranchActivity.mContext, MainActivity.class);
            close.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            ViewBranchActivity.mContext.startActivity(close);
            ((Activity) ViewBranchActivity.mContext).finish();
        }
    }

    private static class CreateNewNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        private MemoDao memoDao;
        private MemoListDao memoListDao;
        private MemoVo memoVo;
        private MemoListVo memoListVo;
        public CreateNewNoteAsyncTask(MemoDao memoDao, MemoListDao memoListDao, MemoVo memo, MemoListVo memoList) {
            this.memoDao = memoDao;
            this.memoListDao = memoListDao;
            this.memoVo = memo;
            this.memoListVo = memoList;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            memoDao.insert(memoVo);
            memoListDao.insert(memoListVo);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            ViewMemoAsyncTask task = new ViewMemoAsyncTask(memoListDao);
            task.onPostExecute(this.memoListVo.getCode());
//            Intent intent = new Intent(NewnoteActivity.mContext, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            NewnoteActivity.mContext.startActivity(intent);
            ((Activity)NewnoteActivity.mContext).finish();
        }
    }//end of InsertMemoAsyncTask

    private static class GetCodeExistAsyncTask extends AsyncTask<String, Void, Integer>{
        private DBRepository repository = null;
        private MemoListDao memoListDao;
        public GetCodeExistAsyncTask(MemoListDao memoListDao) {
            this.memoListDao = memoListDao;
        }
        @Override
        protected Integer doInBackground(String... code) {
            return (Integer)memoListDao.selectCode(code[0]);
        }
        @Override
        protected void onPostExecute(Integer val) {
            repository.codeCount = (int)val;
        }
    }//end of GetCodeExistAsyncTask

    private static class ViewMemoAsyncTask extends AsyncTask<Integer, Void, String>{
        private MemoListDao memoListDao;
        public ViewMemoAsyncTask(MemoListDao memoListDao){
            this.memoListDao = memoListDao;
        }
        @Override
        protected String doInBackground(Integer... pos) {
            return memoListDao.getAll().get((int)pos[0]).getCode();
        }
        @Override
        protected void onPostExecute(String code) {
            Intent intent = new Intent(MainActivity.mContext, ViewBranchActivity.class);
            intent.putExtra("code", code);
            MainActivity.mContext.startActivity(intent);
        }
    }//end of ViewMemoAsyncTask

    private static class LoadMemoAsyncTask extends AsyncTask<String, Void, List<MemoVo>>{
        private MemoDao memoDao;
        private MemoListDao memoListDao;
        private String notename;
        public LoadMemoAsyncTask(MemoDao memoDao, MemoListDao memoListDao){
            this.memoDao = memoDao;
            this.memoListDao = memoListDao;
        }
        @Override
        protected List<MemoVo> doInBackground(String... code) {
            notename = memoListDao.get(code[0]).getTitle();
            return memoDao.getAll(code[0]);
        }
        @Override
        protected void onPostExecute(List<MemoVo> list) {
            ((ViewBranchActivity) ViewBranchActivity.mContext).getData(list, notename);
        }
    }

    private static class InsertMemoAsyncTask extends AsyncTask<MemoVo, Void, List<MemoVo>> {
        private MemoDao memoDao;
        private MemoListDao memoListDao;
        private String notename;
        public InsertMemoAsyncTask(MemoDao memoDao, MemoListDao memoListDao, String notename) {
            this.memoDao = memoDao;
            this.memoListDao = memoListDao;
            this.notename = notename;
        }
        @Override
        protected List<MemoVo> doInBackground(MemoVo... memo) {
            memoDao.insert(memo[0]);
            return memoDao.getAll(memo[0].getCode());
        }
        @Override
        protected void onPostExecute(List<MemoVo> list) {
            ((ViewBranchActivity) ViewBranchActivity.mContext).getData(list, notename);
        }
    }//end of InsertMemoAsyncTask

    private static class UpdateMemoAsyncTask extends AsyncTask<MemoVo, Void, List<MemoVo>>{
        private MemoDao memoDao;
        private MemoListDao memoListDao;
        private String notename;
        public UpdateMemoAsyncTask(MemoDao memoDao, MemoListDao memoListDao, String notename){
            this.memoDao = memoDao;
            this.memoListDao = memoListDao;
            this.notename = notename;
        }
        @Override
        protected List<MemoVo> doInBackground(MemoVo... memo) {
            memo[0].setId(memoDao.getThisId(memo[0].getCode()));
            memoDao.update(memo[0]);
            return memoDao.getAll(memo[0].getCode());
        }
        @Override
        protected void onPostExecute(List<MemoVo> list) {
            ((ViewBranchActivity) ViewBranchActivity.mContext).getData(list, notename);
        }
    }

    private static class DeleteMemoAsyncTask extends AsyncTask<MemoVo, Void, Void> {
        private MemoDao memoDao;
        public DeleteMemoAsyncTask(MemoDao memoDao) {
            this.memoDao = memoDao;
        }
        @Override
        protected Void doInBackground(MemoVo... memo) {
            memoDao.delete(memo[0].getId());
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = ((Activity) ViewBranchActivity.mContext).getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            ((Activity) ViewBranchActivity.mContext).finish();
            ViewBranchActivity.mContext.startActivity(intent);
        }
    }//end of DeleteMemoAsyncTask



    private static class InsertMemoListAsyncTask extends AsyncTask<MemoListVo, Void, Void> {
        private MemoListDao memoListDao;
        public InsertMemoListAsyncTask(MemoListDao memoListDao) {
            this.memoListDao = memoListDao;
        }
        @Override
        protected Void doInBackground(MemoListVo... memoListVos) {
            memoListDao.insert(memoListVos[0]);
            return null;
        }
    }//end of InsertMemoListAsyncTask

    private static class DeleteMemoListAsyncTask extends AsyncTask<String, Void, Void> {
        private MemoListDao memoListDao;
        public DeleteMemoListAsyncTask(MemoListDao memoListDao) {
            this.memoListDao = memoListDao;
        }
        @Override
        protected Void doInBackground(String... code) {
            memoListDao.delete(code[0]);
            return null;
        }
    }//end of DeleteMemoListAsyncTask


}//end of DBRepository


package com.example.branchmemo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Date;

@Entity(tableName = "memolist_data")
public class MemoListVo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String code;
    private String title;
    @TypeConverters(DateConverters.class)
    private Date dateval;

    public MemoListVo() {}

    public MemoListVo(String code, String title, Date date) {
        setCode(code);
        setTitle(title);
        setDateval(date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getCode() {
        return code;
    }

    public void setCode(String code) { this.code = code; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDateval() {
        return dateval;
    }

    public void setDateval(Date dateval) {
        this.dateval = dateval;
    }
}


package com.example.branchmemo;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemoListAdapter extends RecyclerView.Adapter<MemoListAdapter.ViewHolder> {
    List<MemoListVo> memoList_lists;

    public MemoListAdapter(List<MemoListVo> memoList_lists) {
        this.memoList_lists = memoList_lists;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        MemoListVo md = memoList_lists.get(i);
        holder.title.setText(md.getTitle());
        holder.date.setText(MainActivity.date.format(md.getDateval())+" "+MainActivity.time24.format(md.getDateval()));

    }

    @Override
    public int getItemCount() {
        return memoList_lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, date;
        private View list;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.listtitle);
            date = itemView.findViewById(R.id.listdate);
            list = itemView.findViewById(R.id.listitem);
            list.setVisibility(View.VISIBLE);
            list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        MainActivity.DBModel.viewNote(pos);
                    }
                }
            });
        }
    }

}

package com.example.branchmemo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.sql.Date;
import java.util.List;

@Dao
public interface MemoListDao {
    @Query("SELECT * FROM memolist_data")
    List<MemoListVo> getAll();

    @Query("SELECT * FROM memolist_data WHERE code=:code")
    MemoListVo get(String code);

    @Insert
    void insert(MemoListVo memolist);

    @Update
    void update(MemoListVo memolist);

    @Delete
    void delete(MemoListVo memolist);

    @Query("DELETE FROM memolist_data")
    void deleteAll();

    @Query("select EXISTS (select * from memolist_data where code=:code_) as success")
    int selectCode(String code_);

    @Query("UPDATE memolist_data SET title=:new_notename, dateval=:date WHERE code=:code_")
    void updateNote(String code_,String new_notename, Date date);

    @Query("DELETE FROM memolist_data WHERE code=:code_")
    void delete(String code_);


}


package com.example.branchmemo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MemoListVo.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class MemoListDatabase extends RoomDatabase {
    private static MemoListDatabase INSTANCE;

    public abstract MemoListDao memoListDao();

    public static synchronized MemoListDatabase getAppDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, MemoListDatabase.class, "memolist-db")
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }
}


package com.example.branchmemo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemoDao {

    @Query("SELECT * FROM memo_data WHERE code=:code")
    List<MemoVo> getAll(String code);

    @Insert
    void insert(MemoVo memo);

    @Update
    void update(MemoVo memo);

    @Delete
    void delete(MemoVo memo);

    @Query("DELETE FROM memo_data WHERE id=:id_")
    void delete(int id_);

    @Query("DELETE FROM memo_data")
    void deleteAll();

    @Query("DELETE FROM memo_data WHERE code=:code_")
    void delete(String code_);

    @Query("SELECT MAX(id) FROM memo_data WHERE code=:code_")
    int getThisId(String code_);
}


package com.example.branchmemo;

import android.provider.BaseColumns;

import java.sql.PreparedStatement;

public final class FeedReaderContract {
    private static PreparedStatement pstmt;

    private FeedReaderContract() {}
    private static class FeedEntry implements BaseColumns {
        public static final String COLUMN_NAME_INDEXVAL = "indexval";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CONTENTBODY = "contentbody";
        public static final String COLUMN_NAME_DATEVAL = "dateval";
    }

    public String SQL_INITIAL = "CREATE TABLE tablelistinfo (";

    public static final String SQL_CREATE = "CREATE TABLE  ?  (" +
            FeedEntry.COLUMN_NAME_INDEXVAL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FeedEntry.COLUMN_NAME_TITLE + " VARCHAR," +
            FeedEntry.COLUMN_NAME_CONTENTBODY + " TEXT" +
            FeedEntry.COLUMN_NAME_DATEVAL + " DATETIME DEFAULT DATETIME())";

    public static final String SQL_INSERT = "INSERT INTO ? ("+FeedEntry.COLUMN_NAME_TITLE
            +", "+FeedEntry.COLUMN_NAME_CONTENTBODY+") values(?, ?)";
    public static final String SQL_DELETE = "DELETE FROM ? WHERE indexval = ?";

    //수정 후
    public static final String SQL_UPDATE = "UPDATE student SET "+FeedEntry.COLUMN_NAME_INDEXVAL+"=MAX("+FeedEntry.COLUMN_NAME_INDEXVAL+"-1), "
            +FeedEntry.COLUMN_NAME_TITLE+"=?, "
            +FeedEntry.COLUMN_NAME_CONTENTBODY+"=?, "+FeedEntry.COLUMN_NAME_DATEVAL+"=datetime()" +
            " WHERE "+FeedEntry.COLUMN_NAME_INDEXVAL+"=MAX("+FeedEntry.COLUMN_NAME_INDEXVAL+")";
    public static final String SQL_DROP = "DROP TABLE IF EXISTS ?";


}

package com.example.branchmemo;

import com.example.branchmemo.MainActivity;

import java.util.Random;

public class CodeCreater {

    public String getNewCode(){
        String code = "";
        Random random = new Random();
        loop:while(true){
            for(int i=0; i<10; i++){
                int tmp;
                int flag = random.nextInt(3);

                if(flag==0){
                    code+=Integer.toString(random.nextInt(10));
                }else if(flag==1){
                    tmp = random.nextInt(26) + 65;
                    code += Character.toString((char)tmp);
                }else if(flag==2){
                    tmp = random.nextInt(26) + 97;
                    code += Character.toString((char)tmp);
                }
            }//end of for
            //Checking if exist of DB
            if(MainActivity.DBModel.selectCode(code) == 0)
                break loop;
        }//end of while
        return code;
    }//end of getNewCode

}


package com.example.branchmemo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {
    List<MemoVo> memo_lists;

    public MemoAdapter(List<MemoVo> memo_lists){
        this.memo_lists = memo_lists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        MemoVo m = memo_lists.get(i);
        holder.title.setText(m.getTitle());
        holder.content.setText(m.getContentbody());
        holder.date.setText(MainActivity.date.format(m.getDateval())+" "+MainActivity.time24.format(m.getDateval()));
        def_Branch(holder.branches, i);
    }

    private void def_Branch(View parent, int i){
        ImageView def_branch_top = new ImageView(ViewBranchActivity.mContext);
        ImageView def_branch_bttm = new ImageView(ViewBranchActivity.mContext);
        if(i==0){ //처음이면
            def_branch_top.setImageResource(R.drawable.start);
            def_branch_bttm.setImageResource(R.drawable.straight);
        }else{
            def_branch_top.setImageResource(R.drawable.middle);
            def_branch_bttm.setImageResource(R.drawable.straight);
        }
        setBranchSize(parent, def_branch_top, 50);
        setBranchSize(parent, def_branch_bttm, 50);
        ((LinearLayout)parent).addView(def_branch_top);
        ((LinearLayout)parent).addView(def_branch_bttm);
    }

    private void setBranchSize(View parent, ImageView branch, int dp){
        LayoutParams pa_params = parent.getLayoutParams();
        LayoutParams params = new LayoutParams(pa_params.width, MainActivity.DPtoPX(ViewBranchActivity.mContext, dp));
        branch.setLayoutParams(params);
    }

    @Override
    public int getItemCount() { return memo_lists.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, content, date;
        private View branch_area, branches, card, cardLayout, contentPane;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            branch_area = itemView.findViewById(R.id.branch_area);
            branches = itemView.findViewById(R.id.branches);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
            contentPane = itemView.findViewById(R.id.contentPane);
            contentPane.setVisibility(View.GONE);
            card = itemView.findViewById(R.id.itemcard);
            cardLayout = itemView.findViewById(R.id.itemcard_layout);
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toggle
                    if(contentPane.getVisibility()==View.VISIBLE) {
                        ((ViewGroup)branches).removeAllViews();
                        def_Branch(branches, getAdapterPosition());
                        contentPane.setVisibility(View.GONE);
                        LayoutParams params = branch_area.getLayoutParams();
                        branch_area.setLayoutParams(params);
                    } else {
                        contentPane.setVisibility(View.VISIBLE);
                        //뷰 길이 맞추기
                        LayoutParams pa_params = branch_area.getLayoutParams(); //동적으로 변한 크기
                        branch_area.measure(pa_params.MATCH_PARENT, pa_params.WRAP_CONTENT); //를 다시 붙이기?
                        int pa_height = branch_area.getMeasuredHeight(); //동적으로 변한 크기 사용
                        int dp50 = MainActivity.DPtoPX(ViewBranchActivity.mContext, 50);
                        int quan = pa_height/dp50 - 2; //def branch 제외

                        if(0<pa_height%dp50) quan++;

                        for(int i=0; i<quan; i++){
                            ImageView branch_add = new ImageView(ViewBranchActivity.mContext);
                            branch_add.setImageResource(R.drawable.straight);
                            setBranchSize(branches, branch_add, 50);
                            ((LinearLayout)branches).addView(branch_add);
                        }

                        RecyclerView rec = ((ViewBranchActivity)ViewBranchActivity.mContext).rv; //동적으로 변환 크기?
                        LayoutParams params = rec.getLayoutParams();
//                        rec.measure(params.MATCH_PARENT, params.WRAP_CONTENT);
//                        rec.setLayoutParams(new LayoutParams(rec.getWidth(), rec.getHeight()));

//
//                        LayoutParams params = (LayoutParams)rec.getLayoutParams();
                        Log.d("rec", ""+rec.getMeasuredHeight());
//                        rec.measure(params.width, params.height);
//                        params = new RecyclerView.LayoutParams(rec.getMeasuredWidth(), rec.getMeasuredHeight());
//                        rec.setLayoutParams(params);
                    }
                }
            });
            cardLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                //메모 삭제
                PopupMenu popup = new PopupMenu(ViewBranchActivity.mContext, card);
                popup.getMenuInflater().inflate(R.menu.del_memo_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        MainActivity.DBModel.deleteMemo(memo_lists.get(getAdapterPosition()));
                        Intent intent = ((Activity) ViewBranchActivity.mContext).getIntent();
                        ((Activity) ViewBranchActivity.mContext).finish();
                        ViewBranchActivity.mContext.startActivity(intent);
                        return true;
                    }
                });
                popup.show();
                return true;
                }
            });
        }
    }
}

