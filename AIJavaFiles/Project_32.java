```java
// File: SmartPlanner.java

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

// Import Glide for image handling
import com.bumptech.glide.Glide;

import com.example.smartplanner.task.TaskItem;
import com.example.smartplanner.task.TaskAdapter;
import com.example.smartplanner.task.TaskDatabaseHelper;

import java.util.List;

public class SmartPlanner extends AppCompatActivity {

    private static final String CHANNEL_ID = "10001";
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField, nameField;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        nameField = findViewById(R.id.name);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
    }

    // Navigation listner for BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            menuItem -> {
                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.nav_tasks:
                        selectedFragment = new TaskFragment();
                        break;
                    case R.id.nav_productivity:
                        selectedFragment = new ProductivityFragment();
                        break;
                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            };

    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                Toast.makeText(SmartPlanner.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        updateUI(user);
                    }
                });
            } else {
                Toast.makeText(SmartPlanner.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SmartPlanner.this, TaskActivity.class);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SmartPlannerChannel";
            String description = "Channel for Smart Planner notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Function to schedule task reminders using notifications
    private void scheduleNotification(String title, String text, long time) {
        Intent intent = new Intent(SmartPlanner.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SmartPlanner.this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SmartPlanner.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SmartPlanner.this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

// Fragment class for Task Management
public class TaskFragment extends Fragment {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private TaskDatabaseHelper dbHelper;
    private List<TaskItem> taskList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Initialize RecyclerView
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize Database Helper
        dbHelper = new TaskDatabaseHelper(getContext());

        // Load tasks from the database
        loadTasks();

        return view;
    }

    private void loadTasks() {
        taskList = dbHelper.getAllTasks();
        taskAdapter = new TaskAdapter(getContext(), taskList);
        taskRecyclerView.setAdapter(taskAdapter);
    }
}

// Task Database Helper Class
public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DUE_DATE = "due_date";
    private static final String COLUMN_PRIORITY = "priority";
    private static final String COLUMN_TAGS = "tags";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DUE_DATE + " TEXT,"
                + COLUMN_PRIORITY + " INTEGER,"
                + COLUMN_TAGS + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // CRUD operations for tasks
    public void addTask(TaskItem task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_TAGS, task.getTags());
        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    public List<TaskItem> getAllTasks() {
        List<TaskItem> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskItem task = new TaskItem();
                task.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                task.setDueDate(cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE)));
                task.setPriority(cursor.getInt(cursor.getColumnIndex(COLUMN_PRIORITY)));
                task.setTags(cursor.getString(cursor.getColumnIndex(COLUMN_TAGS)));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }
}

// TaskItem class
public class TaskItem {

    private int id;
    private String title;
    private String description;
    private String dueDate;
    private int priority;
    private String tags;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}

// TaskAdapter class
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context mContext;
    private List<TaskItem> taskList;

    public TaskAdapter(Context context, List<TaskItem> taskList) {
        this.mContext = context;
        this.taskList = taskList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        TaskItem task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.dueDate.setText(task.getDueDate());
        holder.priority.setText(String.valueOf(task.getPriority()));
        holder.tags.setText(task.getTags());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, dueDate, priority, tags;

        public TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            dueDate = itemView.findViewById(R.id.task_due_date);
            priority = itemView.findViewById(R.id.task_priority);
            tags = itemView.findViewById(R.id.task_tags);
        }
    }
}
```