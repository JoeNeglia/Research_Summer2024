Sure! Below is the complete code for the "HabitPro - Daily Habit Tracker" Android application in Java, encapsulated in a single file. Given the complexity and length of Android projects, this code is broken down into major parts with comments explaining each section.

Note that some advanced implementations like Firebase setup, actual XML layout definitions, and detailed UI components have been simplified or outlined for brevity.

```java
// Import necessary packages
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// MainActivity class responsible for handling user login and authentication using Firebase
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText inputEmail, inputPassword;
    private Button loginButton, signupButton;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Define layout in XML
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get references to UI elements
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);
        statusTextView = findViewById(R.id.status);

        // Login button functionality
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        // Signup button functionality
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    // Method to handle user login
    private void login() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    FirebaseUser user = mAuth.getCurrentUser();
                    statusTextView.setText("Login successful!");
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    statusTextView.setText("Authentication failed.");
                    updateUI(null);
                }
            });
    }

    // Method to handle user signup
    private void signup() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    FirebaseUser user = mAuth.getCurrentUser();
                    statusTextView.setText("Signup successful!");
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    statusTextView.setText("Signup failed.");
                    updateUI(null);
                }
            });
    }

    // Update UI after login/signup
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Navigate to main content activity
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Stay on login/signup page
            statusTextView.setText("Please log in or sign up.");
        }
    }
}

// DashboardActivity class responsible for displaying and managing habits
class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView habitRecyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList;
    private Button addHabitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Define layout in XML

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("habits");
        habitList = new ArrayList<>();

        habitRecyclerView = findViewById(R.id.habit_recycler_view);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(habitList);
        habitRecyclerView.setAdapter(habitAdapter);

        addHabitButton = findViewById(R.id.add_habit_button);
        addHabitButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddHabitActivity.class);
            startActivity(intent);
        });

        loadHabits(); // Load habits from Firebase
    }

    // Method to load habits from Firebase real-time database
    private void loadHabits() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                habitList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Habit habit = postSnapshot.getValue(Habit.class);
                    habitList.add(habit);
                }
                habitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to load habits", databaseError.toException());
            }
        });
    }
}

// AddHabitActivity class for creating a new habit
class AddHabitActivity extends AppCompatActivity {

    private EditText habitTitleInput;
    private Button saveHabitButton;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit); // Define layout in XML

        mDatabase = FirebaseDatabase.getInstance().getReference().child("habits");

        habitTitleInput = findViewById(R.id.habit_title_input);
        saveHabitButton = findViewById(R.id.save_habit_button);

        // Functionality to save the new habit
        saveHabitButton.setOnClickListener(v -> saveHabit());
    }

    // Method to save a new habit into Firebase
    private void saveHabit() {
        String title = habitTitleInput.getText().toString().trim();
        if (!title.isEmpty()) {
            String id = mDatabase.push().getKey();
            Habit habit = new Habit(id, title, false); // False indicates the habit is not yet done
            mDatabase.child(id).setValue(habit);
            finish(); // Go back to the previous activity
        }
    }
}

// Habit class representing a habit data model for Firebase
class Habit {
    public String id;
    public String title;
    public boolean completed;

    public Habit() {
        // Default constructor required for calls to DataSnapshot.getValue(Habit.class)
    }

    public Habit(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }
}

// HabitAdapter class for RecyclerView to display habits
class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList;

    public HabitAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item, parent, false); // Define layout in XML
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.habitTitleTextView.setText(habit.title);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), HabitDetailActivity.class);
            intent.putExtra("HABIT_ID", habit.id);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView habitTitleTextView;

        public HabitViewHolder(View itemView) {
            super(itemView);
            habitTitleTextView = itemView.findViewById(R.id.habit_title);
        }
    }
}

// HabitDetailActivity class to view and edit a specific habit
class HabitDetailActivity extends AppCompatActivity {

    private TextView habitTitleTextView;
    private String habitId;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail); // Define layout in XML

        habitTitleTextView = findViewById(R.id.habit_title_detail);
        habitId = getIntent().getStringExtra("HABIT_ID");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("habits").child(habitId);

        loadHabit();
    }

    // Method to load the specific habit details from Firebase
    private void loadHabit() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Habit habit = dataSnapshot.getValue(Habit.class);
                if (habit != null) {
                    habitTitleTextView.setText(habit.title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to load habit", databaseError.toException());
            }
        });
    }
}

// ReminderService class to handle push notifications for reminders
public class ReminderService extends IntentService {

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String habitTitle = intent.getStringExtra("HABIT_TITLE");
        createNotification(habitTitle);
    }

    // Create a notification for the habit reminder
    private void createNotification(String habitTitle) {
        String channelId = "habittracker_channel_id";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Habit Reminder")
            .setContentText("Remember to complete your habit: " + habitTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, DashboardActivity.class);
        // Switches the current intent
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        
        // Creates the notification channel if necessary
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Habit Tracker", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Habit Tracker notifications");
            NotificationManager notificationManagerO = getSystemService(NotificationManager.class);
            notificationManagerO.createNotificationChannel(channel);
        }

        // Notify the user
        notificationManager.notify(123, builder.build());
    }
}

// Remember to also defined all the XML layouts and resources, including activity_main, activity_dashboard, activity_add_habit,
// activity_habit_detail, habit_item, notification icon, and other assets.
```

This code provides a comprehensive foundation for a simple Habit Tracker app with the key features outlined in your prompt. Note that some functionalities, like the detailed UI and advanced analytics, would require significant additional development to fully implement. For example, Firebase setup details or MPAndroidChart usage are not shown here but would be part of a complete project. To fully realize this application, you also need corresponding XML layouts, drawable resources, Firebase configuration files, and other assets.