package com.vishwajeeth.medicinetime;

import android.content.Context;
import androidx.annotation.NonNull;


import com.vishwajeeth.medicinetime.data.source.MedicineRepository;
import com.vishwajeeth.medicinetime.data.source.local.MedicinesLocalDataSource;


/**
 * Created by vishwajeeth on 13/05/17.
 */

public class Injection {

    public static MedicineRepository provideMedicineRepository(@NonNull Context context) {
        return MedicineRepository.getInstance(MedicinesLocalDataSource.getInstance(context));
    }
}

package com.vishwajeeth.medicinetime;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

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
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        assertEquals("com.gautam.medicinetime", appContext.getPackageName());
    }
}


package com.vishwajeeth.medicinetime;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package com.vishwajeeth.medicinetime;

import android.content.Context;
import androidx.annotation.NonNull;

import com.vishwajeeth.medicinetime.data.source.MedicineRepository;
import com.vishwajeeth.medicinetime.data.source.local.MedicinesLocalDataSource;

/**
 * Created by vishwajeeth on 07/07/21.
 */

public class Injection {

    public static MedicineRepository provideMedicineRepository(@NonNull Context context) {
        return MedicineRepository.getInstance(MedicinesLocalDataSource.getInstance(context));
    }
}


package com.vishwajeeth.medicinetime;

import android.app.Application;
import android.content.Context;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public class MedicineApp extends Application {

    private static Context mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mInstance == null) {
            mInstance = getApplicationContext();
        }
    }

    public static Context getInstance() {
        return mInstance;
    }
}


package com.vishwajeeth.medicinetime;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public interface BaseView<T> {

    void setPresenter(T presenter);
}


package com.vishwajeeth.medicinetime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.vishwajeeth.medicinetime.medicine.MedicineActivity;

public class SplashActivity extends Activity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashfile);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashActivity.this, MedicineActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);

    }
}

package com.vishwajeeth.medicinetime;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public interface BasePresenter {

    void start();
}


package com.vishwajeeth.medicinetime.addmedicine;

import com.vishwajeeth.medicinetime.BasePresenter;
import com.vishwajeeth.medicinetime.BaseView;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.util.List;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public interface AddMedicineContract {

    interface View extends BaseView<Presenter> {

        void showEmptyMedicineError();

        void showMedicineList();

        boolean isActive();

    }

    interface  Presenter extends BasePresenter{


        void saveMedicine(MedicineAlarm alarm, Pills pills);


        boolean isDataMissing();

        boolean isMedicineExits(String pillName);

        long addPills(Pills pills);

        Pills getPillsByName(String pillName);

        List<MedicineAlarm> getMedicineByPillName(String pillName);

        List<Long> tempIds();

        void deleteMedicineAlarm(long alarmId);

    }
}


package com.vishwajeeth.medicinetime.addmedicine;

import android.os.PersistableBundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.vishwajeeth.medicinetime.Injection;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.utils.ActivityUtils;

public class AddMedicineActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_TASK = 1;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";

    public static final String EXTRA_TASK_ID = "task_extra_id";
    public static final String EXTRA_TASK_NAME = "task_extra_name";

    private AddMedicinePresenter mAddMedicinePresenter;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddMedicineFragment addMedicineFragment = (AddMedicineFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        int medId = getIntent().getIntExtra(AddMedicineFragment.ARGUMENT_EDIT_MEDICINE_ID,0);
        String medName = getIntent().getStringExtra(AddMedicineFragment.ARGUMENT_EDIT_MEDICINE_NAME);

        setToolbarTitle(medName);


        if (addMedicineFragment == null) {
            addMedicineFragment = AddMedicineFragment.newInstance();

            if (getIntent().hasExtra(AddMedicineFragment.ARGUMENT_EDIT_MEDICINE_ID)) {
                Bundle bundle = new Bundle();
                bundle.putInt(AddMedicineFragment.ARGUMENT_EDIT_MEDICINE_ID, medId);
                addMedicineFragment.setArguments(bundle);
            }
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addMedicineFragment, R.id.contentFrame);
        }

        boolean shouldLoadDataFromRepo = true;
        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

//        // Create the presenter
        mAddMedicinePresenter = new AddMedicinePresenter(
                medId,
                Injection.provideMedicineRepository(getApplicationContext()),
                addMedicineFragment,
                shouldLoadDataFromRepo);

    }

    public void setToolbarTitle(String medicineName) {
        if (medicineName == null) {
            mActionBar.setTitle(getString(R.string.new_medicine));
        } else {
            mActionBar.setTitle(medicineName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddMedicinePresenter.isDataMissing());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


package com.vishwajeeth.medicinetime.addmedicine;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.alarm.ReminderActivity;
import com.vishwajeeth.medicinetime.alarm.ReminderFragment;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.Pills;
import com.vishwajeeth.medicinetime.views.DayViewCheckBox;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public class AddMedicineFragment extends Fragment implements AddMedicineContract.View {

    public static final String ARGUMENT_EDIT_MEDICINE_ID = "ARGUMENT_EDIT_MEDICINE_ID";

    public static final String ARGUMENT_EDIT_MEDICINE_NAME = "ARGUMENT_EDIT_MEDICINE_NAME";

    @BindView(R.id.edit_med_name)
    EditText editMedName;

    @BindView(R.id.every_day)
    AppCompatCheckBox everyDay;

    @BindView(R.id.dv_sunday)
    DayViewCheckBox dvSunday;

    @BindView(R.id.dv_monday)
    DayViewCheckBox dvMonday;

    @BindView(R.id.dv_tuesday)
    DayViewCheckBox dvTuesday;

    @BindView(R.id.dv_wednesday)
    DayViewCheckBox dvWednesday;

    @BindView(R.id.dv_thursday)
    DayViewCheckBox dvThursday;

    @BindView(R.id.dv_friday)
    DayViewCheckBox dvFriday;

    @BindView(R.id.dv_saturday)
    DayViewCheckBox dvSaturday;

    @BindView(R.id.checkbox_layout)
    LinearLayout checkboxLayout;

    @BindView(R.id.tv_medicine_time)
    RobotoBoldTextView tvMedicineTime;

    @BindView(R.id.tv_dose_quantity)
    EditText tvDoseQuantity;

    @BindView(R.id.spinner_dose_units)
    AppCompatSpinner spinnerDoseUnits;

    private List<String> doseUnitList;

    private boolean[] dayOfWeekList = new boolean[7];

    private int hour, minute;

    Unbinder unbinder;

    private AddMedicineContract.Presenter mPresenter;

    private View rootView;

    private String doseUnit;


    static AddMedicineFragment newInstance() {
        Bundle args = new Bundle();
        AddMedicineFragment fragment = new AddMedicineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(setClickListener);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_medicine, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setCurrentTime();
        setSpinnerDoseUnits();
        return rootView;
    }

    @Override
    public void setPresenter(AddMedicineContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showEmptyMedicineError() {
        // Snackbar.make(mTitle, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMedicineList() {
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.every_day, R.id.dv_monday, R.id.dv_tuesday, R.id.dv_wednesday,
            R.id.dv_thursday, R.id.dv_friday, R.id.dv_saturday, R.id.dv_sunday})
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        /** Checking which checkbox was clicked */
        switch (view.getId()) {
            case R.id.dv_monday:
                if (checked) {
                    dayOfWeekList[1] = true;
                } else {
                    dayOfWeekList[1] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_tuesday:
                if (checked) {
                    dayOfWeekList[2] = true;
                } else {
                    dayOfWeekList[2] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_wednesday:
                if (checked) {
                    dayOfWeekList[3] = true;
                } else {
                    dayOfWeekList[3] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_thursday:
                if (checked) {
                    dayOfWeekList[4] = true;
                } else {
                    dayOfWeekList[4] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_friday:
                if (checked) {
                    dayOfWeekList[5] = true;
                } else {
                    dayOfWeekList[5] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_saturday:
                if (checked) {
                    dayOfWeekList[6] = true;
                } else {
                    dayOfWeekList[6] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.dv_sunday:
                if (checked) {
                    dayOfWeekList[0] = true;
                } else {
                    dayOfWeekList[0] = false;
                    everyDay.setChecked(false);
                }
                break;
            case R.id.every_day:
                LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.checkbox_layout);
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View v = ll.getChildAt(i);
                    ((DayViewCheckBox) v).setChecked(checked);
                    onCheckboxClicked(v);
                }
                break;
        }
    }

    @OnClick(R.id.tv_medicine_time)
    void onMedicineTimeClick() {
        showTimePicker();
    }

    private void showTimePicker() {
        Calendar mCurrentTime = Calendar.getInstance();
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                tvMedicineTime.setText(String.format(Locale.getDefault(), "%d:%d", selectedHour, selectedMinute));
            }
        }, hour, minute, false);//No 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void setCurrentTime() {
        Calendar mCurrentTime = Calendar.getInstance();
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE);

        tvMedicineTime.setText(String.format(Locale.getDefault(), "%d:%d", hour, minute));
    }

    private void setSpinnerDoseUnits() {
        doseUnitList = Arrays.asList(getResources().getStringArray(R.array.medications_shape_array));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_dropdown_item_1line, doseUnitList);
        spinnerDoseUnits.setAdapter(adapter);
    }

    @OnItemSelected(R.id.spinner_dose_units)
    void onSpinnerItemSelected(int position) {
        if (doseUnitList == null || doseUnitList.isEmpty()) {
            return;
        }

        doseUnit = doseUnitList.get(position);
        Log.d("TAG", doseUnit);
    }

    private View.OnClickListener setClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int checkBoxCounter = 0;

            String pill_name = editMedName.getText().toString();
            String doseQuantity = tvDoseQuantity.getText().toString();

            Calendar takeTime = Calendar.getInstance();
            Date date = takeTime.getTime();
            String dateString = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);

            /** Updating model */
            MedicineAlarm alarm = new MedicineAlarm();
            int alarmId = new Random().nextInt(100);

            /** If Pill does not already exist */
            if (!mPresenter.isMedicineExits(pill_name)) {
                Pills pill = new Pills();
                pill.setPillName(pill_name);
                alarm.setDateString(dateString);
                alarm.setHour(hour);
                alarm.setMinute(minute);
                alarm.setPillName(pill_name);
                alarm.setDayOfWeek(dayOfWeekList);
                alarm.setDoseUnit(doseUnit);
                alarm.setDoseQuantity(doseQuantity);
                alarm.setAlarmId(alarmId);
                pill.addAlarm(alarm);
                long pillId = mPresenter.addPills(pill);
                pill.setPillId(pillId);
                mPresenter.saveMedicine(alarm, pill);
            } else { // If Pill already exists
                Pills pill = mPresenter.getPillsByName(pill_name);
                alarm.setDateString(dateString);
                alarm.setHour(hour);
                alarm.setMinute(minute);
                alarm.setPillName(pill_name);
                alarm.setDayOfWeek(dayOfWeekList);
                alarm.setDoseUnit(doseUnit);
                alarm.setDoseQuantity(doseQuantity);
                alarm.setAlarmId(alarmId);
                pill.addAlarm(alarm);
                mPresenter.saveMedicine(alarm, pill);
            }

            List<Long> ids = new LinkedList<>();
            try {
                List<MedicineAlarm> alarms = mPresenter.getMedicineByPillName(pill_name);
                for (MedicineAlarm tempAlarm : alarms) {
                    if (tempAlarm.getHour() == hour && tempAlarm.getMinute() == minute) {
                        ids = tempAlarm.getIds();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < 7; i++) {
                if (dayOfWeekList[i] && pill_name.length() != 0) {

                    int dayOfWeek = i + 1;
                    long _id = ids.get(checkBoxCounter);
                    int id = (int) _id;
                    checkBoxCounter++;

                    /** This intent invokes the activity ReminderActivity, which in turn opens the AlertAlarm window */
                    Intent intent = new Intent(getActivity(), ReminderActivity.class);
                    intent.putExtra(ReminderFragment.EXTRA_ID, _id);

                    PendingIntent operation = PendingIntent.getActivity(getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    /** Getting a reference to the System Service ALARM_SERVICE */
                    AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(ALARM_SERVICE);

                    /** Creating a calendar object corresponding to the date and time set by the user */
                    Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                    /** Converting the date and time in to milliseconds elapsed since epoch */
                    long alarm_time = calendar.getTimeInMillis();

                    if (calendar.before(Calendar.getInstance()))
                        alarm_time += AlarmManager.INTERVAL_DAY * 7;

                    assert alarmManager != null;
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm_time,
                            AlarmManager.INTERVAL_DAY * 7, operation);
                }
            }
            Toast.makeText(getContext(), "Alarm for " + pill_name + " is set successfully", Toast.LENGTH_SHORT).show();
            showMedicineList();
        }
    };
}


package com.vishwajeeth.medicinetime.addmedicine;

import androidx.annotation.NonNull;

import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.util.List;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public class AddMedicinePresenter implements AddMedicineContract.Presenter, MedicineDataSource.GetTaskCallback {

    @NonNull
    private final MedicineDataSource mMedicineRepository;

    private final AddMedicineContract.View mAddMedicineView;

    private int mMedicineId;

    private boolean mIsDataMissing;

    public AddMedicinePresenter(int mMedicineId, @NonNull MedicineDataSource mMedicineRepository, AddMedicineContract.View mAddMedicineView, boolean mIsDataMissing) {
        this.mMedicineId = mMedicineId;
        this.mMedicineRepository = mMedicineRepository;
        this.mAddMedicineView = mAddMedicineView;
        this.mIsDataMissing = mIsDataMissing;

        mAddMedicineView.setPresenter(this);
    }

    private boolean isNewTask() {
        return mMedicineId <= 0;
    }

    @Override
    public void start() {

    }

    @Override
    public void saveMedicine(MedicineAlarm alarm, Pills pills) {
        mMedicineRepository.saveMedicine(alarm, pills);
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    @Override
    public boolean isMedicineExits(String pillName) {
        return mMedicineRepository.medicineExits(pillName);
    }

    @Override
    public long addPills(Pills pills) {
        return mMedicineRepository.savePills(pills);
    }

    @Override
    public Pills getPillsByName(String pillName) {
        return mMedicineRepository.getPillsByName(pillName);
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        return mMedicineRepository.getMedicineByPillName(pillName);
    }

    @Override
    public List<Long> tempIds() {
        return mMedicineRepository.tempIds();
    }

    @Override
    public void deleteMedicineAlarm(long alarmId) {
        mMedicineRepository.deleteAlarm(alarmId);
    }

    @Override
    public void onTaskLoaded(MedicineAlarm medicineAlarm) {
        // The view may not be able to handle UI updates anymore
        /*if (mAddMedicineView.isActive()){
            mAddMedicineView.setDose(medicineAlarm.getDose());
            mAddMedicineView.setMedName(medicineAlarm.getMedicineName());
            mAddMedicineView.setDays(medicineAlarm.getDays());
            mAddMedicineView.setTime(medicineAlarm.getTime());
        }
        mIsDataMissing = false;*/
    }

    @Override
    public void onDataNotAvailable() {
        if (mAddMedicineView.isActive()) {
            mAddMedicineView.showEmptyMedicineError();
        }
    }
}


package com.vishwajeeth.medicinetime.utils;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollingCalendarBehavior extends AppBarLayout.Behavior {

    public ScrollingCalendarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        return false;/*super.onInterceptTouchEvent(parent, child, ev);*/
    }
}

package com.vishwajeeth.medicinetime.utils;

import android.content.Context;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.View;


/**
 * Created by vishwajeeth on 13/07/17.
 */

public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void setBackGround(Context context, View view, @DrawableRes int drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(context, drawable));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(context, drawable));
        }
    }

}

package com.vishwajeeth.medicinetime.utils;

import android.graphics.Typeface;

import com.vishwajeeth.medicinetime.MedicineApp;

import java.util.Hashtable;

public class FontUtil {

    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String ROBOTO_LIGHT = "fonts/Roboto-Light.ttf";
    public static final String ROBOTO_BOLD = "fonts/Roboto-Bold.ttf";


    // Constructor
    private FontUtil() { }

    // Cache fonts in hash table
    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();
    public static Typeface getTypeface(String name) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(MedicineApp.getInstance().getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}


package com.vishwajeeth.medicinetime.medicine;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.addmedicine.AddMedicineActivity;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.views.RobotoLightTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicineFragment extends Fragment implements MedicineContract.View, MedicineAdapter.OnItemClickListener {

    @BindView(R.id.medicine_list)
    RecyclerView rvMedList;

    Unbinder unbinder;

    @BindView(R.id.noMedIcon)
    ImageView noMedIcon;

    @BindView(R.id.noMedText)
    RobotoLightTextView noMedText;

    @BindView(R.id.add_med_now)
    TextView addMedNow;

    @BindView(R.id.no_med_view)
    View noMedView;

    @BindView(R.id.progressLoader)
    ProgressBar progressLoader;


    private MedicineContract.Presenter presenter;

    private MedicineAdapter medicineAdapter;


    public MedicineFragment() {

    }

    public static MedicineFragment newInstance() {
        Bundle args = new Bundle();
        MedicineFragment fragment = new MedicineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        medicineAdapter = new MedicineAdapter(new ArrayList<>(0));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine, container, false);
        unbinder = ButterKnife.bind(this, view);
        setAdapter();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fab_add_task);
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(v -> presenter.addNewMedicine());
    }

    private void setAdapter() {
        rvMedList.setAdapter(medicineAdapter);
        rvMedList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMedList.setHasFixedSize(true);
        medicineAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        presenter.onStart(day);
    }

    @Override
    public void setPresenter(MedicineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if (getView() == null) {
            return;
        }
        progressLoader.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMedicineList(List<MedicineAlarm> medicineAlarmList) {
        medicineAdapter.replaceData(medicineAlarmList);
        rvMedList.setVisibility(View.VISIBLE);
        noMedView.setVisibility(View.GONE);
    }

    @Override
    public void showAddMedicine() {
        Intent intent = new Intent(getContext(), AddMedicineActivity.class);
        startActivityForResult(intent, AddMedicineActivity.REQUEST_ADD_TASK);
    }


    @Override
    public void showMedicineDetails(long taskId, String medName) {
        Intent intent = new Intent(getContext(), AddMedicineActivity.class);
        intent.putExtra(AddMedicineActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(AddMedicineActivity.EXTRA_TASK_NAME, medName);
        startActivity(intent);
    }


    @Override
    public void showLoadingMedicineError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    @Override
    public void showNoMedicine() {
        showNoTasksViews(
                getResources().getString(R.string.no_medicine_added)
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_me_message));
    }

    @Override
    public void showMedicineDeletedSuccessfully() {
        showMessage(getString(R.string.successfully_deleted_message));
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        presenter.onStart(day);
    }

    private void showMessage(String message) {
        if (getView() != null)
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.add_med_now)
    void addMedicine() {
        showAddMedicine();
    }

    private void showNoTasksViews(String mainText) {
        rvMedList.setVisibility(View.GONE);
        noMedView.setVisibility(View.VISIBLE);
        noMedText.setText(mainText);
        noMedIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_my_health));
        addMedNow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
    }

    @Override
    public void onMedicineDeleteClicked(MedicineAlarm medicineAlarm) {
        presenter.deleteMedicineAlarm(medicineAlarm, getActivity());
    }
}



package com.vishwajeeth.medicinetime.medicine;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vishwajeeth.medicinetime.addmedicine.AddMedicineActivity;
import com.vishwajeeth.medicinetime.alarm.ReminderActivity;
import com.vishwajeeth.medicinetime.alarm.ReminderFragment;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.MedicineRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicinePresenter implements MedicineContract.Presenter {

    private final MedicineRepository mMedicineRepository;

    private final MedicineContract.View mMedView;


    MedicinePresenter(@NonNull MedicineRepository medicineRepository, @NonNull MedicineContract.View medView) {
        this.mMedicineRepository = medicineRepository;
        this.mMedView = medView;
        medView.setPresenter(this);
    }

    @Override
    public void loadMedicinesByDay(int day, boolean showIndicator) {
        loadListByDay(day, showIndicator);
    }

    @Override
    public void deleteMedicineAlarm(MedicineAlarm medicineAlarm, Context context) {
        List<MedicineAlarm> alarms = mMedicineRepository.getAllAlarms(medicineAlarm.getPillName());
        for (MedicineAlarm alarm : alarms) {
            mMedicineRepository.deleteAlarm(alarm.getId());
            /** This intent invokes the activity ReminderActivity, which in turn opens the AlertAlarm window */
            Intent intent = new Intent(context, ReminderActivity.class);
            intent.putExtra(ReminderFragment.EXTRA_ID, alarm.getAlarmId());

            PendingIntent operation = PendingIntent.getActivity(context, alarm.getAlarmId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


            /** Getting a reference to the System Service ALARM_SERVICE */
            AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(operation);
            }
        }
        mMedView.showMedicineDeletedSuccessfully();
    }

    @Override
    public void start() {

    }

    @Override
    public void onStart(int day) {
        Log.d("TAG", "onStart: " + day);
        loadMedicinesByDay(day, true);
    }

    @Override
    public void reload(int day) {
        Log.d("TAG", "reload: " + day);
        loadListByDay(day, true);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (AddMedicineActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mMedView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void addNewMedicine() {
        mMedView.showAddMedicine();
    }

    private void loadListByDay(int day, final boolean showLoadingUi) {
        if (showLoadingUi)
            mMedView.showLoadingIndicator(true);

        mMedicineRepository.getMedicineListByDay(day, new MedicineDataSource.LoadMedicineCallbacks() {
            @Override
            public void onMedicineLoaded(List<MedicineAlarm> medicineAlarmList) {
                processMedicineList(medicineAlarmList);
                // The view may not be able to handle UI updates anymore
                if (!mMedView.isActive()) {
                    return;
                }
                if (showLoadingUi) {
                    mMedView.showLoadingIndicator(false);
                }
            }

            @Override
            public void onDataNotAvailable() {
                if (!mMedView.isActive()) {
                    return;
                }
                if (showLoadingUi) {
                    mMedView.showLoadingIndicator(false);
                }

                mMedView.showNoMedicine();
            }
        });
    }

    private void processMedicineList(List<MedicineAlarm> medicineAlarmList) {

        if (medicineAlarmList.isEmpty()) {
            // Show a message indicating there are no tasks for that filter type.
            mMedView.showNoMedicine();
        } else {
            //Show the list of Medicines
            Collections.sort(medicineAlarmList);
            mMedView.showMedicineList(medicineAlarmList);
        }
    }

}


package com.vishwajeeth.medicinetime.medicine;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;
import com.vishwajeeth.medicinetime.views.RobotoRegularTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<MedicineAlarm> medicineAlarmList;
    private OnItemClickListener onItemClickListener;

    public MedicineAdapter(List<MedicineAlarm> medicineAlarmList) {
        this.medicineAlarmList = medicineAlarmList;
    }

    public void replaceData(List<MedicineAlarm> medicineAlarmList) {
        this.medicineAlarmList = medicineAlarmList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        final MedicineAlarm medicineAlarm = medicineAlarmList.get(position);
        if (medicineAlarm == null) {
            return;
        }
        holder.tvMedTime.setText(medicineAlarm.getStringTime());
        holder.tvMedicineName.setText(medicineAlarm.getPillName());
        holder.tvDoseDetails.setText(medicineAlarm.getFormattedDose());
        holder.ivAlarmDelete.setVisibility(View.VISIBLE);
        holder.ivAlarmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onMedicineDeleteClicked(medicineAlarm);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (medicineAlarmList != null && !medicineAlarmList.isEmpty()) ? medicineAlarmList.size() : 0;
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_med_time)
        RobotoBoldTextView tvMedTime;

        @BindView(R.id.tv_medicine_name)
        RobotoBoldTextView tvMedicineName;

        @BindView(R.id.tv_dose_details)
        RobotoRegularTextView tvDoseDetails;

        @BindView(R.id.iv_medicine_action)
        ImageView ivMedicineAction;

        @BindView(R.id.iv_alarm_delete)
        ImageView ivAlarmDelete;

        MedicineViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    interface OnItemClickListener {
        void onMedicineDeleteClicked(MedicineAlarm medicineAlarm);
    }
}


package com.vishwajeeth.medicinetime.medicine;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vishwajeeth.medicinetime.Injection;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.report.MonthlyReportActivity;
import com.vishwajeeth.medicinetime.utils.ActivityUtils;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MedicineActivity extends AppCompatActivity {


    @BindView(R.id.compactcalendar_view)
    CompactCalendarView mCompactCalendarView;

    @BindView(R.id.date_picker_text_view)
    TextView datePickerTextView;

    @BindView(R.id.date_picker_button)
    RelativeLayout datePickerButton;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.contentFrame)
    FrameLayout contentFrame;

    @BindView(R.id.fab_add_task)
    FloatingActionButton fabAddTask;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.date_picker_arrow)
    ImageView arrow;

    private MedicinePresenter presenter;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", /*Locale.getDefault()*/Locale.ENGLISH);

    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mCompactCalendarView.setLocale(TimeZone.getDefault(), /*Locale.getDefault()*/Locale.ENGLISH);

        mCompactCalendarView.setShouldDrawDaysHeader(true);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setSubtitle(dateFormat.format(dateClicked));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateClicked);

                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (isExpanded) {
                    ViewCompat.animate(arrow).rotation(0).start();
                } else {
                    ViewCompat.animate(arrow).rotation(180).start();
                }
                isExpanded = !isExpanded;
                appBarLayout.setExpanded(isExpanded, true);
                presenter.reload(day);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });
        setCurrentDate(new Date());
        MedicineFragment medicineFragment = (MedicineFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (medicineFragment == null) {
            medicineFragment = MedicineFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), medicineFragment, R.id.contentFrame);
        }

        //Create MedicinePresenter
        presenter = new MedicinePresenter(Injection.provideMedicineRepository(MedicineActivity.this), medicineFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.medicine_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_stats) {
            Intent intent = new Intent(this, MonthlyReportActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setCurrentDate(Date date) {
        setSubtitle(dateFormat.format(date));
        mCompactCalendarView.setCurrentDate(date);
    }

    public void setSubtitle(String subtitle) {
        datePickerTextView.setText(subtitle);
    }

    @OnClick(R.id.date_picker_button)
    void onDatePickerButtonClicked() {
        if (isExpanded) {
            ViewCompat.animate(arrow).rotation(0).start();
        } else {
            ViewCompat.animate(arrow).rotation(180).start();
        }

        isExpanded = !isExpanded;
        appBarLayout.setExpanded(isExpanded, true);
    }
}


package com.vishwajeeth.medicinetime.medicine;

import android.content.Context;

import com.vishwajeeth.medicinetime.BasePresenter;
import com.vishwajeeth.medicinetime.BaseView;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;

import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public interface MedicineContract {

    interface View extends BaseView<Presenter>{

        void showLoadingIndicator(boolean active);

        void showMedicineList(List<MedicineAlarm> medicineAlarmList);

        void showAddMedicine();

        void showMedicineDetails(long medId, String medName);

        void showLoadingMedicineError();

        void showNoMedicine();

        void showSuccessfullySavedMessage();

        void  showMedicineDeletedSuccessfully();

        boolean isActive();


    }

    interface Presenter extends BasePresenter{

        void onStart(int day);

        void reload(int day);

        void result(int requestCode, int resultCode);

        void loadMedicinesByDay(int day, boolean showIndicator);

        void deleteMedicineAlarm(MedicineAlarm medicineAlarm, Context context);

        void addNewMedicine();

    }
}


package com.vishwajeeth.medicinetime.report;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.views.RobotoLightTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MonthlyReportFragment extends Fragment implements MonthlyReportContract.View {

    @BindView(R.id.rv_history_list)
    RecyclerView rvHistoryList;

    @BindView(R.id.progressLoader)
    ProgressBar progressLoader;

    @BindView(R.id.noMedIcon)
    ImageView noMedIcon;

    @BindView(R.id.noMedText)
    RobotoLightTextView noMedText;

    @BindView(R.id.no_med_view)
    View noMedView;

    Unbinder unbinder;

    @BindView(R.id.filteringLabel)
    TextView filteringLabel;

    @BindView(R.id.tasksLL)
    LinearLayout tasksLL;

    private HistoryAdapter mHistoryAdapter;

    private MonthlyReportContract.Presenter presenter;

    public static MonthlyReportFragment newInstance() {
        Bundle args = new Bundle();
        MonthlyReportFragment fragment = new MonthlyReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistoryAdapter = new HistoryAdapter(new ArrayList<History>());
        setHasOptionsMenu(true);
    }

    private void setAdapter() {
        rvHistoryList.setAdapter(mHistoryAdapter);
        rvHistoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistoryList.setHasFixedSize(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        setAdapter();
        return view;
    }

    @Override
    public void onResume() {
            super.onResume();
            presenter.start();
    }

    @Override
    public void setPresenter(MonthlyReportContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (getView() == null) {
            return;
        }
        progressLoader.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showHistoryList(List<History> historyList) {
        mHistoryAdapter.replaceData(historyList);
        tasksLL.setVisibility(View.VISIBLE);
        noMedView.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingError() {

    }

    @Override
    public void showNoHistory() {
        showNoHistoryView(
                getString(R.string.no_history),
                R.drawable.icon_my_health
        );
    }

    @Override
    public void showTakenFilterLabel() {
        filteringLabel.setText(R.string.taken_label);
    }

    @Override
    public void showIgnoredFilterLabel() {
        filteringLabel.setText(R.string.ignore_label);
    }

    @Override
    public void showAllFilterLabel() {
        filteringLabel.setText(R.string.all_label);
    }

    @Override
    public void showNoTakenHistory() {
        showNoHistoryView(
                getString(R.string.no_taken_med_history),
                R.drawable.icon_my_health
        );
    }

    @Override
    public void showNoIgnoredHistory() {
        showNoHistoryView(
                getString(R.string.no_ignored_history),
                R.drawable.icon_my_health
        );
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_history, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.all:
                        presenter.setFiltering(FilterType.ALL_MEDICINES);
                        break;
                    case R.id.taken:
                        presenter.setFiltering(FilterType.TAKEN_MEDICINES);
                        break;
                    case R.id.ignored:
                        presenter.setFiltering(FilterType.IGNORED_MEDICINES);
                        break;
                }
                presenter.loadHistory(true);
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
        }
        return true;
    }

    private void showNoHistoryView(String mainText, int iconRes) {
        tasksLL.setVisibility(View.GONE);
        noMedView.setVisibility(View.VISIBLE);

        noMedText.setText(mainText);
        noMedIcon.setImageDrawable(getResources().getDrawable(iconRes));
    }
}


package com.vishwajeeth.medicinetime.report;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;
import com.vishwajeeth.medicinetime.views.RobotoRegularTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {


    private List<History> mHistoryList;

    HistoryAdapter(List<History> historyList) {
        setList(historyList);
    }

    void replaceData(List<History> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<History> historyList) {
        this.mHistoryList = historyList;
    }


    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        History history = mHistoryList.get(position);
        if (history == null) {
            return;
        }
        holder.tvMedDate.setText(history.getFormattedDate());
        setMedicineAction(holder, history.getAction());
        holder.tvMedicineName.setText(history.getPillName());
        holder.tvDoseDetails.setText(history.getFormattedDose());

    }

    private void setMedicineAction(HistoryViewHolder holder, int action) {
        switch (action) {
            case 0:
            default:
                holder.ivMedicineAction.setVisibility(View.GONE);
                break;
            case 1:
                holder.ivMedicineAction.setVisibility(View.VISIBLE);
                holder.ivMedicineAction.setImageResource(R.drawable.image_reminder_taken);
                break;
            case 2:
                holder.ivMedicineAction.setImageResource(R.drawable.image_reminder_not_taken);
                holder.ivMedicineAction.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (mHistoryList != null && !mHistoryList.isEmpty()) ? mHistoryList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_med_date)
        RobotoBoldTextView tvMedDate;

        @BindView(R.id.tv_medicine_name)
        RobotoBoldTextView tvMedicineName;

        @BindView(R.id.tv_dose_details)
        RobotoRegularTextView tvDoseDetails;

        @BindView(R.id.iv_medicine_action)
        ImageView ivMedicineAction;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}


package com.vishwajeeth.medicinetime.report;

import androidx.annotation.NonNull;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.MedicineRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MonthlyReportPresenter implements MonthlyReportContract.Presenter {


    private final MedicineRepository mMedicineRepository;

    private final MonthlyReportContract.View mMonthlyReportView;

    private FilterType mCurrentFilteringType = FilterType.ALL_MEDICINES;

    public MonthlyReportPresenter(@NonNull MedicineRepository medicineRepository, MonthlyReportContract.View monthlyReportView) {
        this.mMedicineRepository = medicineRepository;
        this.mMonthlyReportView = monthlyReportView;
        mMonthlyReportView.setPresenter(this);
    }


    @Override
    public void start() {
        loadHistory(true);
    }


    @Override
    public void loadHistory(boolean showLoading) {
        loadHistoryFromDb(showLoading);
    }

    private void loadHistoryFromDb(final boolean showLoading) {
        if (showLoading) {
            mMonthlyReportView.setLoadingIndicator(true);
        }
        mMedicineRepository.getMedicineHistory(new MedicineDataSource.LoadHistoryCallbacks() {
            @Override
            public void onHistoryLoaded(List<History> historyList) {
                List<History> historyShowList = new ArrayList<>();

                //We will filter the History based on request type
                for (History history : historyList) {
                    switch (mCurrentFilteringType) {
                        case ALL_MEDICINES:
                            historyShowList.add(history);
                            break;
                        case TAKEN_MEDICINES:
                            if (history.getAction() == 1) {
                                historyShowList.add(history);
                            }
                            break;
                        case IGNORED_MEDICINES:
                            if (history.getAction() == 2) {
                                historyShowList.add(history);
                            }
                            break;
                    }
                }
                processHistory(historyShowList);
                if (!mMonthlyReportView.isActive()) {
                    return;
                }
                if (showLoading) {
                    mMonthlyReportView.setLoadingIndicator(false);
                }

            }

            @Override
            public void onDataNotAvailable() {
                if (!mMonthlyReportView.isActive()) {
                    return;
                }
                if (showLoading) {
                    mMonthlyReportView.setLoadingIndicator(false);
                }
                mMonthlyReportView.showLoadingError();
            }
        });

    }

    private void processHistory(List<History> historyList) {

        if (historyList.isEmpty()) {
            // Show a message indicating there are no history for that filter type.
            processEmptyHistory();
        } else {
            //Show the list of history
            mMonthlyReportView.showHistoryList(historyList);
            //Set filter label's text
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFilteringType) {
            case ALL_MEDICINES:
                mMonthlyReportView.showAllFilterLabel();
                break;
            case TAKEN_MEDICINES:
                mMonthlyReportView.showTakenFilterLabel();
                break;
            case IGNORED_MEDICINES:
                mMonthlyReportView.showIgnoredFilterLabel();
                break;
            default:
                mMonthlyReportView.showAllFilterLabel();
        }
    }


    private void processEmptyHistory() {
        switch (mCurrentFilteringType) {
            case ALL_MEDICINES:
                mMonthlyReportView.showNoHistory();
                break;
            case TAKEN_MEDICINES:
                mMonthlyReportView.showNoTakenHistory();
                break;
            case IGNORED_MEDICINES:
                mMonthlyReportView.showNoIgnoredHistory();
                break;
            default:
                mMonthlyReportView.showNoHistory();
                break;
        }
    }


    @Override
    public void setFiltering(FilterType filterType) {
        mCurrentFilteringType = filterType;
    }

    @Override
    public FilterType getFilterType() {
        return mCurrentFilteringType;
    }
}


package com.vishwajeeth.medicinetime.report;

import com.vishwajeeth.medicinetime.BasePresenter;
import com.vishwajeeth.medicinetime.BaseView;
import com.vishwajeeth.medicinetime.data.source.History;
import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public interface MonthlyReportContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showHistoryList(List<History> historyList);

        void showLoadingError();

        void showNoHistory();

        void showTakenFilterLabel();

        void showIgnoredFilterLabel();

        void showAllFilterLabel();

        void showNoTakenHistory();

        void showNoIgnoredHistory();

        boolean isActive();

        void showFilteringPopUpMenu();

    }

    interface Presenter extends BasePresenter {

        void loadHistory(boolean showLoading);

        void setFiltering(FilterType filterType);

        FilterType getFilterType();
    }
}


package com.vishwajeeth.medicinetime.report;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.vishwajeeth.medicinetime.Injection;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.utils.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MonthlyReportActivity extends AppCompatActivity {

    private static final String CURRENT_FILTERING_TYPE = "current_filtering_type";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MonthlyReportPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Create Fragment
        MonthlyReportFragment monthlyReportFragment = (MonthlyReportFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (monthlyReportFragment == null) {
            monthlyReportFragment = MonthlyReportFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), monthlyReportFragment, R.id.contentFrame);
        }

        //Create TaskPresenter
        presenter = new MonthlyReportPresenter(Injection.provideMedicineRepository(MonthlyReportActivity.this), monthlyReportFragment);

        //Load previous saved Instance
        if (savedInstanceState != null) {
            FilterType taskFilterType = (FilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_TYPE);
            presenter.setFiltering(taskFilterType);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_TYPE, presenter.getFilterType());
        super.onSaveInstanceState(outState);
    }
}


package com.vishwajeeth.medicinetime.report;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public enum  FilterType {

    ALL_MEDICINES,

    TAKEN_MEDICINES,

    IGNORED_MEDICINES
}


package com.vishwajeeth.medicinetime.alarm;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.medicine.MedicineActivity;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;
import com.vishwajeeth.medicinetime.views.RobotoRegularTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class ReminderFragment extends Fragment implements ReminderContract.View {

    public static final String EXTRA_ID = "extra_id";

    @BindView(R.id.tv_med_time)
    RobotoBoldTextView tvMedTime;

    @BindView(R.id.tv_medicine_name)
    RobotoBoldTextView tvMedicineName;

    @BindView(R.id.tv_dose_details)
    RobotoRegularTextView tvDoseDetails;

    @BindView(R.id.iv_ignore_med)
    ImageView ivIgnoreMed;

    @BindView(R.id.iv_take_med)
    ImageView ivTakeMed;

    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;

    Unbinder unbinder;

    private MedicineAlarm medicineAlarm;

    private long id;

    private MediaPlayer mMediaPlayer;

    private Vibrator mVibrator;

    private ReminderContract.Presenter presenter;

    static ReminderFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_ID, id);
        ReminderFragment fragment = new ReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments().getLong(EXTRA_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(ReminderContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showMedicine(MedicineAlarm medicineAlarm) {
        this.medicineAlarm = medicineAlarm;
        mVibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 10000};
        mVibrator.vibrate(pattern, 0);

        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.cuco_sound);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        tvMedTime.setText(medicineAlarm.getStringTime());
        tvMedicineName.setText(medicineAlarm.getPillName());
        tvDoseDetails.setText(medicineAlarm.getFormattedDose());
    }

    @Override
    public void showNoData() {
        //
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onStart(id);
    }

    @OnClick(R.id.iv_take_med)
    void onMedTakeClick() {
        onMedicineTaken();
        stopMedialPlayer();
        stopVibrator();
    }

    @OnClick(R.id.iv_ignore_med)
    void onMedIgnoreClick() {
        onMedicineIgnored();
        stopMedialPlayer();
        stopVibrator();
    }

    private void stopMedialPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    private void stopVibrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void onMedicineTaken() {
        History history = new History();

        Calendar takeTime = Calendar.getInstance();
        Date date = takeTime.getTime();
        String dateString = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);

        int hour = takeTime.get(Calendar.HOUR_OF_DAY);
        int minute = takeTime.get(Calendar.MINUTE);
        String am_pm = (hour < 12) ? "am" : "pm";

        history.setHourTaken(hour);
        history.setMinuteTaken(minute);
        history.setDateString(dateString);
        history.setPillName(medicineAlarm.getPillName());
        history.setAction(1);
        history.setDoseQuantity(medicineAlarm.getDoseQuantity());
        history.setDoseUnit(medicineAlarm.getDoseUnit());

        presenter.addPillsToHistory(history);

        String stringMinute;
        if (minute < 10)
            stringMinute = "0" + minute;
        else
            stringMinute = "" + minute;

        int nonMilitaryHour = hour % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;

        Toast.makeText(getContext(), medicineAlarm.getPillName() + " was taken at " + nonMilitaryHour + ":" + stringMinute + " " + am_pm + ".", Toast.LENGTH_SHORT).show();

        Intent returnHistory = new Intent(getContext(), MedicineActivity.class);
        startActivity(returnHistory);
        getActivity().finish();
    }


    private void onMedicineIgnored() {
        History history = new History();

        Calendar takeTime = Calendar.getInstance();
        Date date = takeTime.getTime();
        String dateString = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);

        int hour = takeTime.get(Calendar.HOUR_OF_DAY);
        int minute = takeTime.get(Calendar.MINUTE);
        String am_pm = (hour < 12) ? "am" : "pm";

        history.setHourTaken(hour);
        history.setMinuteTaken(minute);
        history.setDateString(dateString);
        history.setPillName(medicineAlarm.getPillName());
        history.setAction(2);
        history.setDoseQuantity(medicineAlarm.getDoseQuantity());
        history.setDoseUnit(medicineAlarm.getDoseUnit());

        presenter.addPillsToHistory(history);

        String stringMinute;
        if (minute < 10)
            stringMinute = "0" + minute;
        else
            stringMinute = "" + minute;

        int nonMilitaryHour = hour % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;

        Toast.makeText(getContext(), medicineAlarm.getPillName() + " was ignored at " + nonMilitaryHour + ":" + stringMinute + " " + am_pm + ".", Toast.LENGTH_SHORT).show();

        Intent returnHistory = new Intent(getContext(), MedicineActivity.class);
        startActivity(returnHistory);
        getActivity().finish();
    }


    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onFinish() {
        stopMedialPlayer();
        stopVibrator();
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}


package com.vishwajeeth.medicinetime.alarm;

import androidx.annotation.NonNull;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.MedicineRepository;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class ReminderPresenter implements ReminderContract.Presenter {

    private final MedicineRepository mMedicineRepository;

    private final ReminderContract.View mReminderView;

    ReminderPresenter(@NonNull MedicineRepository medicineRepository, @NonNull ReminderContract.View reminderView) {
        this.mMedicineRepository = medicineRepository;
        this.mReminderView = reminderView;

        mReminderView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void finishActivity() {
        mReminderView.onFinish();
    }

    @Override
    public void onStart(long id) {
        loadMedicineById(id);
    }

    @Override
    public void loadMedicineById(long id) {
        loadMedicine(id);
    }


    private void loadMedicine(long id) {
        mMedicineRepository.getMedicineAlarmById(id, new MedicineDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(MedicineAlarm medicineAlarm) {
                if (!mReminderView.isActive()) {
                    return;
                }
                if (medicineAlarm == null) {
                    return;
                }
                mReminderView.showMedicine(medicineAlarm);
            }

            @Override
            public void onDataNotAvailable() {
                mReminderView.showNoData();
            }
        });
    }

    @Override
    public void addPillsToHistory(History history) {
        mMedicineRepository.saveToHistory(history);
    }
}


package com.vishwajeeth.medicinetime.alarm;

import com.vishwajeeth.medicinetime.BasePresenter;
import com.vishwajeeth.medicinetime.BaseView;
import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public interface ReminderContract {

    interface View extends BaseView<Presenter> {

        void showMedicine(MedicineAlarm medicineAlarm);

        void showNoData();

        boolean isActive();

        void onFinish();

    }

    interface Presenter extends BasePresenter {

        void finishActivity();

        void onStart(long id);

        void loadMedicineById(long id);

        void addPillsToHistory(History history);

    }
}


package com.vishwajeeth.medicinetime.alarm;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.vishwajeeth.medicinetime.Injection;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.utils.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReminderActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    ReminderPresenter mReminderPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_actvity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (!intent.hasExtra(ReminderFragment.EXTRA_ID)) {
            finish();
            return;
        }
        long id = intent.getLongExtra(ReminderFragment.EXTRA_ID, 0);
        ReminderFragment reminderFragment = (ReminderFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (reminderFragment == null) {
            reminderFragment = ReminderFragment.newInstance(id);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), reminderFragment, R.id.contentFrame);
        }

        //Create MedicinePresenter
        mReminderPresenter = new ReminderPresenter(Injection.provideMedicineRepository(ReminderActivity.this), reminderFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mReminderPresenter != null) {
                mReminderPresenter.finishActivity();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mReminderPresenter != null) {
            mReminderPresenter.finishActivity();
        }
    }
}


package com.vishwajeeth.medicinetime.data;

import androidx.annotation.VisibleForTesting;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * Created by vishwajeeth on 13/05/17.
 */

public class FakeMedicineLocalDataSource implements MedicineDataSource {

    private static FakeMedicineLocalDataSource INSTANCE;

    private static final Map<String, MedicineAlarm> MEDICINE_SERVICE_DATA;

    private static final Map<String, History> HISTORY_SERVICE_DATA;

    private static final Map<String, Pills> PILLS_SERVICE_DATA;


    //Prevent from direct Instantiation
    private FakeMedicineLocalDataSource() {

    }

    static {
        MEDICINE_SERVICE_DATA = new LinkedHashMap<>();
        HISTORY_SERVICE_DATA = new LinkedHashMap<>();
        PILLS_SERVICE_DATA = new LinkedHashMap<>();

        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);

        Date date = mCurrentTime.getTime();
        String dateString = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);

        addPills("Paracetamol", 1);
        addPills("Crocin", 2);
        int alarmId = new Random().nextInt(100);
        addMedicine(1, hour, minute, "Paracetamol", "1.0", "tablet(s)", alarmId);
        addMedicine(2, hour + 2, minute + 1, "Crocin", "2.0", "capsule(s)", alarmId);

        addHistory(hour, minute, dateString, "Crocin", 2, "2.0", "capsule(s)", alarmId);
        addHistory(hour + 2, minute + 1, dateString, "Paracetamol", 1, "1.0", "tablet(s)", alarmId);
    }


    private static void addMedicine(long id, int hour, int minute, String pillName, String doseQuantity, String doseUnit, int alarmId) {

        MedicineAlarm medicineAlarm = new MedicineAlarm(id, hour, minute, pillName, doseQuantity, doseUnit, alarmId);
        MEDICINE_SERVICE_DATA.put(String.valueOf(id), medicineAlarm);
    }

    private static void addHistory(int hourTaken, int minuteTaken, String dateString, String pillName, int action, String doseQuantity, String doseUnit, int alarmId) {
        History history = new History(hourTaken, minuteTaken, dateString, pillName, action, doseQuantity, doseUnit, alarmId);
        HISTORY_SERVICE_DATA.put(pillName, history);
    }

    private static void addPills(String pillName, long pillId) {
        Pills pills = new Pills(pillName, pillId);
        PILLS_SERVICE_DATA.put(String.valueOf(pillId), pills);
    }

    public static FakeMedicineLocalDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeMedicineLocalDataSource();
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public void addMedicines(MedicineAlarm... medicineAlarms) {
        for (MedicineAlarm medicineAlarm : medicineAlarms) {
            MEDICINE_SERVICE_DATA.put(String.valueOf(medicineAlarm.getId()), medicineAlarm);
        }
    }

    public void addMedicine(MedicineAlarm... medicineAlarms) {
        for (MedicineAlarm medicineAlarm : medicineAlarms) {
            MEDICINE_SERVICE_DATA.put(String.valueOf(medicineAlarm.getId()), medicineAlarm);
        }
    }

    @Override
    public void getMedicineHistory(LoadHistoryCallbacks loadHistoryCallbacks) {
        loadHistoryCallbacks.onHistoryLoaded(new ArrayList<History>(HISTORY_SERVICE_DATA.values()));
    }

    @Override
    public void getMedicineAlarmById(long id, GetTaskCallback callback) {
        callback.onTaskLoaded(MEDICINE_SERVICE_DATA.get(String.valueOf(id)));
    }

    @Override
    public void saveMedicine(MedicineAlarm medicineAlarm, Pills pills) {
        medicineAlarm.addId(pills.getPillId());
        MEDICINE_SERVICE_DATA.put(String.valueOf(pills.getPillId()), medicineAlarm);
    }

    @Override
    public void getMedicineListByDay(int day, LoadMedicineCallbacks callbacks) {
        callbacks.onMedicineLoaded(new ArrayList<>(MEDICINE_SERVICE_DATA.values()));
    }

    @Override
    public boolean medicineExits(String pillName) {
        return false;
    }

    @Override
    public List<Long> tempIds() {
        return null;
    }

    @Override
    public void deleteAlarm(long alarmId) {
        MEDICINE_SERVICE_DATA.remove(String.valueOf(alarmId));
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        List<MedicineAlarm> medicineAlarms = new ArrayList<>();
        for (Map.Entry<String, MedicineAlarm> entry : MEDICINE_SERVICE_DATA.entrySet()) {
            MedicineAlarm medicineAlarm = entry.getValue();
            if (medicineAlarm.getPillName().equalsIgnoreCase(pillName)) {
                medicineAlarms.add(medicineAlarm);
            }
        }
        return medicineAlarms;
    }

    @Override
    public List<MedicineAlarm> getAllAlarms(String pillName) {
        List<MedicineAlarm> medicineAlarms = new ArrayList<>();
        for (Map.Entry<String, MedicineAlarm> entry : MEDICINE_SERVICE_DATA.entrySet()) {
            MedicineAlarm medicineAlarm = entry.getValue();
            if (medicineAlarm.getPillName().equalsIgnoreCase(pillName)) {
                medicineAlarms.add(medicineAlarm);
            }
        }
        return medicineAlarms;
    }

    @Override
    public Pills getPillsByName(String pillName) {
        for (Map.Entry<String, Pills> entry : PILLS_SERVICE_DATA.entrySet()) {
            Pills pills = entry.getValue();
            if (pills.getPillName().equalsIgnoreCase(pillName)) {
                return pills;
            }
        }
        return null;
    }

    @Override
    public long savePills(Pills pills) {
        PILLS_SERVICE_DATA.put(String.valueOf(pills.getPillId()), pills);
        return pills.getPillId();
    }

    @Override
    public void saveToHistory(History history) {
        HISTORY_SERVICE_DATA.put(String.valueOf(history.getPillName()), history);
    }
}


package com.vishwajeeth.medicinetime.data.source;

import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public interface MedicineDataSource {

    interface LoadMedicineCallbacks {

        void onMedicineLoaded(List<MedicineAlarm> medicineAlarmList);

        void onDataNotAvailable();
    }

    interface GetTaskCallback {

        void onTaskLoaded(MedicineAlarm medicineAlarm);

        void onDataNotAvailable();
    }

    interface LoadHistoryCallbacks {

        void onHistoryLoaded(List<History> historyList);

        void onDataNotAvailable();
    }

    void getMedicineHistory(LoadHistoryCallbacks loadHistoryCallbacks);

    void getMedicineAlarmById(long id, GetTaskCallback callback);

    void saveMedicine(MedicineAlarm medicineAlarm, Pills pills);

    void getMedicineListByDay(int day, LoadMedicineCallbacks callbacks);

    boolean medicineExits(String pillName);

    List<Long> tempIds();

    void deleteAlarm(long alarmId);

    List<MedicineAlarm> getMedicineByPillName(String pillName);

    List<MedicineAlarm> getAllAlarms(String pillName);

    Pills getPillsByName(String pillName);

    long savePills(Pills pills);

    void saveToHistory(History history);

}


package com.vishwajeeth.medicinetime.data.source;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicineRepository implements MedicineDataSource {

    private static MedicineRepository mInstance = null;

    private final MedicineDataSource localDataSource;


    private MedicineRepository(@NonNull MedicineDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }


    public static MedicineRepository getInstance(MedicineDataSource localDataSource) {
        if (mInstance == null) {
            mInstance = new MedicineRepository(localDataSource);
        }
        return mInstance;
    }

    @Override
    public void getMedicineHistory(final LoadHistoryCallbacks loadHistoryCallbacks) {
        localDataSource.getMedicineHistory(new LoadHistoryCallbacks() {
            @Override
            public void onHistoryLoaded(List<History> historyList) {
                loadHistoryCallbacks.onHistoryLoaded(historyList);
            }

            @Override
            public void onDataNotAvailable() {
                loadHistoryCallbacks.onDataNotAvailable();
            }
        });
    }

    @Override
    public void getMedicineAlarmById(long id, final GetTaskCallback callback) {
        localDataSource.getMedicineAlarmById(id, new GetTaskCallback() {
            @Override
            public void onTaskLoaded(MedicineAlarm medicineAlarm) {
                if (medicineAlarm == null) {
                    return;
                }
                callback.onTaskLoaded(medicineAlarm);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    @Override
    public void saveMedicine(MedicineAlarm medicineAlarm, Pills pills) {
        localDataSource.saveMedicine(medicineAlarm, pills);
    }


    @Override
    public void getMedicineListByDay(int day, final LoadMedicineCallbacks callbacks) {
        localDataSource.getMedicineListByDay(day, new LoadMedicineCallbacks() {
            @Override
            public void onMedicineLoaded(List<MedicineAlarm> medicineAlarmList) {
                callbacks.onMedicineLoaded(medicineAlarmList);
            }

            @Override
            public void onDataNotAvailable() {
                callbacks.onDataNotAvailable();
            }
        });
    }

    @Override
    public boolean medicineExits(String pillName) {
        return false;
    }

    @Override
    public List<Long> tempIds() {
        return localDataSource.tempIds();
    }

    @Override
    public void deleteAlarm(long alarmId) {
        localDataSource.deleteAlarm(alarmId);
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        return localDataSource.getMedicineByPillName(pillName);
    }

    @Override
    public List<MedicineAlarm> getAllAlarms(String pillName) {
        return localDataSource.getAllAlarms(pillName);
    }

    @Override
    public Pills getPillsByName(String pillName) {
        return localDataSource.getPillsByName(pillName);
    }

    @Override
    public long savePills(Pills pills) {
        return localDataSource.savePills(pills);
    }

    @Override
    public void saveToHistory(History history) {
        localDataSource.saveToHistory(history);
    }
}


package com.vishwajeeth.medicinetime.data.source;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class Pills {

    private String pillName;

    private long pillId;

    private List<MedicineAlarm> medicineAlarms = new LinkedList<MedicineAlarm>();

    public Pills(){

    }

    public Pills(String pillName, long pillId) {
        this.pillName = pillName;
        this.pillId = pillId;
    }

    public String getPillName() { return pillName; }

    public void setPillName(String pillName) { this.pillName = pillName; }

    /**
     *
     * @param medicineAlarm
     * allows a new medicineAlarm sto be added to a preexisting medicineAlarm
     */
    public void addAlarm(MedicineAlarm medicineAlarm) {
        medicineAlarms.add(medicineAlarm);
        Collections.sort(medicineAlarms);
    }

    public long getPillId() {
        return pillId;
    }

    public void setPillId(long pillID) {
        this.pillId = pillID;
    }
}


package com.vishwajeeth.medicinetime.data.source;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by vishwajeeth on 12/07/17.
 */

public class MedicineAlarm implements Comparable<MedicineAlarm> {

    private long id;  // DB id number

    private int hour; //

    private int minute;

    private String pillName;

    private String doseQuantity;

    private String doseUnit;

    private String dateString;

    private  int alarmId;

    public MedicineAlarm() {

    }

    public MedicineAlarm(long id, int hour, int minute, String pillName, String doseQuantity, String doseUnit, int alarmId) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.pillName = pillName;
        this.doseQuantity = doseQuantity;
        this.doseUnit = doseUnit;
        this.alarmId = alarmId;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getDoseQuantity() {
        return doseQuantity;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseQuantity(String doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    private List<Long> ids = new LinkedList<Long>();

    private boolean[] dayOfWeek = new boolean[7];

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public List<Long> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public boolean[] getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(boolean[] dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void addId(long id) {
        ids.add(id);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    private String getAm_pm() {
        return (hour < 12) ? "am" : "pm";
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    /**
     * Overrides the compareTo() method so that alarms can be sorted by time of day from earliest to
     * latest.
     */
    @Override
    public int compareTo(@NonNull MedicineAlarm medicineAlarm) {
        if (hour < medicineAlarm.getHour())
            return -1;
        else if (hour > medicineAlarm.getHour())
            return 1;
        else {
            if (minute < medicineAlarm.getMinute())
                return -1;
            else if (minute > medicineAlarm.getMinute())
                return 1;
            else
                return 0;
        }
    }

    /**
     * A helper method which returns the time of the alarm in string form
     * hour:minutes am/pm
     */
    public String getStringTime() {
        int nonMilitaryHour = hour % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;
        String min = Integer.toString(minute);
        if (minute < 10)
            min = "0" + minute;
        return String.format(Locale.getDefault(), "%d:%s %s", nonMilitaryHour, min, getAm_pm());
    }

    /**
     * A helper method which returns the formatted medicine dose
     * doseQuantity doseUnit
     */
    public String getFormattedDose() {
        return String.format(Locale.getDefault(), "%s %s", doseQuantity, doseUnit);
    }

}


package com.vishwajeeth.medicinetime.data.source;

import java.util.Locale;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class History {

    private int hourTaken;

    private int minuteTaken;

    private String dateString;

    private String pillName;

    private int action;

    private String doseQuantity;

    private String doseUnit;

    private  int alarmId;

    public History() {
    }

    public History(int hourTaken, int minuteTaken, String dateString, String pillName, int action, String doseQuantity, String doseUnit, int alarmId) {
        this.hourTaken = hourTaken;
        this.minuteTaken = minuteTaken;
        this.dateString = dateString;
        this.pillName = pillName;
        this.action = action;
        this.doseQuantity = doseQuantity;
        this.doseUnit = doseUnit;
        this.alarmId = alarmId;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public void setDoseQuantity(String doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public String getDoseQuantity() {
        return doseQuantity;
    }

    public int getHourTaken() {
        return hourTaken;
    }

    public void setHourTaken(int hourTaken) {
        this.hourTaken = hourTaken;
    }

    public int getMinuteTaken() {
        return minuteTaken;
    }

    public void setMinuteTaken(int minuteTaken) {
        this.minuteTaken = minuteTaken;
    }

    public String getAm_pmTaken() {
        return (hourTaken < 12) ? "am" : "pm";
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    /**
     * A helper method which returns the time of the alarm in string form
     * hour:minutes am/pm
     */
    public String getStringTime() {
        int nonMilitaryHour = hourTaken % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;
        String min = Integer.toString(minuteTaken);
        if (minuteTaken < 10)
            min = "0" + minuteTaken;
        return String.format(Locale.getDefault(), "%d:%s %s", nonMilitaryHour, min, getAm_pmTaken());
    }

    public String getFormattedDate() {
        return String.format(Locale.getDefault(), "%s %s", dateString, getStringTime());
    }

    /**
     * A helper method which returns the formatted medicine dose
     * doseQuantity doseUnit
     */
    public String getFormattedDose() {
        return String.format(Locale.getDefault(), "%s %s", doseQuantity, doseUnit);
    }
}


package com.vishwajeeth.medicinetime.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicineDBHelper extends SQLiteOpenHelper {

    /**
     * Database name
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Database version
     */
    private static final String DATABASE_NAME = "MedicineAlarm.db";

    /**
     * Table names
     */
    private static final String PILL_TABLE = "pills";
    private static final String ALARM_TABLE = "alarms";
    private static final String PILL_ALARM_LINKS = "pill_alarm";
    private static final String HISTORIES_TABLE = "histories";

    /**
     * Common column name and location
     */
    public static final String KEY_ROWID = "id";

    /**
     * Pill table columns, used by History Table
     */
    private static final String KEY_PILLNAME = "pillName";

    /**
     * Alarm table columns, Hour & Minute used by History Table
     */
    private static final String KEY_INTENT = "intent";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_DAY_WEEK = "day_of_week";
    private static final String KEY_ALARMS_PILL_NAME = "pillName";
    private static final String KEY_DOSE_QUANTITY = "dose_quantity";
    private static final String KEY_DOSE_UNITS = "dose_units";
    private static final String KEY_ALARM_ID = "alarm_id";


    /**
     * Pill-Alarm link table columns
     */
    private static final String KEY_PILLTABLE_ID = "pill_id";
    private static final String KEY_ALARMTABLE_ID = "alarm_id";

    /**
     * History Table columns, some used above
     */
    private static final String KEY_DATE_STRING = "date";
    private static final String KEY_ACTION = "action";

    /**
     * Pill Table: create statement
     */
    private static final String CREATE_PILL_TABLE =
            "create table " + PILL_TABLE + "("
                    + KEY_ROWID + " integer primary key not null,"
                    + KEY_PILLNAME + " text not null" + ")";

    /**
     * Alarm Table: create statement
     */
    private static final String CREATE_ALARM_TABLE =
            "create table " + ALARM_TABLE + "("
                    + KEY_ROWID + " integer primary key,"
                    + KEY_ALARM_ID + " integer,"
                    + KEY_HOUR + " integer,"
                    + KEY_MINUTE + " integer,"
                    + KEY_ALARMS_PILL_NAME + " text not null,"
                    + KEY_DATE_STRING + " text,"
                    + KEY_DOSE_QUANTITY + " text,"
                    + KEY_DOSE_UNITS + " text,"
                    + KEY_DAY_WEEK + " integer" + ")";

    /**
     * Pill-Alarm link table: create statement
     */
    private static final String CREATE_PILL_ALARM_LINKS_TABLE =
            "create table " + PILL_ALARM_LINKS + "("
                    + KEY_ROWID + " integer primary key not null,"
                    + KEY_PILLTABLE_ID + " integer not null,"
                    + KEY_ALARMTABLE_ID + " integer not null" + ")";

    /**
     * Histories Table: create statement
     */
    private static final String CREATE_HISTORIES_TABLE =
            String.format("CREATE TABLE %s(%s integer primary key, %s text not null, %s text, %s text, %s text, %s integer, %s integer, %s integer , %s integer)", HISTORIES_TABLE, KEY_ROWID, KEY_PILLNAME, KEY_DOSE_QUANTITY, KEY_DOSE_UNITS, KEY_DATE_STRING, KEY_HOUR, KEY_ACTION, KEY_MINUTE, KEY_ALARM_ID);

    /**
     * Constructor
     */
    public MedicineDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    /** Creating tables */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PILL_TABLE);
        db.execSQL(CREATE_ALARM_TABLE);
        db.execSQL(CREATE_PILL_ALARM_LINKS_TABLE);
        db.execSQL(CREATE_HISTORIES_TABLE);
    }

    @Override
    // TODO: change this so that updating doesn't delete old data
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PILL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PILL_ALARM_LINKS);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORIES_TABLE);
        onCreate(db);
    }


// ############################## create methods ###################################### //


    /**
     * createPill takes a pill object and inserts the relevant data into the database
     *
     * @param pill a model pill object
     * @return the long row_id generate by the database upon entry into the database
     */
    public long createPill(Pills pill) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PILLNAME, pill.getPillName());
        return db.insert(PILL_TABLE, null, values);
    }

    /**
     * takes in a model alarm object and inserts a row into the database
     * for each day of the week the alarm is meant to go off.
     *
     * @param alarm   a model alarm object
     * @param pill_id the id associated with the pill the alarm is for
     * @return a array of longs that are the row_ids generated by the database when the rows are inserted
     */
    public long[] createAlarm(MedicineAlarm alarm, long pill_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long[] alarm_ids = new long[7];

        /** Create a separate row in the table for every day of the week for this alarm */
        int arrayPos = 0;
        for (boolean day : alarm.getDayOfWeek()) {
            if (day) {
                ContentValues values = new ContentValues();
                values.put(KEY_HOUR, alarm.getHour());
                values.put(KEY_MINUTE, alarm.getMinute());
                values.put(KEY_DAY_WEEK, arrayPos + 1);
                values.put(KEY_ALARMS_PILL_NAME, alarm.getPillName());
                values.put(KEY_DOSE_QUANTITY, alarm.getDoseQuantity());
                values.put(KEY_DOSE_UNITS, alarm.getDoseUnit());
                values.put(KEY_DATE_STRING, alarm.getDateString());
                values.put(KEY_ALARM_ID, alarm.getAlarmId());

                /** Insert row */
                long alarm_id = db.insert(ALARM_TABLE, null, values);
                alarm_ids[arrayPos] = alarm_id;

                /** Link alarm to a pill */
                createPillAlarmLink(pill_id, alarm_id);
            }
            arrayPos++;
        }
        return alarm_ids;
    }

    /**
     * private function that inserts a row into a table that links pills and alarms
     *
     * @param pill_id  the row_id of the pill that is being added to or edited
     * @param alarm_id the row_id of the alarm that is being added to the pill
     * @return returns the row_id the database creates when a row is created
     */
    private long createPillAlarmLink(long pill_id, long alarm_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PILLTABLE_ID, pill_id);
        values.put(KEY_ALARMTABLE_ID, alarm_id);
        return db.insert(PILL_ALARM_LINKS, null, values);
    }

    /**
     * uses a history model object to store histories in the DB
     *
     * @param history a history model object
     */
    public void createHistory(History history) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PILLNAME, history.getPillName());
        values.put(KEY_DATE_STRING, history.getDateString());
        values.put(KEY_HOUR, history.getHourTaken());
        values.put(KEY_MINUTE, history.getMinuteTaken());
        values.put(KEY_DOSE_QUANTITY, history.getDoseQuantity());
        values.put(KEY_DOSE_UNITS, history.getDoseUnit());
        values.put(KEY_ACTION, history.getAction());
        values.put(KEY_ALARM_ID, history.getAlarmId());

        /** Insert row */
        db.insert(HISTORIES_TABLE, null, values);
    }

// ############################# get methods ####################################### //

    /**
     * allows pillBox to retrieve a row from pill table in Db
     *
     * @param pillName takes in a string of the pill Name
     * @return returns a pill model object
     */
    public Pills getPillByName(String pillName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String dbPill = "select * from "
                + PILL_TABLE + " where "
                + KEY_PILLNAME + " = "
                + "'" + pillName + "'";

        Cursor c = db.rawQuery(dbPill, null);

        Pills pill = new Pills();

        if (c.moveToFirst() && c.getCount() >= 1) {
            pill.setPillName(c.getString(c.getColumnIndex(KEY_PILLNAME)));
            pill.setPillId(c.getLong(c.getColumnIndex(KEY_ROWID)));
            c.close();
        }
        return pill;
    }

    /**
     * allows the pillBox to retrieve all the pill rows from database
     *
     * @return a list of pill model objects
     */
    public List<Pills> getAllPills() {
        List<Pills> pills = new ArrayList<>();
        String dbPills = "SELECT * FROM " + PILL_TABLE;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(dbPills, null);

        /** Loops through all rows, adds to list */
        if (c.moveToFirst()) {
            do {
                Pills p = new Pills();
                p.setPillName(c.getString(c.getColumnIndex(KEY_PILLNAME)));
                p.setPillId(c.getLong(c.getColumnIndex(KEY_ROWID)));

                pills.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return pills;
    }


    /**
     * Allows pillBox to retrieve all Alarms linked to a Pill
     * uses combineAlarms helper method
     *
     * @param pillName string
     * @return list of alarm objects
     * @throws URISyntaxException honestly do not know why, something about alarm.getDayOfWeek()
     */
    public List<MedicineAlarm> getAllAlarmsByPill(String pillName) throws URISyntaxException {
        List<MedicineAlarm> alarmsByPill = new ArrayList<>();

        /** HINT: When reading string: '.' are not periods ex) pill.rowIdNumber */
        String selectQuery = "SELECT * FROM " +
                ALARM_TABLE + " alarm, " +
                PILL_TABLE + " pill, " +
                PILL_ALARM_LINKS + " pillAlarm WHERE " +
                "pill." + KEY_PILLNAME + " = '" + pillName + "'" +
                " AND pill." + KEY_ROWID + " = " +
                "pillAlarm." + KEY_PILLTABLE_ID +
                " AND alarm." + KEY_ROWID + " = " +
                "pillAlarm." + KEY_ALARMTABLE_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                MedicineAlarm al = new MedicineAlarm();
                al.setId(c.getInt(c.getColumnIndex(KEY_ROWID)));
                al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
                al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                al.setPillName(c.getString(c.getColumnIndex(KEY_ALARMS_PILL_NAME)));
                al.setDoseQuantity(c.getString(c.getColumnIndex(KEY_DOSE_QUANTITY)));
                al.setDoseUnit(c.getString(c.getColumnIndex(KEY_DOSE_UNITS)));
                al.setDateString(c.getString(c.getColumnIndex(KEY_DATE_STRING)));
                al.setAlarmId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));

                alarmsByPill.add(al);
            } while (c.moveToNext());
        }

        c.close();


        return combineAlarms(alarmsByPill);
    }

    public List<MedicineAlarm> getAllAlarms(String pillName) throws URISyntaxException {
        List<MedicineAlarm> alarmsByPill = new ArrayList<>();

        /** HINT: When reading string: '.' are not periods ex) pill.rowIdNumber */
        String selectQuery = "SELECT * FROM " +
                ALARM_TABLE + " alarm, " +
                PILL_TABLE + " pill, " +
                PILL_ALARM_LINKS + " pillAlarm WHERE " +
                "pill." + KEY_PILLNAME + " = '" + pillName + "'" +
                " AND pill." + KEY_ROWID + " = " +
                "pillAlarm." + KEY_PILLTABLE_ID +
                " AND alarm." + KEY_ROWID + " = " +
                "pillAlarm." + KEY_ALARMTABLE_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                MedicineAlarm al = new MedicineAlarm();
                al.setId(c.getInt(c.getColumnIndex(KEY_ROWID)));
                al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
                al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                al.setPillName(c.getString(c.getColumnIndex(KEY_ALARMS_PILL_NAME)));
                al.setDoseQuantity(c.getString(c.getColumnIndex(KEY_DOSE_QUANTITY)));
                al.setDoseUnit(c.getString(c.getColumnIndex(KEY_DOSE_UNITS)));
                al.setDateString(c.getString(c.getColumnIndex(KEY_DATE_STRING)));
                al.setAlarmId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));

                alarmsByPill.add(al);
            } while (c.moveToNext());
        }

        c.close();


        return alarmsByPill;
    }

    /**
     * returns all individual alarms that occur on a certain day of the week,
     * alarms returned do not know of their counterparts that occur on different days
     *
     * @param day an integer that represents the day of week
     * @return a list of Alarms (not combined into full-model-alarms)
     */
    public List<MedicineAlarm> getAlarmsByDay(int day) {
        List<MedicineAlarm> daysAlarms = new ArrayList<>();

        String selectQuery = "SELECT * FROM " +
                ALARM_TABLE + " alarm WHERE " +
                "alarm." + KEY_DAY_WEEK +
                " = '" + day + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                MedicineAlarm al = new MedicineAlarm();
                al.setId(c.getInt(c.getColumnIndex(KEY_ROWID)));
                al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
                al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                al.setPillName(c.getString(c.getColumnIndex(KEY_ALARMS_PILL_NAME)));
                al.setDoseQuantity(c.getString(c.getColumnIndex(KEY_DOSE_QUANTITY)));
                al.setDoseUnit(c.getString(c.getColumnIndex(KEY_DOSE_UNITS)));
                al.setDateString(c.getString(c.getColumnIndex(KEY_DATE_STRING)));
                al.setAlarmId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));
                daysAlarms.add(al);
            } while (c.moveToNext());
        }
        c.close();

        return daysAlarms;
    }

    /**
     * @param alarm_id
     * @return
     * @throws URISyntaxException
     */
    public MedicineAlarm getAlarmById(long alarm_id) throws URISyntaxException {

        String dbAlarm = "SELECT * FROM " +
                ALARM_TABLE + " WHERE " +
                KEY_ROWID + " = " + alarm_id;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(dbAlarm, null);

        if (c != null)
            c.moveToFirst();

        MedicineAlarm al = new MedicineAlarm();
        al.setId(c.getInt(c.getColumnIndex(KEY_ROWID)));
        al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
        al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
        al.setPillName(c.getString(c.getColumnIndex(KEY_ALARMS_PILL_NAME)));
        al.setDoseQuantity(c.getString(c.getColumnIndex(KEY_DOSE_QUANTITY)));
        al.setDoseUnit(c.getString(c.getColumnIndex(KEY_DOSE_UNITS)));
        al.setDateString(c.getString(c.getColumnIndex(KEY_DATE_STRING)));
        al.setAlarmId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));
        c.close();

        return al;
    }

    /**
     * Private helper function that combines rows in the databse back into a
     * full model-alarm with a dayOfWeek array.
     *
     * @param dbAlarms a list of dbAlarms (not-full-alarms w/out day of week info)
     * @return a list of model-alarms
     * @throws URISyntaxException
     */
    private List<MedicineAlarm> combineAlarms(List<MedicineAlarm> dbAlarms) throws URISyntaxException {
        List<String> timesOfDay = new ArrayList<>();
        List<MedicineAlarm> combinedAlarms = new ArrayList<>();

        for (MedicineAlarm al : dbAlarms) {
            if (timesOfDay.contains(al.getStringTime())) {
                /** Add this db row to alarm object */
                for (MedicineAlarm ala : combinedAlarms) {
                    if (ala.getStringTime().equals(al.getStringTime())) {
                        int day = getDayOfWeek(al.getId());
                        boolean[] days = ala.getDayOfWeek();
                        days[day - 1] = true;
                        ala.setDayOfWeek(days);
                        ala.addId(al.getId());
                    }
                }
            } else {
                /** Create new Alarm object with day of week array */
                MedicineAlarm newAlarm = new MedicineAlarm();
                boolean[] days = new boolean[7];

                newAlarm.setPillName(al.getPillName());
                newAlarm.setMinute(al.getMinute());
                newAlarm.setHour(al.getHour());
                newAlarm.addId(al.getId());
                newAlarm.setDateString(al.getDateString());
                newAlarm.setAlarmId(al.getAlarmId());
                int day = getDayOfWeek(al.getId());
                days[day - 1] = true;
                newAlarm.setDayOfWeek(days);

                timesOfDay.add(al.getStringTime());
                combinedAlarms.add(newAlarm);
            }
        }

        Collections.sort(combinedAlarms);
        return combinedAlarms;
    }

    /**
     * Get a single pillapp.Model-Alarm
     * Used as a helper function
     */
    public int getDayOfWeek(long alarm_id) throws URISyntaxException {
        SQLiteDatabase db = this.getReadableDatabase();

        String dbAlarm = "SELECT * FROM " +
                ALARM_TABLE + " WHERE " +
                KEY_ROWID + " = " + alarm_id;

        Cursor c = db.rawQuery(dbAlarm, null);

        if (c != null)
            c.moveToFirst();

        int dayOfWeek = c.getInt(c.getColumnIndex(KEY_DAY_WEEK));
        c.close();

        return dayOfWeek;
    }

    /**
     * allows pillBox to retrieve from History table
     *
     * @return a list of all history objects
     */
    public List<History> getHistory() {
        List<History> allHistory = new ArrayList<>();
        String dbHist = "SELECT * FROM " + HISTORIES_TABLE;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(dbHist, null);

        if (c.moveToFirst()) {
            do {
                History h = new History();
                h.setPillName(c.getString(c.getColumnIndex(KEY_PILLNAME)));
                h.setDateString(c.getString(c.getColumnIndex(KEY_DATE_STRING)));
                h.setHourTaken(c.getInt(c.getColumnIndex(KEY_HOUR)));
                h.setMinuteTaken(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                h.setDoseQuantity(c.getString(c.getColumnIndex(KEY_DOSE_QUANTITY)));
                h.setDoseUnit(c.getString(c.getColumnIndex(KEY_DOSE_UNITS)));
                h.setAction(c.getInt(c.getColumnIndex(KEY_ACTION)));
                h.setAlarmId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));

                allHistory.add(h);
            } while (c.moveToNext());
        }
        c.close();
        return allHistory;
    }


// ############################### delete methods##################################### //


    private void deletePillAlarmLinks(long alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PILL_ALARM_LINKS, KEY_ALARMTABLE_ID
                + " = ?", new String[]{String.valueOf(alarmId)});
    }

    public void deleteAlarm(long alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();

        /** First delete any link in PillAlarmLink Table */
        deletePillAlarmLinks(alarmId);

        /* Then delete alarm */
        db.delete(ALARM_TABLE, KEY_ROWID
                + " = ?", new String[]{String.valueOf(alarmId)});
    }

    public void deletePill(String pillName) throws URISyntaxException {
        SQLiteDatabase db = this.getWritableDatabase();
        List<MedicineAlarm> pillsAlarms;

        /** First get all Alarms and delete them and their Pill-links */
        pillsAlarms = getAllAlarmsByPill(pillName);
        for (MedicineAlarm alarm : pillsAlarms) {
            long id = alarm.getId();
            deleteAlarm(id);
        }

        /** Then delete Pill */
        db.delete(PILL_TABLE, KEY_PILLNAME
                + " = ?", new String[]{pillName});
    }
}


package com.vishwajeeth.medicinetime.data.source.local;

import android.content.Context;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class MedicinesLocalDataSource implements MedicineDataSource {

    private static MedicinesLocalDataSource mInstance;

    private MedicineDBHelper mDbHelper;


    private MedicinesLocalDataSource(Context context) {
        mDbHelper = new MedicineDBHelper(context);
    }

    public static MedicinesLocalDataSource getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MedicinesLocalDataSource(context);
        }
        return mInstance;
    }


    @Override
    public void getMedicineHistory(LoadHistoryCallbacks loadHistoryCallbacks) {
        List<History> historyList = mDbHelper.getHistory();
        loadHistoryCallbacks.onHistoryLoaded(historyList);
    }

    @Override
    public void getMedicineAlarmById(long id, GetTaskCallback callback) {

        try {
            MedicineAlarm medicineAlarm = getAlarmById(id);
            if (medicineAlarm != null) {
                callback.onTaskLoaded(medicineAlarm);
            } else {
                callback.onDataNotAvailable();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            callback.onDataNotAvailable();
        }

    }

    @Override
    public void saveMedicine(MedicineAlarm medicineAlarm, Pills pill) {
        mDbHelper.createAlarm(medicineAlarm, pill.getPillId());
    }

    @Override
    public void getMedicineListByDay(int day, LoadMedicineCallbacks callbacks) {
        List<MedicineAlarm> medicineAlarmList = mDbHelper.getAlarmsByDay(day);
        callbacks.onMedicineLoaded(medicineAlarmList);
    }

    @Override
    public boolean medicineExits(String pillName) {
        for (Pills pill : getPills()) {
            if (pill.getPillName().equals(pillName))
                return true;
        }
        return false;
    }

    @Override
    public List<Long> tempIds() {
        return null;
    }

    @Override
    public void deleteAlarm(long alarmId) {
        deleteAlarmById(alarmId);
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        try {
            return getMedicineByPill(pillName);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<MedicineAlarm> getAllAlarms(String pillName) {
        try {
            return getAllAlarmsByName(pillName);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Pills getPillsByName(String pillName) {
        return getPillByName(pillName);
    }

    @Override
    public long savePills(Pills pills) {
        return savePill(pills);
    }

    @Override
    public void saveToHistory(History history) {
        mDbHelper.createHistory(history);
    }

    private List<Pills> getPills() {
        return mDbHelper.getAllPills();
    }

    private long savePill(Pills pill) {
        long pillId = mDbHelper.createPill(pill);
        pill.setPillId(pillId);
        return pillId;
    }

    private Pills getPillByName(String pillName) {
        return mDbHelper.getPillByName(pillName);
    }

    private List<MedicineAlarm> getMedicineByPill(String pillName) throws URISyntaxException {
        return mDbHelper.getAllAlarmsByPill(pillName);
    }

    private List<MedicineAlarm> getAllAlarmsByName(String pillName) throws URISyntaxException {
        return mDbHelper.getAllAlarms(pillName);
    }

    public void deletePill(String pillName) throws URISyntaxException {
        mDbHelper.deletePill(pillName);
    }

    private void deleteAlarmById(long alarmId) {
        mDbHelper.deleteAlarm(alarmId);
    }

    public void addToHistory(History h) {
        mDbHelper.createHistory(h);
    }

    public List<History> getHistory() {
        return mDbHelper.getHistory();
    }

    private MedicineAlarm getAlarmById(long alarm_id) throws URISyntaxException {
        return mDbHelper.getAlarmById(alarm_id);
    }

    public int getDayOfWeek(long alarm_id) throws URISyntaxException {
        return mDbHelper.getDayOfWeek(alarm_id);
    }


}


package com.vishwajeeth.medicinetime.views;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.vishwajeeth.medicinetime.utils.FontUtil;


public class RobotoBoldTextView extends AppCompatTextView {

    public RobotoBoldTextView(Context context) {
        super(context);
        applyCustomFont();
    }

    public RobotoBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont();
    }

    public RobotoBoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont();
    }

    private void applyCustomFont() {
        Typeface customFont = FontUtil.getTypeface(FontUtil.ROBOTO_BOLD);
        setTypeface(customFont);
    }
}

package com.vishwajeeth.medicinetime.views;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.vishwajeeth.medicinetime.R;

/**
 * Created by vishwajeeth on 13/07/17.
 */

public class DayViewCheckBox extends AppCompatCheckBox {

    private final Context context;

    public DayViewCheckBox(Context context) {
        super(context);
        this.context = context;
    }

    public DayViewCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public void setChecked(boolean t){
        if(t) {
            this.setTextColor(Color.WHITE);
        } else {
            this.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }
        super.setChecked(t);
    }
}


package com.vishwajeeth.medicinetime.views;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.vishwajeeth.medicinetime.utils.FontUtil;


public class RobotoRegularTextView extends AppCompatTextView {

    public RobotoRegularTextView(Context context) {
        super(context);
        applyCustomFont();
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont();
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont();
    }

    private void applyCustomFont() {
        Typeface customFont = FontUtil.getTypeface(FontUtil.ROBOTO_REGULAR);
        setTypeface(customFont);
    }
}

package com.vishwajeeth.medicinetime.views;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.vishwajeeth.medicinetime.utils.FontUtil;


public class RobotoLightTextView extends AppCompatTextView {

    public RobotoLightTextView(Context context) {
        super(context);
        applyCustomFont();
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont();
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont();
    }

    private void applyCustomFont() {
        Typeface customFont = FontUtil.getTypeface(FontUtil.ROBOTO_LIGHT);
        setTypeface(customFont);
    }
}

