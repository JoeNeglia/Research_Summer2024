package com.android.countdowntimer;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
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
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.android.countdowntimer", appContext.getPackageName());
    }
}


package com.android.countdowntimer;

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

package com.android.countdowntimer;

import android.app.Application;

import io.paperdb.Paper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }
}


package com.android.countdowntimer.home;

public class StateType {

    public static final int GONE = 0;
    public static final int ONGOING = 1;
    public static final int WAITING = 2;
    public static final int COMPLETED = 3;
}


package com.android.countdowntimer.home;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.completedevents.CompletedEventsActivity;
import com.android.countdowntimer.detail.EventDetailActivity;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.utils.NotificationUtils;
import com.android.countdowntimer.utils.RemindType;
import com.android.countdowntimer.utils.ReminderUtils;
import com.android.countdowntimer.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    public static final String EVENT_NOTIFICATION_ID = "EVENT_NOTIFICATION";
    private EventsAdapter mEventsAdapter;
    private static final String DIALOG_DATE = "DATE";
    private List<Event> events;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(EVENT_NOTIFICATION_ID, getString(R.string.event_notification), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Add Event");
                // Create TextView
                final EditText input = new EditText (MainActivity.this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText() == null || input.getText().equals("")) {
                            Toast.makeText(getApplicationContext(),"Please Enter valid Name for the Event",Toast.LENGTH_LONG).show();
                        }
                        name = input.getText().toString();
                        long date = DateTimeUtils.getCurrentTimeWithoutSec();
                        FragmentManager fm = getSupportFragmentManager();
                        DateDialogFragment dialogFragment = DateDialogFragment.newInstance(date, false);
                        dialogFragment.show(fm, DIALOG_DATE);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alert.show();
            }
        });
        setupEventList();
    }

    private void setupEventList() {
        RecyclerView recyclerView = findViewById(R.id.list_events);
        mEventsAdapter = new EventsAdapter(this);
        recyclerView.setAdapter(mEventsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new EventTouchHelperCallback(mEventsAdapter));
        touchHelper.attachToRecyclerView(recyclerView);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animationController);

        events = Paper.book().read("events");
        if(events == null) {
            events = new ArrayList<>();
            Paper.book().write("events", events);
        }
        filterCompletedEvents();
        refreshList(events);

        mEventsAdapter.setEventItemActionListener(new EventItemActionListener() {
            @Override
            public void onItemSwiped(String eventId) {
                deleteEvent(eventId);
                refreshList(events);
            }

            @Override
            public void onItemClicked(String eventId) {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            }
        });
    }

    private void filterCompletedEvents() {
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getEndDate() < System.currentTimeMillis()) {
               deleteEvent(event.getId());
            }
        }
    }

    private void deleteEvent(String id) {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getId().equals(id)) {
                addToCompletedEvents(event);
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("events", events);
        }
    }

    private void addToCompletedEvents(Event event) {
        List<Event> completedEvents = Paper.book().read("completed_events");
        if(completedEvents == null) {
            completedEvents = new ArrayList<>();
        }
        completedEvents.add(event);
        Paper.book().write("completed_events", completedEvents);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        long endDate = data.getLongExtra(DateDialogFragment.EXTRA_DATE, 0);
        if (endDate - System.currentTimeMillis() < 0) {
            Toast.makeText(getApplicationContext(),"End Date cannot be in Past",Toast.LENGTH_LONG).show();
            return;
        }
        String eventId = String.valueOf(System.currentTimeMillis());
        events.add(new Event(name, "", 0, endDate, StateType.ONGOING, false, 0, eventId, 0, "", System.currentTimeMillis()));
        NotificationUtils.buildNormalReminder(getApplication(), endDate, name, RemindType.SINGLE_DUE_DATE, ReminderUtils.getSingleRemindInterval(RemindType.SINGLE_DUE_DATE), eventId);
        Paper.book().write("events", events);
        refreshList(events);
    }

    private void refreshList(List<Event> events) {
        mEventsAdapter.setEvents(events);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_show_completed_events:
                showCompletedEvents();
                return true;
            case R.id.action_export_events:
                exportEventsToJson();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exportEventsToJson() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        Gson gson = new Gson();
                        String userJson = gson.toJson(events);
                        // init
                        Storage storage = new Storage(getApplicationContext());

                        // get external storage
                        String path = storage.getExternalStorageDirectory();
                        storage.createFile(path+"/CountdownTimer.json", userJson);
                        Toast.makeText(getApplicationContext(),"Events exported to Json in file:" + path + "/CountdownTimer.json",Toast.LENGTH_LONG).show();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
    }

    private void showCompletedEvents() {
        Intent intent = new Intent(MainActivity.this, CompletedEventsActivity.class);
        startActivity(intent);
    }
}


package com.android.countdowntimer.home;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import cn.iwgang.countdownview.CountdownView;
import cn.iwgang.countdownview.DynamicConfig;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> implements EventTouchHelperListener {

    private EventItemActionListener mEventItemActionListener;
    private final LayoutInflater mInflater;
    private List<Event> mEvents;
    private Context mContext;

    @Override
    public void onItemSwipeToStart(int position) {
        mEventItemActionListener.onItemSwiped(mEvents.get(position).getId());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Event mEvent;
        private TextView mTitle;
        private View mTimerContainer;
        private TimerView mTimer;
        private TextView mTimerState;
        private MaterialCardView mCard;
        private TextView mDueDate;
        private TimelineView mMarker;

        private ViewHolder(View itemView, int type) {
            super(itemView);

            mCard = itemView.findViewById(R.id.event_card);
            mTitle = itemView.findViewById(R.id.event_title);
            mTimerContainer = itemView.findViewById(R.id.event_timer_container);
            mTimer = itemView.findViewById(R.id.event_timer);
            mTimerState = itemView.findViewById(R.id.event_timer_state);
            mDueDate = itemView.findViewById(R.id.event_due_date);
            mMarker = itemView.findViewById(R.id.event_marker);
            mMarker.initLine(type);
        }

        private void bind(Event event) {
            mEvent = event;
            mDueDate.setText(DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.TIME) + "\n" +
                    DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.DATE));
            refreshNormal(mEvent);
            mTitle.setText(event.getTitle());
            if(event.getEndDate() < System.currentTimeMillis()) {
                mTimer.stop();
                mEventItemActionListener.onItemSwiped(event.getId());
            }
        }

        private void refreshNormal(final Event event) {
            activeEvent();
            mTimer.start(mEvent.getEndDate() - System.currentTimeMillis());
            toTealTheme();
            mTimerState.setText(R.string.timer_state_to_reach);
            mTimer.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    mEventItemActionListener.onItemSwiped(event.getId());
                }
            });
        }

        private void activeEvent() {
            mTitle.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            mDueDate.setPadding(0, 0, 0, 0);
            mTimerContainer.setVisibility(View.VISIBLE);
        }

        private void toTealTheme() {
            mTitle.setTextColor(mContext.getResources().getColor(R.color.white));
            mDueDate.setTextColor(mContext.getResources().getColor(R.color.yellow_300));
            mCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.teal_600));
            mTimerState.setTextColor(mContext.getResources().getColor(R.color.white_secondary));
            DynamicConfig.Builder dynamicConfigBuilder = new DynamicConfig.Builder();
            dynamicConfigBuilder
                    .setTimeTextColor(mContext.getResources().getColor(R.color.white))
                    .setSuffixTextColor(mContext.getResources().getColor(R.color.white_secondary))
                    .setShowSecond(true)
                    .setShowMinute(true);
            mTimer.dynamicShow(dynamicConfigBuilder.build());
        }
    }

    EventsAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_event, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mEvents != null) {
            final Event current = mEvents.get(position);
            holder.bind(current);

            if (mEventItemActionListener != null) {
                holder.mCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mEventItemActionListener.onItemClicked(current.getId());
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        if (mEvents != null) {
            return mEvents.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.mTimer.stop();
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
        notifyDataSetChanged();
    }

    public void setEventItemActionListener(EventItemActionListener listener) {
        mEventItemActionListener = listener;
    }
}


package com.android.countdowntimer.home;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public class Event {

    @NonNull
    private String mId;

    @Nullable
    private String mTitle;

    @Nullable
    private String mNote;

    private long mStartDate;

    private long mEndDate;

    private int mState;

    private boolean mDurableEvent;

    private int mPriority;

    private int mReminder;

    private String mCategory;

    private long mCreationDate;

    //Use this constructor to create a new active Event.
    public Event(@Nullable String title, @Nullable String note, long startDate, long endDate, int state, boolean durableEvent, int priority, int reminder, String category) {
        this(title, note, startDate, endDate, state, durableEvent, priority, UUID.randomUUID().toString(), reminder, category, System.currentTimeMillis());
    }

    public Event(@Nullable String title, @Nullable String note, long startDate, long endDate, int state, boolean durableEvent, int priority, String id, int reminder, String category, long creationDate) {
        mId = id;
        mCreationDate = creationDate;
        mTitle = title;
        mNote = note;
        mStartDate = startDate;
        mEndDate = endDate;
        mState = state;
        mDurableEvent = durableEvent;
        mPriority = priority;
        mReminder = reminder;
        mCategory = category;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    public long getCreationDate() {
        return mCreationDate;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getNote() {
        return mNote;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public int getState() {
        return mState;
    }

    public boolean isDurableEvent() {
        return mDurableEvent;
    }

    public int getPriority() {
        return mPriority;
    }

    public int getReminder() {
        return mReminder;
    }

    public String getCategory() {
        return mCategory;
    }
}


package com.android.countdowntimer.home;

public interface TimerUpdateListener {

    void onUpdateView(long duration);
}


package com.android.countdowntimer.home;

import android.content.Context;
import android.util.AttributeSet;

import cn.iwgang.countdownview.CountdownView;

public class TimerView extends CountdownView {
    private TimerUpdateListener mTimerUpdateListener;

    public TimerView(Context context) {
        super(context);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void updateShow(long ms) {
        super.updateShow(ms);

        if (mTimerUpdateListener != null) {
            mTimerUpdateListener.onUpdateView(ms);
        }
    }

    public void setTimerUpdateListener(TimerUpdateListener timerUpdateListener) {
        mTimerUpdateListener = timerUpdateListener;
    }
}


package com.android.countdowntimer.home;

public interface EventItemActionListener {
    void onItemSwiped(String eventId);
    void onItemClicked(String eventId);
}


package com.android.countdowntimer.home;

import android.graphics.Canvas;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class EventTouchHelperCallback extends ItemTouchHelper.Callback {

    private EventTouchHelperListener mEventTouchHelperListener;

    public EventTouchHelperCallback(EventTouchHelperListener listener) {
        mEventTouchHelperListener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int swipeFlags = ItemTouchHelper.START;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//        mEventTouchHelperListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mEventTouchHelperListener.onItemSwipeToStart(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setScrollX(0);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (Math.abs(dX) <= getSlideLimitation(viewHolder)) {
                viewHolder.itemView.scrollTo(-(int) dX, 0);
            }

        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    public int getSlideLimitation(RecyclerView.ViewHolder viewHolder) {
        ViewGroup viewGroup = (ViewGroup) viewHolder.itemView;
        return viewGroup.getChildAt(2).getLayoutParams().width;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.3f;
    }
}


package com.android.countdowntimer.home;

public interface EventTouchHelperListener {

    void onItemSwipeToStart(int position);

}


package com.android.countdowntimer.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.R;

import java.util.Calendar;

public class DateDialogFragment extends DialogFragment {

    public static final String EXTRA_DATE = "EXTRA_DATE";

    private static final String ARG_DATE = "DATE";

    private static final String ARG_ONLY_TIME_PICKER = "ONLY_TIME_PICKER";

    private Calendar mCalendar;

    private long extraDate;

    private boolean isDatePicker = true;

    public static DateDialogFragment newInstance(long date, boolean onlyTimePicker) {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, date);
        args.putBoolean(ARG_ONLY_TIME_PICKER, onlyTimePicker);
        DateDialogFragment fragment = new DateDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        long argDate = getArguments().getLong(ARG_DATE);
        final boolean argOnlyTimePicker = getArguments().getBoolean(ARG_ONLY_TIME_PICKER);

        if (argDate == 0) {
            mCalendar = Calendar.getInstance();
        } else {
            mCalendar = DateTimeUtils.longToCal(argDate);
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        final DatePicker datePicker = view.findViewById(R.id.dialog_date);
        final TimePicker timePicker = view.findViewById(R.id.dialog_time);

        datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(mCalendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(mCalendar.get(Calendar.MINUTE));
        } else {
            timePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));
        }

        if (argOnlyTimePicker) {
            timePicker.setVisibility(View.VISIBLE);
            datePicker.setVisibility(View.GONE);
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.DatePickerDialog)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.date, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isDatePicker && !argOnlyTimePicker) {
                            mCalendar.set(Calendar.YEAR, datePicker.getYear());
                            mCalendar.set(Calendar.MONTH, datePicker.getMonth());
                            mCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                            datePicker.setVisibility(View.GONE);
                            timePicker.setVisibility(View.VISIBLE);
                            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                            isDatePicker = false;
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                                mCalendar.set(Calendar.MINUTE, timePicker.getMinute());
                            } else {
                                mCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                                mCalendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                            }
                            extraDate = DateTimeUtils.calToLong(mCalendar);
                            sendResult(Activity.RESULT_OK, extraDate);
                            dismiss();
                        }

                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        timePicker.setVisibility(View.GONE);
                        datePicker.setVisibility(View.VISIBLE);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                        isDatePicker = true;
                    }
                });
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }

    private void sendResult(int resultCode, long date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        ((MainActivity)getActivity()).onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}


package com.android.countdowntimer.completedevents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.android.countdowntimer.R;
import com.android.countdowntimer.detail.EventDetailActivity;
import com.android.countdowntimer.home.Event;
import com.android.countdowntimer.home.EventItemActionListener;
import com.android.countdowntimer.home.EventTouchHelperCallback;
import com.android.countdowntimer.home.EventsAdapter;
import com.android.countdowntimer.home.MainActivity;
import com.android.countdowntimer.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.paperdb.Paper;

public class CompletedEventsActivity extends AppCompatActivity {

    private List<Event> events;
    private CompletedEventsAdapter mEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_events);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);
        setupEventList();
    }

    private void setupEventList() {
        RecyclerView recyclerView = findViewById(R.id.list_events);
        mEventsAdapter = new CompletedEventsAdapter(this);
        recyclerView.setAdapter(mEventsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new EventTouchHelperCallback(mEventsAdapter));
        touchHelper.attachToRecyclerView(recyclerView);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animationController);

        events = Paper.book().read("completed_events");
        if(events == null) {
            events = new ArrayList<>();
            Paper.book().write("completed_events", events);
        }
        refreshList(events);

        mEventsAdapter.setEventItemActionListener(new EventItemActionListener() {
            @Override
            public void onItemSwiped(String eventId) {
                deleteEvent(eventId);
                refreshList(events);
            }

            @Override
            public void onItemClicked(String eventId) {
            }
        });
    }

    private void deleteEvent(String id) {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getId().equals(id)) {
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("completed_events", events);
        }
    }

    private void refreshList(List<Event> events) {
        mEventsAdapter.setEvents(events);
    }
}


package com.android.countdowntimer.completedevents;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.home.Event;
import com.android.countdowntimer.home.EventItemActionListener;
import com.android.countdowntimer.home.EventTouchHelperListener;
import com.android.countdowntimer.home.TimerView;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import cn.iwgang.countdownview.CountdownView;
import cn.iwgang.countdownview.DynamicConfig;

public class CompletedEventsAdapter extends RecyclerView.Adapter<CompletedEventsAdapter.ViewHolder> implements EventTouchHelperListener {

    private EventItemActionListener mEventItemActionListener;
    private final LayoutInflater mInflater;
    private List<Event> mEvents;
    private Context mContext;

    @Override
    public void onItemSwipeToStart(int position) {
        mEventItemActionListener.onItemSwiped(mEvents.get(position).getId());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Event mEvent;
        private TextView mTitle;
        private View mTimerContainer;
        private TimerView mTimer;
        private MaterialCardView mCard;
        private TextView mDueDate;
        private TimelineView mMarker;

        private ViewHolder(View itemView, int type) {
            super(itemView);

            mCard = itemView.findViewById(R.id.event_card);
            mTitle = itemView.findViewById(R.id.event_title);
            mTimerContainer = itemView.findViewById(R.id.event_timer_container);
            mTimer = itemView.findViewById(R.id.event_timer);
            mDueDate = itemView.findViewById(R.id.event_due_date);
            mMarker = itemView.findViewById(R.id.event_marker);
            mMarker.initLine(type);
        }

        private void bind(Event event) {
            mEvent = event;
            mDueDate.setText(DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.TIME) + "\n" +
                    DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.DATE));
            mTitle.setText(event.getTitle());
            mTimer.setVisibility(View.GONE);
        }
    }

    CompletedEventsAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_event, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mEvents != null) {
            final Event current = mEvents.get(position);
            holder.bind(current);

            if (mEventItemActionListener != null) {
                holder.mCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mEventItemActionListener.onItemClicked(current.getId());
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        if (mEvents != null) {
            return mEvents.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.mTimer.stop();
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
        notifyDataSetChanged();
    }

    public void setEventItemActionListener(EventItemActionListener listener) {
        mEventItemActionListener = listener;
    }
}


package com.android.countdowntimer.utils;

import com.android.countdowntimer.home.StateType;
import com.android.countdowntimer.home.Event;

import java.util.ArrayList;
import java.util.List;

public class FilterUtils {

    public static List<Event> filterTodayTasks(List<Event> events) {
        List<Event> mEvent = new ArrayList<>();
        for (Event event : events) {
            if (event.getEndDate() >= DateTimeUtils.getTodayStart() && event.getEndDate() <= DateTimeUtils.getTodayEnd()) {
                mEvent.add(event);
            }
        }
        return mEvent;
    }

    public static List<Event> filterNext7DaysTasks(List<Event> events) {
        List<Event> mEvent = new ArrayList<>();
        for (Event event : events) {
            if (event.getEndDate() >= DateTimeUtils.getTodayStart() && event.getEndDate() <= DateTimeUtils.getNext7DaysEnd()) {
                mEvent.add(event);
            }
        }
        return mEvent;
    }

    public static List<Event> filterCompletedEvents(List<Event> events) {
        List<Event> mEvent = new ArrayList<>();
        for (Event event : events) {
            if (event.getState() == StateType.COMPLETED) {
                mEvent.add(event);
            }
        }
        return mEvent;
    }

    public static List<Event> filterCategoryEvents(List<Event> events, String category) {
        List<Event> mEvent = new ArrayList<>();
        for (Event event : events) {
            if (event.getCategory().equals(category)) {
                mEvent.add(event);
            }
        }
        return mEvent;
    }

    public static List<Event> filterUncompletedEvents(List<Event> events) {
        List<Event> mEvent = new ArrayList<>();
        for (Event event : events) {
            if (event.getState() != StateType.COMPLETED) {
                mEvent.add(event);
            }
        }
        return mEvent;
    }

    public static List<Event> cloneEvents(List<Event> events) {
        return new ArrayList<>(events);
    }

}


package com.android.countdowntimer.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor that runs a task on a new background thread.
 */
public class DiskIOThreadExecutor implements Executor {

    private final Executor mDiskIO;

    public DiskIOThreadExecutor() {
        mDiskIO = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        mDiskIO.execute(command);
    }
}


package com.android.countdowntimer.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

import com.android.countdowntimer.R;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static Set<String> getEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet("EVENTS", new HashSet<String>());
    }

    public static void saveEvents(Context context, Set<String> events) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet("EVENTS", events)
                .apply();
    }

    public static void setupSystemUI(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.teal_200));
        } else {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.white, null));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}



/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.countdowntimer.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private static final int THREAD_COUNT = 3;

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    @VisibleForTesting
    AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public AppExecutors() {
        this(new DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT),
                new MainThreadExecutor());
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}


package com.android.countdowntimer.utils;

public class ReminderUtils {

    public static int getSingleRemindInterval(int data) {
        if (data < 10) {
            return RemindType.NONE_REMIND;
        } else {
            return data / 10;
        }
    }

    public static int getRemindType(int data) {
        return data % 10;
    }

    public static int buildReminder(int remindType, int singleRemindInterval) {
        if (remindType == RemindType.NONE_REMIND) {
            return RemindType.NONE_REMIND;
        } else {
            return singleRemindInterval * 10 + remindType;
        }

    }

}


package com.android.countdowntimer.utils;

import android.content.Context;

import com.android.countdowntimer.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;

    public static final int DAWN = 0;
    public static final int MORNING = 1;
//    public static final int NOON = 12;
    public static final int AFTERNOON = 2;
    public static final int EVENING = 3;

    public static final int DATE = 0;
    public static final int TIME = 1;
    public static final int MEDIUM = 2;

    public static String dateToString(Date date, int format) {
        switch (format) {
            case DATE:
                return new SimpleDateFormat("d-MMM").format(date);
            case TIME:
                return SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(date);
            case MEDIUM:
                return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
            default:
                return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
        }
    }

    public static Calendar longToCal(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }

    public static String longToString(long date, int format) {
        if (date == 0) {
            return "";
        }

        Date date1 = new Date(date);
        return dateToString(date1, format);
    }

    public static long calToLong(Calendar date) {
        return date.getTimeInMillis();
    }

    public static float countPercent(long interval, long duration) {
        return ((float) (interval - duration) / interval) * 100;
    }

    public static long getCurrentTimeWithoutSec() {
        return (System.currentTimeMillis() / 10000) * 10000;
    }

    public static int getTimePeriod() {
        long now = System.currentTimeMillis();
        long todayStart = getTodayStart();

        if (todayStart + 2 * HOUR <= now && now < todayStart + 6 * HOUR) {
            return DAWN;
        } else if (todayStart + 6 * HOUR <= now && now < todayStart + 12 * HOUR) {
            return MORNING;
        } else if (todayStart + 12 * HOUR <= now && now < todayStart + 17 * HOUR) {
            return AFTERNOON;
        } else {
            return EVENING;
        }
    }

    public static long getTodayStart() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        return calToLong(todayStart);
    }

    public static long getTodayEnd() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        return calToLong(todayEnd);
    }

    public static long getDayOfWeekStart(boolean isMondayTheFirstDay) {
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        if (isMondayTheFirstDay) {
            int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {
                dayOfWeek += 7;
            }
            weekStart.add(Calendar.DATE, 2 - dayOfWeek);
        } else {
            weekStart.set(Calendar.DAY_OF_WEEK, 1);
        }
        return calToLong(weekStart);
    }

    public static long getDayOfWeekEnd(boolean isMondayTheFirstDay) {
        Calendar weekEnd = Calendar.getInstance();
        weekEnd.setTimeInMillis(getDayOfWeekStart(isMondayTheFirstDay));
        weekEnd.add(Calendar.DAY_OF_WEEK, 6);
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);
        return calToLong(weekEnd);
    }

    public static long getMonthStart() {
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DATE, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        return calToLong(monthStart);
    }

    public static long getMonthEnd() {
        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(Calendar.DATE, 1);
        monthEnd.set(Calendar.HOUR_OF_DAY, 23);
        monthEnd.set(Calendar.MINUTE, 59);
        monthEnd.set(Calendar.SECOND, 59);
        monthEnd.add(Calendar.MONTH, 1);
        monthEnd.add(Calendar.DATE, -1);
        return calToLong(monthEnd);
    }

    public static long getYearStart() {
        Calendar yearStart = Calendar.getInstance();
        yearStart.set(Calendar.DAY_OF_YEAR, 1);
        yearStart.set(Calendar.HOUR_OF_DAY, 0);
        yearStart.set(Calendar.MINUTE, 0);
        yearStart.set(Calendar.SECOND, 0);
        return calToLong(yearStart);
    }

    public static long getYearEnd() {
        Calendar yearEnd = Calendar.getInstance();
        yearEnd.set(Calendar.MONTH, 11);
        yearEnd.set(Calendar.DATE, 31);
        yearEnd.set(Calendar.HOUR_OF_DAY, 23);
        yearEnd.set(Calendar.MINUTE, 59);
        yearEnd.set(Calendar.SECOND, 59);
        return calToLong(yearEnd);
    }

    public static long getNext7DaysEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        return calToLong(calendar);
    }

    public static float getTodayProgress() {
        return ((float) (System.currentTimeMillis() - getTodayStart()) / (getTodayEnd() - getTodayStart())) * 100;
    }

    public static float getDayOfWeekProgress(boolean isMondayTheFirstDay) {
        return ((float) (System.currentTimeMillis() - getDayOfWeekStart(isMondayTheFirstDay)) / (getDayOfWeekEnd(isMondayTheFirstDay) - getDayOfWeekStart(isMondayTheFirstDay))) * 100;
    }

    public static float getMonthProgress() {
        return ((float) (System.currentTimeMillis() - getMonthStart()) / (getMonthEnd() - getMonthStart())) * 100;
    }

    public static float getYearProgress() {
        return ((float) (System.currentTimeMillis() - getYearStart()) / (getYearEnd() - getYearStart())) * 100;
    }

    public static String getCurrentDateTimeName(int field) {
        Calendar calendar = Calendar.getInstance();
        if (field == Calendar.YEAR) {
            return String.valueOf(calendar.get(Calendar.YEAR));
        } else {
            return calendar.getDisplayName(field, Calendar.SHORT, Locale.getDefault());
        }
    }

    public static String convertTimeUnit(Context context, long interval) {
        if (interval >= DAY) {
            int unit = (int) TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS);
            return context.getResources().getQuantityString(R.plurals.remind_unit_day, unit, unit);
        } else if (interval >= HOUR) {
            int unit = (int) TimeUnit.HOURS.convert(interval, TimeUnit.MILLISECONDS);
            return context.getResources().getQuantityString(R.plurals.remind_unit_hour, unit, unit);
        } else if (interval >= MINUTE) {
            int unit = (int) TimeUnit.MINUTES.convert(interval, TimeUnit.MILLISECONDS);
            return context.getResources().getQuantityString(R.plurals.remind_unit_minute, unit, unit);
        } else {
            int unit = (int) TimeUnit.SECONDS.convert(interval, TimeUnit.MILLISECONDS);
            return context.getResources().getQuantityString(R.plurals.remind_unit_second, unit, unit);
        }
    }

}


package com.android.countdowntimer.utils;

public class RemindType {

    public static final int NONE_REMIND = -1;

    public static final int SINGLE_REMIND = 0;
    public static final int SINGLE_DUE_DATE = 1;
    public static final int SINGLE_MIN = 2;
    public static final int SINGLE_HOUR = 3;
    public static final int SINGLE_DAY = 4;
    public static final int SINGLE_WEEK = 5;

    public static final int REPEATED_EVERYDAY = 6;
    public static final int REPEATED_ALWAYS = 7;

}


package com.android.countdowntimer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle = intent.getStringExtra(NotificationUtils.EXTRA_NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NotificationUtils.EXTRA_NOTIFICATION_CONTENT);
        NotificationUtils.showNotification(context, notificationTitle, content);
    }

}


package com.android.countdowntimer.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.android.countdowntimer.R;
import com.android.countdowntimer.home.MainActivity;

public class NotificationUtils {

    public static final String EXTRA_IS_REPEATED = "IS_REPEATED";

    public static final String EXTRA_EVENT_TITLE = "EVENT_TITLE";

    public static final String EXTRA_EVENT_DATE = "EVENT_DATE";

    public static final String EXTRA_EVENT_ID = "EVENT_ID";

    public static final String EXTRA_NOTIFICATION_TITLE = "NOTIFICATION_TITLE";

    public static final String EXTRA_NOTIFICATION_CONTENT = "NOTIFICATION_CONTENT";

    public static void buildNormalReminder(Context context, long date, String title, int type, int interval, String id) {

        long showDate;
        String dateUnit;

        switch (type) {
            case RemindType.SINGLE_DUE_DATE:
                showDate = date;
                dateUnit = context.getString(R.string.remind_unit_now);
                break;
            case RemindType.SINGLE_MIN:
                showDate = date - interval * DateTimeUtils.MINUTE;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_minute, interval, interval);
                break;
            case RemindType.SINGLE_HOUR:
                showDate = date - interval * DateTimeUtils.HOUR;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_hour, interval, interval);
                break;
            case RemindType.SINGLE_DAY:
                showDate = date - interval * DateTimeUtils.DAY;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_day, interval, interval);
                break;
            case RemindType.SINGLE_WEEK:
                showDate = date - interval * DateTimeUtils.WEEK;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_week, interval, interval);
                break;
            default:
                showDate = date;
                dateUnit = context.getString(R.string.remind_unit_now);
        }

        createAlarmManager(context, date, title, id, showDate, dateUnit, false);
    }

    public static void buildOngoingReminder(Context context) {

    }

    public static void cancelReminder(Context context, String id) {
        ComponentName componentName = new ComponentName(context, "com.gwokhou.deadline.ReminderReceiver");
        Intent intent = new Intent();
        intent.setData(Uri.parse(id));
        intent.setComponent(componentName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    private static void createAlarmManager(Context context, long date, String title, String id, long showDate, String dateUnit, boolean isRepeated) {
        ComponentName componentName = new ComponentName(context, "com.android.countdowntimer.utils.ReminderReceiver");
        Intent intent = new Intent();
        intent.setData(Uri.parse(id));
        intent.setComponent(componentName);
        intent.putExtra(EXTRA_IS_REPEATED, isRepeated);
        intent.putExtra(EXTRA_NOTIFICATION_TITLE, context.getString(R.string.remind_title, title));
        intent.putExtra(EXTRA_NOTIFICATION_CONTENT, DateTimeUtils.longToString(date, DateTimeUtils.MEDIUM));

        if (isRepeated) {
            intent.putExtra(EXTRA_EVENT_TITLE, title);
            intent.putExtra(EXTRA_EVENT_DATE, date);
            intent.putExtra(EXTRA_EVENT_ID, id);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, showDate, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, showDate, pendingIntent);
        }
    }

    public static void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context, MainActivity.EVENT_NOTIFICATION_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(1, notification);
    }

}


package com.android.countdowntimer.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.home.Event;
import com.android.countdowntimer.home.TimerView;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.utils.Utils;

import java.util.List;

import io.paperdb.Paper;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");
        Event event = null;

        List<Event> events = Paper.book().read("events");
        for (Event e : events) {
            if(e.getId().equals(eventId)) {
                event = e;
            }
        }
        getSupportActionBar().setTitle(event.getTitle());
        TimerView mTimer = findViewById(R.id.event_timer);
        mTimer.start(event.getEndDate() - System.currentTimeMillis());

        TextView mDueDate = findViewById(R.id.event_due_date);
        mDueDate.setText("Timer ends on\n" + DateTimeUtils.longToString(event.getEndDate(), DateTimeUtils.MEDIUM));

    }
}


