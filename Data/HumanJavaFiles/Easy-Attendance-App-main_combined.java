package com.ajstudios.easyattendance;

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
        assertEquals("com.ajstudios.notesapplication", appContext.getPackageName());
    }
}

package com.ajstudios.easyattendance;

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

package com.ajstudios.easyattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ajstudios.easyattendance.Adapter.ReportsAdapter;
import com.ajstudios.easyattendance.realm.Attendance_Reports;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class Reports_Activity extends AppCompatActivity {

    String subjectName, className, room_ID;
    RecyclerView recyclerView;
    Realm realm;

    ReportsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        Realm.init(this);
        subjectName = getIntent().getStringExtra("subject_name");
        className = getIntent().getStringExtra("class_name");
        room_ID = getIntent().getStringExtra("room_ID");

        recyclerView = findViewById(R.id.recyclerView_reports);

        Toolbar toolbar = findViewById(R.id.toolbar_reports);
        setSupportActionBar(toolbar);
        toolbar.setTitle(subjectName);
        toolbar.setSubtitle(className);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        RealmResults<Attendance_Reports> results;
        realm = Realm.getDefaultInstance();
        results = realm.where(Attendance_Reports.class)
                .equalTo("classId", room_ID)
                .findAll();


        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);

        recyclerView.setLayoutManager(gridLayoutManager);

        mAdapter = new ReportsAdapter( results,Reports_Activity.this, room_ID);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_dot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.ajstudios.easyattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ajstudios.easyattendance.realm.Class_Names;

import java.util.Objects;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import io.realm.Realm;
import io.realm.RealmAsyncTask;

public class Insert_class_Activity extends AppCompatActivity {

    Button create_button;
    EditText _className;
    EditText _subjectName;

    Realm realm;
    RealmAsyncTask transaction;

    private  String position_bg = "0";

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_class_);

        Toolbar toolbar = findViewById(R.id.toolbar_insert_class);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        create_button = findViewById(R.id.button_createClass);
        _className = findViewById(R.id.className_createClass);
        _subjectName = findViewById(R.id.subjectName_createClass);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        final RadioRealButton button1 = (RadioRealButton) findViewById(R.id.button1);
        final RadioRealButton button2 = (RadioRealButton) findViewById(R.id.button2);
        final RadioRealButton button3 = (RadioRealButton) findViewById(R.id.button3);
        final RadioRealButton button4 = (RadioRealButton) findViewById(R.id.button4);
        final RadioRealButton button5 = (RadioRealButton) findViewById(R.id.button5);
        final RadioRealButton button6 = (RadioRealButton) findViewById(R.id.button6);

        RadioRealButtonGroup group = (RadioRealButtonGroup) findViewById(R.id.group);
        group.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                position_bg = String.valueOf(position);
            }
        });

        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isValid()) {

                    final ProgressDialog progressDialog = new ProgressDialog(Insert_class_Activity.this);
                    progressDialog.setMessage("Creating class..");
                    progressDialog.show();

                    transaction = realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Class_Names class_name = realm.createObject(Class_Names.class);
                            String id = _className.getText().toString() + _subjectName.getText().toString();
                            class_name.setId(id);
                            class_name.setName_class(_className.getText().toString());
                            class_name.setName_subject(_subjectName.getText().toString());
                            class_name.setPosition_bg(position_bg);
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Toast.makeText(Insert_class_Activity.this, "Successfully created", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            progressDialog.dismiss();
                            Toast.makeText(Insert_class_Activity.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(Insert_class_Activity.this, "Fill all details", Toast.LENGTH_SHORT).show();
                }

                //-------

            }
        });


    }

    public boolean isValid(){

        return !_className.getText().toString().isEmpty() && !_subjectName.getText().toString().isEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.ajstudios.easyattendance;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ajstudios.easyattendance.Adapter.ClassListAdapter;
import com.ajstudios.easyattendance.realm.Class_Names;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    BottomAppBar bottomAppBar;
    FloatingActionButton fab_main;
    RecyclerView recyclerView;
    TextView sample;

    ClassListAdapter mAdapter;

    Realm realm;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);

        getWindow().setEnterTransition(null);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        fab_main = findViewById(R.id.fab_main);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Insert_class_Activity.class);
                startActivity(intent);
            }
        });

        realm = Realm.getDefaultInstance();

        RealmResults<Class_Names> results;

        results = realm.where(Class_Names.class)
                .findAll();


        sample = findViewById(R.id.classes_sample);
        recyclerView = findViewById(R.id.recyclerView_main);

        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        mAdapter = new ClassListAdapter( results,MainActivity.this);
        recyclerView.setAdapter(mAdapter);


    }

    @Override
    protected void onResume() {
        realm.refresh();
        realm.setAutoRefresh(true);
        super.onResume();
    }
}

package com.ajstudios.easyattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ajstudios.easyattendance.Adapter.Reports_Detail_Adapter;
import com.ajstudios.easyattendance.realm.Attendance_Students_List;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class Reports_Detail_Activity extends AppCompatActivity {

    RecyclerView recyclerView;
    Reports_Detail_Adapter mAdapter;

    TextView subj, className, toolbar_title;

    Realm realm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports__detail);
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        String room_ID = getIntent().getStringExtra("ID");
        String classname = getIntent().getStringExtra("class");
        String subjName = getIntent().getStringExtra("subject");
        String date = getIntent().getStringExtra("date");

        Toolbar toolbar = findViewById(R.id.toolbar_reports_detail);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView_reports_detail);
        subj = findViewById(R.id.subjName_report_detail);
        className = findViewById(R.id.classname_report_detail);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(date);
        subj.setText(subjName);
        className.setText(classname);



        RealmResults<Attendance_Students_List> list = realm.where(Attendance_Students_List.class)
                            .equalTo("date_and_classID", room_ID)
                            .sort("studentName", Sort.ASCENDING)
                            .findAllAsync();


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new Reports_Detail_Adapter( list,Reports_Detail_Activity.this, room_ID);
        recyclerView.setAdapter(mAdapter);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_dot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.ajstudios.easyattendance;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajstudios.easyattendance.Adapter.StudentsListAdapter;
import com.ajstudios.easyattendance.realm.Attendance_Reports;
import com.ajstudios.easyattendance.realm.Attendance_Students_List;
import com.ajstudios.easyattendance.realm.Students_List;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class ClassDetail_Activity extends AppCompatActivity {

    private ImageView themeImage;
    private TextView className, total_students, place_holder;
    private CardView addStudent, reports_open;
    private Button submit_btn;
    private EditText student_name, reg_no, mobile_no;
    private LinearLayout layout_attendance_taken;
    private RecyclerView mRecyclerview;


    String room_ID, subject_Name, class_Name;

    public static final String TAG = "ClassDetail_Activity";

    Realm realm;
    RealmAsyncTask transaction;
    RealmChangeListener realmChangeListener;

    private Handler handler = new Handler();
    StudentsListAdapter mAdapter;

    ProgressBar progressBar;
    Dialog lovelyCustomDialog;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail_);

        getWindow().setExitTransition(null);
        Realm.init(this);

        final String theme = getIntent().getStringExtra("theme");
        class_Name = getIntent().getStringExtra("className");
        subject_Name = getIntent().getStringExtra("subjectName");
        room_ID = getIntent().getStringExtra("classroom_ID");


        Toolbar toolbar = findViewById(R.id.toolbar_class_detail);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_disease_detail);
        collapsingToolbarLayout.setTitle(subject_Name);

        themeImage = findViewById(R.id.image_disease_detail);
        className = findViewById(R.id.classname_detail);
        total_students = findViewById(R.id.total_students_detail);
        layout_attendance_taken = findViewById(R.id.attendance_taken_layout);
        layout_attendance_taken.setVisibility(View.GONE);
        addStudent = findViewById(R.id.add_students);
        reports_open = findViewById(R.id.reports_open_btn);
        className.setText(class_Name);
        mRecyclerview = findViewById(R.id.recyclerView_detail);
        progressBar = findViewById(R.id.progressbar_detail);
        place_holder = findViewById(R.id.placeholder_detail);
        place_holder.setVisibility(View.GONE);
        submit_btn = findViewById(R.id.submit_attendance_btn);
        submit_btn.setVisibility(View.GONE);

        switch (theme) {
            case "0":
                themeImage.setImageResource(R.drawable.asset_bg_paleblue);
                break;
            case "1":
                themeImage.setImageResource(R.drawable.asset_bg_green);

                break;
            case "2":
                themeImage.setImageResource(R.drawable.asset_bg_yellow);

                break;
            case "3":
                themeImage.setImageResource(R.drawable.asset_bg_palegreen);

                break;
            case "4":
                themeImage.setImageResource(R.drawable.asset_bg_paleorange);

                break;
            case "5":
                themeImage.setImageResource(R.drawable.asset_bg_white);
                break;

        }

        //---------------------------------

        Runnable r = new Runnable() {
            @Override
            public void run() {
                RealmInit();
                progressBar.setVisibility(View.GONE);
            }
        };
        handler.postDelayed(r, 500);

        //----------------------------------------

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long count = realm.where(Students_List.class)
                        .equalTo("class_id", room_ID)
                        .count();
                final String size, size2;
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ClassDetail_Activity.this);
                size = String.valueOf(preferences.getAll().size());
                size2 = String.valueOf(count);

                if (size.equals(size2)){
                    submitAttendance();
                }else {
                    Toast.makeText(ClassDetail_Activity.this, "Select all........", Toast.LENGTH_SHORT).show();
                }

            }
        });

        reports_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassDetail_Activity.this, Reports_Activity.class);
                intent.putExtra("class_name", class_Name);
                intent.putExtra("subject_name", subject_Name);
                intent.putExtra("room_ID", room_ID);
                startActivity(intent);
            }
        });



        addStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                    LayoutInflater inflater = LayoutInflater.from(ClassDetail_Activity.this);
                    final View view1 = inflater.inflate(R.layout.popup_add_student, null);
                    student_name = view1.findViewById(R.id.name_student_popup);
                    reg_no = view1.findViewById(R.id.regNo_student_popup);
                    mobile_no = view1.findViewById(R.id.mobileNo_student_popup);

                    lovelyCustomDialog = new LovelyCustomDialog(ClassDetail_Activity.this)
                            .setView(view1)
                            .setTopColorRes(R.color.theme_light)
                            .setTitle("Add Student")
                            .setIcon(R.drawable.ic_baseline_person_add_24)
                            .setCancelable(false)
                            .setListener(R.id.add_btn_popup, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    String name = student_name.getText().toString();
                                    String regNo = reg_no.getText().toString();
                                    String mobNo = mobile_no.getText().toString();

                                    if (isValid()){
                                    addStudentMethod(name, regNo, mobNo);
                                    }else{
                                        Toast.makeText(ClassDetail_Activity.this, "Please fill all the details..", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            })
                            .setListener(R.id.cancel_btn_popup, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    lovelyCustomDialog.dismiss();
                                }
                            })
                            .show();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void RealmInit(){

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        final String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                long count = realm.where(Students_List.class)
                        .equalTo("class_id", room_ID)
                        .count();

                total_students.setText("Total Students : " + count);

                long reports_size = realm.where(Attendance_Reports.class)
                        .equalTo("date_and_classID", date+room_ID)
                        .count();
                if (!(reports_size==0)){
                    layout_attendance_taken.setVisibility(View.VISIBLE);
                    submit_btn.setVisibility(View.GONE);
                }else {
                    layout_attendance_taken.setVisibility(View.GONE);
                    submit_btn.setVisibility(View.VISIBLE);

                    if (!(count==0)){
                        submit_btn.setVisibility(View.VISIBLE);
                        place_holder.setVisibility(View.GONE);
                    }else if (count==0) {
                        submit_btn.setVisibility(View.GONE);
                        place_holder.setVisibility(View.VISIBLE);
                    }

                }

            }
        };
        realm.addChangeListener(realmChangeListener);
        RealmResults<Students_List> students ;
        students = realm.where(Students_List.class)
                .equalTo("class_id", room_ID)
                .sort("name_student", Sort.ASCENDING)
                .findAllAsync();


        long count = realm.where(Students_List.class)
                .equalTo("class_id", room_ID)
                .count();
        long reports_size = realm.where(Attendance_Reports.class)
                .equalTo("date_and_classID", date+room_ID)
                .count();


        if (!(reports_size==0)){
            layout_attendance_taken.setVisibility(View.VISIBLE);
            submit_btn.setVisibility(View.GONE);
        }else if (reports_size==0) {

            layout_attendance_taken.setVisibility(View.GONE);
            submit_btn.setVisibility(View.VISIBLE);

            if (!(count==0)){
                submit_btn.setVisibility(View.VISIBLE);
                place_holder.setVisibility(View.GONE);
            }else if (count==0){
                submit_btn.setVisibility(View.GONE);
                place_holder.setVisibility(View.VISIBLE);
            }
        }


        total_students.setText("Total Students : " + count);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        String extraClick = "";
        mAdapter = new StudentsListAdapter( students,ClassDetail_Activity.this, date+room_ID, extraClick);
        mRecyclerview.setAdapter(mAdapter);

    }

    public void submitAttendance(){

        final ProgressDialog progressDialog = new ProgressDialog(ClassDetail_Activity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.show();
        final String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
                final RealmResults<Attendance_Students_List> list_students ;

                list_students = realm.where(Attendance_Students_List.class)
                        .equalTo("date_and_classID", date+room_ID)
                        .sort("studentName", Sort.ASCENDING)
                        .findAllAsync();

                final RealmList<Attendance_Students_List> list = new RealmList<>();
                list.addAll(list_students);

                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                final String dateOnly = String.valueOf(calendar.get(Calendar.DATE));
                @SuppressLint("SimpleDateFormat")
                final String monthOnly = new SimpleDateFormat("MMM").format(calendar.getTime());

                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Attendance_Reports attendance_reports = realm.createObject(Attendance_Reports.class);
                            attendance_reports.setClassId(room_ID);
                            attendance_reports.setAttendance_students_lists(list);
                            attendance_reports.setDate(date);
                            attendance_reports.setDateOnly(dateOnly);
                            attendance_reports.setMonthOnly(monthOnly);
                            attendance_reports.setDate_and_classID(date+room_ID);
                            attendance_reports.setClassname(class_Name);
                            attendance_reports.setSubjName(subject_Name);

                        }
                    });
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(ClassDetail_Activity.this, "Attendance Submitted", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();


                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(ClassDetail_Activity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }


    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        super.onDestroy();
    }

    public void addStudentMethod(final String studentName, final String regNo, final String mobileNo) {

        final ProgressDialog progressDialog = new ProgressDialog(ClassDetail_Activity.this);
        progressDialog.setMessage("Creating class..");
        progressDialog.show();

        transaction = realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Students_List students_list = realm.createObject(Students_List.class);
                String id = studentName+regNo;
                students_list.setId(id);
                students_list.setName_student(studentName);
                students_list.setRegNo_student(regNo);
                students_list.setMobileNo_student(mobileNo);
                students_list.setClass_id(room_ID);

            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                lovelyCustomDialog.dismiss();
                realm.refresh();
                realm.setAutoRefresh(true);
                Toast.makeText(ClassDetail_Activity.this, "Student Added", Toast.LENGTH_SHORT).show();

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                progressDialog.dismiss();
                lovelyCustomDialog.dismiss();
                Toast.makeText(ClassDetail_Activity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean isValid(){

        if (student_name.getText().toString().isEmpty() || reg_no.getText().toString().isEmpty() || mobile_no.getText().toString().isEmpty()){
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_class_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}

package com.ajstudios.easyattendance.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Attendance_Reports;
import com.ajstudios.easyattendance.viewholders.ViewHolder_reports;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class ReportsAdapter extends RealmRecyclerViewAdapter<Attendance_Reports, ViewHolder_reports> {

    private final Activity mActivity;
    RealmResults<Attendance_Reports> mList;
    String stuID, mroomID;
    Realm realm = Realm.getDefaultInstance();

    public ReportsAdapter(RealmResults<Attendance_Reports> list, Activity context, String roomID) {

        super(context, list, true);

        mActivity = context;
        mList = list;
        mroomID =roomID;
    }

    @NonNull
    @Override
    public ViewHolder_reports onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reports_adapter_item, parent, false);
        return new ViewHolder_reports(itemView, mActivity, mList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder_reports holder, int position) {
        Attendance_Reports temp = getItem(position);
        holder.month.setText(temp.getMonthOnly());
        holder.date.setText(temp.getDateOnly());

    }


}


package com.ajstudios.easyattendance.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Class_Names;
import com.ajstudios.easyattendance.realm.Students_List;
import com.ajstudios.easyattendance.viewholders.ViewHolder;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class ClassListAdapter extends RealmRecyclerViewAdapter<Class_Names, ViewHolder> {

    private final Activity mActivity;
    RealmResults<Class_Names> mList;

    Realm realm;
    RealmChangeListener realmChangeListener;

    public ClassListAdapter(RealmResults<Class_Names> list, Activity context) {

        super(context, list, true);
        Realm realm = Realm.getDefaultInstance();
        mActivity = context;
        mList = list;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_adapter, parent, false);
        return new ViewHolder(itemView, mActivity, mList);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Class_Names temp = getItem(position);

        Realm.init(mActivity);
        realm = Realm.getDefaultInstance();
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                long count = realm.where(Students_List.class)
                        .equalTo("class_id", temp.getId())
                        .count();
                holder.total_students.setText("Students : " + count);
            }
        };
        realm.addChangeListener(realmChangeListener);

        long count = realm.where(Students_List.class)
                .equalTo("class_id", temp.getId())
                .count();
        holder.total_students.setText("Students : " + count);
        holder.class_name.setText(temp.getName_class());
        holder.subject_name.setText(temp.getName_subject());

        switch (temp.getPosition_bg()) {
            case "0":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_paleblue);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_1);
                break;
            case "1":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_green);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_2);
                break;
            case "2":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_yellow);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_3);
                break;
            case "3":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_palegreen);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_4);
                break;
            case "4":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_paleorange);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_5);
                break;
            case "5":
                holder.imageView_bg.setImageResource(R.drawable.asset_bg_white);
                holder.frameLayout.setBackgroundResource(R.drawable.gradient_color_6);
                holder.subject_name.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_color_secondary));
                holder.class_name.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_color_secondary));
                holder.total_students.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_color_secondary));
                break;
        }

    }
}


package com.ajstudios.easyattendance.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Attendance_Students_List;
import com.ajstudios.easyattendance.viewholders.ViewHolder_reports_detail;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class Reports_Detail_Adapter extends RealmRecyclerViewAdapter<Attendance_Students_List, ViewHolder_reports_detail> {

    private final Activity mActivity;
    RealmResults<Attendance_Students_List> mList;
    String stuID, mroomID;
    Realm realm = Realm.getDefaultInstance();

    public Reports_Detail_Adapter(RealmResults<Attendance_Students_List> list, Activity context, String roomID) {

        super(context, list, true);

        mActivity = context;
        mList = list;
        mroomID =roomID;
    }

    @NonNull
    @Override
    public ViewHolder_reports_detail onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_detail_adapter_item, parent, false);
        return new ViewHolder_reports_detail(itemView, mActivity, mList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder_reports_detail holder, int position) {
        Attendance_Students_List temp = getItem(position);
        holder.namE.setText(temp.getStudentName());
        holder.regNo.setText(temp.getStudentRegNo());
        if (temp.getAttendance().equals("Present")){
            holder.status.setText("P");
            holder.circle.setCardBackgroundColor(mActivity.getResources().getColor(R.color.green_new));
        }else{
            holder.status.setText("A");
            holder.circle.setCardBackgroundColor(mActivity.getResources().getColor(R.color.red_new));
        }
    }


}


package com.ajstudios.easyattendance.Adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Students_List;
import com.ajstudios.easyattendance.viewholders.ViewHolder_students;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class StudentsListAdapter extends RealmRecyclerViewAdapter<Students_List, ViewHolder_students> {

    private final Activity mActivity;
    RealmResults<Students_List> mList;
    String stuID, mroomID;
    Realm realm = Realm.getDefaultInstance();

    public StudentsListAdapter(RealmResults<Students_List> list, Activity context, String roomID, String extraClick) {

        super(context, list, true);

        mActivity = context;
        mList = list;
        mroomID =roomID;
    }

    @NonNull
    @Override
    public ViewHolder_students onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_attendance_adapter, parent, false);
        return new ViewHolder_students(itemView, mActivity, mList, mroomID);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder_students holder, final int position) {
        Students_List temp = getItem(position);
        holder.student_name.setText(temp.getName_student());
        holder.student_regNo.setText(temp.getRegNo_student());


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        stuID = temp.getRegNo_student();
        String value = preferences.getString(stuID, null);
        if (value==null){

        }else {
            if (value.equals("Present")) {
                holder.radioButton_present.setChecked(true);
            } else {
                holder.radioButton_absent.setChecked(true);
            }
        }
    }

}


package com.ajstudios.easyattendance.viewholders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.Reports_Detail_Activity;
import com.ajstudios.easyattendance.realm.Attendance_Reports;

import io.realm.RealmResults;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;

public class ViewHolder_reports extends RecyclerView.ViewHolder {


    public TextView month;
    public TextView date;

    public Activity mActivity;
    RealmResults<Attendance_Reports> mList;

    public ViewHolder_reports(@NonNull final View itemView, Activity MainActivity, final RealmResults<Attendance_Reports> list) {
        super(itemView);

        month = itemView.findViewById(R.id.month_report_adapter);
        date = itemView.findViewById(R.id.date_report_adapter);

        mActivity = MainActivity;
        mList = list;


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Reports_Detail_Activity.class);
                intent.putExtra("ID", mList.get(getAbsoluteAdapterPosition()).getDate_and_classID());
                intent.putExtra("date", mList.get(getAbsoluteAdapterPosition()).getDate());
                intent.putExtra("subject", mList.get(getAbsoluteAdapterPosition()).getSubjName());
                intent.putExtra("class", mList.get(getAbsoluteAdapterPosition()).getClassname());
                view.getContext().startActivity(intent);
            }
        });

    }

}


package com.ajstudios.easyattendance.viewholders;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Attendance_Students_List;

import io.realm.RealmResults;

public class ViewHolder_reports_detail extends RecyclerView.ViewHolder {

    public TextView namE;
    public TextView regNo;
    public TextView status;

    public CardView circle;

    public Activity mActivity;
    RealmResults<Attendance_Students_List> mList;

    public ViewHolder_reports_detail(@NonNull final View itemView, Activity MainActivity, RealmResults<Attendance_Students_List> list) {
        super(itemView);

        namE = itemView.findViewById(R.id.student_name_report_detail_adapter);
        regNo = itemView.findViewById(R.id.student_regNo_report_detail_adapter);
        status = itemView.findViewById(R.id.status_report_detail_adapter);
        circle = itemView.findViewById(R.id.cardView_report_detail_adapter);


        mActivity = MainActivity;
        mList = list;

    }

}


package com.ajstudios.easyattendance.viewholders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.ajstudios.easyattendance.ClassDetail_Activity;
import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Class_Names;

import io.realm.RealmResults;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;

public class ViewHolder extends RecyclerView.ViewHolder {

    public final TextView class_name;
    public final TextView subject_name;
    public final TextView total_students;
    public ImageView imageView_bg;
    public RelativeLayout frameLayout;
    public CardView cardView;



    public Activity mActivity;
    RealmResults<Class_Names> mList;

    public ViewHolder(@NonNull final View itemView, Activity MainActivity, RealmResults<Class_Names> list) {
        super(itemView);

        class_name = itemView.findViewById(R.id.className_adapter);
        subject_name = itemView.findViewById(R.id.subjectName_adapter);
        imageView_bg = itemView.findViewById(R.id.imageClass_adapter);
        frameLayout = itemView.findViewById(R.id.frame_bg);
        cardView = itemView.findViewById(R.id.cardView_adapter);
        total_students = itemView.findViewById(R.id.totalStudents_adapter);

        mActivity = MainActivity;
        mList = list;


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ClassDetail_Activity.class);
                intent.putExtra("theme", mList.get(getAdapterPosition()).getPosition_bg());
                intent.putExtra("className", mList.get(getAdapterPosition()).getName_class());
                intent.putExtra("subjectName", mList.get(getAdapterPosition()).getName_subject());
                intent.putExtra("classroom_ID", mList.get(getAdapterPosition()).getId());
                Pair<View, String> p1 = Pair.create((View) cardView, "ExampleTransition");
                ActivityOptionsCompat optionsCompat = makeSceneTransitionAnimation(mActivity, p1);
                view.getContext().startActivity(intent, optionsCompat.toBundle());
            }
        });

    }

}


package com.ajstudios.easyattendance.viewholders;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ajstudios.easyattendance.BottomSheet.Student_Edit_Sheet;
import com.ajstudios.easyattendance.R;
import com.ajstudios.easyattendance.realm.Attendance_Reports;
import com.ajstudios.easyattendance.realm.Attendance_Students_List;
import com.ajstudios.easyattendance.realm.Students_List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;

public class ViewHolder_students extends RecyclerView.ViewHolder{

    public Activity mActivity;
    RealmResults<Students_List> mList;

    public final TextView student_name;
    public final TextView student_regNo;
    public LinearLayout layout;
    public String stuName, regNo, mobileNo, mRoomID;
    public RadioGroup radioGroup;
    public RadioButton radioButton_present, radioButton_absent;

    Realm realm;
    RealmChangeListener realmChangeListener;

    public ViewHolder_students(@NonNull final View itemView, Activity MainActivity, RealmResults<Students_List> list, final String roomID) {
        super(itemView);

        student_name = itemView.findViewById(R.id.student_name_adapter);
        student_regNo = itemView.findViewById(R.id.student_regNo_adapter);
        radioGroup = itemView.findViewById(R.id.radioGroup);
        radioButton_present = itemView.findViewById(R.id.radio_present);
        radioButton_absent = itemView.findViewById(R.id.radio_absent);
        layout = itemView.findViewById(R.id.layout_click);

        mActivity = MainActivity;
        mList = list;
        mRoomID = roomID;



        Realm.init(mActivity);
        realm = Realm.getDefaultInstance();
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                long reports_size = realm.where(Attendance_Reports.class)
                        .equalTo("date_and_classID", roomID)
                        .count();
                if (!(reports_size==0)){
                    radioGroup.setVisibility(View.GONE);
                }else if (reports_size==0) {
                    radioGroup.setVisibility(View.VISIBLE);
                }
            }
        };
        realm.addChangeListener(realmChangeListener);
        long reports_size = realm.where(Attendance_Reports.class)
                .equalTo("date_and_classID", roomID)
                .count();
        if (!(reports_size==0)){
            radioGroup.setVisibility(View.GONE);
        }else if (reports_size==0) {
            radioGroup.setVisibility(View.VISIBLE);
        }



        radioButton_present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String attendance = "Present";
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(mList.get(getAbsoluteAdapterPosition()).getRegNo_student(), attendance);
                editor.apply();



                final Attendance_Students_List attendance_students_list = new Attendance_Students_List();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        attendance_students_list.setStudentName((mList.get(getAbsoluteAdapterPosition()).getName_student()));
                        attendance_students_list.setAttendance(attendance);
                        attendance_students_list.setMobNo((mList.get(getAbsoluteAdapterPosition()).getMobileNo_student()));
                        attendance_students_list.setStudentRegNo(mList.get(getAbsoluteAdapterPosition()).getRegNo_student());
                        attendance_students_list.setClassID(mList.get(getAbsoluteAdapterPosition()).getClass_id());
                        attendance_students_list.setDate_and_classID(mRoomID);
                        attendance_students_list.setUnique_ID(mList.get(getAbsoluteAdapterPosition()).getRegNo_student()+mRoomID);

                        realm.copyToRealmOrUpdate(attendance_students_list);
                    }
                });




            }
        });
        radioButton_absent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String attendance = "Absent";
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(mList.get(getAbsoluteAdapterPosition()).getRegNo_student(), attendance);
                editor.apply();

                final Attendance_Students_List attendance_students_list = new Attendance_Students_List();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        attendance_students_list.setStudentName((mList.get(getAbsoluteAdapterPosition()).getName_student()));
                        attendance_students_list.setAttendance(attendance);
                        attendance_students_list.setMobNo((mList.get(getAbsoluteAdapterPosition()).getMobileNo_student()));
                        attendance_students_list.setStudentRegNo(mList.get(getAbsoluteAdapterPosition()).getRegNo_student());
                        attendance_students_list.setClassID(mList.get(getAbsoluteAdapterPosition()).getClass_id());
                        attendance_students_list.setDate_and_classID(mRoomID);
                        attendance_students_list.setUnique_ID(mList.get(getAbsoluteAdapterPosition()).getRegNo_student()+mRoomID);

                        realm.copyToRealmOrUpdate(attendance_students_list);
                    }
                });

            }
        });


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stuName = mList.get(getAbsoluteAdapterPosition()).getName_student();
                regNo = mList.get(getAbsoluteAdapterPosition()).getRegNo_student();
                mobileNo = mList.get(getAbsoluteAdapterPosition()).getMobileNo_student();
                Student_Edit_Sheet student_edit_sheet = new Student_Edit_Sheet(stuName, regNo, mobileNo);
                student_edit_sheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetTheme);
                student_edit_sheet.show(((FragmentActivity)view.getContext()).getSupportFragmentManager(), "BottomSheet");
            }
        });

    }


}


package com.ajstudios.easyattendance.BottomSheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.ajstudios.easyattendance.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Student_Edit_Sheet extends BottomSheetDialogFragment {

    public String _name, _regNo, _mobNo;
    public EditText name_student, regNo_student, mobNo_student;
    public CardView call;

    public Student_Edit_Sheet(String stuName, String regNo, String mobileNo) {
        _name = stuName;
        _regNo = regNo;
        _mobNo = mobileNo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.bottomsheet_student_edit, container, false);

        name_student = v.findViewById(R.id.stu_name_edit);
        regNo_student = v.findViewById(R.id.stu_regNo_edit);
        mobNo_student = v.findViewById(R.id.stu_mobNo_edit);
        call = v.findViewById(R.id.call_edit);

        name_student.setText(_name);
        regNo_student.setText(_regNo);
        mobNo_student.setText(_mobNo);

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "tel:" + _mobNo.trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        return v;
    }
}


package com.ajstudios.easyattendance.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Attendance_Students_List extends RealmObject {

    String studentName;
    String studentRegNo;
    String attendance;
    String mobNo;
    String classID;
    String date_and_classID;
    @PrimaryKey
    String unique_ID;


    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRegNo() {
        return studentRegNo;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentRegNo = studentRegNo;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getDate_and_classID() {
        return date_and_classID;
    }

    public void setDate_and_classID(String date_and_classID) {
        this.date_and_classID = date_and_classID;
    }

    public String getUnique_ID() {
        return unique_ID;
    }

    public void setUnique_ID(String unique_ID) {
        this.unique_ID = unique_ID;
    }
}


package com.ajstudios.easyattendance.realm;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Attendance_Reports extends RealmObject {

    String date;
    String monthOnly;
    String dateOnly;
    String classId;
    String date_and_classID;
    String classname;
    String subjName;
    RealmList<Attendance_Students_List> attendance_students_lists;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public RealmList<Attendance_Students_List> getAttendance_students_lists() {
        return attendance_students_lists;
    }

    public void setAttendance_students_lists(RealmList<Attendance_Students_List> attendance_students_lists) {
        this.attendance_students_lists = attendance_students_lists;
    }

    public String getDate_and_classID() {
        return date_and_classID;
    }

    public void setDate_and_classID(String date_and_classID) {
        this.date_and_classID = date_and_classID;
    }

    public String getMonthOnly() {
        return monthOnly;
    }

    public void setMonthOnly(String monthOnly) {
        this.monthOnly = monthOnly;
    }

    public String getDateOnly() {
        return dateOnly;
    }

    public void setDateOnly(String dateOnly) {
        this.dateOnly = dateOnly;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getSubjName() {
        return subjName;
    }

    public void setSubjName(String subjName) {
        this.subjName = subjName;
    }
}


package com.ajstudios.easyattendance.realm;

import io.realm.RealmObject;

public class Class_Names extends RealmObject {


    String id;

    String name_class;
    String name_subject;
    String position_bg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName_class() {
        return name_class;
    }

    public void setName_class(String name_class) {
        this.name_class = name_class;
    }

    public String getName_subject() {
        return name_subject;
    }

    public void setName_subject(String name_subject) {
        this.name_subject = name_subject;
    }

    public String getPosition_bg() {
        return position_bg;
    }

    public void setPosition_bg(String position_bg) {
        this.position_bg = position_bg;
    }
}


package com.ajstudios.easyattendance.realm;

import io.realm.RealmObject;


public class Students_List extends RealmObject {

    String id;
    String name_student;
    String regNo_student;
    String mobileNo_student;
    String class_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName_student() {
        return name_student;
    }

    public void setName_student(String name_student) {
        this.name_student = name_student;
    }

    public String getRegNo_student() {
        return regNo_student;
    }

    public void setRegNo_student(String regNo_student) {
        this.regNo_student = regNo_student;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }

    public String getMobileNo_student() {
        return mobileNo_student;
    }

    public void setMobileNo_student(String mobileNo_student) {
        this.mobileNo_student = mobileNo_student;
    }


}


